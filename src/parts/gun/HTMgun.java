package parts.gun;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import bots.basic.F;
import bots.basic.Part;
import bots.basic.ShadowBot;
import bots.basic.Statics;
import bots.basic.htm4.Encoder;
import bots.basic.htm4.HTM4;
import bots.sparsity.SparseBitMatrix2D;
import bots.sparsity.SparseBitVector;
import bots.sparsity.SparseVectorFloat;
import parts.misc.TimeMachine;
import robocode.DeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class HTMgun extends Part {
	private static HTM4 htm = null;
	private boolean init_done = false, run_once = false;
	private TimeMachine tm;

	public HTMgun() {
		this.setTVS("gun", "0.9", "gun that uses the HTM algorithm I'm implenting");
	}

	/* tiles categories */
	private List<Rectangle2D> tiles = new ArrayList<>();
	private double max_tile_dimension = 50;
	private double tiles_rows, tiles_columns, tile_dimension;
	private int numberOfCategories;
	
	private SparseBitVector input, real_input;
	private int[] labels;
	int[] output, real_output;
	private long onScannedRobot_exectime = 0;
	private long init_exectime = 0;
	private int[] timeSteps = new int[] {100};
	private int NUMBEROFSTEPSTOPREDICT = timeSteps[timeSteps.length-1];
	private ShadowBot oldestEnemy, oldestMe, currentEnemy, currentMe;
	
	@Override
	public void run() {
//		if(saveFile) {
//			Object snapshot = robot.loadSnapshot(filename);
//			if(snapshot instanceof HTM3) {
//				htm = (HTM3) snapshot;
//				F.p("loaded");
//			}
//		}
		
		if(!run_once) {
			/* TM init */
			Vector<Part> misc = robot.getParts("misc");
			if(misc.size() > 0 && misc.get(0)  instanceof TimeMachine) {
				tm = (TimeMachine) misc.get(0);
				if(!tm.activated) tm.activate(NUMBEROFSTEPSTOPREDICT);
				else {
					if(tm.size > NUMBEROFSTEPSTOPREDICT) NUMBEROFSTEPSTOPREDICT = tm.size;
					else tm.setSize(NUMBEROFSTEPSTOPREDICT);
				}
			}
			
			/* tiles init */
			tile_dimension = Statics.gcd((int)robot.getBattleFieldHeight(),  (int)robot.getBattleFieldWidth());
			
			while(tile_dimension > max_tile_dimension) tile_dimension*=0.5;
			
			tiles_columns = robot.getBattleFieldWidth()/tile_dimension;
			tiles_rows = robot.getBattleFieldHeight()/tile_dimension;
			for (int column = 0; column < tiles_columns; column++) {
				for (int row = 0; row < tiles_rows; row++) {
					tiles.add(new Rectangle2D.Double(column*tile_dimension, row*tile_dimension, tile_dimension, tile_dimension));
				}
			}
			
			/* vectors init */
			input = new SparseBitVector(tiles.size()*2);
			real_input = new SparseBitVector(tiles.size()*2);
			output = new int[timeSteps.length];
			real_output = new int[timeSteps.length];
			numberOfCategories = tiles.size();
			
			run_once = true;
		}
	}

//	@Override
//	public void onStatus(StatusEvent e) {
//		if(init_done && tm.haveData()) {
//			currentEnemy = tm.getLatestEnemy();
//			currentMe = tm.getLatestMe();
//			
//			//real input
//			SparseBitVector enemySDR = new SparseBitVector(tiles.size());
//			SparseBitVector mySDR = new SparseBitVector(tiles.size());
//			
//			enemySDR.set(getTile(currentEnemy.xy.x, currentEnemy.xy.y));
//			mySDR.set(getTile(currentMe.xy.x, currentMe.xy.y));
//			
//			real_input.clone(enemySDR);
//			real_input.concatenate(mySDR);
//			
//			real_output = htm.process(real_input);
//			
//			Rectangle2D choosen_tile = tiles.get(output[0]); 
//			
//			shootTo(choosen_tile.getCenterX(), choosen_tile.getCenterY());
//		}
//	}

	private int getTile(float x, float y) {
		int number_of_column = (int) (x/tile_dimension);
		int number_of_row = (int) (y/tile_dimension);
		
		return (int) (number_of_column * tiles_rows + number_of_row);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		if (tm.elementsNumber == tm.size) {
			oldestEnemy = tm.getOldestEnemy();
			oldestMe = tm.getOldestMe();
			
			SparseBitVector enemySDR = Encoder.multiBitEncoder((double)getTile(oldestEnemy.xy.x, oldestEnemy.xy.y), 0.0, (double) (tiles.size()-1), 20);
			SparseBitVector mySDR = Encoder.multiBitEncoder((double)getTile(oldestMe.xy.x, oldestMe.xy.y), 0.0, (double) (tiles.size()-1), 20);
			
			input.clone(enemySDR);
			input.concatenate(mySDR);
			
			labels = new int[timeSteps.length];
			int max = timeSteps[timeSteps.length-1];
			for (int step = 0; step < timeSteps.length; step++) {
				int wanted_step = max - timeSteps[step];
				ShadowBot enemy = tm.getPastBot(wanted_step, true);
				
				labels[step] = getTile(enemy.xy.x, enemy.xy.y);
			}
			
			/* htm init */
			if (!init_done) {
				robot.CPUcount_start("init time");
				htm = new HTM4();
				htm.init(input.size, numberOfCategories, timeSteps);
				init_done = true;
				init_exectime = robot.CPUcount_stop("init time");
			}
			
			/* htm invoke */
			output = htm.process(input, labels);
			
		}
		
		super.onScannedRobot(e);
	}
	
	private void shootTo(double x, double y) {
		double angle = Math.atan2(x - robot.getX(), y - robot.getY());
		robot.setTurnGunRightRadians(Utils.normalRelativeAngle(angle - robot.getGunHeadingRadians()));
	}

//	private String filename = "HTM";

	@Override
	public void onWin(WinEvent e) {
//		robot.saveSnapshot(filename, getsnapShot());
		super.onWin(e);
	}

	@Override
	public void onDeath(DeathEvent e) {
//		robot.saveSnapshot(filename, getsnapShot());
		super.onDeath(e);
	}

//	private Object getsnapShot() {
//		return htm;
//	}

	@Override
	public void onSkippedTurn(SkippedTurnEvent event) {
		super.onSkippedTurn(event);
	}

	@Override
	public void onPaint(Graphics2D g) {
		try {
	//		drawExecTimers(g);
			
			drawTiles(g);
			
			drawShadowBot(g, oldestEnemy, java.awt.Color.ORANGE);
			drawShadowBot(g, oldestMe, java.awt.Color.GREEN);
			
			drawLabels(g);
			drawOutput(g, output, java.awt.Color.ORANGE);
			drawOutput(g, real_output, java.awt.Color.CYAN);
			
			drawGunHeadings(g);
	
			drawHMT(g);
		} catch (Exception e) {}
		super.onPaint(g);
	}
	
	private void drawOutput(Graphics2D g, int[] output, Color color) {
		g.setColor(color);
		for (int o = 0; o < output.length; o++) {
			Rectangle2D tile = tiles.get(output[o]);
			Ellipse2D shape = new Ellipse2D.Double(tile.getCenterX()-10, tile.getCenterY()-10, 20, 20);
			g.fill(shape);
		}
	}
	
	private void drawLabels(Graphics2D g) {
		g.setColor(java.awt.Color.ORANGE);
		for (int label = 0; label < labels.length; label++) {
			Rectangle2D tile = tiles.get(labels[label]);
			g.draw(tile);
		}
	}

	private void drawTiles(Graphics2D g) {
		Iterator<Rectangle2D> itr = tiles.iterator();
		while(itr.hasNext()) {
			Rectangle2D tile = itr.next();
			g.setColor(java.awt.Color.LIGHT_GRAY);
			g.draw(tile);
			int tile_index = tiles.indexOf(tile);
			if(tile.contains(tm.getOldestMe().xy)) g.drawString(Integer.toString(tile_index), (float) tile.getCenterX(), (float) tile.getCenterY()); 
			if(tile.contains(tm.getOldestEnemy().xy)) g.drawString(Integer.toString(tile_index), (float) tile.getCenterX(), (float) tile.getCenterY()); 
		}
	}
	
	private void drawShadowBot(Graphics2D g, ShadowBot bot, Color color) {
		Ellipse2D shape = new Ellipse2D.Double(bot.xy.x-18, bot.xy.y-18, 36, 36);
		g.setColor(color);
		g.draw(shape);
	}

	private void drawHMT(Graphics2D g) {
		if (init_done) {

			g.setColor(java.awt.Color.GREEN);
			// g.drawString("input", -150, (int) (robot.getBattleFieldHeight()-10));
			drawInputSDR(g, 2, htm.getColumnDimensions() + 4);

			g.setColor(java.awt.Color.ORANGE);
			// g.drawString("column activity", -150, (int)
			// (robot.getBattleFieldHeight()-34));
			drawWinnerColumns(g, input.size + 6, 2);

			g.setColor(java.awt.Color.CYAN);
			// g.drawString("cell activation", -150, (int)
			// (robot.getBattleFieldHeight()-46));
//			drawCellStates(g, input.size + 20 + htm.getNumCellsPercolumn() * 15, 2);

			g.setColor(java.awt.Color.PINK);
			// g.drawString("cell predicting", -150, (int)
			// (robot.getBattleFieldHeight()-46));
//			drawCellPredicting(g, input.size + 20 + htm.getNumCellsPercolumn() * 15, 2);

			g.setColor(java.awt.Color.MAGENTA);
			// g.drawString("column permanences", -150, (int)
			// (robot.getBattleFieldHeight()-22));
			drawColumnPermanences(g, 2, 2);
		}
	}

	private void drawGunHeadings(Graphics2D g) {
		for (int shift = 0; shift <= 0; shift++) {
			int x2 = (int) (robot.getX() + 1000 * Math.sin(robot.getGunHeadingRadians()+Statics.ONEDEGREE*shift));
			int y2 = (int) (robot.getY() + 1000 * Math.cos(robot.getGunHeadingRadians()+Statics.ONEDEGREE*shift));
			g.drawLine((int)robot.getX(), (int)robot.getY(), x2, y2);
		}
	}

//	private void drawCellPredicting(Graphics2D g, int x, int y) {
//		SparseBitMatrix2D predictiveStates = htm.getTm().getPredictiveStateT_1();
//		/* draw border */
//		int numberOfCellsPerColumn = htm.getNumCellsPercolumn();
//		g.drawRect(x - numberOfCellsPerColumn * 15, y, numberOfCellsPerColumn * 15, htm.getColumnDimensions());
//
//		for (int column = 0, y2 = y; column < predictiveStates.columns; column++, y2++) {
//			for (int cell = predictiveStates.rows - 1, x2 = x - 15 * htm.getNumCellsPercolumn()
//					+ 2; cell >= 0; cell--, x2 += 15) {
//				if (predictiveStates.isSet(cell, column)) {
//					g.drawLine(x2, y2, x2 + 10, y2);
//				}
//			}
//		}
//	}

//	private void drawCellStates(Graphics2D g, int x, int y) {
//		if (init_done) {
////			SparseBitVector[] predictiveStates = htm.getPredictiveState();
//			SparseBitMatrix2D activeStates = htm.getTm().getActiveStateT_1();
//
//			/* draw border */
//			g.setColor(java.awt.Color.CYAN);
//			int numberOfCellsPerColumn = htm.getNumCellsPercolumn();
//
//			g.drawRect(x - numberOfCellsPerColumn * 15, y, numberOfCellsPerColumn * 15, htm.getColumnDimensions());
//
//			for (int column = 0, y2 = y; column < activeStates.columns; column++, y2++) {
//				for (int cell = activeStates.rows - 1, x2 = x - 15 * htm.getNumCellsPercolumn()
//						+ 2; cell >= 0; cell--, x2 += 15) {
//					if (activeStates.isSet(cell, column)) {
//						g.setColor(java.awt.Color.CYAN);
//						g.drawLine(x2, y2, x2 + 10, y2);
//					}
//				}
//			}
//		}
//	}

	private void drawWinnerColumns(Graphics2D g, int x, int y) {
		if (init_done) {
			SparseBitVector winnerColumns = htm.getSp().getWinnerColumns();

			/* draw border */
			g.setColor(java.awt.Color.ORANGE);
			g.drawRect(x - 2, y, 14, winnerColumns.size);

			int x2 = x + 10;
			for (int i = 0; i < winnerColumns.size; i++, y++) {
				if (winnerColumns.isSet(i)) {
					g.setColor(java.awt.Color.ORANGE);
					g.drawLine(x, y, x2, y);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void drawExecTimers(Graphics2D g) {
//		g.drawRect((int) (robot.getBattleFieldWidth()+2), 13, 20, (int) onScannedRobot_exectime/1000);
		g.drawString("onScannedRobot: " + onScannedRobot_exectime, (int) (robot.getBattleFieldWidth() - 180), 26);

		g.drawString("init: " + init_exectime, (int) (robot.getBattleFieldWidth() - 180), 13);

		g.drawString("CPU time", (int) (robot.getBattleFieldWidth() - 180), 2);
	}

	public void drawInputSDR(Graphics2D g, int x, int y) {
		/* draw border */
		g.setColor(java.awt.Color.GREEN);
		g.drawRect(x, y, input.size, 10);

		Iterator<Long> itr = input.iterator();
		while (itr.hasNext()) {
			long key = itr.next();
			int x_position = (int) (key + x);
			g.drawLine(x_position, y, x_position, y + 10);
		}
	}

	private void drawColumnPermanences(Graphics2D g, int x, int y) {
		if (init_done) {
			SparseVectorFloat[] columnSynapses = htm.getSp().getColumnSynapses();

			/* draw box */
			g.setColor(new java.awt.Color(179, 0, 179));
			int height = htm.getColumnDimensions();
			int width = input.size;
			g.drawRect(x, y, width, height);

//			for (int column=0, x2=x+2; column < htm.getColumnDimensions(); column++, x2++) {
//				SparseVectorFloat currentSyn = columnSynapses[column];
//				for (int synapse=0, y2=y; synapse < currentSyn.size; synapse++, y2++) {
//					if(currentSyn.isSet(synapse)) {
//						if(currentSyn.get(synapse) >= 0.5) {
//							g.drawLine(x2, y2, x2, y2);
//						}
//					}
//				}
//			}

			for (int column = 0; column < htm.getColumnDimensions(); column++) {
				SparseVectorFloat currentSyn = columnSynapses[column];
				Iterator<Long> itr = currentSyn.getKeysIterator();
				while (itr.hasNext()) {
					long key = itr.next();
					if (currentSyn.get(key) >= 0.5)
						g.drawLine((int) key, column + y, (int) key, column + y);
				}
			}
		}
	}
}
