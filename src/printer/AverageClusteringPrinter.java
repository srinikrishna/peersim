/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package printer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.graph.GraphAlgorithms;
import peersim.util.IncrementalStats;

public class AverageClusteringPrinter extends GraphStatsPrinter {

	/**
	 * The number of nodes to use to sample average clustering. If zero is
	 * given, then no statistics will be printed about clustering. If a negative
	 * value is given then the value is the full size of the graph. Defaults to
	 * zero. Value for nc comes from the RandomExample.txt and ShuffleExample.txt scripts
	 */
	private static final String PAR_NC = "nc";

	private final int nc;

	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine.
	 * 
	 * @param name
	 *            the configuration prefix for this class
	 * @throws FileNotFoundException
	 */
	public AverageClusteringPrinter(String name) throws FileNotFoundException {
		super(name);

		nc = Configuration.getInt(name + "." + PAR_NC, 0);
		
		init("output-clustering-" + type + "-c" + cacheSize + ".txt");
	}

	/**
	 * This algorithm was taken from Peersim's {@code GraphStats} class.
	 * 
	 * @return
	 */
	private double getAverageClusteringCoefficient() {
		IncrementalStats stats = new IncrementalStats();

		final int n = nc < 0 ? g.size() : nc;
		for (int i = 0; i < n && i < g.size(); ++i) {
			stats.add(GraphAlgorithms.clustering(g, i));
		}

		return stats.getAverage();
	}

	@Override
	protected boolean execute(PrintWriter writer) {
		int cycle = CommonState.getIntTime() / step;

		updateGraph();

		if (nc != 0) {
			double avg = getAverageClusteringCoefficient();
			writer.println(cycle + "\t" + avg);
		}

		return false;
	}
}
