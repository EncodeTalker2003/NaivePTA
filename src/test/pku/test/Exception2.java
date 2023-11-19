package test;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

public class Exception2 {

  public static void main(String[] args) {

    A a = new A();
    Benchmark.alloc(1);
    A b = new A();

    try {
      Integer.parseInt("abc");
      a = b;

    } catch (RuntimeException e) {
      Benchmark.test(1, "b");
    }

  }
}