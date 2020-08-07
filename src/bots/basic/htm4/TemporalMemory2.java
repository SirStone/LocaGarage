package bots.basic.htm4;

import java.io.Serializable;
import java.util.Iterator;

import bots.basic.F;
import bots.sparsity.SparseBitMatrixXD;
import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseMatrixFloatXD;

public class TemporalMemory2 implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int numberOfColumns, numberOfCellsPerColumn, maxDentritesPerCell;
	
	private SparseBitMatrixXD activeSegmentsT_1, activeSegments, activeCellsT_1, activeCells, matchingSegmentsT_1, matchingSegments;
	private SparseMatrixFloatXD activePotentialSynapsesT_1;

	public TemporalMemory2(int numberOfColumns, int numberOfCellsPerColumn, int maxDentritesPerCell) {
		this.numberOfColumns = numberOfColumns;
		this.numberOfCellsPerColumn = numberOfCellsPerColumn;
		this.maxDentritesPerCell = maxDentritesPerCell;
		activeSegmentsT_1 = new SparseBitMatrixXD(numberOfColumns, numberOfCellsPerColumn, maxDentritesPerCell);
		matchingSegmentsT_1 = new SparseBitMatrixXD(numberOfColumns, numberOfCellsPerColumn, maxDentritesPerCell);
		activeCellsT_1 = new SparseBitMatrixXD(numberOfColumns, numberOfCellsPerColumn);
		activePotentialSynapsesT_1 = new SparseMatrixFloatXD(numberOfColumns, numberOfCellsPerColumn, maxDentritesPerCell, numberOfColumns, numberOfCellsPerColumn);
	}

	public SparseBitMatrixXD evaluate(SparseBitVector activeColumns, boolean learn) {
		activeCells = new SparseBitMatrixXD(numberOfColumns, numberOfCellsPerColumn);
		activeSegments = new SparseBitMatrixXD(numberOfColumns, numberOfCellsPerColumn, maxDentritesPerCell);
		matchingSegments = new SparseBitMatrixXD(numberOfColumns, numberOfCellsPerColumn, maxDentritesPerCell);
		
		for (int column = 0; column < numberOfColumns; column++) {
			if(activeColumns.isSet(column)) {
				if (count_segmentsForColumn(column, activeSegmentsT_1) > 0 ) {
					activatePredictedColumn(column);
				}
				else {
					burstColumn(column);
				}
			}
			else {
				if (count_segmentsForColumn(column, matchingSegmentsT_1) > 0 ) {
					punishPredictedColumn(column);
				}
			}
		}
		
		activeCellsT_1 = activeCells;
		activeSegmentsT_1 = activeSegments;
		matchingSegmentsT_1 = matchingSegments;
		return activeCells;
	}

	private int count_segmentsForColumn(long column, SparseBitMatrixXD activeSegments) {
		int count = 0;
		for (int cell = 0; cell < numberOfCellsPerColumn; cell++) {
			for (int dentrite = 0; dentrite < maxDentritesPerCell; dentrite++) {
				if(activeSegments.isSet(column, cell, dentrite)) count++;
			}
		}
//		F.p("column",column,"has",count,"active dentrites");
		return count;
	}
	
	private void activatePredictedColumn(long column) {
		// TODO Auto-generated method stub
		
	}

	private void burstColumn(long column) {
		for (int cell = 0; cell < numberOfCellsPerColumn; cell++) {
			activeCells.set(column, cell);
		}
		
		if(count_segmentsForColumn(column, matchingSegmentsT_1) > 0) {
			long winnerCell = bestMatchingSegment(column);
		}
		
	}

	private void punishPredictedColumn(int column) {
		// TODO Auto-generated method stub
		
	}
	
	private long bestMatchingSegment(long column) {
		long cellOfBestMatchingSegment = -1;
		int bestScore = -1;
		for (int cell = 0; cell < numberOfCellsPerColumn; cell++) {
			for (int segment = 0; segment < maxDentritesPerCell; segment++) {
				int score = 0;
				if(matchingSegmentsT_1.isSet(column, cell, segment)) {
					for (int syn_column = 0; syn_column < numberOfColumns; syn_column++) {
						for (int syn_cell = 0; syn_cell < numberOfCellsPerColumn; syn_cell++) {
							if( activePotentialSynapsesT_1.isSet(column, cell, segment, syn_cell, syn_cell)) {
								score++;
							}
						}
					}
				}
				if(score>bestScore) {
					cellOfBestMatchingSegment = cell;
					bestScore = score;
				}
			}
		}
		
		return cellOfBestMatchingSegment;
	}
}
