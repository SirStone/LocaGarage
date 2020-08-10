/**
 * 
 */
package bots.basic.htm4;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseVectorFloat;

/**
 * @author dtp4
 *
 */
public class SpatialPooler implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * each element represent a column, in particular is a float vector that
	 * contains the permanence values of the synapses of that column
	 */
	private SparseVectorFloat columnSynapses[];
	
	/**
	 * this vector size is the number of total columns, if a bit is active means
	 * that the column with that key is a winner column
	 */
	private SparseBitVector winnerColumns;
	
	private int numWinningColumns = -1;

	public SpatialPooler(int inputDimension, int numberOfColumns, int numWinningColumns) {
		/* init columns */
		columnSynapses = new SparseVectorFloat[numberOfColumns];
		Random random = new Random();
		int todo = (int) (inputDimension * 0.5); // 50% of the input dimension
		for (int i = 0; i < columnSynapses.length; i++) {
			SparseVectorFloat synapses = new SparseVectorFloat(inputDimension);
			int done = 0;
			while (done < todo) {
				long random_synapse = random.nextInt(inputDimension);
				if (!synapses.isSet(random_synapse)) {
					float random_permanence = random.nextFloat();
					synapses.set(random_synapse, random_permanence);
					done++;
				}
			}
			columnSynapses[i] = synapses;
		}
		winnerColumns = new SparseBitVector(numberOfColumns);
		this.numWinningColumns = numWinningColumns;
	}
	
	/**
	 * Spatial Pooler receive the input SDR and return an SDR that represents the
	 * winning columns
	 * 
	 * @param input
	 * @param learn 
	 * @return
	 */
	public SparseBitVector evaluate(SparseBitVector input, boolean learn) {
		winnerColumns.reset();
		/* find the winning columns */
		int[][] columnScores = new int[columnSynapses.length][2];
		for (int i = 0; i < columnSynapses.length; i++) {
			int score = 0;
			SparseVectorFloat columnSynapse = columnSynapses[i];
			Iterator<Long> itr = input.iterator();
			while (itr.hasNext()) {
				long input_key = itr.next();
				if (columnSynapse.isSet(input_key) && columnSynapse.get(input_key) >= 0.5)
					score++;
			}
			columnScores[i][0] = i;
			columnScores[i][1] = score;
		}

		/* sort the scores */
		Arrays.sort(columnScores, Comparator.comparingInt(o -> o[1])); // the result is from min to max

		/* get the top 2% (highest score) */
		for (int count = 0, i = columnScores.length - 1; count < numWinningColumns && i >= 0; i--, count++) {
			int columnIndex = columnScores[i][0];
			winnerColumns.set(columnIndex);

			/* column training */
			if (learn) trainColumn(input, columnIndex, columnSynapses);
		}

		return winnerColumns;
	}

	/**
	 * This implements the basic step of the training for the proximal dentrytes
	 * connections without any boosting or neighborhood
	 * 
	 * @param input
	 * @param columnIndex
	 */
//	private int timesUnchanged = 0;
	private void trainColumn(SparseBitVector input, int columnIndex, SparseVectorFloat[] columnSynapses) {
//		int numChanges = 0;
		SparseVectorFloat column = columnSynapses[columnIndex];
		Iterator<Long> itr = column.getKeysIterator();
		while (itr.hasNext()) {
			long key = itr.next();
			float current_value = column.get(key);
			if (input.isSet(key) && current_value < 1) {
				float new_value = current_value + 0.2F;
				column.set(key, new_value > 1 ? 1 : new_value);
//				numChanges++;
			} else {
				float new_value = current_value - 0.2F;
				column.set(key, new_value < 0 ? 0 : new_value);
//				numChanges++;
			}
		}
//		column.removeEquals(0);
//		if (numChanges <= 1)
//			timesUnchanged++;
//		else
//			timesUnchanged = 0;

//		if (timesUnchanged >= 1000) htm.setSPlearn(false);
	}

	public SparseBitVector getWinnerColumns() {
		return winnerColumns;
	}

	public SparseVectorFloat[] getColumnSynapses() {
		return columnSynapses;
	}
}
