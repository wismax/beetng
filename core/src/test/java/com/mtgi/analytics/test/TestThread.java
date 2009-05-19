/**
 * 
 */
package com.mtgi.analytics;

import static org.junit.Assert.assertFalse;

import java.util.concurrent.Semaphore;


public class TestThread extends Thread {
	
	private TestCallback stats;
	private Throwable error;
	private Semaphore in, out;
	private InstrumentedRunnable job;
	private int testLoop;
	
	public TestThread(TestCallback stats, Semaphore in, Semaphore out, TestCase job, int testLoop) {
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