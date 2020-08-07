package bots.basic.gru;

import java.io.Serializable;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import bots.basic.F;
import bots.basic.Statics;
import parts.libs.ejml.dense.row.RandomMatrices_DDRM;
import parts.libs.ejml.simple.SimpleMatrix;

public class GRU {
	// design parameters
	int inputSize, memory;
	
	// weights
	SimpleMatrix xt, Wr, Wz, Wh, Ur, Uz, Uh, br, bz, bh, ht, ht_1, _1_zt, hhat, hhat_input, zt, zt_input, rt, rt_input, rtht_1;
	
	double limit = 0;
	
	public GRU(int inputSize, int memory) {
		this.inputSize = inputSize;
		this.memory = memory;
		this.limit = Math.sqrt(6D / ((double)inputSize + (double)memory));
		ht_1 = new SimpleMatrix(memory, 1);
		initWeights();
	}

	private void initWeights() {
		Random rand = new Random();
		Wr = SimpleMatrix.random_DDRM(memory, inputSize, limit, limit, rand);
		Wz = SimpleMatrix.random_DDRM(memory, inputSize, -limit, limit, rand);
		Wh = SimpleMatrix.random_DDRM(memory, inputSize, -limit, limit, rand);
		Ur = new SimpleMatrix(RandomMatrices_DDRM.orthogonal(memory, memory, rand));
		Uz = new SimpleMatrix(RandomMatrices_DDRM.orthogonal(memory, memory, rand));
		Uh = new SimpleMatrix(RandomMatrices_DDRM.orthogonal(memory, memory, rand));
		br = new SimpleMatrix(memory, 1);
		br.fill(1);
		bz = new SimpleMatrix(memory, 1);
		bh = new SimpleMatrix(memory, 1);
	}
	
	public float[] evaluate(float[] input) {
		if(input.length != inputSize) throw new InputMismatchException("input is not of the expected size of "+inputSize);
		
		xt = new SimpleMatrix(new float[][] {input});
		xt.reshape(inputSize, 1);
		
		if (ht != null) ht_1 = ht;
		
		////////GRU Start////////////////////////
		//update gate
		zt_input = Wz.mult(xt).plus(Uz.mult(ht_1)).plus(bz);
		zt = Statics.SIGMOID.apply(zt_input);
		//forget gate
		rt_input = Wr.mult(xt).plus(Ur.mult(ht_1)).plus(br);
		rt = Statics.HARDSIGMOID.apply(rt_input);
		_1_zt = zt.negative().plus(1);
		rtht_1 = rt.elementMult(ht_1);
		hhat_input = Wh.mult(xt).plus(Uh.mult(rtht_1)).plus(bh);
		hhat = Statics.TANH.apply(hhat_input);
		ht = zt.elementMult(ht_1).plus(_1_zt.elementMult(hhat));
		////////GRU End//////////////////////////
		
		return toFloatArray(ht);
	}
	
	/**
	 * @param δE error from Loss function
	 * @param lr learning rate
	 * @return float[0][]=Δxt, float[1][]=Δht_1
	 */
	public double train(double δE, double lr) {
		SimpleMatrix δError = new SimpleMatrix(memory, 1);
		δError.fill(δE);
		SimpleMatrix δ1 = δError.elementMult(_1_zt);
		SimpleMatrix δ2 = δError.elementMult(hhat);
		SimpleMatrix δ3 = δ2.negative();
		SimpleMatrix δ4 = δError.elementMult(ht_1);
		SimpleMatrix δ5 = δError.elementMult(zt);
		SimpleMatrix δ6 = δ3.plus(δ4).elementMult(Statics.SIGMOID.derivate(zt_input, zt));
		SimpleMatrix δ7 = Uz.transpose().mult(δ6);
		SimpleMatrix δ8 = Wz.transpose().mult(δ6);
		SimpleMatrix δ9 = δ1.elementMult(Statics.TANH.derivate(hhat_input, hhat));
		SimpleMatrix δ10 = Uh.transpose().mult(δ9);
		SimpleMatrix δ11 = δ10.elementMult(ht_1);
		SimpleMatrix δ12 = δ10.elementMult(rt);
		SimpleMatrix δ13 = δ11.elementMult(Statics.HARDSIGMOID.derivate(rt_input, rt));
		SimpleMatrix δ14 = Wh.transpose().mult(δ9);
		SimpleMatrix δ15 = Wr.transpose().mult(δ13);
		SimpleMatrix δ16 = Ur.transpose().mult(δ13);
		
		// weights updates
		Wz.plus(-lr, δ6.mult(xt.transpose()));
		Uz.plus(-lr, δ6.mult(ht_1.transpose()));
		bz.plus(-lr, δ6);
		Wh.plus(-lr, δ9.mult(xt.transpose()));
		Uh.plus(-lr, δ9.mult(rtht_1.transpose()));
		bh.plus(-lr, δ9);
		Wz.plus(-lr, δ13.mult(xt.transpose()));
		Uz.plus(-lr, δ13.mult(ht_1.transpose()));
		bz.plus(-lr, δ13);
		
		SimpleMatrix Δxt = δ8.plus(δ14).plus(δ15);
		SimpleMatrix Δht_1 = δ12.plus(δ16).plus(δ7).plus(δ5);
		
		SimpleMatrix deltaerrorout = xt.transpose().mult(Δxt).plus(ht_1.transpose().mult(Δht_1));
		return deltaerrorout.get(0);
	}

	private float[] toFloatArray(SimpleMatrix in) {
		float[] out = new float[in.getNumElements()];
		for (int i = 0; i < out.length; i++) {
			out[i] = (float) in.get(i);
		}
		return out;
	}
	
	public Object getSnapshot() {
		return new Snapshot(Wr, Wz, Wh, Ur, Uz, Uh, br, bz, bh, ht_1);
	}
	
	public void loadSnapshot(Object snapshot) {
		Snapshot allWeights = (Snapshot) snapshot;
		Wr = allWeights.Wr;
		Wz = allWeights.Wz;
		Wh = allWeights.Wh;
		Ur = allWeights.Ur;
		Uz = allWeights.Uz;
		Uh = allWeights.Uh;
		br = allWeights.br;
		bz = allWeights.bz;
		bh = allWeights.bh;
		ht_1 = allWeights.ht_1;
	}

}

class Snapshot implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1016241427046323446L;
	
	

	public Snapshot(SimpleMatrix wr, SimpleMatrix wz, SimpleMatrix wh, SimpleMatrix ur, SimpleMatrix uz,
			SimpleMatrix uh, SimpleMatrix br, SimpleMatrix bz, SimpleMatrix bh, SimpleMatrix ht_1) {
		super();
		Wr = wr;
		Wz = wz;
		Wh = wh;
		Ur = ur;
		Uz = uz;
		Uh = uh;
		this.br = br;
		this.bz = bz;
		this.bh = bh;
		this.ht_1 = ht_1;
	}



	SimpleMatrix Wr, Wz, Wh, Ur, Uz, Uh, br, bz, bh, ht_1;
}