package bots.basic;

import java.awt.geom.Point2D;
import java.math.BigInteger;
import java.util.Arrays;

public class F {

	public F() {
		// TODO Auto-generated constructor stub
	}
	
	public static double angle(Point2D.Double x, Point2D.Double y) {
		return Math.atan2(x.x-y.x, x.y-y.y)+Math.PI;
	}
	
	public static double angle(Point2D.Float x, Point2D.Float y) {
		return Math.atan2(x.x-y.x, x.y-y.y)+Math.PI;
	}
	
	public static double x(double startx, double distance, double angle) {
		return startx + distance * Math.sin(angle);
	}
	
	public static double y(double starty, double distance, double angle) {
		return starty + distance * Math.cos(angle);
	}

	public static void yell() {
		yell(2,2);
	}
	
	public static void yell(int max) {
		yell(2,max);
	}
	
	public static void yell(int min, int max) {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		int maxi = Math.min(max, stackTrace.length);
		for (int j = maxi; j >= min; j--) {
			if(j > min)
				System.out.printf("%s->",stackTrace[j].getMethodName());
			else
				System.out.printf("%s%n",stackTrace[j].getMethodName());
		}
	}
//	
//	public static void p(Object s) {
//		System.out.println(s);
//	}
	
//	public static void p(String s, Object a) {
//		p(s, new Object[] {a});
//	}
//	
//	public static void p(String s, Object a, Object b) {
//		p(s, new Object[] {a,b});
//	}
//	
//	public static void p(String s, Object a, Object b, Object c) {
//		p(s, new Object[] {a,b,c});
//	}
//	
//	public static void p(String s, Object a, Object b, Object c, Object d) {
//		p(s, new Object[] {a,b,c,d});
//	}
//	
//	public static void p(String s, Object a, Object b, Object c, Object d, Object e) {
//		p(s, new Object[] {a,b,c,d,e});
//	}
//	
//	public static void p(String format, Object[] args) {
//		System.out.printf(format, args);
//	}
	
	public static void p(Object...x ) {
		String out = "";
		for (int i = 0; i < x.length; i++) {
			if(i>0) out += " ";
			Object o = x[i];
			if(o.getClass().isArray()) {
				String componentType = o.getClass().getComponentType().toString();
				int numberOfSquares = (int) componentType.chars().filter(ch -> ch == '[').count();
				int numberOfI = (int) componentType.chars().filter(ch -> ch == 'I').count();
				int numberOfF = (int) componentType.chars().filter(ch -> ch == 'F').count();
				int numberOfD = (int) componentType.chars().filter(ch -> ch == 'D').count();
				int numberOfS = (int) componentType.chars().filter(ch -> ch == 'S').count();
				switch (numberOfSquares) {
				case 0:
					if(componentType.contentEquals("int")) out += Arrays.toString((int[])o);
					else if(componentType.contentEquals("float")) out += Arrays.toString((float[])o);
					else if(componentType.contentEquals("double")) out += Arrays.toString((double[])o);
					else if(componentType.contentEquals("long")) out += Arrays.toString((long[])o);
					else if(componentType.contentEquals("class java.lang.String")) out += Arrays.toString((String[])o);
					else if(componentType.contentEquals("class java.math.BigInteger")) out += Arrays.toString((BigInteger[])o);
					break;
				case 1:
					if(numberOfI>0) out += Arrays.deepToString((int[][])o);
					else if(numberOfF>0) out += Arrays.deepToString((float[][])o);
					else if(numberOfD>0) out += Arrays.deepToString((double[][])o);
					else if(numberOfS>0) out += Arrays.deepToString((String[][])o);
					break;
				case 2:
					if(numberOfI>0) out += Arrays.deepToString((int[][][])o);
					else if(numberOfF>0) out += Arrays.deepToString((float[][][])o);
					else if(numberOfD>0) out += Arrays.deepToString((double[][][])o);
					else if(numberOfS>0) out += Arrays.deepToString((String[][][])o);
					break;
				case 3:
					if(numberOfI>0) out += Arrays.deepToString((int[][][][])o);
					else if(numberOfF>0) out += Arrays.deepToString((float[][][][])o);
					else if(numberOfD>0) out += Arrays.deepToString((double[][][][])o);
					else if(numberOfS>0) out += Arrays.deepToString((String[][][][])o);
					break;

				default:
					break;
				}
			}
			else {
				out += o;
			}
		}
		System.out.println(out);
	}
	
//	public static void p(Object a1, Object a2) {
//		System.out.println(a1+" "+a2);
//	}
//	
//	public static void p(Object a1, Object a2, Object a3) {
//		System.out.println(a1+" "+a2+" "+a3);
//	}
//	
//	public static void p(Object a1, Object a2, Object a3, Object a4) {
//		System.out.println(a1+" "+a2+" "+a3+" "+a4);
//	}
//	
//	public static void p(Object a1, Object a2, Object a3, Object a4, Object a5) {
//		System.out.println(a1+" "+a2+" "+a3+" "+a4+" "+a5);
//	}
//	
//	public static void p(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) {
//		System.out.println(a1+" "+a2+" "+a3+" "+a4+" "+a5+" "+a6+" "+a7+" "+a8+" "+a9);
//	}
}
