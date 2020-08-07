package bots.sparsity;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SparseBitVector implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int size;
	private Set<Long> set;

	public SparseBitVector(int size) {
		this.size = size;
		this.set = new HashSet<>(size);
	}

	public void set(long index) {
		this.set.add(index);
	}
	
	public void set(int[] indexes) {
		for (int index = 0; index < indexes.length; index++) {
			set(indexes[index]);
		}
	}
	
	public void unset(long index) {
		this.set.remove(index);
	}
	
	public boolean get(long index) {
		return this.set.contains(index);
	}
	
	public void set(int fromIndex, int toIndex) {
		for (int i = fromIndex; i < toIndex; i++) {
			this.set.add((long) i);
		}
	}
	
	public void concatenate(SparseBitVector input) {
		for (Long index : input.set) {
			this.set.add(index+this.size);
		}
		this.size += input.size;
	}
	
	public int cardinality()  {
		return set.size();
	}

	public void fill() {
		for (int i = 0; i < size; i++) {
			set(i);
		}
	}
	
	public Iterator<Long> iterator() {
		return this.set.iterator();
	}
	
	public String toString() {
		return this.set.toString()+"\n";
	}

	public boolean isSet(long i) {
		return set.contains(i);
	}

	public void reset() {
		set.clear();
	}

	public void clone(SparseBitVector vector) {
		this.reset();
		this.size = vector.size;
		this.set.addAll(vector.set);
	}
}
