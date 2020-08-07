package bots.basic.lstm;

import static org.junit.Assert.*;

import org.junit.Test;

import bots.basic.F;
import parts.libs.ejml.simple.SimpleMatrix;

public class LSTMTest {

	@Test
	public void forwardTest() {
		LSTM lstm = new LSTM();
		lstm.memory = 10;
		
		double[] input0 = new double[] {0.5, 0.6};
		double label0 = 0.73;
		double input1 = 0.2;
		double label1 = 0.01;
		
		double out0 = lstm.forward(input0, label0);
		
//		double out1 = lstm.forward(input1, label1);

		lstm.backward(0.1);
	}

	private boolean equals(double a, double b) {
		return Math.abs(Math.abs(a) - Math.abs(b)) < 0.0001;
	}
}
