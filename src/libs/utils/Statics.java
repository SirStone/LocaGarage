/**
 * 
 */
package bots.basic;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.TreeMap;

import robocode.ScannedRobotEvent;

/**
 * @author dtp4
 *
 */
public class Statics {
	public static final double ONEDEGREE = FastMath.PI/180;
	public static final double THREEOVERTWOPI = 3/2 * FastMath.PI;
	
	public static int[] scalarEncoder(double input, double minVal, double maxVal, int w, boolean periodic, int radius) {
		if(w%2==0) throw new IllegalStateException("W must be an odd number (to eliminate centering difficulty)");
		if(minVal >= maxVal) throw new IllegalStateException("maxVal must be > minVal");
		
		double rangeInternal = maxVal - minVal;
		int halfWidth = (w-1)/2;
		double padding = periodic? 0 : halfWidth;
		double resolution = (float)radius/w;
		double range = periodic?rangeInternal:rangeInternal+resolution;
		int n = (int) Math.ceil(w*(range/radius)+2*padding);
		double nInternal = n-2*padding;
		
		int[] output = new int[n];
		
		if(Double.isNaN(input)) {
			return output;
		}
		
		if(input < minVal) {
			if(periodic) throw new IllegalStateException("input (" + input +") less than range (" +minVal + " - " + maxVal + ")");
			else input = minVal;
		}
		else if(input > maxVal) {
			if(periodic) throw new IllegalStateException("input (" + input +") greater than periodic range (" +minVal + " - " + maxVal + ")");
			else input = maxVal;
		}
		
		double centerbin = 0;
		if(periodic) {
			centerbin = (input-minVal)*nInternal/range+padding;
		}
		else {
			centerbin = ((input-minVal)+resolution*0.5)/resolution+padding;
		}
		int bucketVal = (int) (centerbin - halfWidth);
		int minbin = bucketVal;
		int maxbin = bucketVal+2*halfWidth;
		if(periodic) {
			if(maxbin >= n) {
				int bottombins = maxbin-n+1;
				for (int i = 0; i < bottombins; i++) {
					output[i] = 1;
				}
				maxbin = n-1;
			}
			if(minbin < 0) {
				int topbins = -minbin;
				for (int i = n-topbins; i < n; i++) {
					output[i] = 1;
				}
				minbin = 0;
			}
		}
		
		for (int i = minbin; i < maxbin+1; i++) {
			output[i] = 1;
		}
		
		return output;
	}
	
	public static double[] joinArrays(int[] a, int[] b) {
		double[] out = new double[a.length+b.length];
		int i = 0;
		for (int j = 0; j < a.length; j++) {
			out[i++] = a[j];
		}
		for (int j = 0; j < b.length; j++) {
			out[i++] = b[j];
		}
		return out;
	}
	
	public static java.awt.geom.Point2D.Double linearGuess(double x, double y, ScannedRobotEvent e) {
		double lgx = x + FastMath.sin(e.getHeadingRadians())*e.getVelocity()*20;
		double lgy = y + FastMath.cos(e.getHeadingRadians())*e.getVelocity()*20;
		return new Point2D.Double(lgx, lgy);
	}

	public static double normalize(double x, double min, double max) {
		return (x - min) / (max - min);
	}
	
	public static double denormalize(double y, double min, double max) {
		return y * (max - min) + min;
	}
	
	public static double denormalize2(double y, double oldMin, double oldMax, double newMin, double newMax) {
		double oldRange = oldMin - oldMax;  
		double newRange = newMax - newMin;  
		return (((y - oldMin) * newRange) / oldRange) + newMin;
	}
	
	public static double clip(double value, double minClip, double maxClip) {
		value = value<minClip?minClip:value;
		value = value>maxClip?maxClip:value;
		return value;
	}

	public static void clip(double[] values, int minClip, int maxClip) {
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i]<minClip?minClip:values[i];
			values[i] = values[i]>maxClip?maxClip:values[i];
		}
	}
	
	public static double nextGaussian(double mean, double standardDeviation, Random random) {
		return random.nextGaussian()*standardDeviation+mean;
	}

	public static java.awt.geom.Point2D.Double getEnemyPosition(ScannedRobotEvent e, WhenBot robot) {
		double absAngle = e.getBearingRadians()+robot.getHeadingRadians();
		double x = robot.getX() + e.getDistance() * Math.sin(absAngle);
		double y = robot.getY() + e.getDistance() * Math.cos(absAngle);
		return new Point2D.Double(x, y);
	}

	public static int getWinner(double[] output) {
		int max = -1;
		double maxVal = 0;
		for (int i = 0; i < output.length; i++) {
			if(output[i]>maxVal) {
				maxVal = output[i];
				max = i;
			}
		}
		return max;
	}

	public static double getGain(double p) {
		double a = p / (9*p-2);
		return (4*p + 2*(p-1))*a+3*p*a-p;
	}

	public static double[] makeTarget(int index, int outputSize) {
		double[] out = new double[outputSize];
		out[index] = 1;
		return out;
	}
	
	public static int cantor(long a, long b) {
		return (int) (0.5*(a+b)*(a+b+1)+b);
	}
	

	public static int cantor(long a, long b, long c) {
		return cantor(cantor(a,b), c);
	}
	
	/**
	 * @param z
	 * @return int[2] = {x,y}
	 */
	public static int[] cantorInverse2values(int z) {
		double w = Math.floor((Math.sqrt(8*z+1)-1)*0.5);
		double t = (w*w+w)*0.5;
		double y = z-t;
		double x = w-y;
		
		int[] ab = new int[] {(int) x,(int) y};
		return ab;
	}

	public static int[] cantorInverse3values(int z) {
		int[] _2valuesFirst = cantorInverse2values(z);
		int[] _2valuesSecond = cantorInverse2values(_2valuesFirst[0]);
		return new int[] {_2valuesSecond[0], _2valuesSecond[1], _2valuesFirst[1]};
	}
	
	public static int gcd(int a, int b) {
		int bigger, smaller, mod;
		if(a>b) {
			bigger = a;
			smaller = b;
		}
		else {
			bigger = b;
			smaller = a;
		}
		
		while((mod = bigger % smaller) != 0) {
			bigger = smaller;
			smaller = mod;
		}
		
		return smaller;
	}
	
	private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

    public static String toRoman(int number) {
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }
}