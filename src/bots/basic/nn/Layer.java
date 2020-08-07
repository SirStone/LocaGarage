package bots.basic.nn;

import java.util.InputMismatchException;
import java.util.Random;

import bots.basic.ActivationFunctions;
import bots.basic.ActivationFunctions.ActivationFunction;
import parts.libs.ejml.simple.SimpleMatrix;

public class Layer {
	
	public int inputSize, outputSize;
	private SimpleMatrix input, output, weights, bias, z;
	private ActivationFunction func;

	public Layer(int inputSize, int outputSize, ActivationFunction func) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.func = func;
		
		initWeights(1);
	}

	public Layer(SimpleMatrix[] snapshot) {
		this.weights = snapshot[0];
		this.bias = snapshot[1];
		this.inputSize = this.weights.numCols();
		this.outputSize = this.weights.numRows();
		this.func = ActivationFunctions.SIGMOID;
	}

	public SimpleMatrix forward(SimpleMatrix input) {
		if ( !input.isVector() ) { throw new InputMismatchException("Input needs to be a vector"); }
		if ( input.getNumElements() != inputSize || input.numCols() != 1) { throw new InputMismatchException("Input is ["+input.numRows()+","+input.numCols()+"] but expected to be ["+inputSize+",1]"); }
		
		/* save the input for BP */
		this.input = input.copy();

		/* apply the weights */
		output = weights.mult(this.input);
		
		/* add bias vector */
		output = output.plus(bias);
		
		/* taking a copy for training */
		z = output.copy();
		
		/* apply activation function */
//		out = applySin(z);
		output = func.apply(z);
		
		return output;
	}
	
	public SimpleMatrix backpropagation(SimpleMatrix deltaIN, double learningRate) throws Exception {
		if ( !deltaIN.isVector()) { throw new InputMismatchException("deltaL input needs to be a vector"); }
		if(input == null) throw new Exception("the input for this layer is empty");
		
		/* deltaFunc */
		SimpleMatrix deltaFunc = deltaIN.elementMult(func.derivate(this.z, this.output));
//		F.p("deltaFunc%s\t","->");deltaFunc.printDimensions();
		
		/* deltaW */
		SimpleMatrix deltaW = deltaFunc.mult(input.transpose());
//		F.p("deltaW%s\t","->");deltaW.printDimensions();
		
		/* Modifying the weights */
//		F.p("weights%s\t","->");weights.printDimensions();
		weights = weights.plus(learningRate, deltaW.negative());
		
		/* Modifying the bias */
//		F.p("bias%s\t","->");bias.printDimensions();
		bias = bias.plus(learningRate, deltaFunc.negative());
		
		/* deltaOut */
		SimpleMatrix deltaOUT = weights.transpose().mult(deltaFunc);
		
		return deltaOUT;
	}
	
	public void initWeights(int method) {
		switch (method) {
		case 1:
			Random rnd = new Random();
			weights = SimpleMatrix.random_DDRM(outputSize, inputSize, -1, 1, rnd);
			break;
		case 2:
			weights = new SimpleMatrix(outputSize, inputSize);
			weights.fill(0.5);
			break;
		default:
			break;
		}
		
		bias = new SimpleMatrix(outputSize, 1);
		bias.fill(1);
	}
	
	public SimpleMatrix getWeights() {
		return this.weights;
	}
	
	public void setWeights(SimpleMatrix newWeights) {
		this.weights = newWeights;
	}
	
	public SimpleMatrix getBias() {
		return this.bias;
	}
	
	public void setBias(SimpleMatrix newBias) {
		this.bias = newBias;
	}
	
	public void setFunc(ActivationFunction func) {
		this.func = func;
	}
	
	public ActivationFunction getFunc() {
		return this.func;
	}
}
