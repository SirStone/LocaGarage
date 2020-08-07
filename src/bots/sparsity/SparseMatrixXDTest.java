package bots.sparsity;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Test;

import bots.basic.F;

public class SparseMatrixXDTest {

	@Test
	public void SparseBitMatrixXDTest() {
		long x = 10, y = 4, z = 3;
		long[] arraySizes = new long[] {x,y,z};
		
		SparseBitMatrixXD testMatrix = new SparseBitMatrixXD(x,y,z);
		
		for (int i = 0; i < arraySizes.length; i++) {
			assertTrue(testMatrix.getSizes()[i].compareTo(BigInteger.valueOf(arraySizes[i])) == 0);
		}
		assertTrue(testMatrix.getFullSize().compareTo(BigInteger.valueOf(x*y*z)) == 0);
		
		testMatrix = new SparseBitMatrixXD(arraySizes);
		
		for (int i = 0; i < arraySizes.length; i++) {
			assertTrue(testMatrix.getSizes()[i].compareTo(BigInteger.valueOf(arraySizes[i])) == 0);
		}
		assertTrue(testMatrix.getFullSize().compareTo(BigInteger.valueOf(x*y*z)) == 0);
		
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(0)) == 0);
		
		testMatrix.set(3,2,1);
		
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(1)) == 0);
		assertTrue(testMatrix.isSet(SparseBitMatrixXD.pair(3,2,1)));
		assertTrue(testMatrix.isSet(3,2,1));
		
		testMatrix = new SparseBitMatrixXD(10);
		assertTrue(testMatrix.getFullSize().compareTo(BigInteger.valueOf(10)) == 0);
		testMatrix.set(5);
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(1)) == 0);
		assertTrue(testMatrix.isSet(5));
		testMatrix.unset(5);
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(0)) == 0);
		assertFalse(testMatrix.isSet(5));
	}

	@Test
	public void pairingTest() {
		int[] sizes = new int[] {2,3,5,8,13};
		Random rnd = new Random();
		for (int size = 0; size < sizes.length; size++) {
			long[] values = new long[sizes[size]];
			for (int i = 0; i < sizes[size]; i++) {
				values[i] = rnd.nextInt(10000);
			}
			BigInteger paired = SparseBitMatrixXD.pair(values);
			BigInteger[] unpaired = SparseBitMatrixXD.unpair(paired, values.length);
			for (int i = 0; i < sizes[size]; i++) {
				assertTrue(BigInteger.valueOf(values[i]).compareTo(unpaired[i]) == 0);
			}
		}
	}
	
	@Test
	public void SparseMatrixFloatXDTest() {
		int x = 10, y = 4, z = 3;
		int[] arraySizes = new int[] {x,y,z};
		
		SparseMatrixFloatXD testMatrix = new SparseMatrixFloatXD(x,y,z);
		
		for (int i = 0; i < arraySizes.length; i++) {
			assertTrue(testMatrix.getSizes()[i] == arraySizes[i]);
		}
		assertTrue(testMatrix.getFullSize()==x*y*z);
		
		testMatrix = new SparseMatrixFloatXD(arraySizes);
		
		for (int i = 0; i < arraySizes.length; i++) {
			assertTrue(testMatrix.getSizes()[i]==arraySizes[i]);
		}
		assertTrue(testMatrix.getFullSize()==x*y*z);
		
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(0)) == 0);
		
		testMatrix.set(32.53F,3,2,1);
		
		assertTrue(testMatrix.cardinality()==1);
		assertTrue(testMatrix.isSet(SparseBitMatrixXD.pair(3,2,1)));
		assertTrue(testMatrix.isSet(3,2,1));
		
		testMatrix = new SparseMatrixFloatXD(10);
		assertTrue(testMatrix.getFullSize()==10);
		testMatrix.set(5.4F, 5);
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(1)) == 0);
		assertTrue(testMatrix.isSet(5));
		testMatrix.unset(5);
		assertTrue(testMatrix.cardinality().compareTo(BigInteger.valueOf(0)) == 0);
		assertFalse(testMatrix.isSet(5));
	}
	
	@Test
	public void sizesTest() {
		long[] sizes = new long[] {Integer.MAX_VALUE,Integer.MAX_VALUE, Integer.MAX_VALUE};
		BigInteger paired = SparseBitMatrixXD.pair(sizes);
		F.p(sizes,"->",paired,">",Long.MAX_VALUE,"?",paired.compareTo(BigInteger.valueOf(Long.MAX_VALUE))==1);
	}
}
