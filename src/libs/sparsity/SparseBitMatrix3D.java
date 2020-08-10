package bots.sparsity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import bots.basic.Statics;

public class SparseBitMatrix3D implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int x, y, z;
	private Set<Integer> bitSet;

	public SparseBitMatrix3D(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.bitSet = new HashSet<>();
	}

	public void set(int key) {
		this.bitSet.add(key);
	}
	
	public void set(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Out of bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		this.bitSet.add(key);
	}
	
	public void unset(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Out of bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		this.bitSet.remove(key);
	}
	
	public boolean get(int key) {
		return this.bitSet.contains(key);
	}
	
	public boolean get(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Out of bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		return this.bitSet.contains(key);
	}
	
	public int cardinality()  {
		return bitSet.size();
	}

	public boolean isSet(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Out of bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		return this.bitSet.contains(key);
	}

	public void reset() {
		this.bitSet.clear();
	}
	
	public Iterator<Integer> iterator() {
		return this.bitSet.iterator();
	}
	
	public String toString() {
		return "["+this.x+"x"+this.y+"x"+this.z+"]"+this.bitSet+"\n";
	}
}
