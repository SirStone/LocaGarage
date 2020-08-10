package bots.basic.htm4;

import java.util.Iterator;

import bots.sparsity.SparseBitVector;

public class Encoder {

	public static SparseBitVector scalarEncoder(double input, double minVal, double maxVal, int w, boolean periodic, int radius) {
		if (w % 2 == 0)
			throw new IllegalStateException("W must be an odd number (to eliminate centering difficulty)");
		if (minVal >= maxVal)
			throw new IllegalStateException("maxVal must be > minVal");

		double rangeInternal = maxVal - minVal;
		int halfWidth = (w - 1) / 2;
		double padding = periodic ? 0 : halfWidth-1;
		double resolution = (float) radius / w;
		double range = periodic ? rangeInternal : rangeInternal + resolution;
		int n = (int) Math.ceil(w * (range / radius) + 2 * padding);
		double nInternal = n - 2 * padding;

		SparseBitVector output = new SparseBitVector(n);

		if (Double.isNaN(input)) {
			return output;
		}

		if (input < minVal) {
			if (periodic)
				throw new IllegalStateException(
						"input (" + input + ") less than range (" + minVal + " - " + maxVal + ")");
			else
				input = minVal;
		} else if (input > maxVal) {
			if (periodic)
				throw new IllegalStateException(
						"input (" + input + ") greater than periodic range (" + minVal + " - " + maxVal + ")");
			else
				input = maxVal;
		}

		double centerbin = 0;
		if (periodic) {
			centerbin = (input - minVal) * nInternal / range + padding;
		} else {
			centerbin = ((input - minVal) + resolution * 0.5) / resolution + padding;
		}
		int bucketVal = (int) (centerbin - halfWidth);
		int minbin = bucketVal;
		int maxbin = bucketVal + 2 * halfWidth;
		if (periodic) {
			if (maxbin >= n) {
				int bottombins = maxbin - n + 1;
				for (int i = 0; i < bottombins; i++) {
					output.set(i);
				}
				maxbin = n - 1;
			}
			if (minbin < 0) {
				int topbins = -minbin;
				for (int i = n - topbins; i < n; i++) {
					output.set(i);
				}
				minbin = 0;
			}
		}
		
		for (int i = minbin; i < maxbin+1; i++) {
			output.set(i);
		}

		return output;
	}
	
	public static SparseBitVector singleBitEncoder(double value, double minVal, double maxVal, int dimension) {
		if (minVal >= maxVal)
			throw new IllegalStateException("maxVal must be > minVal");
		if (dimension < 1)
			throw new IllegalStateException("dimension must be > 0");
		
		SparseBitVector output = new SparseBitVector(dimension);
		
		double range = maxVal - minVal;
		double realPositionPercent = (value - minVal) /range;
		long encodedPosition = (long) (dimension * realPositionPercent);
		if(encodedPosition < 0 ) encodedPosition = 0;
		else if(encodedPosition > dimension-1) encodedPosition = dimension-1;
		output.set(encodedPosition);
//		F.p("value", value, "range", range, "real%", realPositionPercent, "encodedPosition", encodedPosition);
		return output;
	}
	
	public static SparseBitVector multiBitEncoder(SparseBitVector singleBitSDR, int bitsPerValue) {
		
		SparseBitVector multiBitSDR = new SparseBitVector(singleBitSDR.size + bitsPerValue - 1);
		Iterator<Long> itr = singleBitSDR.iterator();
		while(itr.hasNext()) {
			long index = itr.next();
			multiBitSDR.set((int)index, (int)index+bitsPerValue);
		}
		
		return multiBitSDR;
	}
	
	public static SparseBitVector multiBitEncoder(double value, double minVal, double maxVal, int bitsPerValue) {
		int dimension = (int) (maxVal - minVal + 1);
		SparseBitVector singleBitSDR = singleBitEncoder(value, minVal, maxVal, dimension);
		return multiBitEncoder(singleBitSDR, bitsPerValue);
	}
}
