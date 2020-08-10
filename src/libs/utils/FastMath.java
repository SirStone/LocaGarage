package bots.basic;

import static java.lang.Math.abs;
import java.io.PrintStream;

/**
 * FastMath - a faster replacement to Trig
 *
 * Run command: java -Xmx1G wiki.FastMath
 *
 * @author Rednaxela
 * @author Skilgannon
 * @author Starrynte
 * @author Nat
 */
public final class FastMath {
	public static final double PI = 3.1415926535897932384626433832795D;
	public static final double TWO_PI = 6.2831853071795864769252867665590D;
	public static final double HALF_PI = 1.5707963267948966192313216916398D;
	public static final double QUARTER_PI = 0.7853981633974483096156608458199D;
	public static final double THREE_OVER_TWO_PI = 4.7123889803846898576939650749193D;

	private static final int TRIG_DIVISIONS = 8192;// MUST be power of 2!!!
	private static final int TRIG_HIGH_DIVISIONS = 131072;// MUST be power of 2!!!
	private static final double K = TRIG_DIVISIONS / TWO_PI;
	private static final double ACOS_K = (TRIG_HIGH_DIVISIONS - 1) / 2;
	private static final double TAN_K = TRIG_HIGH_DIVISIONS / PI;

	private static final double[] sineTable = new double[TRIG_DIVISIONS];
	private static final double[] tanTable = new double[TRIG_HIGH_DIVISIONS];
	private static final double[] acosTable = new double[TRIG_HIGH_DIVISIONS];

	static {
		for (int i = 0; i < TRIG_DIVISIONS; i++) {
			sineTable[i] = Math.sin(i / K);
		}
		for (int i = 0; i < TRIG_HIGH_DIVISIONS; i++) {
			tanTable[i] = Math.tan(i / TAN_K);
			acosTable[i] = Math.acos(i / ACOS_K - 1);
		}
	}

	public static final void init() {
		// A little hack to allow you to initialize it either way
		sineTable[0] = sineTable[0];
	}

	public static final double sin(double value) {
		return sineTable[(int) (((value * K + 0.5) % TRIG_DIVISIONS + TRIG_DIVISIONS)) & (TRIG_DIVISIONS - 1)];
	}

	public static final double cos(double value) {
		return sineTable[(int) (((value * K + 0.5) % TRIG_DIVISIONS + 1.25 * TRIG_DIVISIONS)) & (TRIG_DIVISIONS - 1)];
	}

	private static final double tan(double value) {
		return tanTable[(int) (((value * TAN_K + 0.5) % TRIG_HIGH_DIVISIONS + TRIG_HIGH_DIVISIONS))
				& (TRIG_HIGH_DIVISIONS - 1)];
	}

	public static final double asin(double value) {
		// return atan(x / Math.sqrt(1 - x*x));
		return HALF_PI - acos(value);
	}

	public static final double pow(final double a, final double b) {
		final long tmp = Double.doubleToLongBits(a);
		final long tmp2 = (long) (b * (tmp - 4606921280493453312L)) + 4606921280493453312L;
		return Double.longBitsToDouble(tmp2);
	}

	public static final double acos(double value) {
		return acosTable[(int) (value * ACOS_K + (ACOS_K + 0.5))];
	}

	public static final double atan(double value) {
		return (value >= 0 ? acos(1 / sqrt(value * value + 1)) : -acos(1 / sqrt(value * value + 1)));
	}

	public static final double atan2(double x, double y) {
		return (x >= 0 ? acos(y / sqrt(x * x + y * y)) : -acos(y / sqrt(x * x + y * y)));
	}

	public static final double sqrt(double x) {
		return Math.sqrt(x);
		// return x * (1.5d - 0.5*x* (x = Double.longBitsToDouble(0x5fe6ec85e7de30daL -
		// (Double.doubleToLongBits(x)>>1) )) *x) * x;
	}
	
	public static final double SQRT_2_PI_INVERSE = 1 / Math.sqrt(2 * Math.PI);
	public static final double EXP_LIMIT = 700;
	public static final double GAUSSIAN_LIMIT = Math.sqrt(EXP_LIMIT * 2);
	public static final double MIN_GAUSSIAN_VALUE = gaussian(GAUSSIAN_LIMIT);
	
	public static double gaussian(final double u) {
	    if (u > GAUSSIAN_LIMIT || u < -GAUSSIAN_LIMIT) {
	        return MIN_GAUSSIAN_VALUE;
	    }
	    return exp(u * u * -0.5) * SQRT_2_PI_INVERSE;
	}
	 
	private static double exp(final double val) {
	    final long tmp = (long) (1512775 * val + 1072632447);
	    return Double.longBitsToDouble(tmp << 32);
	}

