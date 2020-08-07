package parts.body;

import java.awt.geom.Point2D;

import bots.basic.F;
import bots.basic.FastMath;
import robocode.Rules;

public class BulletWave {
	long time;
	double power;
	double bearing0;
	Point2D.Double start_point = new Point2D.Double();
	double distance;
	double speed;
	double[] particlesDirection; // angle from start point
	double[] particlesDanger;
	final static int NUMBEROFPARTICLES = 100;
	final static double ONEOVERNUMBEROFPARTICLES = 1 / NUMBEROFPARTICLES;
	private double starting_degree;
	
	public BulletWave(long time, double power, double bearing0, Point2D.Double start_point) {
		super();
		this.time = time;
		this.power = power;
		this.bearing0 = bearing0;
		this.start_point = start_point;
		this.speed = (Rules.getBulletSpeed(power));
		
		/* the bullets are detected 1 turn after have been fired */
		this.distance = this.speed;
		starting_degree = bearing0 - slice * NUMBEROFPARTICLES * 0.5;
		
		/* particles init */
		particlesDirection = new double[NUMBEROFPARTICLES];
		particlesDanger = new double[NUMBEROFPARTICLES];
		initParticles();
	}
	
	public void evaluateDanger(Point2D.Double[] dangerPoints) {
		/* interesting points calculations */
//		double totalDanger = 0;
		for (Point2D.Double dangerPoint : dangerPoints) {
			if(dangerPoint != null) {
				double angle = F.angle(dangerPoint, this.start_point);
				double distance = dangerPoint.distance(this.start_point) - this.distance;
				double x = dangerPoint.x + distance * FastMath.sin(angle);
				double y = dangerPoint.y + distance * FastMath.cos(angle);
				Point2D.Double interestPoint = new Point2D.Double(x, y);
			
				/* calculating the dangers */
				for (int i = 0; i < NUMBEROFPARTICLES; i++) {
					x = F.x(this.start_point.x, this.distance, particlesDirection[i]);
					y = F.y(this.start_point.y, this.distance, particlesDirection[i]);
					particlesDanger[i] += interestPoint.distance(x,y);
	//				totalDanger += particlesDanger[i];
				}
			}
		}
		
		/* normalization */
//		totalDanger = 1/totalDanger;
//		double sum2 = 0;
//		for (int i = 0; i < NUMBEROFPARTICLES; i++) {
//			particlesDanger[i] = 1 - (particlesDanger[i] * totalDanger);
//			sum2 += particlesDanger[i];
//		}
//		sum2 = 1/sum2;
//		for (int i = 0; i < NUMBEROFPARTICLES; i++) {
//			particlesDanger[i] = particlesDanger[i] * sum2;
//		}
		
	}
	
	private final static double slice = 5 * Rules.GUN_TURN_RATE_RADIANS / NUMBEROFPARTICLES;
	private void initParticles() {
		for (int i = 0; i < NUMBEROFPARTICLES; i++) {
			particlesDirection[i] = starting_degree + slice*i;
		}
	}
	
	public void update(long time) {
		this.distance = this.speed * (time - this.time +1);
	}
}