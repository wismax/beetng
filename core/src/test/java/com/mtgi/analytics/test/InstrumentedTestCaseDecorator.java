/**
 * 
 */
package com.mtgi.analytics.test;

import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * A basic implementation of {@link InstrumentedTestCase} that uses
 * {@link ThreadMXBean} to measure the reasource consumption used during
 * {@link #run()} calls.
 */
public class InstrumentedTestCaseDecorator implements InstrumentedTestCase {

	private static final long serialVersionUID = -6052621119682386618L;

	private TestCase delegate;
	private volatile Long runtime;

	/**
	 * convert the given {@link TestCase} to an {@link InstrumentedTestCase}
	 * if necessary.
	 */
	public static InstrumentedTestCase instrument(TestCase job) {
		return job instanceof InstrumentedTestCase ? (InstrumentedTestCase)job : new InstrumentedTestCaseDecorator(job);
	}
	
	public InstrumentedTestCaseDecorator(TestCase delegate) {
		this.delegate = delegate;
	}

	public synchronized long getLastRuntimeNanos() {
		return runtime;
	}

	public void setUp() throws Throwable {
		delegate.setUp();
	}

	public void tearDown() throws Throwable {
		delegate.tearDown();
	}

	public void run() {
		synchronized (this) {
			runtime = null;
		}
		ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
		assertTrue(mxb.isCurrentThreadCpuTimeSupported());
		long start = mxb.getCurrentThreadCpuTime();
		try {
			delegate.run();
		} finally {
			synchronized (this) {
				runtime = mxb.getCurrentThreadCpuTime() - start;
			}
		}
	}
	
}