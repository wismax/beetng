package com.mtgi.analytics.aop;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mtgi.analytics.AbstractPerformanceTestCase;
import com.mtgi.analytics.aop.BehaviorAdviceTest.ServiceA;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public class PerformanceTest extends AbstractPerformanceTestCase {

	private static final int METHOD_DELAY = 10;
	
	public PerformanceTest() {
		super(2); //each test job generates two BT events.
	}
	
	@Test
	public void testPerformance() throws Throwable {
		ClassPathXmlApplicationContext basisContext = 
			new ClassPathXmlApplicationContext("com/mtgi/analytics/aop/PerformanceTest-basis.xml");
		ClassPathXmlApplicationContext testContext = 
			new ClassPathXmlApplicationContext(new String[]{ 
					"com/mtgi/analytics/aop/PerformanceTest-basis.xml",
					"com/mtgi/analytics/aop/PerformanceTest-tracking.xml"
			});
		
		TestJob basisJob = new TestJob((ServiceA)basisContext.getBean("serviceA"));
		TestJob testJob = new TestJob((ServiceA)testContext.getBean("serviceA"));
		testPerformance(basisJob, testJob);
	}
	
	public static class TestJob implements Runnable {
		
		private ServiceA service;
		
		public TestJob(ServiceA service) {
			this.service = service;
		}

		public void run() {
			try {
				Thread.sleep(METHOD_DELAY);
				//method call results in two events logged.
				service.getTracked("sleepy");
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
