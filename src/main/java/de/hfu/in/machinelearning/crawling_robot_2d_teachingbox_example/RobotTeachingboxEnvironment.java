package de.hfu.in.machinelearning.crawling_robot_2d_teachingbox_example;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.hswgt.teachingbox.core.rl.datastructures.ActionSet;
import org.hswgt.teachingbox.core.rl.env.Action;
import org.hswgt.teachingbox.core.rl.env.Environment;
import org.hswgt.teachingbox.core.rl.env.State;

public class RobotTeachingboxEnvironment implements Environment {
	private static final long serialVersionUID = -8885092345248368584L;
	private static final int ACTION_DURATION = 500;
	private static final LinkedHashMap<Action, String> ACTIONS;
	public static final ActionSet ACTION_SET;

	static {
		ACTIONS = new LinkedHashMap<Action, String>();
		ACTIONS.put(new Action(new double[] { 0, 0 }), "-- --");
		ACTIONS.put(new Action(new double[] { -1, 0 }), "<- --");
		ACTIONS.put(new Action(new double[] { -1, -1 }), "<- <-");
		ACTIONS.put(new Action(new double[] { -1, 1 }), "<- ->");
		ACTIONS.put(new Action(new double[] { 1, 0 }), "-> --");
		ACTIONS.put(new Action(new double[] { 1, -1 }), "-> <-");
		ACTIONS.put(new Action(new double[] { 1, 1 }), "-> ->");
		ACTIONS.put(new Action(new double[] { 0, -1 }), "-- <-");
		ACTIONS.put(new Action(new double[] { 0, 1 }), "-- ->");

		ACTION_SET = new ActionSet();
		for (Entry<Action, String> e : ACTIONS.entrySet())
			ACTION_SET.add(e.getKey());
	}

	private final RobotRpcClient rpc;
	private double totalReward;
	private double lastDistance;
	private double averageTotalReward;
	private State state;
	private int counter;
	private final StringBuilder csvBuilder;

	public RobotTeachingboxEnvironment(RobotRpcClient rpc) {
		this.rpc = rpc;
		totalReward = lastDistance = averageTotalReward = 0.0;
		state = new State(new double[] { 0.0, 0.0, 0.0 });
		counter = 0;
		csvBuilder = new StringBuilder();
	}

	public double doAction(Action action) {
		double[] result = rpc.doRobotAction(ACTION_DURATION, (int) action.get(0), (int) action.get(1));
		if (result != null) {
			state = new State(new double[] { discreteAngle(result[1]), discreteAngle(result[2]),
					accelerationAboveThreshold(result[4]) ? 1.0 : 0.0 });
			double distance = result[0];
			double delta = distance - lastDistance;
			lastDistance = distance;
			double reward = delta;
			if (Double.compare(delta, -5.0e-4) < 0)
				reward = delta * 2;
			else if (Double.compare(delta, 10.0e-4) < 0)
				reward = 0.0;
			totalReward += reward;
			return reward;
		}
		return 0.0;
	}

	public State getState() {
		return state;
	}

	public boolean isTerminalState() {
		return false;
	}

	public void initRandom() {
		reset();
		rpc.doResetSimulation();
	}

	public void init(State state) {
		initRandom();
	}

	private int discreteAngle(double rad) {
		return (int) Math.toDegrees(rad) / 4;
	}

	private boolean accelerationAboveThreshold(double acceleration) {
		return Double.compare(acceleration, 4.0e-1) > 0;
	}

	private void reset() {
		if (counter > 0) {
			averageTotalReward = averageTotalReward + ((totalReward) - averageTotalReward) / counter;
			csvBuilder.append(String.format("%d,%.8f\n", System.currentTimeMillis(), averageTotalReward));
			System.out.format("[%04d] Previous Total Reward: %.4f [Average: %.4f]\n", counter, totalReward,
					averageTotalReward);
			state = new State(new double[] { 0.0, 0.0, 0.0 });
			lastDistance = totalReward = 0.0;
		}
		++counter;
	}

	public void writeCsv() throws IOException {
		try (FileWriter fw = new FileWriter(
				String.format("%s.csv", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())))) {
			fw.write(csvBuilder.toString());
		}
		csvBuilder.setLength(0);
	}
}
