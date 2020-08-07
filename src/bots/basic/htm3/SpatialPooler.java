package bots.basic.htm3;


import java.util.Random;

import bots.basic.F;
import bots.sparsity.SparseBitVector;

public class SpatialPooler {
	
	public static void init(int input_size, Column[] columns, float threshold, Random random) {
		/**
		 * Phase 1 – Initialize Spatial Pooling algorithm parameters
		 * 
		 * Prior to receiving any inputs, the Spatial Pooling algorithm is initialized
		 * by computing a list of initial potential synapses for each column. This
		 * consists of a random set of inputs selected from the input space (within a
		 * column’s inhibition radius). Each input is represented by a synapse and
		 * assigned a random permanence value. The random permanence values are chosen
		 * with two criteria. (TODO) First, the values are chosen to be in a small range
		 * around connectedPerm, the minimum permanence value at which a synapse is
		 * considered “connected”. This enables potential synapses to become connected
		 * (or disconnected) after a small number of training iterations. (TODO)Second,
		 * each column has a natural center over the input region, and the permanence
		 * values have a bias towards this center, so that they have higher values near
		 * the center.
		 */
		int half_input_size = (int) (input_size * 0.5);
		for (int column = 0; column < columns.length; column++) {
			columns[column] = new Column(input_size, half_input_size, threshold, random);
		}
	}

	public static void process(SparseBitVector input, Column[] columns, int number_of_winning_columns, int threshold_for_columns_activation) {
		// TODO Auto-generated method stub
		/**
		 * Phase 2 – Compute the overlap with the current input for each column
		 * 
		 * Given an input vector, this phase calculates the overlap of each column with
		 * that vector. The overlap for each column is simply the number of connected
		 * synapses with active inputs, (TODO)multiplied by the column’s boost factor.
		 */
		SparseBitVector winning_columns = new SparseBitVector(number_of_winning_columns);
		for (int column = 0; column < columns.length; column++) {
			columns[column].overlap(input);
			if(columns[column].overlap >= threshold_for_columns_activation) winning_columns.set(column);
		}

		/**
		 * Phase 3 – Compute the winning columns after inhibition
		 * 
		 * The third phase calculates which columns remain as winners after the
		 * inhibition step. (TODO)localAreaDensity is a parameter that controls the
		 * desired density of active columns within a local inhibition area.
		 * Alternatively, the density can be controlled by parameter
		 * numActiveColumnsPerInhArea. When using this method, the localAreaDensity
		 * parameter must be less than 0. The inhibition logic will ensure that at most
		 * numActiveColumnsPerInhArea columns become active in each local inhibition
		 * area. For example, if numActiveColumnsPerInhArea is 10, a column will be a
		 * winner if it has a non-zero overlap and its overlap score ranks 10th or
		 * higher among the columns within its inhibition radius.
		 */
		SparseBitVector winning_columns = new SparseBitVector(number_of_winning_columns);
		for (int column=0; column<columns.length; column++) {
			if(columns[column].overlap >= threshold_for_columns_activation) winning_columns.set(column);
		}
		F.p(winning_columns.cardinality());
	}
}
