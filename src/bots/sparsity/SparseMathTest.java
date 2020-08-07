package bots.sparsity;

import static org.junit.Assert.*;

import org.junit.Test;

import bots.basic.F;

public class SparseMathTest {

	@Test
	public void test() {
		SparseBitVector a = new SparseBitVector(10);
		a.set(5);
		SparseBitVector b = new SparseBitVector(10);
		b.set(3);
		b.set(5);
		SparseBitVector c = SparseMath.and(a, b);
		F.p(a,b,c);
	}

}
