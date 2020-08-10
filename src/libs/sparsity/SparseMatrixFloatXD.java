package bots.sparsity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SparseMatrixFloatXD implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int[] sizes;
	private long fullSize;
	private long biggestIndex;
	private Map<Long,Float> values;

	public SparseMatrixFloatXD(int...sizes) {
		if(sizes.length <= 0) throw new IllegalArgumentException("can't create a 0 dimension bit matrix or lower");
		
		this.sizes = new int[sizes.length];
		
		for (int i = 0; i < sizes.length; i++) {
			this.sizes[i] = sizes[i];
			
			if(sizes[i] <= 0) {
				throw new IllegalArgumentException("all dimensions must be > 0, found "+sizes[i]+" in position "+i);
			}
			fullSize = fullSize==0?this.sizes[i]:fullSize * this.sizes[i];
		}
		this.values = new HashMap<>();
		
		int[] container = new int[sizes.length];
		for (int i = 0; i < container.length; i++) {
			container[i] = sizes[i]-1;
		}
		biggestIndex = pair(container);
	}

	public void set(float value, long key) {
		if(key > getBiggestIndex()) throw new IllegalArgumentException("the key "+key+" is out of range [0,"+getBiggestIndex()+")");
		int[] indexes = unpair(key, sizes.length);
		for (int size = 0; size < sizes.length; size++) {
			if(indexes[size] >= sizes[size] || indexes[size] <= 0) {
				throw new IllegalArgumentException("the key "+key+" have been found invalid");
			}
		}
		
		this.values.put(key, value);
	}
	
	public void set(float value, int...indexes) {
		if(indexes.length != getSizes().length) throw new IllegalArgumentException("you need to give "+getSizes().length+" indexes");
		for (int index = 0; index < indexes.length; index++) {
			if(indexes[index] < 0 || indexes[index]>= getSizes()[index]) throw new IllegalArgumentException("index "+indexes[index]+" is out of range [0,"+getSizes()[index]+") for dimension "+index);
		}
		
		set(value, pair(indexes));
	}
	
	public void unset(long key) {
		if(key > getBiggestIndex()) throw new IllegalArgumentException("the key "+key+" is out of range [0,"+getBiggestIndex()+")");
		int[] indexes = unpair(key, sizes.length);
		for (int size = 0; size < sizes.length; size++) {
			if(indexes[size]>=sizes[size] || indexes[size] <= 0) {
				throw new IllegalArgumentException("the key "+key+" have been found invalid");
			}
		}
		
		this.values.remove(key);
	}
	
	public void unset(int...indexes) {
		if(indexes.length != getSizes().length) throw new IllegalArgumentException("you nee to give "+getSizes().length+" indexes");
		for (int index = 0; index < indexes.length; index++) {
			if(indexes[index] < 0 || indexes[index]>=getSizes()[index]) throw new IllegalArgumentException("index "+index+" is out of range [0,"+getSizes()[index]+")");
		}
		
		unset(pair(indexes));
	}
	
	public float get(int...indexes) {
		return this.values.get(pair(indexes));
	}
	
	public long cardinality()  {
		return values.size();
	}
	
	public void reset() {
		values.clear();
	}
	
	public Iterator<Float> iteratorValues() {
		return this.values.values().iterator();
	}
	
	public Iterator<Long> iteratorKeys() {
		return this.values.keySet().iterator();
	}
	
	public String toString() {
		return this.values.toString()+"\n";
	}

	public boolean isSet(long key) {
		return values.keySet().contains(key);
	}
	
	public boolean isSet(int...indexes) {
		return isSet(pair(indexes));
	}
	
	private static final long pairBasic(long x, long y) {
	    return x >= y ? x*x+x+y : y*y+x;
	}
	
	public static final long pair(int...x) {
		long key = x[0]; 
		for (int i = 1; i < x.length; i++) {
			key = pairBasic(key, x[i]);
		}
		return key;
	}

	private static final long[] unpairBasic(long z) {
		long sqrtz = (long) Math.sqrt(z);
		long sqz = sqrtz * sqrtz;
	    return (z-sqz)>=sqrtz ? new long[] {sqrtz, z-sqz-sqrtz} : new long[] {z-sqz, sqrtz};  
	}
	
	public static final int[] unpair(long z, int numberOfIndexes) {
		long[] out = new long[numberOfIndexes];

		long val = z;
		for (int i = 0; i < numberOfIndexes-1; i++) {
			long[] unpaired = unpairBasic(val);
			out[numberOfIndexes-1-i] = unpaired[1];
			val = unpaired[0];
		}
		out[0] = val;
		int[] int_out = new int[out.length];
		for (int i = 0; i < int_out.length; i++) {
			int_out[i] = (int) out[i];
		}
		return int_out;
	}

	public int[] getSizes() {
		return sizes;
	}

	public long getFullSize() {
		return fullSize;
	}

	public long getBiggestIndex() {
		return biggestIndex;
	}
}