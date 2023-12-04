package cipta;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.CallGraphs;
import pascal.taie.analysis.graph.callgraph.CallKind;
import pascal.taie.analysis.graph.callgraph.DefaultCallGraph;
import pascal.taie.analysis.graph.callgraph.Edge;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.analysis.pta.core.heap.AllocationSiteBasedModel;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.ir.exp.InvokeExp;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.proginfo.MethodRef;
import pascal.taie.ir.stmt.Copy;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.LoadArray;
import pascal.taie.ir.stmt.LoadField;
import pascal.taie.ir.stmt.New;
import pascal.taie.ir.stmt.StmtVisitor;
import pascal.taie.ir.stmt.StoreArray;
import pascal.taie.ir.stmt.StoreField;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JMethod;
import pascal.taie.util.AnalysisException;
import pascal.taie.language.type.Type;
import pascal.taie.analysis.misc.IRDumper;
import pascal.taie.config.AnalysisConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.TreeMap;
import java.util.*;

import pascal.taie.ir.stmt.Throw;

class Solver {

	private static final Logger logger = LogManager.getLogger(IRDumper.class);

	private final File dumpPath = new File("result.txt");

    private final HeapModel heapModel;

    private DefaultCallGraph callGraph;

    private PointerFlowGraph pointerFlowGraph;

    private WorkList workList;

    private StmtProcessor stmtProcessor;

    private ClassHierarchy hierarchy;

	private PreprocessResult preprocessResult;

	private PointerAnalysisResult finalResult;

	private HashMap<Obj, New> obj_stmt;

    Solver(HeapModel heapModel) {
        this.heapModel = heapModel;
    }

    /**
     * Runs pointer analysis algorithm.
     */
    public PointerAnalysisResult solve() {
        initialize();
        mainAnalyze();
		logger.info("Finised main analysis");
		outputResult();
		System.out.println("finished all analysis");
		return finalResult;
    }

    /**
     * Initializes pointer analysis.
     */
    private void initialize() {
        workList = new WorkList();
        pointerFlowGraph = new PointerFlowGraph();
        callGraph = new DefaultCallGraph();
        stmtProcessor = new StmtProcessor();
        hierarchy = World.get().getClassHierarchy();
		preprocessResult = new PreprocessResult();
		finalResult = new PointerAnalysisResult();
		obj_stmt = new HashMap<>();

		World.get().getClassHierarchy().applicationClasses().forEach(jclass->{
            logger.info("Add benchmark for class {}", jclass.getName());
            jclass.getDeclaredMethods().forEach(method->{
                if(!method.isAbstract())
                    preprocessResult.analysis(method.getIR());
            });
        });
        // initialize main method
        JMethod main = World.get().getMainMethod();
        callGraph.addEntryMethod(main);
        addReachable(main);
    }

    /**
     * Processes new reachable method.
     */
    private void addReachable(JMethod method) {
        if (!callGraph.contains(method)) {
			callGraph.addReachableMethod(method);
			method.getIR().getStmts().forEach(stmt -> {
				//System.out.println(stmt);
				if (stmt instanceof Throw) {
					// System.out.println("Throw statement is not supported");
					//throw new AnalysisException("Throw statement is not supported");
				}
				stmt.accept(stmtProcessor);
			});
		}
		System.out.println();
    }

    /**
     * Processes statements in new reachable methods.
     */
    private class StmtProcessor implements StmtVisitor<Void> {
        
		@Override
		public Void visit(New stmt) {
			Pointer ptr = pointerFlowGraph.getVarPtr(stmt.getLValue());
			Obj obj = heapModel.getObj(stmt);
			workList.addEntry(ptr, new PointsToSet(obj));
			obj_stmt.put(obj, stmt);
			//System.out.println("Marked an object " + stmt.toString());
			return null;
		}

		@Override
		public Void visit(Copy stmt) {
			addPFGEdge(
				pointerFlowGraph.getVarPtr(stmt.getRValue()), 
				pointerFlowGraph.getVarPtr(stmt.getLValue())
			);
			return null;
		}

		@Override
		public Void visit(Invoke stmt) {
			if ((stmt.isStatic()) && (notBenchmark(stmt))) {
				JMethod callee = resolveCallee(null, stmt);
				processSingleCall(stmt, callee);
			}
			return null;
		}

		boolean notBenchmark(Invoke stmt) {
			/*String signature = stmt.getMethodRef().getSignature();
			if (signature.contains("<benchmark.internal.Benchmark") || signature.contains("<benchmark.internal.BenchmarkN")) {
				return false;
			}*/
			return true;
		}

		@Override
		public Void visit(LoadField stmt) {
			if (stmt.isStatic()) {
				addPFGEdge(
					pointerFlowGraph.getStaticField(stmt.getFieldRef().resolve()),
					pointerFlowGraph.getVarPtr(stmt.getLValue())
				);
			}
			return null;
		}

