package bots.sparsity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bots.basic.Statics;

public class SparseMatrixFloat3D implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int x, y, z;
	private Map<Integer,Float> values;

	public SparseMatrixFloat3D(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.values = new HashMap<>();
	}

	public void set(int cantorKey, float value) {
		int[] unkey = Statics.cantorInverse3values(cantorKey);
		if(unkey[0]>=this.x || unkey[1]>=this.y || unkey[2]>=this.z) throw new IndexOutOfBoundsException("Invalid key");
		this.values.put(cantorKey, value);
	}

	public void set(long x, long y, long z, float value) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Values ["+x+","+y+","+z+"] are out of the bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		this.values.put(key, value);
	}
	
	public void unset(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Values ["+x+","+y+","+z+"] are out of the bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		this.values.remove(key);
	}
	
	public float get(int cantorKey) {
		int[] unkey = Statics.cantorInverse3values(cantorKey);
		if(unkey[0]>=this.x || unkey[1]>=this.y || unkey[2]>=this.z) throw new IndexOutOfBoundsException("Invalid key");
		if(this.values.containsKey(cantorKey)) return this.values.get(cantorKey);
		else return 0;
	}
	
	public float get(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Values ["+x+","+y+","+z+"] are out of the bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		if(this.values.containsKey(key)) return this.values.get(key);
		else return 0;
	}
	
	public int cardinality()  {
		return values.size();
	}

	public boolean isSet(long x, long y, long z) {
		if(x>=this.x || y>=this.y || z>=this.z) throw new IndexOutOfBoundsException("Values ["+x+","+y+","+z+"] are out of the bounds ["+this.x+","+this.y+","+this.z+"]");
		int key = Statics.cantor(x, y, z);
		return values.containsKey(key);
	}
	
	public Iterator<Integer> iteratorKey() {
		return values.keySet().iterator();
	}
	
	public Iterator<Float> iteratorValue() {
		return values.values().iterator();
	}

	public int[] getXYZ(int cantorZ) {
		return Statics.cantorInverse3values(cantorZ);
	}
	
	public String toString() {
		return "["+this.x+"x"+this.y+"x"+this.z+"]"+this.values+"\n";
	}

	public SparseBitMatrix3D getOverThreshold(float threshold) {
		SparseBitMatrix3D out = new SparseBitMatrix3D(x, y, z);
		Iterator<Integer> itr = iteratorKey();
		while(itr.hasNext()) {
			int key = itr.next();
			if(get(key) > threshold) out.set(key);
		}
		return out;
	}
}