	public static void main(String args[]) {
		double[] angletest, arctest, atantest, tantest;
		double[][] atan2test, powtest;
		double[] expect, current;
		PrintStream o = System.out;
		final int NUM = 10000000;
		long ms;
		double error;
		angletest = new double[NUM];
		tantest = new double[NUM];
		arctest = new double[NUM];
		atantest = new double[NUM];
		atan2test = new double[NUM][2];
		powtest = new double[NUM][2];
		System.out.println("Initializing FastMath...");
		for (int i = 0; i < NUM; i++) {
			// angletest[i] = (Math.random() + 1d) + i * Math.PI - Math.PI / 3d;
			angletest[i] = Math.random() * (PI * 3) - PI;
			tantest[i] = Math.random() * PI - HALF_PI;
			arctest[i] = Math.random() * 2d - 1d;
			atantest[i] = Math.random() * 16000d - 8000d;
			atan2test[i][0] = Math.random() * 10000d - 5000d;
			atan2test[i][1] = Math.random() * 10000d - 5000d;
			powtest[i][0] = Math.random() * 10000d - 5000d;
			powtest[i][1] = Math.random() * 10000d - 5000d;
		}
		ms = -System.nanoTime();
		FastMath.init();
		ms += System.nanoTime();
		o.printf("FastMath init time: %.5f seconds\n", ms / 1E9);
		o.println();

		// ---------------------------------------
		o.println("== Sine Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.sin
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.sin(angletest[i]);
		}
		ms += System.nanoTime();
		o.printf("Math.sin() time: %.5f seconds\n", ms / 1E9);

		// FastMath.sin
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.sin(angletest[i]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.sin() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.sin() worst error: %.5f\n", error);
		// ---------------------------------------
		o.println("== Cosine Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.cos
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.cos(angletest[i]);
		}
		ms += System.nanoTime();
		o.printf("Math.cos() time: %.5f seconds\n", ms / 1E9);

		// FastMath.cos
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.cos(angletest[i]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.cos() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.cos() worst error: %.5f\n", error);
		// ---------------------------------------
		o.println("== Tangent Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.tan
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.tan(tantest[i]);
		}
		ms += System.nanoTime();
		o.printf("Math.tan() time: %.5f seconds\n", ms / 1E9);

		// FastMath.tan
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.tan(tantest[i]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.tan() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.tan() max error: %.5f\n", error);
		// ---------------------------------------
		o.println("== Arcsine Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.asin
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.asin(arctest[i]);
		}
		ms += System.nanoTime();
		o.printf("Math.asin() time: %.5f seconds\n", ms / 1E9);

		// FastMath.asin
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.asin(arctest[i]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.asin() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.asin() worst error: %.5f\n", error);
		// ---------------------------------------
		o.println("== Arccosine Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.acos
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.acos(arctest[i]);
		}
		ms += System.nanoTime();
		o.printf("Math.acos() time: %.5f seconds\n", ms / 1E9);

		// FastMath.acos
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.acos(arctest[i]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.acos() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.acos() worst error: %.5f\n", error);
		// ---------------------------------------
		o.println("== Arctangent Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.atan
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.atan(atantest[i]);
		}
		ms += System.nanoTime();
		o.printf("Math.atan() time: %.5f seconds\n", ms / 1E9);

		// FastMath.atan
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.atan(atantest[i]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.atan() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.atan() worst error: %.5f\n", error);
		// ---------------------------------------
		o.println("== Arctangent2 Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.atan2
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.atan2(atan2test[i][0], atan2test[i][1]);
		}
		ms += System.nanoTime();
		o.printf("Math.atan2() time: %.5f seconds\n", ms / 1E9);

		// FastMath.atan2
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.atan2(atan2test[i][0], atan2test[i][1]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.atan2() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.atan2() worst error: %.5f\n", error);

		o.println("== Power Test ==");

		expect = new double[NUM];
		current = new double[NUM];
		error = 0;

		// Math.pow
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			expect[i] = Math.pow(powtest[i][0], powtest[i][1]);
		}
		ms += System.nanoTime();
		o.printf("Math.pow() time: %.5f seconds\n", ms / 1E9);

		// FastMath.pow
		ms = -System.nanoTime();
		for (int i = 0; i < NUM; i++) {
			current[i] = FastMath.pow(powtest[i][0], powtest[i][1]);
		}
		ms += System.nanoTime();
		o.printf("FastMath.pow() time: %.5f seconds\n", ms / 1E9);

		for (int i = 0; i < NUM; i++) {
			error = Math.max(error, abs(expect[i] - current[i]));
		}
		o.printf("FastMath.pow() worst error: %.5f\n", error);
	}
}