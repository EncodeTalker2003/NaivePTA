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

package cspta;

import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.pta.core.heap.AllocationSiteBasedModel;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.plugin.ResultProcessor;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.config.AnalysisOptions;
import pascal.taie.config.ConfigException;
import pascal.taie.util.Strings;

import cspta.selector.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Context-sensitive pointer analysis.
 */
public class CSPTA extends ProgramAnalysis<PointerAnalysisResult> {

    public static final String ID = "pku-pta";

    public CSPTA(AnalysisConfig config) {
        super(config);
    }

    @Override
    public PointerAnalysisResult analyze() {
        AnalysisOptions options = getOptions();
        Solver solver = new Solver(options,
                new AllocationSiteBasedModel(options),
                getContextSelector("2-obj"));
        PointerAnalysisResult result = solver.solve();
        return result;
    }

    private static ContextSelector getContextSelector(String cs) {
		if (cs.equals("ci")) {
            return new CISelector();
        } else if (cs.equals("2-call")) {
			return new _2CallSelector();
		} else if (cs.equals("2-obj")) {
			return new _2ObjSelector();
		} else {
			throw new ConfigException("Unknown context selector: " + cs);
		}
	}
}
