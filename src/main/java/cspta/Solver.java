package cspta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cspta.PreprocessResult;

import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.CallGraphs;
import pascal.taie.analysis.graph.callgraph.CallKind;
import pascal.taie.analysis.graph.callgraph.Edge;
import pascal.taie.analysis.pta.PointerAnalysisResultImpl;
import pascal.taie.analysis.pta.core.cs.CSCallGraph;
import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.element.ArrayIndex;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSManager;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.analysis.pta.core.cs.element.CSObj;
import pascal.taie.analysis.pta.core.cs.element.CSVar;
import pascal.taie.analysis.pta.core.cs.element.InstanceField;
import pascal.taie.analysis.pta.core.cs.element.MapBasedCSManager;
import pascal.taie.analysis.pta.core.cs.element.Pointer;
import pascal.taie.analysis.pta.core.cs.element.StaticField;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.analysis.pta.pts.PointsToSetFactory;
import pascal.taie.config.AnalysisOptions;
import pascal.taie.ir.exp.InvokeExp;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Copy;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.LoadArray;
import pascal.taie.ir.stmt.LoadField;
import pascal.taie.ir.stmt.New;
import pascal.taie.ir.stmt.StmtVisitor;
import pascal.taie.ir.stmt.StoreArray;
import pascal.taie.ir.stmt.StoreField;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.Type;

import cspta.context.ListContext;

import java.util.*;

import cspta.selector.*;
import cspta.context.*;

class Solver {

    private static final Logger logger = LogManager.getLogger(Solver.class);

	private final File dumpPath = new File("result.txt");

    private final AnalysisOptions options;

    private final HeapModel heapModel;

    private final ContextSelector contextSelector;

    private CSManager csManager;

    private CSCallGraph callGraph;

    private PointerFlowGraph pointerFlowGraph;

    private WorkList workList;

    private PointerAnalysisResult result;

	private HashMap<CSObj, New> obj_stmt;

	private HashMap<Obj, TreeSet<CSObj> > obj2csobj;

	private PointerAnalysisResult finalResult;

	private ClassHierarchy hierarchy;

	private PreprocessResult preprocessResult;

	private PointsToSetFactory ptsFactory;

    Solver(AnalysisOptions options, HeapModel heapModel,
           ContextSelector contextSelector) {
        this.options = options;
        this.heapModel = heapModel;
        this.contextSelector = contextSelector;
    }

    public PointerAnalysisResult solve() {
		System.out.println("Begin Solve");
        initialize();
		System.out.println("Initialize");
        analyze();
		System.out.println("Analyze");
		outputResult();
		System.out.println("OutputResult");
		return finalResult;
    }

    private void initialize() {
        csManager = new MapBasedCSManager();
        callGraph = new CSCallGraph(csManager);
        pointerFlowGraph = new PointerFlowGraph();
        workList = new WorkList();
		obj_stmt = new HashMap<>();
		hierarchy = World.get().getClassHierarchy();
		preprocessResult = new PreprocessResult();
		finalResult = new PointerAnalysisResult();
		ptsFactory = new PointsToSetFactory(csManager.getObjectIndexer());
		obj2csobj = new HashMap<>();
		
		World.get().getClassHierarchy().applicationClasses().forEach(jclass->{
            logger.info("Add benchmark for class {}", jclass.getName());
            jclass.getDeclaredMethods().forEach(method->{
                if(!method.isAbstract())
                    preprocessResult.analysis(method.getIR());
            });
        });
        // process program entry, i.e., main method
        Context defContext = contextSelector.getEmptyContext();
        JMethod main = World.get().getMainMethod();
        CSMethod csMethod = csManager.getCSMethod(defContext, main);
        callGraph.addEntryMethod(csMethod);
        addReachable(csMethod);
    }

    /**
     * Processes new reachable context-sensitive method.
     */
    private void addReachable(CSMethod csMethod) {
        if (!callGraph.contains(csMethod)) {
			callGraph.addReachableMethod(csMethod);
			csMethod.getMethod().getIR().getStmts().forEach(stmt -> {
				/*if (stmt instanceof InvokeStatic callSite) {
					Context nowContext = csMethod.getContext();
					var methodRef = ((InvokeStatic)exp).getMethodRef();
                    var className = methodRef.getDeclaringClass().getName();
                    var methodName = methodRef.getName();
				}*/
				stmt.accept(new StmtProcessor(csMethod));
			});
		}
    }

