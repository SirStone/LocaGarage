package bots.basic;

import java.awt.Graphics2D;

import robocode.BattleEndedEvent;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.WinEvent;

public abstract class Part {
	public Part() {
		this.id = this.toString();
	};
	
	protected String partVersion = "1.4";
	protected WhenBot robot;
	protected String id = "";
	protected String version = "0.1";
	protected String status = "unknown";
	protected String type = "unknown";
	
	public final void setTVS(String type, String version, String status) {
		this.type = type;
		this.version = version;
		this.status = status;
	}
	
	public void setRobot(WhenBot robot) {
		this.robot = robot;
	}
	
	public final String getVersion() { return this.version; }
	
	public String getType() {
		return this.type;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public Part setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void run() {};
	
	public void onStatus(StatusEvent e) {};
	
	public void onCustomEvent(CustomEvent e) {};

	public void onScannedRobot(ScannedRobotEvent e) {}

	public void onBulletHit(BulletHitEvent e) {}

	public void onHitByBullet(HitByBulletEvent e) {}

	public void onPaint(Graphics2D g) {}

	public void onRobotDeath(RobotDeathEvent e) {}

	public void onWin(WinEvent e) {}

	public void onDeath(DeathEvent e) {}

	public void onBattleEnded(BattleEndedEvent e) {}

	public void onBulletHitBullet(BulletHitBulletEvent e) {}

	public void onBulletMissed(BulletMissedEvent e) {}

	public void onSkippedTurn(SkippedTurnEvent event) {}
}
