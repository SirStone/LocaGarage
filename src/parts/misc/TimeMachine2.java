package parts.misc;

import java.util.ArrayList;
import java.util.List;

import bots.basic.Part;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class TimeMachine2 extends Part{
	private List<ScannedRobotEvent> scannedRobotEvents;
	private List<RobotStatus> robotStatuses;
	public int size;

	public int elementsNumber = 0;
	public boolean activated = false;
	
	public TimeMachine2() {
		this.type = "misc";
		this.version = "2";
		this.status = "emoved ShadowBots";
	}
	
	public void activate(int size) {
		this.size = size;
		scannedRobotEvents = new ArrayList<ScannedRobotEvent>();
		robotStatuses = new ArrayList<RobotStatus>();
		activated = true;
	}
	
	public RobotStatus getOldestRobotStatus() {
		return elementsNumber>0 ? robotStatuses.get(0): null;
	}
	
	public ScannedRobotEvent getOldestScannedRobotEvent() {
		return elementsNumber>0 ? scannedRobotEvents.get(0): null;
	}
	
	public ScannedRobotEvent getLatestScannedRobotEvent() {
		return elementsNumber>0 ? scannedRobotEvents.get(elementsNumber-1): null;
	}
	
	public RobotStatus getLatestRobotStatus() {
		return elementsNumber>0 ? robotStatuses.get(elementsNumber-1): null;
	}
	
	public ScannedRobotEvent getPastScannedRobotEvent(int t) {
		t = t<0?-t:t;
		int index = elementsNumber-1-t;
		if(index<0) throw new IllegalArgumentException("can't go "+t+" turns in the past");		
		return scannedRobotEvents.get(index);
	}
	
	public RobotStatus getPastRobotStatus(int t) {
		t = t<0?-t:t;
		int index = elementsNumber-1-t;
		if(index<0) throw new IllegalArgumentException("can't go "+t+" turns in the past");		
		return robotStatuses.get(index);
	}
	
	public void add(RobotStatus robotStatus, ScannedRobotEvent scannedRobotEvent) {
		if(elementsNumber == size) {
			scannedRobotEvents.remove(0);
			robotStatuses.remove(0);
		}
		else elementsNumber++;
		
		scannedRobotEvents.add(scannedRobotEvent);
		robotStatuses.add(robotStatus);
	}
	
	public boolean haveData() {
		if(this.elementsNumber > 0) return true;
		else return false;
	}

	@Override
	public void run() {
		scannedRobotEvents = new ArrayList<ScannedRobotEvent>();
		robotStatuses = new ArrayList<RobotStatus>();
		
		elementsNumber = 0;
		
		super.run();
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		if(!activated) return;
		/*----------Time Machine update---------------------*/
		add(latest_status, e);
		/*----------Time Machine update ends here------------*/
	}

	RobotStatus latest_status = null;
	@Override
	public void onStatus(StatusEvent e) {
		latest_status = e.getStatus();
	}
	
	public void setSize(int size) {
		this.size = size;
	}
}
