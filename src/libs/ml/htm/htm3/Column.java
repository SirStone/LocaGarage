package bots.basic.htm3;

import java.io.Serializable;
import java.util.Random;

import bots.basic.F;
import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseMath;
import bots.sparsity.SparseVectorFloat;

public class Column implements Serializable{
	
	private SparseBitVector activeConnections;
	private SparseVectorFloat proximal_synapses;
	
	public int overlap;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Column(int input_size, int half_input_size, float threshold, Random random) {
		activeConnections = new SparseBitVector(input_size);
		proximal_synapses = new SparseVectorFloat(input_size);
		
		for (int bit = 0; bit < half_input_size; bit++) {
			int randomBit = random.nextInt(input_size);
			if (proximal_synapses.isSet(randomBit))
				bit--;
			else {
				float random_permanence = random.nextFloat();
				proximal_synapses.set(randomBit, random_permanence);
				
				if(random_permanence >= threshold) activeConnections.set(randomBit);
			}
		}
	}

	public void overlap(SparseBitVector input) {
		overlap = SparseMath.and(input, activeConnections).cardinality();
	}

}
