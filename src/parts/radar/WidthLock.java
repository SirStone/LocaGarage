/**
 * 
 */
package parts.radar;

import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import bots.basic.Part;

/**
 * @author dtp4
 *
 */
public class WidthLock extends Part {
	public WidthLock() {
		this.version = "1.1";
		this.status = "perfectly working";
		this.type = "radar";
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent enemy) {
		// Absolute angle towards target
	    double angleToEnemy = robot.getHeadingRadians() + enemy.getBearingRadians();

	    // Subtract current radar heading to get the turn required to face the enemy, be sure it is normalized
	    double radarTurn = Utils.normalRelativeAngle( angleToEnemy - robot.getRadarHeadingRadians() );

	    // Distance we want to scan from middle of enemy to either side
	    // The 36.0 is how many units from the center of the enemy robot it scans.
	    double extraTurn = Math.min( Math.atan( 37.0 / enemy.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );

	    // Adjust the radar turn so it goes that much further in the direction it is going to turn
	    // Basically if we were going to turn it left, turn it even more left, if right, turn more right.
	    // This allows us to overshoot our enemy so that we get a good sweep that will not slip.
	    if (radarTurn < 0)
	        radarTurn -= extraTurn;
	    else
	        radarTurn += extraTurn;
	    
	    //Turn the radar
	    robot.setTurnRadarRightRadians(radarTurn);
	}

	@Override
	public void run() {
		robot.setAdjustRadarForGunTurn(true);
		robot.setAdjustRadarForRobotTurn(true);
		if ( robot.getRadarTurnRemaining() == 0.0 ) robot.setTurnRadarRightRadians( Double.POSITIVE_INFINITY );
		
		super.run();
	}
}
