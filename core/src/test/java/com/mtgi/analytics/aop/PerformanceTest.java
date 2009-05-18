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
 
package com.mtgi.analytics.aop;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mtgi.analytics.AbstractPerformanceTestCase;
import com.mtgi.analytics.aop.BehaviorAdviceTest.ServiceA;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public class PerformanceTest extends AbstractPerformanceTestCase {

	//this is CPU clock overhead, so we're being fairly strict here.  while ideally we
	//anticipate fewer than 10 monitored method calls per user request, complicated, abusive,
	//or misconfigured client applications might push this boundary out by a factor of 10.
	private static final long AVERAGE_OVERHEAD_NS = 100000;
	private static final long WORST_OVERHEAD_NS = 10000;

	private static final long TIME_BASIS = 100000;

	private ClassPathXmlApplicationContext basisContext;
	private ClassPathXmlApplicationContext testContext;
	
	public PerformanceTest() {
		super(5, 50, TIME_BASIS, AVERAGE_OVERHEAD_NS, WORST_OVERHEAD_NS); //each test job generates two BT events.
	}
	
	@Before
	public void initContext() {
		basisContext = new ClassPathXmlApplicationContext("com/mtgi/analytics/aop/PerformanceTest-basis.xml");
		testContext = new ClassPathXmlApplicationContext(new String[]{ 
							"com/mtgi/analytics/aop/PerformanceTest-basis.xml",
							"com/mtgi/analytics/aop/PerformanceTest-tracking.xml"
					});
	}
	
	@After
	public void destroyContext() {
		basisContext.destroy();
		testContext.destroy();
	}
	
	@Test
	public void testPerformance() throws Throwable {
		TestJob basisJob = new TestJob((ServiceA)basisContext.getBean("serviceA"));
		TestJob testJob = new TestJob((ServiceA)testContext.getBean("serviceA"));
		testPerformance(basisJob, testJob);
	}
	
	public static class TestJob implements Runnable {
		
		private ServiceA service;
		
		public TestJob(ServiceA service) {
			this.service = service;
		}

		public int fib(int n) {
			if (n == 0 || n == 1)
				return 1;
			return fib(n - 1) + fib(n - 2);
		}
		
		public void run() {
			fib(20);
			service.getTracked("sleepy");
		}
	}
}
