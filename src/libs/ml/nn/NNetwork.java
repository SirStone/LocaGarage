package bots.basic.nn;

import java.io.Serializable;
import java.util.ArrayList;

import bots.basic.ActivationFunctions;
import bots.basic.ActivationFunctions.ActivationFunction;
import parts.libs.ejml.simple.SimpleMatrix;

public class NNetwork implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ArrayList<Layer> network = new ArrayList<>();
	int inputSize = -1;
	int outputSize = -1;
	
	//Adam hyperparametere
	private static int t = 0;
	private static double alpha = 0.001;
	private static double beta1 = 0.9;
	private static double beta2 = 0.999;
	private static double epsilon = Math.pow(10, -8);
	private static double m = 0, v = 0;

	public NNetwork() {
		super();
	}

	/**
	 * @param networkNumbers first number is the input size, every subsequent number
	 *                       is the size of the new layer, by default the layers will
	 *                       be given a sigmoid function
	 */
	public void addNetwork(int[] networkNumbers) {
		if(networkNumbers.length > 2) {
			this.inputSize = networkNumbers[0];
			this.outputSize = networkNumbers[networkNumbers.length-1];
			for (int i = 1; i < networkNumbers.length; i++) {
				network.add(new Layer(inputSize, networkNumbers[i], ActivationFunctions.SIGMOID));
				inputSize = networkNumbers[i];
			}
		}
	}
	
	//alternative constructor
	public int addLayer(int inputSize, int outputSize, ActivationFunction func) {
		if(network.size() == 0) this.inputSize = inputSize;
		network.add(new Layer(inputSize, outputSize, func));
		this.outputSize = outputSize;
		return network.size()-1;
	}
	
	public void setFunction(ActivationFunction func[]) {
		int max = Math.min(func.length, network.size());
		for (int i = 0; i < max; i++) {
			network.get(i).setFunc(func[i]);
		}
	}
	
	public double[] evaluate(double[] input) {
		SimpleMatrix in = new SimpleMatrix(new double[][] {input}).transpose();
		SimpleMatrix out = in.copy();
		for (int i = 0; i < network.size(); i++) {
			out = network.get(i).forward(out).copy();
		}
		return toArray(out);
	}
	
	public void trainAdam(double g) {
		t++;
		m = beta1 * m + (1-beta1) * g;
		v = beta2 * v + (1-beta2) * Math.pow(g, 2);
		double m_hat = m/(1 - Math.pow(beta1, t));
		double v_hat = v/(1 - Math.pow(beta2, t));
		
		double update = -alpha * m_hat/(Math.sqrt(v_hat) + epsilon);
		
		for (int i = 0; i < network.size(); i++) {
			Layer l = network.get(i);
			l.setWeights(l.getWeights().plus(update));
			l.setBias(l.getBias().plus(update));
		}
	}
	
	public void train(double ΔE, double learningRate) {
		SimpleMatrix deltaIN = new SimpleMatrix(this.outputSize, 1);
		deltaIN.fill(ΔE);
		train(deltaIN, learningRate);
	}
	
	public void train(double[] ΔE, double learningRate) {
		SimpleMatrix deltaIN = new SimpleMatrix(new double[][] {ΔE}).transpose();
		train(deltaIN, learningRate);
	}
	
	private void train(SimpleMatrix deltaIN, double learningRate) {
		int nnsize = network.size();
		for (int i = nnsize-1; i >= 0; i--) {
			try {
				deltaIN = network.get(i).backpropagation(deltaIN, learningRate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double[] toArray(SimpleMatrix in) {
		double[] out = new double[in.getNumElements()];
		for (int i = 0; i < out.length; i++) {
			out[i] = in.get(i);
		}
		return out;
	}

	public Object getSnapshot() {
		ArrayList<Object> snapshot = new ArrayList<>(2);
		SimpleMatrix[][] weights = new SimpleMatrix[network.size()][2];
		ActivationFunction[] functions = new ActivationFunction[network.size()];
		for (int i = 0; i < weights.length; i++) {
			Layer l = network.get(i);
			SimpleMatrix[] layersnapshot = new SimpleMatrix[2];
			layersnapshot[0] = l.getWeights().copy();
			layersnapshot[1] = l.getBias().copy();
			weights[i] = layersnapshot;
			
			functions[i] = l.getFunc();
		}
		snapshot.add(weights);
		snapshot.add(functions);
		return snapshot;
	}

	public void loadSnapshot(Object input) {
		@SuppressWarnings("unchecked")
		ArrayList<Object> snapshot = (ArrayList<Object>) input;
		SimpleMatrix[][] weights = (SimpleMatrix[][]) snapshot.get(0);
		ActivationFunction[] functions = (ActivationFunction[]) snapshot.get(1);
		network.clear();
		for (int i = 0; i < weights.length; i++) {
			network.add(new Layer(weights[i]));
			network.get(network.size()-1).setFunc(functions[i]);
		}
		
		this.inputSize = weights[0][0].numCols();
		this.outputSize = weights[weights.length-1][0].numRows();
	}
	
	public NNetwork clone() {
		NNetwork clone = new NNetwork();
		clone.loadSnapshot(this.getSnapshot());
		return clone;
	}
	
	public String toString() {
		String out = "";
		
		for (int i = 0; i < this.network.size(); i++) {
			if(i>0) out += "-->";
			Layer l = this.network.get(i);
			out += "["+l.inputSize+"x"+l.outputSize+"]-->"+l.getFunc().toString();
		}
		
		return out;
	}

	public void polyakAveraging(NNetwork critic1, double targetupdaterate) {
		double oneminustargetupdaterate = 1-targetupdaterate;
		
		for (int i = 0; i < this.network.size(); i++) {
			Layer thisLayer = this.network.get(i);
			Layer critic1Layer = critic1.network.get(i);
			
			thisLayer.setWeights(critic1Layer.getWeights().scale(targetupdaterate).plus(thisLayer.getWeights().scale(oneminustargetupdaterate)));
			thisLayer.setBias(critic1Layer.getBias().scale(targetupdaterate).plus(thisLayer.getBias().scale(oneminustargetupdaterate)));
		}
	}
}