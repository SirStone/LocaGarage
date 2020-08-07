package bots.sparsity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import bots.basic.Statics;

public class SparseBitMatrix2D implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int rows, columns;
	private Set<Integer> bits;

	public SparseBitMatrix2D(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.bits = new HashSet<>(Statics.cantor(rows, columns));
	}

	public void set(int key) {
		this.bits.add(key);
	}
	
	public void set(long row, long column) {
		int key = Statics.cantor(row, column);
		this.bits.add(key);
	}
	
	public void unset(long row, long column) {
		int key = Statics.cantor(row, column);
		this.bits.remove(key);
	}
	
	public boolean get(int key) {
		return this.bits.contains(key);
	}
	
	public boolean get(long row, long column) {
		int key = Statics.cantor(row, column);
		return this.bits.contains(key);
	}
	
	public int cardinality()  {
		return bits.size();
	}
	
	public int columnCardinality(int column) {
		if(column > this.columns) throw new IllegalArgumentException("the column "+column+" is out of range, max is "+(this.columns-1));
		int columnCardinality = 0;
		for (int row = 0; row < this.rows; row++) {
			if(isSet(row, column)) columnCardinality++;
		}
		return columnCardinality;
	}
	
	public int rowCardinality(int row) {
		if(row > this.rows) throw new IllegalArgumentException("the row "+row+" is out of range, max is "+(this.rows-1));
		int rowCardinality = 0;
		for (int column = 0; column < this.columns; column++) {
			if(isSet(row, column)) rowCardinality++;
		}
		return rowCardinality;
	}

	public boolean isSet(long row, long column) {
		int key = Statics.cantor(row, column);
		return this.bits.contains(key);
	}
	
	public boolean isSet(int key) {
		return this.bits.contains(key);
	}

	public void reset() {
		this.bits.clear();
	}
	
	public Iterator<Integer> iterator() {
		return this.bits.iterator();
	}
	
	public String toString() {
		String out = "["+this.rows+"x"+this.columns+"] ";
		if(cardinality() > 100) {
			out += "cardinality = "+cardinality()+" first 10:[";
			Iterator<Integer> itr = iterator();
			for (int i = 0; i < 10; i++) {
				int[] unkey = Statics.cantorInverse2values(itr.next());
				out += "["+unkey[1]+","+unkey[0]+"],";
			}
			out += "...]";
		}
		else out += this.bits;
		
		return out + "\n";
	}

	public SparseBitVector flatten() {
		int size = rows*columns;
		SparseBitVector out = new SparseBitVector(size);
		Iterator<Integer> activeCell = iterator();
		while(activeCell.hasNext()) {
			int[] unkey = Statics.cantorInverse2values(activeCell.next());
			int index = unkey[1] * rows + unkey[0];
			out.set(index);
		}
		return out;
	}
	
	public void columnFill(int column) {
		for (int row = 0; row < this.rows; row++) {
			set(row, column);
		}
	}

}
