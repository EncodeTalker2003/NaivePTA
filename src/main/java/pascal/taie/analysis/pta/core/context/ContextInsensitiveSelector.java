/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2020-- Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020-- Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * Tai-e is only for educational and academic purposes,
 * and any form of commercial use is disallowed.
 * Distribution of Tai-e is disallowed without the approval.
 */

package pascal.taie.analysis.pta.core.context;

import pascal.taie.language.classes.JMethod;
import pascal.taie.analysis.pta.core.cs.CSCallSite;
import pascal.taie.analysis.pta.core.cs.CSMethod;
import pascal.taie.analysis.pta.core.cs.CSObj;
import pascal.taie.analysis.pta.core.heap.Obj;

/**
 * Context-insensitive selector do not use any context elements,
 * thus the type of context elements is irrelevant.
 */
public class ContextInsensitiveSelector extends AbstractContextSelector<Void> {

    @Override
    public Context selectContext(CSCallSite callSite, JMethod callee) {
        return getDefaultContext();
    }

    @Override
    public Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee) {
        return getDefaultContext();
    }

    @Override
    protected Context doSelectHeapContext(CSMethod method, Obj obj) {
        return getDefaultContext();
    }
}
