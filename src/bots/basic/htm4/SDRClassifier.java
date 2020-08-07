package bots.basic.htm4;

import java.io.Serializable;
import java.util.Iterator;

import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseMath;
import bots.sparsity.SparseMatrixFloat2D;

public class SDRClassifier implements Serializable{
	private static final long serialVersionUID = 1L;

	private int inputSize, outputSize;
	private SparseMatrixFloat2D weights;
	private float learningRate = 0.5F;
	
	public SDRClassifier(int inputSize, int outputSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.weights = new SparseMatrixFloat2D(outputSize, inputSize);
	}

	public int evaluate(SparseBitVector input, int label, boolean learn) {
		if(input.size != this.inputSize) throw new IllegalArgumentException("input length is different from expected, found "+input.size+" expected "+this.inputSize);
		if(learn && label < 0 || label >= outputSize) throw new IllegalArgumentException("label is out of range[0,"+outputSize+")");
		
		float[] softamx_result = SparseMath.matrixMult(weights, input);
		softMax(softamx_result);
		
		int max_index = -1;
		float max_value = 0;
		for (int i = 0; i < softamx_result.length; i++) {
			if(softamx_result[i]>=max_value) {
				max_value = softamx_result[i];
				max_index = i;
			}
		}
		
		if(learn && max_index - label != 0) {
		
			/* learning */
			float[] update = new float[softamx_result.length];
			for (int i = 0; i < update.length; i++) {
				update[i] = learningRate * (label==i?1:0 - softamx_result[i]);
			}
			
			Iterator<Long> activeColumns = input.iterator();
			while(activeColumns.hasNext()) {
				long active_column = activeColumns.next();
				for (int ou = 0; ou < outputSize; ou++) {
					weights.set(ou, active_column, weights.get(ou, active_column)+update[ou]);
				}
			
			}
		}
			
		
		return max_index;
	}

	private void softMax(float[] out) {
		float sum = 0;
		for (int i = 0; i < out.length; i++) {
			sum += Math.exp(out[i]);
		}
		
		float scalingValue = 1 / sum;
		
		for (int i = 0; i < out.length; i++) {
			out[i] = (float) (Math.exp(out[i]) * scalingValue);
		}
	}
}
