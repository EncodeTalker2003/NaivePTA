/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package cspta.selector;

import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import cspta.context.ListContext;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.analysis.pta.core.cs.element.CSObj;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;


public class _3CallSelector implements ContextSelector {

    @Override
    public Context getEmptyContext() {
        return ListContext.make();
    }

    @Override
    public Context selectContext(CSCallSite callSite, JMethod callee) {
        Context preContext = callSite.getContext();
		int length = preContext.getLength();
		if (length == 0) {
			return ListContext.make(callSite.getCallSite());
		} else if (length == 1) {
			return ListContext.make(preContext.getElementAt(0), callSite.getCallSite());
		} else if (length == 2) {
			return ListContext.make(preContext.getElementAt(0), preContext.getElementAt(1), callSite.getCallSite());
		} else {
			return ListContext.make(preContext.getElementAt(1), preContext.getElementAt(2), callSite.getCallSite());
		}
    }

    @Override
    public Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee) {
        return selectContext(callSite, callee);
    }

    @Override
    public Context selectHeapContext(CSMethod method, Obj obj) {
        Context preContext = method.getContext();
		int length = preContext.getLength();
		if (length <= 2) {
			return preContext;
		} else {
			return ListContext.make(preContext.getElementAt(1), preContext.getElementAt(2));
		}
    }
}