		@Override
		public Void visit(StoreField stmt) {
			if (stmt.isStatic()) {
				addPFGEdge(
					pointerFlowGraph.getVarPtr(stmt.getRValue()), 
					pointerFlowGraph.getStaticField(stmt.getFieldRef().resolve())
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
		PointsToSet pts = source.getPointsToSet();
		if (!pts.isEmpty()) {
			//System.out.println("addPFGEdge: " + pts.size());
			workList.addEntry(target, pts);
		}
    }

    /**
     * Processes work-list entries until the work-list is empty.
     */
    private void mainAnalyze() {
        while (!workList.isEmpty()) {
			WorkList.Entry entry = workList.pollEntry();
			Pointer pointer = entry.pointer();
			PointsToSet pts = entry.pointsToSet();
			PointsToSet diff = propagate(pointer, pts);
			if (pointer instanceof VarPtr varptr) {
				Var var = varptr.getVar();
				diff.forEach(obj -> {
					var.getStoreFields().forEach(stmt -> {
						addPFGEdge(
							pointerFlowGraph.getVarPtr(stmt.getRValue()), 
							pointerFlowGraph.getInstanceField(obj, stmt.getFieldRef().resolve()));
					});

					var.getLoadFields().forEach(stmt -> {
						addPFGEdge(
							pointerFlowGraph.getInstanceField(obj, stmt.getFieldRef().resolve()),
							pointerFlowGraph.getVarPtr(stmt.getLValue()));
					});

					var.getStoreArrays().forEach(stmt -> {
						addPFGEdge(
							pointerFlowGraph.getVarPtr(stmt.getRValue()), 
							pointerFlowGraph.getArrayIndex(obj));
					});

					var.getLoadArrays().forEach(stmt -> {
						addPFGEdge(
							pointerFlowGraph.getArrayIndex(obj), 
							pointerFlowGraph.getVarPtr(stmt.getLValue()));
					});

					processCall(var, obj);
				});
			}
		}
    }

    /**
     * Propagates pointsToSet to pt(pointer) and its PFG successors,
     * returns the difference set of pointsToSet and pt(pointer).
     */
    private PointsToSet propagate(Pointer pointer, PointsToSet pointsToSet) {
        PointsToSet diff = new PointsToSet();
		for (Obj obj: pointsToSet) {
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

	private void processSingleCall(Invoke callSite, JMethod callee) {
		if (!callGraph.getCalleesOf(callSite).contains(callee)) {
			CallKind kind = null;
			if (callSite.isInterface()) {
				kind = CallKind.INTERFACE;
			} else if (callSite.isSpecial()) {
				kind = CallKind.SPECIAL;
			} else if (callSite.isStatic()) {
				kind = CallKind.STATIC;
			} else if (callSite.isVirtual()) {
				kind = CallKind.VIRTUAL;
			}
			if (kind != null) {
				callGraph.addEdge(new Edge<Invoke,JMethod>(kind, callSite, callee));
				addReachable(callee);
				List<Var> args = callee.getIR().getParams();
				for (int i = 0; i < args.size(); i++) {
					addPFGEdge(
						pointerFlowGraph.getVarPtr(callSite.getRValue().getArg(i)), 
						pointerFlowGraph.getVarPtr(args.get(i)));
				}
				if (callSite.getLValue() != null) {
					callee.getIR().getReturnVars().forEach(ret -> {
						addPFGEdge(
							pointerFlowGraph.getVarPtr(ret), 
							pointerFlowGraph.getVarPtr(callSite.getLValue()));
					});
				}
			}
		}
	}

    /**
     * Processes instance calls when points-to set of the receiver variable changes.
     *
     * @param var the variable that holds receiver objects
     * @param recv a new discovered object pointed by the variable.
     */
    private void processCall(Var var, Obj recv) {
        var.getInvokes().forEach(callSite -> {
			JMethod callee = resolveCallee(recv, callSite);
			workList.addEntry(pointerFlowGraph.getVarPtr(callee.getIR().getThis()), new PointsToSet(recv));
			processSingleCall(callSite, callee);
		});
    }

    /**
     * Resolves the callee of a call site with the receiver object.
     *
     * @param recv     the receiver object of the method call. If the callSite
     *                 is static, this parameter is ignored (i.e., can be null).
     * @param callSite the call site to be resolved.
     * @return the resolved callee.
     */
    private JMethod resolveCallee(Obj recv, Invoke callSite) {
        Type type = recv != null ? recv.getType() : null;
        return CallGraphs.resolveCallee(type, callSite);
    }

	private void outputResult() {
		//System.out.println(obj_stmt.size());
		preprocessResult.test_pts.forEach((test_id, pt) -> {
			Pointer ptr = pointerFlowGraph.getVarPtr(pt);
			PointsToSet pts = ptr.getPointsToSet();
			TreeSet<Integer> ans = new TreeSet<>();
			System.out.println("test_id: " + test_id + ", cnt: " + pts.size());
			System.out.println("Var:" + pt);
			for (Obj obj: pts) {
				//cnt += 1;
				New stmt = obj_stmt.get(obj);
				if (stmt != null) {
					int obj_id = preprocessResult.getObjIdAt(stmt);
					if (obj_id > 0) {
						ans.add(obj_id);
					}
				}
			}
			finalResult.put(test_id, ans);
			//System.out.println("test_id: " + test_id + ", cnt: " + pts.size());
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
