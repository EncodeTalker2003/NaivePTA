package test;

import java.util.HashSet;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

class num {
	A a;
	int x;
	num() {}
	public A get() {
		Benchmark.test(1, a);
		return a;
	}
}

class one extends num {
	one() {
		Benchmark.alloc(1);
		a = new A();
	}

	public A get() {
		Benchmark.test(2, a);
		return a;
	}
}

class two extends num {
	two() {
		Benchmark.alloc(2);
		a = new A();
	}
}

class three extends num {
	three() {
		Benchmark.alloc(3);
		a = new A();
	}
}

public class mor1 {
	public static void main(String[] args) {
		num x = new one();
		A f = x.get();
		
	}
}
