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

package pascal.taie.analysis.oldpta.core.ci;

import pascal.taie.analysis.oldpta.ir.Variable;

/**
 * Represents variable nodes in PFG.
 */
class Var extends Pointer {

    private final Variable var;

    Var(Variable var) {
        this.var = var;
    }

    Variable getVariable() {
        return var;
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
