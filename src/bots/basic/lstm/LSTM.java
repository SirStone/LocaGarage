package bots.basic.lstm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import bots.basic.F;
import bots.basic.Statics;
import parts.libs.ejml.simple.SimpleMatrix;

// builded first following https://blog.aidangomez.ca/2016/04/17/Backpropogating-an-LSTM-A-Numerical-Example/
// then tried to improve it
public class LSTM {
	// for forward step
	SimpleMatrix xt, Wa, Wi, Wf, Wo, Ua, Uf, Ui, Uo, ba, bf, bi, bo, at, it, ft, ot, outt, outt_1, statet, statet_1, Wguess, preGuess, guess;

	// for backward step
	SimpleMatrix Δ, Δoutt, δoutPre, δout, δstate, δa, δi, δf, δo, δx, δW, δU, δb, δstatetplus1, fplus1;
	private ArrayList<SimpleMatrix[]> steps = new ArrayList<>();
	public double error;
	int memory = 10;
	
	public LSTM() {
		outt_1 = new SimpleMatrix(memory, 1);
		statet_1 = new SimpleMatrix(memory, 1);
		
		setNewWeights();
	}
	
	private void setNewWeights() {
		Random rand = new Random();
		Wa = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		Wi = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		Wf = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		Wo = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		Ua = SimpleMatrix.random_DDRM(memory, memory, -1, 1, rand);
		Ui = SimpleMatrix.random_DDRM(memory, memory, -1, 1, rand);
		Uf = SimpleMatrix.random_DDRM(memory, memory, -1, 1, rand);
		Uo = SimpleMatrix.random_DDRM(memory, memory, -1, 1, rand);
		ba = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		bi = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		bf = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		bo = SimpleMatrix.random_DDRM(memory, 1, -1, 1, rand);
		Wguess = SimpleMatrix.random_DDRM(1, memory, -1, 1, rand);
	}
	
	public double forward(double input) {
		return forward(input, 0, false);
	}
	
	public double forward(double input, double label) {
		return forward(input, label, true);
	}

	public double forward(double input, double label, boolean withLabel) {
		convertInput(input);

		at = Statics.TANH.apply(Wa.mult(xt).plus(Ua.mult(outt_1)).plus(ba));
		it = Statics.SIGMOID.apply(Wi.mult(xt).plus(Ui.mult(outt_1)).plus(bi));
		ft = Statics.SIGMOID.apply(Wf.mult(xt).plus(Uf.mult(outt_1)).plus(bf));
		ot = Statics.SIGMOID.apply(Wo.mult(xt).plus(Uo.mult(outt_1)).plus(bo));
		statet = at.elementMult(it).plus(ft.elementMult(statet_1));
		outt = Statics.TANH.apply(statet).elementMult(ot);
		preGuess = Wguess.mult(outt);
		guess = Statics.SIGMOID.apply(preGuess);
		
		if(withLabel) {
			SimpleMatrix δError = errorFunctionDerivative(guess.get(0), label);
			SimpleMatrix δguess = δError.elementMult(Statics.SIN.derivate(preGuess, guess));
			SimpleMatrix Δpre = Wguess.transpose().mult(δguess);  
			
			steps.add(new SimpleMatrix[] {
					outt.copy(),//0
					new SimpleMatrix(new double[][]{{label}}),//1
					at.copy(),//2
					it.copy(),//3
					ft.copy(),//4
					ot.copy(),//5
					statet_1.copy(),//6
					ft.copy(),//7
					statet.copy(),//8
					xt.copy(),//9
					Δpre//10
					});
			SimpleMatrix error = errorFunction(guess.get(0), label);
//			F.p("Error=%f%n",error.get(0));
			this.error =  error.get(0);
		}
		
		// for the next step
		outt_1 = outt.copy();
		statet_1 = statet.copy();
		
		return outt.get(0);
	}
	
