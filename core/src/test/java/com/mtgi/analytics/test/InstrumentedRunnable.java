package com.mtgi.analytics;


/**
 * If subclasses need to implement their own CPU time measurement, they can provide
 * test jobs that implement this interface.
 */
public interface InstrumentedRunnable extends TestCase {
	/** get the total CPU time required for the last invocation of {@link #run()}. */
	public long getLastRuntimeNanos();
}