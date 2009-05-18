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

import com.mtgi.analytics.jmx.StatisticsMBean;

/**
 * Performs some instrumented tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public abstract class AbstractPerformanceTestCase {

	private static final int TEST_ITERATIONS = 20;

	private int testLoop;
	private int testThreads;
	private long averageOverhead;
	private long maxOverhead;
	private long expectedBasis;
	
	/**
	 * @param testThreads the number of normal priority threads to concurrently execute test runnables.
	 * @param testLoop the number of times each test thread invokes the test runnable before exiting
	 * @param expectedBasis <span>the benchmark CPU time against which <code>averageOverhead</code> was established.
	 * 	                    <code>averageOverhead</code> and <code>maxOverhead</code> will be scaled by the ratio of this time to
	 * 						actual measured time during control runs, to account for the processing power available on the system that
	 * 						is actually running the test.</span>
	 * @param averageOverhead <span>the average overhead in CPU nanoseconds expected for each measurement taken.  this figure is scaled
	 * 						  according to <code>expectedBasis</code> before being compared to test run times.</span>
	 * @param maxOverhead <span>the maximum overhead in CPU nanoseconds allowed for each measurement taken.  this figure is scaled
	 * 					  according to <code>expectedBasis</code> before being compared to test run times.</span>
	 */
	protected AbstractPerformanceTestCase(int testThreads, int testLoop, long expectedBasis, long averageOverhead, long maxOverhead) {
		this.testThreads = testThreads;
		this.testLoop = testLoop;
		this.averageOverhead = averageOverhead;
		this.maxOverhead = maxOverhead;
		this.expectedBasis = expectedBasis;
	}
	
	protected void testPerformance(Runnable basisJob, Runnable testJob) throws Throwable {

		ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
		assertTrue("performance tests can only be run on a platform that supports per-thread resource measurement",
					mxb.isCurrentThreadCpuTimeSupported());
		
		StatisticsMBean basis = new StatisticsMBean(),
				  test  = new StatisticsMBean();

		runTest(new StatisticsMBean(), testJob);
		runTest(new StatisticsMBean(), basisJob);

		System.gc();
		System.gc();
		Thread.sleep(100);

		//run several iterations of the test, alternating between instrumented and not
		//to absorb the affects of garbage collection.
		for (int i = 0; i < TEST_ITERATIONS; ++i) {
			System.out.println("iteration " + i);
			//switch the order of test / control runs during iteration to reduce any
			//bias that order might cause
			if (i % 2 == 0) {
				runTest(basis, basisJob);
				runTest(test, testJob);
			} else {
				runTest(test, testJob);
				runTest(basis, basisJob);
			}
		}
		
		assertEquals("basis and test have same sample size", basis.getCount(), test.getCount());
		
		double basisNanos = basis.getAverageTime();
		double cpuCoefficient = basisNanos / expectedBasis;
		
		double expectedAverage = cpuCoefficient * averageOverhead;
		double expectedMax = cpuCoefficient * maxOverhead;
		
		System.out.println("control:\n" + basis);
		System.out.println("test:\n" + test);
		System.out.println("CPU Coefficient: " + cpuCoefficient);
		
		//compute the overhead as the difference between instrumented and uninstrumented
		//runs.  we want the per-event overhead to be less than .5 ms.
		double delta = test.getAverageTime() - basisNanos;
		//deltaWorst, my favorite sausage.  mmmmmm, dellltttaaaWwwwooorrsstt.
		double deltaWorst = test.getMaxTime() - basis.getMaxTime();

		System.out.println("Average overhead: " + delta);
		System.out.println("Worst case overhead: " + deltaWorst);
		
		assertTrue("Average overhead per method cannot exceed " + expectedAverage + "ns [ " + delta + " ]",
					 delta <= expectedAverage);
		
		assertTrue("Worst case per method overhead cannot exceed " + expectedMax + "ns [ " + deltaWorst + " ]",
					deltaWorst <= expectedMax);
	}
	
	private void runTest(StatisticsMBean stats, Runnable job) throws Throwable {
		
		//try to prevent accumulating GC from prior test runs from polluting our
		//numbers.  eventually we need to move the basis and test executions into
		//their own process space.
		System.gc();
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
	
	public static class TestThread extends Thread {
		
		private StatisticsMBean stats;
		private Throwable error;
		private Semaphore in, out;
		private InstrumentedRunnable job;
		private int testLoop;
		
		public TestThread(StatisticsMBean stats, Semaphore in, Semaphore out, Runnable job, int testLoop) {
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
						stats.add(job.getLastRuntimeNanos());
					}
					if ((i + 1) % 10 == 0) {
						System.gc();
						System.gc();
						Thread.yield();
					}
				}
			} catch (Throwable e) {
				error = e;
			} finally {
				out.release(1);
			}
		}
	}
}
