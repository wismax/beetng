package com.mtgi.analytics.aop;

import static org.junit.Assert.*;

import java.util.concurrent.Semaphore;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mtgi.analytics.aop.BehaviorAdviceTest.ServiceA;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public class PerformanceTest {

	private static final int TEST_ITERATIONS = 10;
	private static final int TEST_LOOP = 50;
	private static final int TEST_THREADS = 10;
	private static final int METHOD_DELAY = 10;
	
	@Test
	public void testPerformance() throws Throwable {

		ClassPathXmlApplicationContext basisContext = 
			new ClassPathXmlApplicationContext("com/mtgi/analytics/aop/PerformanceTest-basis.xml");
		ClassPathXmlApplicationContext testContext = 
			new ClassPathXmlApplicationContext(new String[]{ 
					"com/mtgi/analytics/aop/PerformanceTest-basis.xml",
					"com/mtgi/analytics/aop/PerformanceTest-tracking.xml"
			});
		
		TestStats basis = new TestStats(),
				  test  = new TestStats();
		
		//run several iterations of the test, alternating between instrumented and not
		//to absorb the affects of garbage collection.
		for (int i = 0; i < TEST_ITERATIONS; ++i) {
			System.out.println("iteration " + i);
			
			runTest(basis, basisContext);
			runTest(test, testContext);
		}
		
		//compute the overhead as the difference between instrumented and uninstrumented
		//runs.  we want the per-event overhead to be less than .5 ms.
		double calls = TEST_THREADS * TEST_LOOP * TEST_ITERATIONS * 2;
		double delta = test.getAverageMillis() - basis.getAverageMillis();
		double overhead = delta / calls;

		assertTrue("Average overhead per method cannot exceed .05 ms [ " + overhead + " ]",
					 overhead < 0.05);
		
		//deltaWorst, my favorite sausage.  mmmmmm, dellltttaaaWwwwooorrsstt.
		double deltaWorst = test.getWorstMillis() - basis.getWorstMillis();
		overhead = deltaWorst / calls;
		
		assertTrue("Worst case per method overhead cannot exceed .5ms [ " + overhead + " ]",
				  overhead < .5);
	}
	
	private void runTest(TestStats stats, ConfigurableApplicationContext context) throws Throwable {
		
		//set up the test iteration.
		ServiceA service = (ServiceA)context.getBean("serviceA", ServiceA.class);
		Semaphore in = new Semaphore(0),
				  out = new Semaphore(0);
		TestThread[] threads = new TestThread[TEST_THREADS];
		for (int t = 0; t < TEST_THREADS; ++t) {
			threads[t] = new TestThread(in, out, service);
			threads[t].start();
		}
		
		//start the timer
		stats.start();
		try {
			//turn 'em loose.
			in.release(TEST_THREADS);
			//wait for finish.
			out.acquire(TEST_THREADS);

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
		private ServiceA service;
		
		public TestThread(Semaphore in, Semaphore out, ServiceA service) {
			this.in = in;
			this.out = out;
			this.service = service;
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
				for (int i = 0; i < TEST_LOOP; ++i) {
					Thread.sleep(METHOD_DELAY);
					service.getTracked("sleepy");
				}
			} catch (Throwable e) {
				error = e;
			} finally {
				out.release(1);
			}
		}
	}
}
