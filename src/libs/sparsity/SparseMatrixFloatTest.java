package bots.sparsity;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import bots.basic.F;

public class SparseMatrixFloatTest {

	@Test
	public void containsTest() {
		SparseMatrixFloat2D smf = new SparseMatrixFloat2D(100, 100);
		smf.set(10, 10, 10);
		
		Iterator<Integer> itr = smf.iteratorKey();
		while (itr.hasNext()) {
			float value = smf.get(itr.next());
			F.p(value);
		}
		
		assertTrue("isSet true test failed",smf.isSet(10, 10));
		assertFalse("isSet false test failed",smf.isSet(1, 10));
	}

}
