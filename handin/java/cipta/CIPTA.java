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

package cipta;

import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.analysis.pta.core.heap.AllocationSiteBasedModel;
import pascal.taie.analysis.pta.core.heap.HeapModel;

/**
 * Context-insensitive pointer analysis.
 */
public class CIPTA extends ProgramAnalysis<PointerAnalysisResult> {

    public static final String ID = "cipta";

    public CIPTA(AnalysisConfig config) {
        super(config);
    }

    @Override
    public PointerAnalysisResult analyze() {
		PointerAnalysisResult result;
		try {
			HeapModel heapModel = new AllocationSiteBasedModel(getOptions());
        	Solver solver = new Solver(heapModel);
        	result = solver.solve();
		} catch (Exception e) {
			TrivialSolver trivialSolver = new TrivialSolver();
			
			result = trivialSolver.solve();
		}
        return result;
    }
}
