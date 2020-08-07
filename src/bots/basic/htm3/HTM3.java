package bots.basic.htm3;

import java.io.Serializable;
import java.util.Random;

import bots.basic.F;
import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseVectorFloat;

public class HTM3 implements Serializable {
	private int number_of_columns, number_of_cells_per_column, number_of_winning_columns, threshold_for_columns_activation;
	private static float threshold = 0.5F;

	private Column[] columns;

	private Random random = new Random();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HTM3() {
		this(1);
	}

	public HTM3(double scale) {
		number_of_columns = (int) (2048 * scale);
		number_of_cells_per_column = (int) (32 * scale * 2);
		number_of_winning_columns = (int) (number_of_columns * 0.02);
		threshold_for_columns_activation = 2;

		columns = new Column[number_of_columns];
	}

	public void init(int input_size) {
		SpatialPooler.init(input_size, columns, threshold, random);
	}

	public void process(SparseBitVector input) {
		SpatialPooler.process(input, columns, number_of_winning_columns, threshold_for_columns_activation);
	}

}