    /**
     * Processes the statements in context-sensitive new reachable methods.
     */
    private class StmtProcessor implements StmtVisitor<Void> {

        private final CSMethod csMethod;

        private final Context context;

        private StmtProcessor(CSMethod csMethod) {
            this.csMethod = csMethod;
            this.context = csMethod.getContext();
        }

        @Override
		public Void visit(New stmt) {
			Pointer ptr = csManager.getCSVar(context, stmt.getLValue());
			Obj obj = heapModel.getObj(stmt);
			Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
			CSObj csObj = csManager.getCSObj(heapContext, obj);
			PointsToSet pts = ptsFactory.make(csObj);
			workList.addEntry(ptr, pts);
			obj_stmt.put(csObj, stmt);
			return null;
		}

		@Override
		public Void visit(Copy stmt) {
			addPFGEdge(
				csManager.getCSVar(context, stmt.getRValue()), 
				csManager.getCSVar(context, stmt.getLValue())
			);
			return null;
		}

		@Override
		public Void visit(Invoke stmt) {
			if (stmt.isStatic()) {
				JMethod callee = resolveCallee(null, stmt);
				CSCallSite csCallSite = csManager.getCSCallSite(context, stmt);
				Context calleeContext = contextSelector.selectContext(csCallSite, callee);
				CSMethod csMethod = csManager.getCSMethod(calleeContext, callee);
				processSingleCall(csCallSite, csMethod);
			}
			return null;
		}

		@Override
		public Void visit(LoadField stmt) {
			if (stmt.isStatic()) {
				addPFGEdge(
					csManager.getStaticField(stmt.getFieldRef().resolve()),
					csManager.getCSVar(context, stmt.getLValue())
				);
			}
			return null;
		}

		@Override
		public Void visit(StoreField stmt) {
			if (stmt.isStatic()) {
				addPFGEdge(
					csManager.getCSVar(context, stmt.getRValue()),
					csManager.getStaticField(stmt.getFieldRef().resolve())
				);
			}
			return null;
		}
    }

    /**
     * Adds an edge "source -> target" to the PFG.
     */
    private void addPFGEdge(Pointer source, Pointer target) {
        if (pointerFlowGraph.getSuccsOf(source).contains(target)) {
			return;
		}
		pointerFlowGraph.addEdge(source, target);
		if (source.getPointsToSet() == null) {
			source.setPointsToSet(ptsFactory.make());
		}
		PointsToSet pts = source.getPointsToSet();
		if ((pts != null) && (!pts.isEmpty())) {
			workList.addEntry(target, pts);
		}
    }

    /**
     * Processes work-list entries until the work-list is empty.
     */
    private void analyze() {
        while (!workList.isEmpty()) {
			WorkList.Entry entry = workList.pollEntry();
			Pointer pointer = entry.pointer();
			PointsToSet pts = entry.pointsToSet();
			PointsToSet diff = propagate(pointer, pts);
			if (pointer instanceof CSVar varptr) {
				Var var = varptr.getVar();
				Context context = varptr.getContext();
				diff.forEach(obj -> {
					var.getStoreFields().forEach(stmt -> {
						addPFGEdge(
							csManager.getCSVar(context, stmt.getRValue()),
							csManager.getInstanceField(obj, stmt.getFieldRef().resolve()));
					});

					var.getLoadFields().forEach(stmt -> {
						addPFGEdge(
							csManager.getInstanceField(obj, stmt.getFieldRef().resolve()),
							csManager.getCSVar(context, stmt.getLValue()));
					});

					var.getStoreArrays().forEach(stmt -> {
						addPFGEdge(
							csManager.getCSVar(context, stmt.getRValue()), 
							csManager.getArrayIndex(obj));
					});

					var.getLoadArrays().forEach(stmt -> {
						addPFGEdge(
							csManager.getArrayIndex(obj), 
							csManager.getCSVar(context, stmt.getLValue()));
					});

					processCall(varptr, obj);
				});
			}
		}
    }

    /**
     * Propagates pointsToSet to pt(pointer) and its PFG successors,
     * returns the difference set of pointsToSet and pt(pointer).
     */
    private PointsToSet propagate(Pointer pointer, PointsToSet pointsToSet) {
        PointsToSet diff = ptsFactory.make();
		if (pointer.getPointsToSet() == null) {
			pointer.setPointsToSet(ptsFactory.make());
		}
		for (CSObj obj: pointsToSet.getObjects()) {
			if (!pointer.getPointsToSet().contains(obj)) {
				diff.addObject(obj);
			}
		}
		if (!diff.isEmpty()) {
			diff.forEach(obj -> pointer.getPointsToSet().addObject(obj));
			pointerFlowGraph.getSuccsOf(pointer).forEach(succ -> workList.addEntry(succ, diff));
		}
		return diff;
    }

