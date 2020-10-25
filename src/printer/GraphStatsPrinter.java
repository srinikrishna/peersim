package printer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.reports.GraphObserver;

/**
 * This will collects the information about path length and clustering and prints to the file every cycle.
 */

public abstract class GraphStatsPrinter extends GraphObserver {

	private static final String OUTPUT_LOCATION = "plot/";
	
	private static final String PAR_CACHESIZE = "cacheSize";
	private static final String PAR_STEP = "step";
	private static final String PAR_TYPE = "type";

	protected final int cacheSize;
	protected final int step;
	protected final String type;
	private PrintWriter writer;

	protected GraphStatsPrinter(String name) {
		super(name);

		String prefix = name + ".";
		cacheSize = Configuration.getInt(prefix + PAR_CACHESIZE);
		type = Configuration.getString(prefix + PAR_TYPE);
		step = Configuration.getInt(name + "." + PAR_STEP, 0);
	}
	
	protected void init(String filename) throws FileNotFoundException {
		try {
			writer = new PrintWriter(OUTPUT_LOCATION + filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Not possible
		}
	}

	@Override
	public final boolean execute() {
		boolean result = execute(writer);

		// Check if this is the last cycle
		if (lastCycle()) {
			writer.close();
		}

		return result;
	}
	
	protected boolean lastCycle() {
		return CommonState.getTime() + step >= CommonState.getEndTime();
	}

	protected abstract boolean execute(PrintWriter writer);
}
