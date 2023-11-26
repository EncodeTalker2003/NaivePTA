package test;

import benchmark.internal.Benchmark;
import benchmark.objects.*;

public class Interface {
    public static void main(String [] args){
        Benchmark.alloc(1);
        I g = new G();
        Benchmark.alloc(2);
        I h = new H();
        Benchmark.alloc(3);
        A a1 = new A();
        Benchmark.alloc(4);
        A a2 = new A();
		
        A a3 = g.foo(a1);
        A a4 = g.foo(a2);

        h.foo(a2);


        Benchmark.test(1, a3); // expect: 3
        Benchmark.test(2, a4); // expect: 4
        if(g instanceof G) {
            G gg = (G)g;
            Benchmark.test(3, gg.a); // expect: 3 4
        }
        if(h instanceof H) {
            H hh = (H)h;
            Benchmark.test(4, hh.a); // epxect: 4
        }
    }
}