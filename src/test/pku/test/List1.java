package test;

import java.util.ArrayList;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase List1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description ArrayList
 */
public class List1 {

  public static void main(String[] args) {

    ArrayList<A> list = new ArrayList<A>();
	Benchmark.alloc(1);
    A a = new A();
    Benchmark.alloc(2);
    A b = new A();
    list.add(a);
    list.add(b);
    A c = list.get(1);
    Benchmark.test(1, c);
  }
}
