/**
 * 
 */
package com.mtgi.analytics.test;

import java.io.Serializable;

/**
 * Adds to {@link Runnable} {@link #setUp()} and {@link #tearDown()} test
 * lifecycle methods.  This class is serializable so that a test job
 * can be sent between separate processes.
 * 
 * @see TestProcess
 * @see AbstractPerformanceTestCase
 */
public interface TestCase extends Serializable, Runnable {
	/**
	 * Initialize internal transient state for the test.  This method is
	 * called only once during the lifetime of this TestCase instance, before
	 * any invocations of the {@link #run} method.  This should be used
	 * instead of a constructor to set up any complicated internal state for
	 * the test, to keep the cost of serialization down.
	 */
	public void setUp() throws Throwable;
	/**
	 * Clean up any transient test state previously established by {@link #setUp}.
	 * This method is called at most once during the lifetime of a single TestCase
	 * instance.
	 */
	public void tearDown() throws Throwable;
}