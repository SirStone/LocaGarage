package bots.basic.gru;

import static org.junit.Assert.*;

import java.util.InputMismatchException;

import org.junit.Test;

import bots.basic.F;

public class GRUTest {

	@Test
	public void Weights_Test() {
		int inputSize = 5;
		int memory = 10;
		GRU gru = new GRU(inputSize, memory);
		
		assertTrue("Wr is null!", gru.Wr != null);
		assertTrue("Wz is null!", gru.Wz != null);
		assertTrue("Wh is null!", gru.Wh != null);
		assertTrue("br is null!", gru.br != null);
		assertTrue("bz is null!", gru.bz != null);
		assertTrue("bh is null!", gru.bh != null);
	}
	
	@Test
	public void Evaluate_Test() {
		int inputSize = 2;
		int memory = 5;
		GRU gru = new GRU(inputSize, memory);
		
		float[] badInput = new float[] {0.1f,0.2f,0.3f,0.4f,0.5f};
		
		float[] goodInput = new float[] {0.1f,0.2f};
		boolean badInputTest = false;
		try {
			gru.evaluate(badInput);
		} catch (InputMismatchException e) {
			badInputTest = true;
		}
		
		assertTrue(badInputTest);
		
		float[] output = new float[0];
		try {
			output = gru.evaluate(goodInput);
		} catch (InputMismatchException e) {
			assertTrue(false);
		}
		
		assertTrue(output.length == memory);
		
		F.p("limit=%f%n",gru.limit);
		F.p("Wz");gru.Wz.print();
		F.p("Wr");gru.Wr.print();
		F.p("Wh");gru.Wh.print();
		F.p("xt");gru.xt.print();
		F.p("zt_input");gru.xt.print();
		F.p("zt");gru.xt.print();
		
		assertFalse(output[0]==0 && output[1]==0 && output[2]==0 && output[3]==0 && output[4]==0);
	}
}
