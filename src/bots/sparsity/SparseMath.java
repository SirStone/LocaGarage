package bots.sparsity;

import java.util.Iterator;

public class SparseMath {
	public static SparseBitVector and(SparseBitVector a, SparseBitVector b) {
		if(a.size != b.size) throw new IllegalArgumentException("the two vectors are no the same size");
		
		SparseBitVector out = new SparseBitVector(a.size);
		SparseBitVector smaller, bigger;
		
		if(a.cardinality() < b.cardinality()) {
			smaller = a;
			bigger = b;
		}
		else {
			smaller = b;
			bigger = a;
		}
		
		Iterator<Long> itr = smaller.iterator();
		while(itr.hasNext()) {
			long key = itr.next();
			if(bigger.isSet(key)) out.set(key);
		}
		
		return out;
	}

	public static SparseBitMatrix2D and(SparseBitMatrix2D a, SparseBitMatrix2D b) {
		if(a.columns != b.columns || a.rows != b.rows) throw new IllegalArgumentException("the two vectors are no the same size");
		
		SparseBitMatrix2D out = new SparseBitMatrix2D(a.columns, a.rows);
		SparseBitMatrix2D smaller, bigger;
		
		if(a.cardinality() < b.cardinality()) {
			smaller = a;
			bigger = b;
		}
		else {
			smaller = b;
			bigger = a;
		}
		
		Iterator<Integer> itr = smaller.iterator();
		while(itr.hasNext()) {
			int key = itr.next();
			if(bigger.isSet(key)) out.set((int) key);
		}
		
		return out;
	}

	/**
	 * @param matrix, 2D matrix
	 * @param vector, treated as column vector
	 * @return the matrix multiplication matrix x vector
	 */
	public static float[] matrixMult(SparseMatrixFloat2D matrix, SparseBitVector vector) {
		if(matrix.numColumns != vector.size) throw new IllegalArgumentException("matrix is ["+matrix.numRows+"x"+matrix.numColumns+"], vector is ["+vector.size+"x1], the two are not compatible for matrix multiplication");
		
		float[] out = new float[matrix.numRows];
		for (int row = 0; row < matrix.numRows; row++) {
			float rowSum = 0;
			for (int column = 0; column < matrix.numColumns; column++) {
				rowSum += matrix.get(row, column) * (vector.get(column)?1:0);
			}
			out[row] =rowSum;
		}
		return out;
	}

	public static void scale(SparseMatrixFloat2D sparseMatrixFloat2D, float d) {
		Iterator<Integer> itr = sparseMatrixFloat2D.iteratorKey();
		while(itr.hasNext()) {
			int key = itr.next();
			sparseMatrixFloat2D.set(key, sparseMatrixFloat2D.get(key)*d);
		}
	}
}