	public void backward(double epsylon) {
		int t = steps.size()-1;
		Δoutt = new SimpleMatrix(memory,1);
		δstate = new SimpleMatrix(memory,1);
		SimpleMatrix W = Wa.transpose().concatColumns(Wi.transpose()).concatColumns(Wf.transpose()).concatColumns(Wo.transpose());
		SimpleMatrix U = Ua.transpose().concatColumns(Ui.transpose()).concatColumns(Uf.transpose()).concatColumns(Uo.transpose());
		SimpleMatrix b = ba.transpose().concatColumns(bi.transpose()).concatColumns(bf.transpose()).concatColumns(bo.transpose());
		ArrayList<SimpleMatrix[]> bpSteps = new ArrayList<>();
		for (int i = 0; i < steps.size(); i++) {
			SimpleMatrix at = steps.get(t)[2];
			SimpleMatrix it = steps.get(t)[3];
			SimpleMatrix ft = steps.get(t)[4];
			SimpleMatrix ot = steps.get(t)[5];
			SimpleMatrix statet_1 = steps.get(t)[6];
			SimpleMatrix statet = steps.get(t)[8];
			
			if(t+1 == steps.size()) fplus1 = new SimpleMatrix(memory, 1);
			else fplus1 = steps.get(t+1)[7];
			
			Δ = steps.get(t)[10];
			δout = Δ.plus(Δoutt);
			δstate = δout.elementMult(ot).elementMult(Statics.TANH.apply(statet).elementPower(2).negative().plus(1).plus(δstate.elementMult(fplus1)));
			δa = δstate.elementMult(it).elementMult(at.elementPower(2).negative().plus(1));
			δi = δstate.elementMult(at).elementMult(it).elementMult(it.negative().plus(1));
			δf = δstate.elementMult(statet_1).elementMult(ft).elementMult(ft.negative().plus(1));
			δo = δout.elementMult(Statics.TANH.apply(statet)).elementMult(ot).elementMult(ot.negative().plus(1));
			SimpleMatrix δgatest = δa.concatRows(δi).concatRows(δf).concatRows(δo);
			F.p("δgatest:");δgatest.printDimensions();
			δx = W.mult(δgatest);
			Δoutt = U.mult(δgatest);
			
			bpSteps.add(0, new SimpleMatrix[]{
					δgatest,//0
					steps.get(t)[0],//1 outt
					steps.get(t)[9]//2 xt
			});
			
//			F.p("step%d: Δ=%s%n",t, Δ.toString());
//			F.p("step%d: δout=%s%n",t, δout.toString());
//			F.p("step%d: δstate=%s%n",t, δstate.toString());
//			F.p("step%d: δa=%s%n",t, δa.toString());
//			F.p("step%d: δi=%s%n",t, δi.toString());
//			F.p("step%d: δf=%s%n",t, δf.toString());
//			F.p("step%d: δo=%s%n",t, δo.toString());
//			F.p("step%d: δx=%s%n",t, δx.toString());
//			F.p("step%d: Δoutt=%s%n",t, Δoutt.toString());
//			F.p("step%d: δgatest=%s%n",t, δgatest.toString());
			t--;
		}
		
		SimpleMatrix δW = new SimpleMatrix(4*memory, this.xt.getNumElements());
		SimpleMatrix δU = new SimpleMatrix(4*memory, memory);
		SimpleMatrix δb = new SimpleMatrix(4*memory, 1);
		for (int i = 0; i < bpSteps.size(); i++) {
			SimpleMatrix δgatest = bpSteps.get(i)[0]; 
			SimpleMatrix xt = bpSteps.get(i)[2].transpose();
			SimpleMatrix outt = bpSteps.get(i)[1];
			SimpleMatrix δgatestplus1;
			if(i+1 == bpSteps.size()) δgatestplus1 = new SimpleMatrix(4*memory,memory);
			else δgatestplus1 = bpSteps.get(i+1)[0];

			δW = δW.plus(δgatest.mult(xt));
			F.p("δU:");δU.printDimensions();
			F.p("δgatestplus1");δgatestplus1.printDimensions();
			F.p("outt");outt.printDimensions();
			δU = δU.plus(δgatestplus1.mult(outt));
			δb = δb.plus(δgatest);
		}
		
		// updating weights
		W = W.plus(-epsylon, δW.transpose()).transpose();
		F.p("U:");U.printDimensions();
		F.p("δU");δU.printDimensions();
		U = U.plus(-epsylon, δU.transpose()).transpose();
		b = b.plus(-epsylon, δb.transpose()).transpose();
		Wa = W.extractVector(true, 0);
		Wi = W.extractVector(true, 1);
		Wf = W.extractVector(true, 2);
		Wo = W.extractVector(true, 3);
		Ua = U.extractVector(true, 0);
		Ui = U.extractVector(true, 1);
		Uf = U.extractVector(true, 2);
		Uo = U.extractVector(true, 3);
		ba = b.extractVector(true, 0);
		bi = b.extractVector(true, 1);
		bf = b.extractVector(true, 2);
		bo = b.extractVector(true, 3);
		
		steps.clear();
	}

