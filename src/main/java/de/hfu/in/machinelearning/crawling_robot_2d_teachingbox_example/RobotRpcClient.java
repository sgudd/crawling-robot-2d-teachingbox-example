package de.hfu.in.machinelearning.crawling_robot_2d_teachingbox_example;

import java.net.MalformedURLException;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

public class RobotRpcClient {
	private final static String ROBOT_ACTION_NAME = "Robot.action";
	private final static String ROBOT_RESET_NAME = "Robot.reset";
	
	private final XmlRpcClient client;
	
	public RobotRpcClient(String host, int port) throws MalformedURLException {
		client = new XmlRpcClient(String.format("http://%s:%d", host, port), true);
	}
	
	public double[] doRobotAction(int duration, int arm1Direction, int arm2Direction) {
		XmlRpcArray result = (XmlRpcArray) invokeRpc(ROBOT_ACTION_NAME,
				new Object[] { duration, arm1Direction, arm2Direction });
		if (result != null) {
			return new double[] {
				result.getDouble(0),
				result.getDouble(1),
				result.getDouble(2),
				result.getDouble(3),
				result.getDouble(4),
			};
		}
		return null;
	}
	
	public void doResetSimulation() {
		invokeRpc(ROBOT_RESET_NAME);
	}
	
	private Object invokeRpc(String method) {
		return invokeRpc(method, new Object[] {});
	}

	private Object invokeRpc(String method, Object[] arguments) {
		try {
			return client.invoke(method, arguments);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (XmlRpcFault e) {
			e.printStackTrace();
		}
		return null;
	}
}
