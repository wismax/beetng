/**
 * 
 */
package com.mtgi.analytics;

import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


public class InstrumentedRunnableDecorator implements InstrumentedRunnable {

	private static final long serialVersionUID = -6052621119682386618L;

	private TestCase delegate;
	private volatile Long runtime;

	public static InstrumentedRunnable instrument(TestCase job) {
		return job instanceof InstrumentedRunnable ? (InstrumentedRunnable)job : new InstrumentedRunnableDecorator(job);
	}
	
	public InstrumentedRunnableDecorator(TestCase delegate) {
		this.delegate = delegate;
	}

	public synchronized long getLastRuntimeNanos() {
		return runtime;
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