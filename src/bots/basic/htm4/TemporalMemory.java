/**
 * 
 */
package bots.basic.htm4;

import java.io.Serializable;
import java.util.Iterator;

import bots.basic.F;
import bots.sparsity.SparseBitMatrix2D;
import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseMath;
import bots.sparsity.SparseMatrixFloat2D;

/**
 * @author dtp4
 *
 */
public class TemporalMemory implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/* cells */
	/**
	 * this array of bit vectors represent if a cell is predicting/not predicting,
	 * every slot is a column, in every slot there are "number of cells per column"
	 * bits, an active bit means the cell in that column is in predicting state
	 */
	private SparseBitMatrix2D predictiveStateT_1;

	/**
	 * this array of bit vectors represent the active state of the cells, every slot
	 * is a column, in every slot there are "number of cells per column" bits, an
	 * active bit means the cell in that column is active
	 */
	private SparseBitMatrix2D activeStateT_1;

	/**
	 * For each [column, cell, dentrite] there's a 2D sparse matrix that contain the
	 * permanence values for the synaptic lateral connections between cells.
	 */
	private SparseMatrixFloat2D[][][] proximalSynapses;
	
	private int numCellsPerColumn, numberOfColumns, maxDentritesPerCell, minNumberOfActiveProximalSynapsesToActivateDentrite;
	
	public TemporalMemory(int numCellsPerColumn, int numberOfColumns, int maxDentritesPerCell, int minNumberOfActiveProximalSynapsesToActivateDentrite) {
		this.numCellsPerColumn = numCellsPerColumn;
		this.maxDentritesPerCell = maxDentritesPerCell;
		this.numberOfColumns = numberOfColumns;
		this.minNumberOfActiveProximalSynapsesToActivateDentrite = minNumberOfActiveProximalSynapsesToActivateDentrite;
		
		/* Init cells */
		predictiveStateT_1 = new SparseBitMatrix2D(numCellsPerColumn, numberOfColumns);
		activeStateT_1 = new SparseBitMatrix2D(numCellsPerColumn, numberOfColumns);
		proximalSynapses = new SparseMatrixFloat2D[numberOfColumns][numCellsPerColumn][maxDentritesPerCell];
	}

	public SparseBitVector evaluate(SparseBitVector winninColumnsSDR, boolean learn) {
		/*
		 * 1. Receive a set of active columns, evaluate them against predictions, and
		 * choose a set of active cells:
		 * 
		 * a. For each active column, check for cells in the column that have an active
		 * distal dendrite segment (i.e. cells that are in the “predictive state” from
		 * the previous time step), and activate them. If no cells have active segments,
		 * activate all the cells in the column, marking this column as “bursting”. The
		 * resulting set of active cells is the representation of the input in the
		 * context of prior input.
		 * 
		 * b. For each active column, learn on at least one distal segment. For every
		 * bursting column, choose a segment that had some active synapses at any
		 * permanence level. If there is no such segment, grow a new segment on the cell
		 * with the fewest segments, breaking ties randomly. On each of these learning
		 * segments, increase the permanence on every active synapse, decrease the
		 * permanence on every inactive synapse, and grow new synapses to cells that
		 * were previously active.
		 * 
		 * 2. Activate a set of dendrite segments: for every dendrite segment on every
		 * cell in the layer, count how many connected synapses correspond to currently
		 * active cells (computed in step 1). If the number exceeds a threshold, that
		 * dendrite segment is marked as active. The collection of cells with active
		 * distal dendrite segments will be the predicted cells in the next time step.
		 * 
		 * A cell is predicted if it has an active segment.
		 */

		SparseBitMatrix2D activeState = new SparseBitMatrix2D(numCellsPerColumn, numberOfColumns);

		/* 1. For each active column */
		Iterator<Long> winningColumn_itr = winninColumnsSDR.iterator();
		while (winningColumn_itr.hasNext()) {
			long winning_column_key = winningColumn_itr.next();
			int column = (int) winning_column_key;

			/*
			 * check for cells in the column that have an active distal dendrite segment
			 * (i.e. cells that are in the “predictive state” from the previous time step),
			 * and activate them.
			 */
			if (predictiveStateT_1.columnCardinality(column) > 0) {
				for (int cell = 0; cell < numCellsPerColumn; cell++) {
					if (predictiveStateT_1.isSet(cell, column)) {
						activeState.set(cell, column);

						/* grow a new dentrite on this cell */
						for (int dentrite = 0; dentrite < maxDentritesPerCell; dentrite++) {
							if (proximalSynapses[column][cell][dentrite] == null) {

								/*
								 * connect the dentrite to all active cells at step T-1 and give them permanence
								 * 0.5
								 */
								proximalSynapses[column][cell][dentrite] = new SparseMatrixFloat2D(numCellsPerColumn, numberOfColumns);
								proximalSynapses[column][cell][dentrite].clone(activeStateT_1);
								SparseMath.scale(proximalSynapses[column][cell][dentrite], 0.5F);
								break;

							}
						}
					}
				}
			} else {
				/*
				 * If no cells have active segments, activate all the cells in the column,
				 * marking this column as “bursting”.
				 */
				activeState.columnFill(column);

				/*
				 * For every bursting column, choose a segment that had some active synapses at
				 * any permanence level. If there is no such segment, grow a new segment on the
				 * cell with the fewest segments, breaking ties randomly.
				 */
				int minNumDentritesOnACell = maxDentritesPerCell;
				int cellWithMinNumOfDentrites = -1;
				for (int cell = 0; cell < numCellsPerColumn; cell++) {
					int numberOfExisitingDentrites = 0;
					for (int dentrite = 0; dentrite < maxDentritesPerCell; dentrite++) {
						if (proximalSynapses[column][cell][dentrite] != null) {
							numberOfExisitingDentrites++;
						}
					}
					if (numberOfExisitingDentrites < minNumDentritesOnACell) {
						minNumDentritesOnACell = numberOfExisitingDentrites;
						cellWithMinNumOfDentrites = cell;

						if (minNumDentritesOnACell == 0)
							break;
					}
				}
				F.p("column",column,"->cell chosen to grow",cellWithMinNumOfDentrites,"that has",minNumDentritesOnACell,"dentrites");

				/* grow a new dentrite */
				if (minNumDentritesOnACell < maxDentritesPerCell) {
					int dentriteIndex = minNumDentritesOnACell;
					proximalSynapses[column][cellWithMinNumOfDentrites][dentriteIndex] = new SparseMatrixFloat2D(
							numCellsPerColumn, numberOfColumns);

					/*
					 * connect the dentrite to all active cells at step T-1 and give them permanence
					 * 0.5
					 */
					proximalSynapses[column][cellWithMinNumOfDentrites][dentriteIndex].clone(activeStateT_1);
					SparseMath.scale(proximalSynapses[column][cellWithMinNumOfDentrites][dentriteIndex], 0.5F);
				}

			}

		}

		/*
		 * 2. Activate a set of dendrite segments: for every dendrite segment on every
		 * cell in the layer, count how many connected synapses correspond to currently
		 * active cells (computed in step 1). If the number exceeds a threshold, that
		 * dendrite segment is marked as active. The collection of cells with active
		 * distal dendrite segments will be the predicted cells in the next time step.
		 */
		SparseBitMatrix2D predictingCells = new SparseBitMatrix2D(numCellsPerColumn, numberOfColumns);
		for (int column = 0; column < numberOfColumns; column++) {
			for (int cell = 0; cell < numCellsPerColumn; cell++) {
				for (int dentrite = 0; dentrite < maxDentritesPerCell; dentrite++) {
					if (proximalSynapses[column][cell][dentrite] != null) {
						int numberOfActiveSynpases = 0;
						SparseMatrixFloat2D current_dentrite = proximalSynapses[column][cell][dentrite];
						Iterator<Integer> itr = current_dentrite.iteratorKey();
						while (itr.hasNext()) {
							int key = itr.next();
							/*
							 * if this synapse has a value over the threshold and matches an activated cell
							 * at the current time step
							 */
							if(activeState.isSet(key)) {
								if (current_dentrite.get(key) >= 0.5) {
									numberOfActiveSynpases++;
								}
								
								/* increase permanence */
								if(learn) {
									float newVal = current_dentrite.get(key)+0.1F;
									current_dentrite.set(key, newVal>=1?1:newVal);
								}
							}
							else {
								/* decrease permanence */
								if(learn) {
									float newVal = current_dentrite.get(key)-0.1F;
//									if(newVal <= 0) current_dentrite.unset(key);
									current_dentrite.set(key, newVal<=0?0:newVal);
								}
							}
							

							/*
							 * if we reached the threshold for the minimum number of connected synapse we
							 * can activate the dentrite, that will make the cell that the dentrite belongs
							 * in predictive state
							 */
							if (numberOfActiveSynpases >= minNumberOfActiveProximalSynapsesToActivateDentrite) {
								predictingCells.set(key);
								break;
							}
						}
					}
				}
			}
		}

		activeStateT_1 = activeState;
		predictiveStateT_1 = predictingCells;
		
		return activeState.flatten();
	}

	/* getters */
	public SparseBitMatrix2D getActiveStateT_1() {
		return activeStateT_1;
	}

	public SparseMatrixFloat2D[][][] getProximalSynapses() {
		return proximalSynapses;
	}

	public SparseBitMatrix2D getPredictiveStateT_1() {
		return predictiveStateT_1;
	};
}
