/* 
 * Copyright 2008-2009 the original author or authors.
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
 
package com.mtgi.analytics;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Semaphore;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public abstract class AbstractPerformanceTestCase {

	private static final int TEST_ITERATIONS = 10;

	private int testLoop;
	private int testThreads;
	private int eventsPerJob;
	private long averageOverhead;
	private long maxOverhead;
	
	protected AbstractPerformanceTestCase(int eventsPerJob, int testThreads, int testLoop, long averageOverhead, long maxOverhead) {
		this.testThreads = testThreads;
		this.eventsPerJob = eventsPerJob;
		this.testLoop = testLoop;
		this.averageOverhead = averageOverhead;
		this.maxOverhead = maxOverhead;
	}
	
	protected void testPerformance(Runnable basisJob, Runnable testJob) throws Throwable {

		ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
		assertTrue("performance tests can only be run on a platform that supports per-thread resource measurement",
					mxb.isCurrentThreadCpuTimeSupported());
		
		TestStats basis = new TestStats(),
				  test  = new TestStats();

		runTest(new TestStats(), testJob);
		runTest(new TestStats(), basisJob);
		
		//run several iterations of the test, alternating between instrumented and not
		//to absorb the affects of garbage collection.
		for (int i = 0; i < TEST_ITERATIONS; ++i) {
			System.out.println("iteration " + i);
			runTest(basis, basisJob);
			runTest(test, testJob);
		}
		
		//compute the overhead as the difference between instrumented and uninstrumented
		//runs.  we want the per-event overhead to be less than .5 ms.
		double delta = test.getAverageNanos() - basis.getAverageNanos();
		double overhead = delta / eventsPerJob;
		//deltaWorst, my favorite sausage.  mmmmmm, dellltttaaaWwwwooorrsstt.
		double deltaWorst = test.getWorstNanos() - basis.getWorstNanos();
		double worstOverhead = deltaWorst / eventsPerJob;

		System.out.println("Average overhead: " + overhead);
		System.out.println("Worst case overhead: " + worstOverhead);
		
		assertTrue("Average overhead per method cannot exceed " + averageOverhead + "ns [ " + overhead + " ]",
					 overhead <= averageOverhead);
		
		assertTrue("Worst case per method overhead cannot exceed " + maxOverhead + "ns [ " + worstOverhead + " ]",
					worstOverhead <= maxOverhead);
	}
	
	private void runTest(TestStats stats, Runnable job) throws Throwable {
		
		//try to prevent accumulating GC from prior test runs from polluting our
		//numbers.  eventually we need to move the basis and test executions into
		//their own process space.
		System.gc();
		
		//set up the test iteration.
		Semaphore in = new Semaphore(0),
				  out = new Semaphore(0);
		TestThread[] threads = new TestThread[testThreads];
		for (int t = 0; t < testThreads; ++t) {
			threads[t] = new TestThread(stats, in, out, job, testLoop);
			threads[t].start();
		}
		
		//turn 'em loose.
		in.release(testThreads);
		//wait for finish.
		out.acquire(testThreads);

		//verify that all of the test threads are actually finished
		for (TestThread t : threads) {
			t.join(600000);
			t.assertDone();
		}
		
		System.gc();
	}
	
	public static class TestStats {
		private double average = 0;
		private long worstCase = -1;

		private int count;
		
		public double getAverageNanos() {
			return average;
		}
		
		public long getWorstNanos() {
			return worstCase;
		}
		
		public synchronized void update(long delta) {
			worstCase = Math.max(delta, worstCase);
			average = average + (delta - average) / (double)++count;
		}
	}
	
	/**
	 * If subclasses need to implement their own CPU time measurement, they can provide
	 * test jobs that implement this interface.
	 */
	public static interface InstrumentedRunnable extends Runnable {
		/** get the total CPU time required for the last invocation of {@link #run()}. */
		public long getLastRuntimeNanos();
	}
	
	public static class InstrumentedRunnableDecorator implements InstrumentedRunnable {

		private Runnable delegate;
		private volatile Long runtime;

		public static InstrumentedRunnable instrument(Runnable job) {
			return job instanceof InstrumentedRunnable ? (InstrumentedRunnable)job : new InstrumentedRunnableDecorator(job);
		}
		
		public InstrumentedRunnableDecorator(Runnable delegate) {
			this.delegate = delegate;
		}

		public long getLastRuntimeNanos() {
			return runtime;
		}

		public void run() {
			runtime = null;
			ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
			assertTrue(mxb.isCurrentThreadCpuTimeSupported());
			long start = mxb.getCurrentThreadCpuTime();
			try {
				delegate.run();
			} finally {
				runtime = mxb.getCurrentThreadCpuTime() - start;
			}
		}
		
	}
	
	public static class TestThread extends Thread {
		
		private TestStats stats;
		private Throwable error;
		private Semaphore in, out;
		private InstrumentedRunnable job;
		private int testLoop;
		
		public TestThread(TestStats stats, Semaphore in, Semaphore out, Runnable job, int testLoop) {
			this.stats = stats;
			this.in = in;
			this.out = out;
			this.job = InstrumentedRunnableDecorator.instrument(job);
			this.testLoop = testLoop;
		}

		public void assertDone() throws Throwable {
			if (error != null)
				throw error;
			assertFalse("thread " + getName() + " complete", isAlive());
		}
		
		@Override
		public void run() {
			try {
				in.acquire(1);
				for (int i = 0; i < testLoop; ++i) {
					try {
						job.run();
					} finally {
						stats.update(job.getLastRuntimeNanos());
					}
					if ((i + 1) % 10 == 0)
						System.gc();
				}
			} catch (Throwable e) {
				error = e;
			} finally {
				out.release(1);
			}
		}
	}
}