	private SimpleMatrix errorFunction(double guess, double label) {
		return new SimpleMatrix(new double[][] {{Math.pow(guess-label, 2)/2}});
	}
	
	private SimpleMatrix errorFunctionDerivative(double guess, double xtilde) {
		return new SimpleMatrix(new double[][] {{ guess-xtilde }});
	}

	//////////////Not LSTM algorithm related//////////////////////////////
	private void convertInput(double input) {
		this.xt = new SimpleMatrix(new double[][]{{input}});
	}

	public void setXt(SimpleMatrix xt) {
		this.xt = xt;
	}

	public void setWa(SimpleMatrix wa) {
		Wa = wa;
	}

	public void setWi(SimpleMatrix wi) {
		Wi = wi;
	}

	public void setWf(SimpleMatrix wf) {
		Wf = wf;
	}

	public void setWo(SimpleMatrix wo) {
		Wo = wo;
	}

	public void setUa(SimpleMatrix ua) {
		Ua = ua;
	}

	public void setUf(SimpleMatrix uf) {
		Uf = uf;
	}

	public void setUi(SimpleMatrix ui) {
		Ui = ui;
	}

	public void setUo(SimpleMatrix uo) {
		Uo = uo;
	}

	public void setBa(SimpleMatrix ba) {
		this.ba = ba;
	}

	public void setBf(SimpleMatrix bf) {
		this.bf = bf;
	}

	public void setBi(SimpleMatrix bi) {
		this.bi = bi;
	}

	public void setBo(SimpleMatrix bo) {
		this.bo = bo;
	}

	public void setAt(SimpleMatrix at) {
		this.at = at;
	}

	public void setIt(SimpleMatrix it) {
		this.it = it;
	}

	public void setFt(SimpleMatrix ft) {
		this.ft = ft;
	}

	public void setOt(SimpleMatrix ot) {
		this.ot = ot;
	}

	public void setOutt(SimpleMatrix outt) {
		this.outt = outt;
	}

	public void setOutt_1(SimpleMatrix outt_1) {
		this.outt_1 = outt_1;
	}

	public void setStatet(SimpleMatrix statet) {
		this.statet = statet;
	}

	public void setStatet_1(SimpleMatrix statet_1) {
		this.statet_1 = statet_1;
	}

	public Object getAllWeights() {
		AllWeights allWeights = new AllWeights(Wa, Wi, Wf, Wo, Ua, Uf, Ui, Uo, ba, bf, bi, bo);
		return allWeights;
	}

	public void setWeights(Object readAllWeights) {
		AllWeights allWeights = (AllWeights) readAllWeights;
		Wa = allWeights.Wa;
		Wi = allWeights.Wi;
		Wf = allWeights.Wf;
		Wo = allWeights.Wo;
		Ua = allWeights.Ua;
		Ui = allWeights.Ui;
		Uf = allWeights.Uf;
		Uo = allWeights.Uo;
		ba = allWeights.ba;
		bi = allWeights.bi;
		bf = allWeights.bf;
		bo = allWeights.bo;
	}
}

class AllWeights implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1016241427046323446L;
	
	public AllWeights(SimpleMatrix wa, SimpleMatrix wi, SimpleMatrix wf, SimpleMatrix wo, SimpleMatrix ua,
			SimpleMatrix uf, SimpleMatrix ui, SimpleMatrix uo, SimpleMatrix ba, SimpleMatrix bf, SimpleMatrix bi,
			SimpleMatrix bo) {
		super();
		Wa = wa;
		Wi = wi;
		Wf = wf;
		Wo = wo;
		Ua = ua;
		Uf = uf;
		Ui = ui;
		Uo = uo;
		this.ba = ba;
		this.bf = bf;
		this.bi = bi;
		this.bo = bo;
	}

	SimpleMatrix Wa, Wi, Wf, Wo, Ua, Uf, Ui, Uo, ba, bf, bi, bo;
}