	private void processSingleCall(CSCallSite callSite, CSMethod callee) {
		Context caller_ctx = callSite.getContext();
		Context callee_ctx = callee.getContext();
		if (!callGraph.getCalleesOf(callSite).contains(callee)) {
			Invoke stmt = callSite.getCallSite();
			CallKind kind = null;
			if (stmt.isInterface()) {
				kind = CallKind.INTERFACE;
			} else if (stmt.isSpecial()) {
				kind = CallKind.SPECIAL;
			} else if (stmt.isStatic()) {
				kind = CallKind.STATIC;
			} else if (stmt.isVirtual()) {
				kind = CallKind.VIRTUAL;
			}
			if (kind != null) {
				callGraph.addEdge(new Edge<CSCallSite,CSMethod>(kind, callSite, callee));
				addReachable(callee);
				List<Var> args = callee.getMethod().getIR().getParams();
				for (int i = 0; i < args.size(); i++) {
					addPFGEdge(
						csManager.getCSVar(caller_ctx, stmt.getRValue().getArg(i)), 
						csManager.getCSVar(callee_ctx, args.get(i)));
				}
				if (stmt.getLValue() != null) {
					callee.getMethod().getIR().getReturnVars().forEach(ret -> {
						addPFGEdge(
							csManager.getCSVar(callee_ctx, ret), 
							csManager.getCSVar(caller_ctx, stmt.getLValue()));
					});
				}
			}
		}
	}

    /**
     * Processes instance calls when points-to set of the receiver variable changes.
     *
     * @param recv    the receiver variable
     * @param recvObj set of new discovered objects pointed by the variable.
     */
    private void processCall(CSVar recv, CSObj recvObj) {
        recv.getVar().getInvokes().forEach(stmt -> {
			CSCallSite csCallSite = csManager.getCSCallSite(recv.getContext(), stmt);
			JMethod callee = resolveCallee(recvObj, stmt);
			Context context = contextSelector.selectContext(csCallSite, recvObj, callee);
			CSMethod csCallee = csManager.getCSMethod(context, callee);
			PointsToSet new_pts = ptsFactory.make(recvObj);
			workList.addEntry(csManager.getCSVar(context, callee.getIR().getThis()), new_pts);
			processSingleCall(csCallSite, csCallee);
		});
    }

    /**
     * Resolves the callee of a call site with the receiver object.
     *
     * @param recv the receiver object of the method call. If the callSite
     *             is static, this parameter is ignored (i.e., can be null).
     * @param callSite the call site to be resolved.
     * @return the resolved callee.
     */
    private JMethod resolveCallee(CSObj recv, Invoke callSite) {
        Type type = recv != null ? recv.getObject().getType() : null;
        return CallGraphs.resolveCallee(type, callSite);
    }

    private void outputResult() {
		//System.out.println(obj_stmt.size());
		preprocessResult.test_pts.forEach((test_id, pt) -> {
			System.out.println("test_id: " + test_id + ", pt: " + pt + ", cnt: " + csManager.getCSVarsOf(pt).size());
			for (CSVar csVar: csManager.getCSVarsOf(pt)) {
				PointsToSet pts = csVar.getPointsToSet();
				TreeSet<Integer> ans = new TreeSet<>();
				for (CSObj csObj: pts) {
					//cnt += 1;
					//Obj obj = csobj.getObject();
					New stmt = obj_stmt.get(csObj);
					if (stmt != null) {
						int obj_id = preprocessResult.getObjIdAt(stmt);
						if (obj_id > 0) {
							ans.add(obj_id);
						}
					}
				}
				finalResult.put(test_id, ans);
				//System.out.println("test_id: " + test_id + ", cnt: " + pts.size());
			}
		});
		dump(finalResult);
	}

	protected void dump(PointerAnalysisResult result) {
		try (PrintStream out = new PrintStream(new FileOutputStream(dumpPath))) {
			out.println(result);
		} catch (FileNotFoundException e) {
			logger.warn("Failed to dump", e);
		}
	}
}
