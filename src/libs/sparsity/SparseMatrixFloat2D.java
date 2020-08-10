package bots.sparsity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bots.basic.Statics;

public class SparseMatrixFloat2D implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int numRows, numColumns;
	private Map<Integer,Float> values;

	public SparseMatrixFloat2D(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.values = new HashMap<>();
	}

	public void set(int cantorKey, float value) {
		this.values.put(cantorKey, value);
	}

	public void set(long row, long column, float value) {
		int key = Statics.cantor(row, column);
		this.values.put(key, value);
	}
	
	public void unset(long row, long column) {
		int key = Statics.cantor(row, column);
		this.values.remove(key);
	}
	
	public void unset(int cantorKey) {
		this.values.remove(cantorKey);
	}
	
	public float get(int cantorKey) {
		int[] ab = Statics.cantorInverse2values(cantorKey);
		return get(ab[0],ab[1]);
	}
	
	public float get(long row, long column) {
		int key = Statics.cantor(row, column);
		if(this.values.containsKey(key)) return this.values.get(key);
		else return 0;
	}
	
	public int cardinality()  {
		return values.size();
	}

	public boolean isSet(long row, long column) {
		int key = Statics.cantor(row, column);
		return values.containsKey(key);
	}
	
	public Iterator<Integer> iteratorKey() {
		return values.keySet().iterator();
	}
	
	public Iterator<Float> iteratorValue() {
		return values.values().iterator();
	}

	public int[] getRowColumn(int cantorZ) {
		return Statics.cantorInverse2values(cantorZ);
	}
	
	public String toString() {
		String out = "["+this.numRows+"x"+this.numColumns+"] ";
		if(cardinality() > 100) {
			out += "cardinality = "+cardinality()+" first 10:[";
			Iterator<Integer> itr = iteratorKey();
			for (int i = 0; i < 10; i++) {
				out += itr.next()+",";
			}
			out += "...]";
		}
		else out += this.values;
		
		return out + "\n";
	}

	public void clone(SparseBitMatrix2D bitMatrix2D) {
		values = new HashMap<>();
		Iterator<Integer> itr = bitMatrix2D.iterator();
		while(itr.hasNext()) {
			int key = itr.next();
			set(key, 1F);
		}
	}
}