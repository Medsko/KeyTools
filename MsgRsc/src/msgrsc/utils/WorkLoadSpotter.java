package msgrsc.utils;

/**
 * This helper can be used to keep track of performance and memory usage of an
 * application.
 */
public class WorkLoadSpotter {

	private long memoryUsageStart;
	
	private boolean started;
	
	public void markMemory() {
		memoryUsageStart = currentMemoryUsage();
		started = true;
	}
	
	public long memorySinceMark() {
		if (!started)
			throw new IllegalStateException("Call markMemory() before calling"
					+ " memorySinceMark()!!");
		// Reset variable indicating measuring has not started yet.
		started = false;
		
		return currentMemoryUsage() - memoryUsageStart;
	}
	
	public long currentMemoryUsage() {
		return Runtime.getRuntime().totalMemory() 
				- Runtime.getRuntime().freeMemory();
	}
}
