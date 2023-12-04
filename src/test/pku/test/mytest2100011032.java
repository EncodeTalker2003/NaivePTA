package test;


import java.util.HashMap;
import java.util.HashSet;
import java.lang.*;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;
import benchmark.objects.P;
import benchmark.objects.Q;

public class mytest2100011032 {

  public mytest2100011032() {}

  public void callee(A a, A b) {
    Benchmark.test(1, b);
  }

  public void test1() {
    Benchmark.alloc(1);
    A a1 = new A();
    A b1 = a1;
    test11(a1, b1);
  }

  private void test11(A a1, A b1) {
    test111(a1, b1);
  }

  private void test111(A a1, A b1) {
    callee(a1, b1);
  }

  public void test2() {
    Benchmark.alloc(2);
    A a2 = new A();
    Benchmark.alloc(3);
    A b2 = new A();
    test22(a2, b2);
  }

  private void test22(A a2, A b2) {
    test222(a2, b2);
  }

  private void test222(A a2, A b2) {
    callee(a2, b2);
  }
  // ContextSensitivity
  
  private void assign(A x, A y) {
    y.f = x.f;
  }

  private void test3() {
    Benchmark.alloc(4);
    B b = new B();
    Benchmark.alloc(5);
    A a = new A(b);
    Benchmark.alloc(6);
    A c = new A();
    assign(a, c);
    B d = c.f;

    Benchmark.test(2, d);
  }
  // FieldSensitivity

  private void test4() {

    Benchmark.alloc(7);
    A a = new A();
    Benchmark.alloc(8);
    A b = new A();

    Benchmark.test(3, b);

    b = a;
  }
  // FlowSensitivity

  private void test5() {

    Benchmark.alloc(9);
    B b1 = new B();
    Benchmark.alloc(10);
    B b2 = new B();

    Benchmark.alloc(11);
    A a = new A();

    B b3 = a.id(b1);
    B b4 = a.id(b2);

    Benchmark.test(4, b4);
  }
  // ObjectSensitivity

  private void test6() {

    HashMap<String, A> map = new HashMap<String, A>();
    Benchmark.alloc(12);
    A a = new A();
    Benchmark.alloc(13);
    A b = new A();
    map.put("first", a);
    map.put("second", b);
    A c = map.get("second");
    Benchmark.test(5, c);
  }
  // // Map

  private void test7() {

    HashSet<A> set = new HashSet<A>();
    Benchmark.alloc(14);
    A a = new A();
    A c = null;
    Benchmark.alloc(15);
    A b = new A();
    set.add(a);
    set.add(b);
    for (A i : set) {
      c = i;
      break;
    }
    a = null;
    Benchmark.test(6,c);
  }
  // Set

  private void test8() {

    A[][][] a = new A[2][][];
    Benchmark.alloc(16);
    a[0] = new A[1][2];
    Benchmark.alloc(17);
    a[1] = new A[3][4];
    
    a[0] = a[1];

    Benchmark.test(7, a[0]);
  }
  //MultiArray - Simple

  public class A1 {

  }

  public class B1 extends A1 {

  }

  public class C1 extends B1 {

  }

  private void test9() {

    Benchmark.alloc(18);
    A1 a = new B1();
    Benchmark.alloc(19);
    A1 b = new A1();
	Benchmark.alloc(20);
	C1 c = new C1();
	
    B1 d = (B1)a;
	B1 e = new B1();
	if (b instanceof B1) {
		e = (B1)b; // This will never happen
	}
	B1 f = (B1)c;
    Benchmark.test(8, d);
	Benchmark.test(9, e); // This should be nothing
	Benchmark.test(10, f);
  }
  //Cast

  // private void test10() {

  //   Benchmark.alloc(20);
  //   A a = new A();
	//   Benchmark.alloc(21);
  //   A b = new A();

  //   try {
  //     throw new RuntimeException();
  //     b = a;

  //   } catch (RuntimeException e) {
  //     Benchmark.test(9, b);
  //   }

  // }
  //Exception


  public static void main(String[] args) {
    mytest2100011032 Mt = new mytest2100011032();
    Mt.test1();
    Mt.test2();
    Mt.test3();
    Mt.test4();
    Mt.test5();
    Mt.test6();
    Mt.test7();
    Mt.test8();
    Mt.test9();
    // Mt.test10();
  }
}
