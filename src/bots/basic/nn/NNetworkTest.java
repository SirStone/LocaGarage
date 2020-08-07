package bots.basic.nn;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import bots.basic.F;
import parts.libs.ejml.simple.SimpleMatrix;
import robocode.util.Utils;

@SuppressWarnings("unused")
public class NNetworkTest {
	private double[] xor1 = new double[] {0, 0, 0};
	private double[] xor2 = new double[] {0, 1, 1};
	private double[] xor3 = new double[] {1, 0, 1};
	private double[] xor4 = new double[] {1, 0, 0};
	private double[][] xorTest = new double[][] {xor1, xor1, xor3, xor4};
	NNetwork nn;
	
	@Test
	public void initTest() {
		nn = new NNetwork();
		nn.addNetwork(new int[] {2,2,1});
		
		assertTrue(nn.network.size() == 2);
		assertTrue(nn.network.get(0).inputSize == 2);
		assertTrue(nn.network.get(0).outputSize == 2);
		assertTrue(nn.network.get(1).inputSize == 2);
		assertTrue(nn.network.get(1).outputSize == 1);
	}
	
	@Test
	public void xorTest() {
		int outputSize = 10;
		nn = new NNetwork();
		nn.addNetwork(new int[] {2,2,1});
		
		double error = 1;
		double precision = 0;
		double numberOfAttempts = 0;
		double numberOfFails = 0;
		double numberOfTrues = 0;
		while (precision < 0.99) {
			for (int j = 0; j < xorTest.length; j++) {
				numberOfAttempts++;
				double[] input = new double[] {xorTest[j][0],xorTest[j][1]};
				double label = xorTest[j][2];
				double[] output = nn.evaluate(input);
				double guess = Math.round(output[0]);
				double deltaE = output[0]-label;
				if(guess == label) numberOfTrues++;
				error = Math.pow(output[0] - label, 2)/2;
				nn.train(new double[] {deltaE}, 0.1);
				F.p("error=%f%n", error);
			}
		}
	}
}
