package bots.basic.nn;

import static org.junit.Assert.*;

import org.junit.Test;

import bots.basic.Statics;
import bots.basic.Statics.ActivationFunction;
import parts.libs.ejml.simple.SimpleMatrix;

public class LayerTest {

	Layer l1 = new Layer(10, 10, Statics.SIN);

	@Test
	public void allActivationsTest() {
		SimpleMatrix dummyInput = new SimpleMatrix(new double[][] { { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 } });
		dummyInput.reshape(dummyInput.getNumElements(), 1);

		ActivationFunction[] activations = new ActivationFunction[] { Statics.SIN, Statics.RELU, Statics.SIGMOID,
				Statics.TANH };
		int inputSize = dummyInput.getNumElements();
		int outputSize = inputSize;
		for (ActivationFunction activationFunction : activations) {
			try {
				Layer l = new Layer(inputSize, outputSize, activationFunction);
				SimpleMatrix outputF = l.forward(dummyInput);
				assertTrue("outputF size expected to be " + outputSize + " but found " + outputF.getNumElements(),
						outputF.getNumElements() == outputSize);
				assertTrue(outputF.isVector());
				assertTrue(outputF.numCols() == 1);

				SimpleMatrix outputB = l.backpropagation(dummyInput, 0.1f);
				assertTrue("outputB size expected to be " + inputSize + " but found " + outputB.getNumElements(),
						outputB.getNumElements() == inputSize);
				assertTrue(outputB.isVector());
				assertTrue(outputB.numCols() == 1);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Should not have thrown any exception");
			}
		}
	}

//	@Test
//	public void outputForwardTest() {
//		int inputSize = 10;
//		int outputSize = 5;
//		Layer l = new Layer(inputSize, outputSize, Statics.SIN);
//		SimpleMatrix input = new SimpleMatrix(new double[][] {{1,2,3,4,5,6,7,8,9,10}});
//		input.reshape(input.getNumElements(), 1);
//		SimpleMatrix output = l.forward(input);
//		assertTrue("output size expected to be "+outputSize+" but found "+output.getNumElements(), output.getNumElements() == outputSize);
//		assertTrue(output.isVector());
//		assertTrue(output.numCols() == 1);
//	}

	@Test(expected = java.util.InputMismatchException.class)
	public void badFormatForwardTest() throws Exception {
		SimpleMatrix input1 = new SimpleMatrix(new double[][] { { 1, 2, 3, 4, 5 } });
		SimpleMatrix input2 = new SimpleMatrix(new double[][] { { 1 }, { 2 }, { 3 }, { 4 }, { 5 } });
		l1.forward(input1);
		l1.forward(input2);
	}

	@Test(expected = java.lang.Exception.class)
	public void inputMissingTest() throws Exception {
		SimpleMatrix input = new SimpleMatrix(new double[][] { { 1, 2, 3, 4, 5 } });
		l1.backpropagation(input, 0.1f);
	}

	@Test(expected = java.util.InputMismatchException.class)
	public void badFormatBackwardTest() throws Exception {
		SimpleMatrix input = new SimpleMatrix(new double[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });
		l1.backpropagation(input, 0.1f);
	}

}
