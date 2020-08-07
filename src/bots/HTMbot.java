package bots;

import bots.basic.Part;
import bots.basic.WhenBot;
import parts.body.WaveSurfing;
import parts.gun.HTMgun;
import parts.misc.TimeMachine;
import parts.radar.WidthLock;

public class HTMbot extends WhenBot {
	private static Part[] parts = new Part[] {
			new WidthLock(),
			new HTMgun(),
			new WaveSurfing(),
			new TimeMachine(),
	};
	
	public HTMbot() {
		this.addParts(parts);
	}
}
