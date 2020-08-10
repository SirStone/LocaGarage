package bots.basic;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

public class ShadowBot {
	public long t;
	public Point2D.Float xy = new Point2D.Float();
	public double heading;
	public double bearing;
	public double velocity;
	public double absoluteBearing;
	
	public ShadowBot(long time, Point2D.Float xy, double heading, double velocity) {
		this(time, xy, heading, velocity, -1, -1);
	}
	
	public ShadowBot(long time, Point2D.Float xy, double heading, double bearing, double velocity, double absoluteAngle) {
		this.t = time;
		this.xy = (Float) xy.clone();
		this.heading = heading;
		this.bearing = bearing;
		this.velocity = velocity;
		this.absoluteBearing = absoluteAngle;
	}
	
	public int igetX() {
		return (int)xy.getX();
	}
	
	public int igetY() {
		return (int)xy.getY();
	}
	
	public double getBearingRadians() { return bearing; }
	
	public double getHeadingRadians() { return heading; }
	
	public double getVelocity() { return velocity; }
}
