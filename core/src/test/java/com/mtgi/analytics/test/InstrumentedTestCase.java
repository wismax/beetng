package com.mtgi.analytics.test;


/**
 * If subclasses need to implement their own CPU time measurement, they can provide
 * test jobs that implement this interface.
 */
public interface InstrumentedTestCase extends TestCase {
	/** get the total CPU time required for the last invocation of {@link #run()} on the calling thread. */
	public long getLastRuntimeNanos();
}