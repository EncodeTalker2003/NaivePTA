package test;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

public class Array {
  public static void main(String[] args) {
	Benchmark.alloc(1);
	A[] a = new A[2];
	Benchmark.alloc(2);
	a[0] = new A();
	Benchmark.alloc(3);
	a[1] = new A();
	Benchmark.test(1, a[0]);
	Benchmark.test(2, a[1]);
  }
}