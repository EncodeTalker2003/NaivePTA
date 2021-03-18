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

package pascal.taie.analysis.pta;

import org.junit.Test;
import pascal.taie.TestUtils;

public class CSPTATest {

    // Tests for context insensitivity
    @Test
    public void testNew() {
        TestUtils.testCSPTA("New");
    }

    @Test
    public void testAssign() {
        TestUtils.testCSPTA("Assign");
    }

    @Test
    public void testStoreLoad() {
        TestUtils.testCSPTA("StoreLoad");
    }

    @Test
    public void testCall() {
        TestUtils.testCSPTA("Call");
    }

    @Test
    public void testAssign2() {
        TestUtils.testCSPTA("Assign2");
    }

    @Test
    public void testInstanceField() {
        TestUtils.testCSPTA("InstanceField");
    }

    @Test
    public void testInstanceField2() {
        TestUtils.testCSPTA("InstanceField2");
    }

    @Test
    public void testCallParamRet() {
        TestUtils.testCSPTA("CallParamRet");
    }

    @Test
    public void testCallField() {
        TestUtils.testCSPTA("CallField");
    }
    
    // Tests for context sensitivity variants
    @Test
    public void testOneCall() {
        TestUtils.testCSPTA("OneCall", "-cs", "1-call");
    }

    @Test
    public void testOneObject() {
        TestUtils.testCSPTA("OneObject", "-cs", "1-obj");
    }

    @Test
    public void testOneType() {
        TestUtils.testCSPTA("OneType", "-cs", "1-type");
    }

    @Test
    public void testTwoCall() {
        TestUtils.testCSPTA("TwoCall", "-cs", "2-call");
    }

    @Test
    public void testTwoObject() {
        TestUtils.testCSPTA("TwoObject", "-cs", "2-obj");
    }

    @Test
    public void testTwoType() {
        TestUtils.testCSPTA("TwoType", "-cs", "2-type");
    }

    // Tests for Java feature supporting
    @Test
    public void testStaticField() {
        TestUtils.testCSPTA("StaticField");
    }

    @Test
    public void testArray() {
        TestUtils.testCSPTA("Array");
    }

    @Test
    public void testCast() {
        TestUtils.testCSPTA("Cast");
    }

    @Test
    public void testNull() {
        TestUtils.testCSPTA("Null");
    }

    @Test
    public void testPrimitive() {
        TestUtils.testCSPTA("Primitive");
    }

    @Test
    public void testStrings() {
        TestUtils.testCSPTA("Strings");
    }

    @Test
    public void testMultiArray() {
        TestUtils.testCSPTA("MultiArray");
    }

    @Test
    public void testClinit() {
        TestUtils.testCSPTA("Clinit");
    }

    @Test
    public void testClassObj() {
        TestUtils.testCSPTA("ClassObj");
    }

    // Tests for handling of non-normal objects
    @Test
    public void testTypeSens() {
        TestUtils.testCSPTA("TypeSens", "-cs", "2-type");
    }

    @Test
    public void testSpecialHeapContext() {
        TestUtils.testCSPTA("SpecialHeapContext", "-cs", "2-object");
    }

    @Test
    public void testNativeModel() {
        TestUtils.testCSPTA("NativeModel");
    }
}
