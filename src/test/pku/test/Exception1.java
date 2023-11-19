package test;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase Exception1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias in exception
 */
public class Exception1 {

  public static void main(String[] args) {

    Benchmark.alloc(1);
    A a = new A();
	Benchmark.alloc(2);
    A b = new A();
	b = a;
	

    try {
      b = a;
      throw new RuntimeException();

    } catch (RuntimeException e) {
      Benchmark.test(1, b);
    }

  }
}