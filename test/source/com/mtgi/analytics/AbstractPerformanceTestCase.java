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
	
	protected AbstractPerformanceTestCase(int eventsPerJob) {
		this(eventsPerJob, 10, 50);
	}

	protected AbstractPerformanceTestCase(int eventsPerJob, int testThreads, int testLoop) {
		this.testThreads = testThreads;
		this.eventsPerJob = eventsPerJob;
		this.testLoop = testLoop;
	}
	
	protected void testPerformance(Runnable basisJob, Runnable testJob) throws Throwable {

		TestStats basis = new TestStats(),
				  test  = new TestStats();
		
		//run several iterations of the test, alternating between instrumented and not
		//to absorb the affects of garbage collection.
		for (int i = 0; i < TEST_ITERATIONS; ++i) {
			System.out.println("iteration " + i);
			
			runTest(basis, basisJob);
			runTest(test, testJob);
		}
		
		//compute the overhead as the difference between instrumented and uninstrumented
		//runs.  we want the per-event overhead to be less than .5 ms.
		double calls = testLoop * TEST_ITERATIONS * eventsPerJob;
		double delta = test.getAverageMillis() - basis.getAverageMillis();
		double overhead = delta / calls;

		assertTrue("Average overhead per method cannot exceed .1 ms [ " + overhead + " ]",
					 overhead < 0.1);
		System.out.println("Average overhead: " + overhead);
		
		//deltaWorst, my favorite sausage.  mmmmmm, dellltttaaaWwwwooorrsstt.
		double deltaWorst = test.getWorstMillis() - basis.getWorstMillis();
		overhead = deltaWorst / calls;
		
		assertTrue("Worst case per method overhead cannot exceed .5ms [ " + overhead + " ]",
				  overhead < .5);
		System.out.println("Worst case overhead: " + overhead);
	}
	
	private void runTest(TestStats stats, Runnable job) throws Throwable {
		
		//set up the test iteration.
		Semaphore in = new Semaphore(0),
				  out = new Semaphore(0);
		TestThread[] threads = new TestThread[testThreads];
		for (int t = 0; t < testThreads; ++t) {
			threads[t] = new TestThread(in, out, job, testLoop);
			threads[t].start();
		}
		
		//start the timer
		stats.start();
		try {
			//turn 'em loose.
			in.release(testThreads);
			//wait for finish.
			out.acquire(testThreads);

			//verify that all of the test threads are actually finished
			for (TestThread t : threads) {
				t.join(100);
				t.assertDone();
			}
		} finally {
			//update stats.
			stats.stop();
		}
	}
	
	public static class TestStats {
		private double average = 0;
		private long worstCase = -1;

		private int count;
		private long start;
		
		public double getAverageMillis() {
			return average;
		}
		
		public long getWorstMillis() {
			return worstCase;
		}
		
		public void start() {
			this.start = System.currentTimeMillis();
		}
		
		public void stop() {
			long delta = System.currentTimeMillis() - start;
			worstCase = Math.max(delta, worstCase);
			average = ((average * count) + delta) / (double)++count;
		}
	}
	
	public static class TestThread extends Thread {
		
		private Throwable error;
		private Semaphore in, out;
		private Runnable job;
		private int testLoop;
		
		public TestThread(Semaphore in, Semaphore out, Runnable job, int testLoop) {
			this.in = in;
			this.out = out;
			this.job = job;
			this.testLoop = testLoop;
		}

		public void assertDone() throws Throwable{
			if (error != null)
				throw error;
			assertFalse("thread " + getName() + " complete", isAlive());
		}
		
		@Override
		public void run() {
			try {
				in.acquire(1);
				for (int i = 0; i < testLoop; ++i)
					job.run();
			} catch (Throwable e) {
				error = e;
			} finally {
				out.release(1);
			}
		}
	}
}
