package bots.basic;

import robocode.*;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class WhenBot extends AdvancedRobot {
	private static final String version = "2.4";
	private static final String status = "added onSkippedTurn and cpu time profiler functions";
	
	private Vector<Part> parts = new Vector<Part>();
	private static Vector<Part> misc = new Vector<Part>();
	private static Vector<Part> radars = new Vector<Part>();
	private static Vector<Part> guns = new Vector<Part>();
	private static Vector<Part> bodies = new Vector<Part>();
	private static Vector<Part> unknown = new Vector<Part>();
	private static boolean doneOnce = false;
	
	public final void addParts(Part[] parts) {
		if(!doneOnce) {
			for (Part part : parts) {
				part.setRobot(this);
				
				switch (part.getType()) {
				case "misc":
					misc.add(part);
					break;
				case "radar":
					radars.add(part);
					break;
				case "gun":
					guns.add(part);
					break;
				case "body":
					bodies.add(part);
					break;
				default:
					unknown.add(part);
					break;
				}
			}
		}
	};

	int z = 0;
	public void run() {
		/* add them in the correct order */
		parts.addAll(misc);
		parts.addAll(radars);
		parts.addAll(guns);
		parts.addAll(bodies);
		parts.addAll(unknown);
		
		doneOnce();
		
		for (Part part : parts) {
			part.run();
		}
	}
	
	private final void doneOnce() {
		if(!doneOnce) {
			this.out.printf("WhenBot Version: %s Status: %s%n", version, status);
			for (Part part : parts) {
				String name = part.getClass().getSimpleName();
				String version = part.getVersion();
				String status = part.getStatus();
				String type = part.getType();
				String format = "LOADED PART(%s): %s Version: %s Status: %s%n";
				Color color = getColor(name);
				switch (type) {
				case "gun":
					this.setGunColor(color);
					this.setBulletColor(color.darker());
					this.out.printf(format, type, name, version, status);
					break;
				case "radar":
					this.setRadarColor(color);
					this.setScanColor(color.brighter());
					this.out.printf(format, type, name, version, status);
					break;
				case "body":
					this.setBodyColor(color);
					this.out.printf(format, type, name, version, status);
					break;
				case "misc":
					this.out.printf(format, type, name, version, status);
					break;
				default:
					this.out.printf("WARNING: %s type is UNKNOWN!!%n", name);
					break;
				}
			}
			doneOnce = true;
		}
	}

	private static final Color getColor(String name) {
		char[] chars = name.toCharArray();
		int seed = 0;
		for (int i = 0; i < chars.length; i++) {
			seed += chars[i];
		}
		Random rand = new Random(seed);
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		return new Color(r,g,b);
	}
	
//	public void sharePart(int key, Part part) {
//		sharedParts.put(key, part);
//	}
	
	public Vector<Part> getParts(String type) {
		switch (type) {
		case "misc":
			return misc;
		case "radars":
			return radars;
		case "guns":
			return guns;
		case "bodies":
			return bodies;
		case "unknown":
			return unknown;
		default:
			return null;
		}
	}
	
	public final void saveSnapshot(String filename, Object snapshot) {
		try
		{
			File file = this.getDataFile(filename+".zip");
			RobocodeFileOutputStream rfos = new RobocodeFileOutputStream(file);
			ZipOutputStream zipout = new ZipOutputStream(rfos);
			zipout.putNextEntry(new ZipEntry(filename));
			ObjectOutputStream out = new ObjectOutputStream(zipout);
			out.writeObject(snapshot);
			out.flush();
			zipout.closeEntry();
			out.close();
		}
		catch (IOException ex)
		{
			System.out.println("Error writing Object:" + ex);
		}
	}
	
	public final Object loadSnapshot(String filename) {
		File file = null;
		try
		{
			file = this.getDataFile(filename+".zip");
			if(file.length() == 0) return null;
			ZipInputStream zipin = new ZipInputStream(new FileInputStream(file));
			zipin.getNextEntry();
			ObjectInputStream in = new ObjectInputStream(zipin);
			Object snapshot = in.readObject();
			in.close();
			return snapshot;
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found!");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("I/O Exception");
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Class not found! :-(");
			file.delete();
//			e.printStackTrace();
		}
		return null;    //could not get the object
	}
	
	static Map<String, Long> running_profilers = new HashMap<>();
	ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	public void CPUcount_start(String counter_name) {
		running_profilers.put(counter_name, threadMXBean.getCurrentThreadCpuTime());
	}
	
	public long CPUcount_stop(String counter_name) {
		long start = running_profilers.get(counter_name);
		long delta = threadMXBean.getCurrentThreadCpuTime() - start;
		return delta;
	}

	@Override
	public final void onCustomEvent(CustomEvent event) {
		for (Part part : parts) {
			part.onCustomEvent(event);
		}
		super.onCustomEvent(event);
	}

	@Override
	public final void onScannedRobot(ScannedRobotEvent event) {
		for (Part part : parts) {
			part.onScannedRobot(event);
		}
		super.onScannedRobot(event);
	}

	@Override
	public final void onBulletHit(BulletHitEvent event) {
		for (Part part : parts) {
			part.onBulletHit(event);
		}
		super.onBulletHit(event);
	}

	@Override
	public final void onHitByBullet(HitByBulletEvent event) {
		for (Part part : parts) {
			part.onHitByBullet(event);
		}
		super.onHitByBullet(event);
	}

	@Override
	public final void onPaint(Graphics2D g) {
		for (Part part : parts) {
			part.onPaint(g);
		}
		super.onPaint(g);
	}
	
	@Override
	public final void onRobotDeath(RobotDeathEvent event) {
		for (Part part : parts) {
			part.onRobotDeath(event);
		}
		super.onRobotDeath(event);
	}

	@Override
	public final void onWin(WinEvent event) {
		for (Part part : parts) {
			part.onWin(event);
		}
		super.onWin(event);
	}

	@Override
	public final void onDeath(DeathEvent event) {
		for (Part part : parts) {
			part.onDeath(event);
		}
		super.onDeath(event);
	}

	@Override
	public void onStatus(StatusEvent e) {
		for (Part part : parts) {
			part.onStatus(e);
		}
		super.onStatus(e);
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		for (Part part : parts) {
			part.onBattleEnded(event);
		}
		super.onBattleEnded(event);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		for (Part part : parts) {
			part.onBulletHitBullet(event);
		}
		super.onBulletHitBullet(event);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		for (Part part : parts) {
			part.onBulletMissed(event);
		}
		super.onBulletMissed(event);
	}

	@Override
	public void onSkippedTurn(SkippedTurnEvent event) {
		for (Part part : parts) {
			part.onSkippedTurn(event);
		}
		super.onSkippedTurn(event);
	}
}
