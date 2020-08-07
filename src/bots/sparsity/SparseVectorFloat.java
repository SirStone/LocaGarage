package bots.sparsity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SparseVectorFloat implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int size;
	private Map<Long,Float> map;

	public SparseVectorFloat(int size) {
		this.size = size;
		this.map = new HashMap<>(size);
	}

	public void set(long index, float value) {
		this.map.put(index, value);
	}
	
	public void unset(long index) {
		this.map.remove(index);
	}
	
	public float get(long index) {
		if(this.map.containsKey(index)) return this.map.get(index);
		else return 0;
	}
	
	public int cardinality()  {
		return map.size();
	}
	
	public String toString() {
		String out = "["+size+"]";
		if(cardinality() > 10) {
			out += "]";
			Iterator<Float> itr = getValuesIterator();
			for (long i = 0; i < 10; i++) {
				if(i!=0) out += ",";
				out += itr.next();
			}
			out += "]";
		}
		else {
			out += map;
		}
			
		return out;
	}

	public Iterator<Float> getValuesIterator() {
		return this.map.values().iterator();
	}
	
	public Iterator<Long> getKeysIterator() {
		return this.map.keySet().iterator();
	}

	public boolean isSet(long i) {
		return map.containsKey(i);
	}

	public void removeEquals(float value) {
		map.entrySet().removeIf(e -> e.getValue() == value);
	}
}
