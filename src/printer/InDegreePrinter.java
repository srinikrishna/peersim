package printer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class InDegreePrinter extends GraphStatsPrinter {

	private final static String PAR_PID = "protocol";

	private final int pid;

	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine to collect the in-degree information and to print in the file.
	 * 
	 * @param name
	 *            the configuration prefix for this class
	 * @throws FileNotFoundException
	 */
	public InDegreePrinter(String name) throws FileNotFoundException {
		super(name);

		pid = Configuration.getPid(name + "." + PAR_PID);
		
		init("output-indegree-" + type + "-c" + cacheSize + ".txt");
	}

	@Override
	protected boolean execute(PrintWriter writer) {
		if (!lastCycle()) {
			return false;
		}

		// Map of all nodes and their in-degree count
		Map<Long, Integer> degreeCount = new HashMap<Long, Integer>();

		for (int i = 0; i < Network.size(); i++) {
			// Get all the nodes in the network
			Node n = Network.get(i);

			if (n.isUp()) {
				// Get the linkable protocol for all the running nodes
				Linkable linkable = (Linkable) n.getProtocol(pid);
				// Go through the neighbor list and update the degrees in the
				// map
				for (int j = 0; j < linkable.degree(); j++) {
					Long nodeId = linkable.getNeighbor(j).getID();
					Integer count = degreeCount.get(nodeId);
					if (count == null) {
						degreeCount.put(nodeId, 1);
					} else {
						degreeCount.put(nodeId, count + 1);
					}
				}
			}
		}

		// Map of the in-degree distribution. The key is the in-degree and the
		// entry is the number of nodes having this distribution
		Map<Integer, Integer> dist = new HashMap<Integer, Integer>();

		// Fill the map with the in-degree distribution of each node
		for (int i = 0; i < Network.size(); i++) {
			Long nodeId = Network.get(i).getID();
			Integer degree = degreeCount.get(nodeId);
			int value = 1;

			if (dist.containsKey(degree)) {
				value = dist.get(degree) + 1;
			}
			dist.put(degree, value);
		}

		// Sort the distribution and print the result
		SortedSet<Integer> sortedKeys = new TreeSet<Integer>(dist.keySet());
		for (int i = 0; i <= sortedKeys.last(); i++) {
			Integer value;

			if (sortedKeys.contains(i)) {
				value = dist.get(i);
			} else {
				value = 0;
			}

			writer.println(i + "\t" + value);
		}

		return false;
	}
}
