package bots.basic.htm4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import bots.sparsity.SparseBitVector;

public class HTM4 implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* Spatial Pooler parameters */

	/**
	 * A sequence representing the dimensions of the columns in the SP region.
	 * Format is (height, width, depth, ...), where each value represents the size
	 * of the dimension.
	 */
	private int numberOfColumns = 2048;


	/**
	 * An alternate way to control the density of the active columns. If
	 * numActiveColumnsPerInhArea is specified then localAreaDensity must be less
	 * than 0, and vice versa. When using numActiveColumnsPerInhArea, the inhibition
	 * logic will insure that at most 'numActiveColumnsPerInhArea' columns remain ON
	 * within a local inhibition area (the size of which is set by the internally
	 * calculated inhibitionRadius, which is in turn determined from the average
	 * size of the connected receptive fields of all columns). When using this
	 * method, as columns learn and grow their effective receptive fields, the
	 * inhibitionRadius will grow, and hence the net density of the active columns
	 * will decrease. This is in contrast to the localAreaDensity method, which
	 * keeps the density of active columns the same regardless of the size of their
	 * receptive fields. This number divided by our total number of columns gives
	 * the sparsity.
	 */
	private int numWinningColumns = 10;

	/* objects involved */

	private int numCellsPerColumn;
	private int maxDentritesPerCell;
	private int maxSynapsesPerDentrite;
	private int minNumberOfActiveProximalSynapsesToActivateDentrite;
	private SpatialPooler sp;
	private TemporalMemory2 tm;
	private SDRClassifier[] classifiers;
	private int[] timeSteps;
	private List<SparseBitVector> tmOutputs = new ArrayList<>();

	public void init(int inputSize, int outputSize, int[] timeSteps) {
		this.timeSteps = timeSteps;
		numberOfColumns = 100;
		numWinningColumns = (int) (numberOfColumns * 0.02); // default ~2%
		numCellsPerColumn = 10;
		maxDentritesPerCell = 4;
		maxSynapsesPerDentrite = 12;
		minNumberOfActiveProximalSynapsesToActivateDentrite = (int) (0.1 * maxSynapsesPerDentrite);

		/* init Spatial Pooler */
		sp = new SpatialPooler(inputSize, numberOfColumns, numWinningColumns);
		
		/* init Temporal Memory */
		tm = new TemporalMemory2(numberOfColumns, numCellsPerColumn, maxDentritesPerCell);
		
		/* init SDR classifier */
		classifiers = new SDRClassifier[timeSteps.length];
		for (int i = 0; i < classifiers.length; i++) {
			classifiers[i] = new SDRClassifier(numberOfColumns * numCellsPerColumn, outputSize);
		}
	}
	
	public int[] process(SparseBitVector input) {
		return process(input, new int[] {}, false);
	}
	
	public int[] process(SparseBitVector input, int[] labels) {
		return process(input, labels, true);
	}

	public int[] process(SparseBitVector input, int[] labels, boolean learn) {
		SparseBitVector winninColumnsSDR = sp.evaluate(input, learn);
		SparseBitVector outputSDR = tm.evaluate(winninColumnsSDR, learn);
//		tmOutputs.add(outputSDR);
//		if(tmOutputs.size() == 1+timeSteps[timeSteps.length-1]) tmOutputs.remove(0);
//		int[] output = sdrClassifier(tmOutputs, labels, learn);
//		return output;
		return null;
	}

	/**
	 * @param tmOutputs
	 * @param learn 
	 * @param label 
	 */
	private int[] sdrClassifier(List<SparseBitVector> tmOutputs, int[] labels, boolean learn) {
		int[] out = new int[timeSteps.length];
		for (int i = 0; i < out.length; i++) {
			int index = tmOutputs.size() - timeSteps[i];
			if(index < 0) continue;
			else out[i] = classifiers[i].evaluate(tmOutputs.get(index), learn?labels[i]:-1, learn);
		}
		return out;
	}

	/* getters and setters zone */
	public int getColumnDimensions() {
		return numberOfColumns;
	}

	public int getNumCellsPercolumn() {
		return numCellsPerColumn;
	}

	public int getMaxDentritesPerCell() {
		return maxDentritesPerCell;
	}

	public int getMaxSynapsesPerDentrite() {
		return maxSynapsesPerDentrite;
	}

	public int getNumActiveColumnsPerInhArea() {
		return numWinningColumns;
	}

	public int getMinNumberOfActiveProximalSynapsesToActivateDentrite() {
		return minNumberOfActiveProximalSynapsesToActivateDentrite;
	}

	public SpatialPooler getSp() {
		return sp;
	}

	public TemporalMemory2 getTm() {
		return tm;
	}
}
