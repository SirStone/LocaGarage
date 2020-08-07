package parts.misc;

import java.io.File;
import java.io.IOException;

import bots.basic.Part;
import robocode.DeathEvent;
import robocode.RobocodeFileOutputStream;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

public class CsvDataWrite extends Part {
	private static String[] header;

	private RobocodeFileOutputStream rfw;
	private File csv = null;
	private boolean header_written = false;
	
	public CsvDataWrite() {
		this.setTVS("misc", "1.0", "write CSV files");
	}

	private boolean run_once = false;
	@Override
	public void run() {
		if(!run_once) {
			double maxDistance = -32 + Math.hypot(robot.getBattleFieldWidth(), robot.getBattleFieldHeight());
			header = new String[] {
					"time",
					"enemyBearingRadians["+(-Math.PI)+";"+(Math.PI)+"]",
					"enemyHeadingRadians[0;"+(2*Math.PI)+"]",
					"enemyVelocity[0;"+Rules.MAX_VELOCITY+"]",
					"enemyDistance[0;"+maxDistance+"]"
			};
			
			run_once = true;
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		/* if it's the first scan we can write the header */
		if(!header_written) {
			csv = robot.getDataFile(robot.getName()+" VS "+e.getName()+"_round"+robot.getRoundNum()+".csv");
			try {
				rfw = new RobocodeFileOutputStream(csv);
				write(header);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			header_written = true;
		}
		
		/*current data to save */
		double[] scan = new double[] {
				robot.getTime(),
				e.getBearingRadians(),
				e.getHeadingRadians(),
				e.getVelocity(),
				e.getDistance(),
		};
		
//		robot.out.println("total space occupied="+csv.getTotalSpace());
		write(scan);
		
		super.onScannedRobot(e);
	}

	private void write(double[] scan) {
		String[] scan_strings = new String[scan.length];
		for (int i = 0; i < scan_strings.length; i++) {
			scan_strings[i] = Double.toString(scan[i]);
		}
		write(scan_strings);
	}

	private void write(String[] header2) {
		String toWrite = "";
		for (int i=0; i<header2.length-1; i++) {
			toWrite += header2[i]+",";
		}
		toWrite += header2[header2.length-1];
		toWrite += '\n';
		
		try {
//			robot.out.print("WRITING:");
//			robot.out.println(toWrite);
			rfw.write(toWrite.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onWin(WinEvent e) {
		try {
			rfw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		super.onWin(e);
	}

	@Override
	public void onDeath(DeathEvent e) {
		try {
			rfw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		super.onDeath(e);
	}
	

}
