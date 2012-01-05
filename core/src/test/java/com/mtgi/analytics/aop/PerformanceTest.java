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

import org.junit.Ignore;
import org.junit.Test;

import com.mtgi.analytics.aop.BehaviorAdviceTest.ServiceA;
import com.mtgi.analytics.test.AbstractPerformanceTestCase;
import com.mtgi.analytics.test.AbstractSpringTestCase;

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

	private static final String[] BASIS_CONFIG = { 
		"com/mtgi/analytics/aop/PerformanceTest-basis.xml" 
	};
	private static final String[] TEST_CONFIG = { 
		"com/mtgi/analytics/aop/PerformanceTest-basis.xml",
		"com/mtgi/analytics/aop/PerformanceTest-tracking.xml"
	};
	
	public PerformanceTest() {
		super(5, 100, TIME_BASIS, AVERAGE_OVERHEAD_NS, WORST_OVERHEAD_NS); //each test job generates two BT events.
	}
	
	@Test
	@Ignore
	public void testPerformance() throws Throwable {
		TestJob basisJob = new TestJob(BASIS_CONFIG);
		TestJob testJob = new TestJob(TEST_CONFIG);
		testPerformance(basisJob, testJob);
	}
	
	public static class TestJob extends AbstractSpringTestCase<ServiceA> {

		private static final long serialVersionUID = 6599513817152651866L;

		public TestJob(String[] configFiles) {
			super("serviceA", ServiceA.class, configFiles);
		}

		public void run() {
			//we need to do something that actually registers some CPU time, in order
			//to effectively measure overhead.
			fib(20);
			bean.getTracked("sleepy");
		}
		
		public int fib(int n) {
			if (n == 0 || n == 1)
				return 1;
			return fib(n - 1) + fib(n - 2);
		}
		
	}
}
