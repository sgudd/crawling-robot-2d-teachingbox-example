package de.hfu.in.machinelearning.crawling_robot_2d_teachingbox_example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Scanner;

import org.hswgt.teachingbox.core.rl.agent.Agent;
import org.hswgt.teachingbox.core.rl.experiment.Experiment;
import org.hswgt.teachingbox.core.rl.learner.TabularQLearner;
import org.hswgt.teachingbox.core.rl.policy.EpsilonGreedyPolicy;
import org.hswgt.teachingbox.core.rl.policy.Policy;
import org.hswgt.teachingbox.core.rl.tabular.HashQFunction;

/**
 * A simple Teachingbox experiment that uses a Tabular-Q-Learner. First 100
 * episodes with 1000 steps each are executed. Then the program is interrupted
 * until the user presses enter, the user can then switch the simulation from
 * headless to visualized mode and continue to see the results.
 * 
 * Additionally the results of the first learning phase (average rewards) are
 * stored in a CSV-file.
 */
public class RobotExperiment {
	public static void main(String[] args) throws MalformedURLException {
		String host = "localhost";
		int port = 8080;
		int learningEpisodes = 100;
		int examinationEpisodes = 5;
		int stepsPerEpisode = 1000;

		for (int i = 0; i < args.length; ++i) {
			if ("--host".equals(args[i]))
				host = args[i + 1];
			else if ("--port".equals(args[i]))
				port = Integer.parseInt(args[i + 1]);
		}

		Locale.setDefault(Locale.ENGLISH);
		RobotRpcClient rpc = new RobotRpcClient(host, port);
		RobotTeachingboxEnvironment env = new RobotTeachingboxEnvironment(rpc);

		HashQFunction Q = new HashQFunction(0, RobotTeachingboxEnvironment.ACTION_SET);
		Policy pi = new EpsilonGreedyPolicy(Q, RobotTeachingboxEnvironment.ACTION_SET, 0.1);
		Agent agent = new Agent(pi);

		TabularQLearner learner = new TabularQLearner(Q);
		learner.setAlpha(0.5);
		learner.setGamma(0.95);
		agent.addObserver(learner);

		System.out.format("About to perform %d episodes a %d steps.\n", learningEpisodes, stepsPerEpisode);
		System.out.println("Press ENTER to start...");
		Scanner scan = new Scanner(System.in);
		scan.nextLine();

		Experiment experiment = new Experiment(agent, env, learningEpisodes, stepsPerEpisode);
		experiment.run();

		try {
			env.writeCsv();
			System.out.println("CSV written.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Simulation finished.");
		System.out.format("About to perform %d more episodes.\n", examinationEpisodes);
		System.out.println("Press ENTER to continue...");
		scan.nextLine();
		scan.close();

		experiment = new Experiment(agent, env, examinationEpisodes, stepsPerEpisode);
		experiment.run();

		System.out.println("Experiment finished.");
	}
}
