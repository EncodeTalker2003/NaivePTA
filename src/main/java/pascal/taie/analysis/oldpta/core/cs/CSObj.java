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

package pascal.taie.analysis.oldpta.core.cs;

import pascal.taie.analysis.oldpta.core.context.Context;
import pascal.taie.analysis.oldpta.ir.Obj;

public class CSObj extends AbstractCSElement {

    private final Obj obj;

    CSObj(Obj obj, Context context) {
        super(context);
        this.obj = obj;
    }

    public Obj getObject() {
        return obj;
    }

    @Override
    public String toString() {
        return context + ":" + obj;
    }
}
