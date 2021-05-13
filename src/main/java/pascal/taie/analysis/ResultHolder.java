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

package pascal.taie.analysis;

/**
 * The holder object of analysis results.
 * The results of each analysis are associated with its id.
 *
 * Currently, this interface is extends by {@link pascal.taie.ir.IR} and
 * held by {@link pascal.taie.World} for storing intra- and inter-procedural
 * analysis results, respectively.
 */
public interface ResultHolder {

    /**
     * Store the analysis result with the analysis id.
     */
    void storeResult(String id, Object result);

    /**
     * Given an analysis id, return the corresponding results.
     */
    Object getResult(String id);

    /**
     * If this holder contains the result for given analysis id,
     * then return the result, otherwise, return the given default result.
     */
    Object getResult(String id, Object defaultResult);

    /**
     * Clear result of the analysis specified by given id.
     */
    void clearResult(String id);

    /**
     * Clear all cached results.
     */
    void clearAll();
}