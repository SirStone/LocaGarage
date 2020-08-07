package bots.sparsity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class SparseBitMatrixXD implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BigInteger[] sizes;
	private BigInteger fullSize;
	private BigInteger biggestIndex = BigInteger.valueOf(0);
	private Set<BigInteger> set;

	public SparseBitMatrixXD(long...sizes) {
		if(sizes.length <= 0) throw new IllegalArgumentException("can't create a 0 dimension bit matrix or lower");
		
		this.sizes = new BigInteger[sizes.length];
		
		for (int i = 0; i < sizes.length; i++) {
			this.sizes[i] = BigInteger.valueOf(sizes[i]);
			
			if(sizes[i] <= 0) {
				throw new IllegalArgumentException("all dimensions must be > 0, found "+sizes[i]+" in position "+i);
			}
			fullSize = fullSize==null?this.sizes[i]:fullSize.multiply(this.sizes[i]);
		}
		this.set = new HashSet<>();
		
		long[] container = new long[sizes.length];
		for (int i = 0; i < container.length; i++) {
			container[i] = sizes[i]-1;
		}
		biggestIndex = pair(container);
	}

	public void set(BigInteger key) {
		if(key.compareTo(getBiggestIndex()) == 1) throw new IllegalArgumentException("the key "+key+" is out of range [0,"+getBiggestIndex()+")");
		BigInteger[] indexes = unpair(key, sizes.length);
		for (int size = 0; size < sizes.length; size++) {
			if(indexes[size].compareTo(sizes[size]) >= 0 || indexes[size].signum() <= 0) {
				throw new IllegalArgumentException("the key "+key+" have been found invalid");
			}
		}
		
		this.set.add(key);
	}
	
	public void set(long...indexes) {
		if(indexes.length != getSizes().length) throw new IllegalArgumentException("you nee to give "+getSizes().length+" indexes");
		for (int index = 0; index < indexes.length; index++) {
			if(indexes[index] < 0 || BigInteger.valueOf(indexes[index]).compareTo(getSizes()[index]) >= 0) throw new IllegalArgumentException("index "+index+" is out of range [0,"+getSizes()[index]+")");
		}
		
		set(pair(indexes));
	}
	
	public void unset(BigInteger key) {
		if(key.compareTo(getBiggestIndex()) == 1) throw new IllegalArgumentException("the key "+key+" is out of range [0,"+getBiggestIndex()+")");
		BigInteger[] indexes = unpair(key, sizes.length);
		for (int size = 0; size < sizes.length; size++) {
			if(indexes[size].compareTo(sizes[size]) >= 0 || indexes[size].signum() <= 0) {
				throw new IllegalArgumentException("the key "+key+" have been found invalid");
			}
		}
		
		this.set.remove(key);
	}
	
	public void unset(long...indexes) {
		if(indexes.length != getSizes().length) throw new IllegalArgumentException("you nee to give "+getSizes().length+" indexes");
		for (int index = 0; index < indexes.length; index++) {
			if(indexes[index] < 0 || BigInteger.valueOf(indexes[index]).compareTo(getSizes()[index]) >= 0) throw new IllegalArgumentException("index "+index+" is out of range [0,"+getSizes()[index]+")");
		}
		
		unset(pair(indexes));
	}
	
	public boolean get(long...indexes) {
		return this.isSet(pair(indexes));
	}
	
	public BigInteger cardinality()  {
		return BigInteger.valueOf(set.size());
	}
	
	public void reset() {
		set.clear();
	}
	
	public Iterator<BigInteger> iterator() {
		return this.set.iterator();
	}
	
	public String toString() {
		return this.set.toString()+"\n";
	}

	public boolean isSet(BigInteger key) {
		return set.contains(key);
	}
	
	public boolean isSet(long...indexes) {
		return set.contains(pair(indexes));
	}
	
	private static final BigInteger pairBasic(BigInteger x, BigInteger y) {
	    return x.compareTo(y) >= 0 ? (x.multiply(x)).add(x).add(y) :  (y.multiply(y)).add(x);
	}
	
	public static final BigInteger pair(long...x) {
		BigInteger key = BigInteger.valueOf(x[0]); 
		for (int i = 1; i < x.length; i++) {
			key = pairBasic(key, BigInteger.valueOf(x[i]));
		}
		return key;
	}

	private static final BigInteger[] unpairBasic(BigInteger z) {
		BigInteger sqrtz = z.sqrt();
		BigInteger sqz = sqrtz.multiply(sqrtz);
	    return (z.subtract(sqz)).compareTo(sqrtz) >= 0 ? new BigInteger[] {sqrtz, z.subtract(sqz).subtract(sqrtz)} : new BigInteger[] {z.subtract(sqz), sqrtz};  
	}
	
	public static final BigInteger[] unpair(BigInteger z, int numberOfIndexes) {
		BigInteger[] out = new BigInteger[numberOfIndexes];

		BigInteger val = z;
		for (int i = 0; i < numberOfIndexes-1; i++) {
			BigInteger[] unpaired = unpairBasic(val);
			out[numberOfIndexes-1-i] = unpaired[1];
			val = unpaired[0];
		}
		out[0] = val;
		return out;
	}

	public BigInteger[] getSizes() {
		return sizes;
	}

	public BigInteger getFullSize() {
		return fullSize;
	}

	public BigInteger getBiggestIndex() {
		return biggestIndex;
	}
}
