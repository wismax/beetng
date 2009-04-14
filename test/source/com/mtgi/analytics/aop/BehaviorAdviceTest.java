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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Required;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.JdbcEventTestCase;
import com.mtgi.analytics.MockSessionContext;

@SpringApplicationContext({"com/mtgi/analytics/aop/BehaviorAdviceTest-applicationContext.xml"})
public class BehaviorAdviceTest extends JdbcEventTestCase {

	/** the bt:advice tag should automatically add a BehaviorTrackingManager to the spring context. */
	@SpringBeanByType
	private BehaviorTrackingManagerImpl manager;
	
	/** the spring bean under test; calling methods here should result in behavior tracking events getting logged. */
	@SpringBeanByType
	private ServiceA service;
	
	@SpringBeanByType
	private MockSessionContext sessionContext;
	
	@Before
	public void setUpTestSession() {
		sessionContext.setContextUserId("testUser");
		sessionContext.setContextSessionId("ABCDEF123");
	}

	/** test basic event tracking configuration, mixing instrumented and uninstrumented methods */
	@Test
	public void testTracking() throws Exception {
		assertEquals("this call should log 2 events", "serviceA:0", service.getTracked("hello"));
		assertEquals("this call should log only 1 event", "serviceA:1", service.getUntracked());
		
		//make sure all event data has been pushed to the test database
		manager.flush();
		assertEventDataMatches("BehaviorAdviceTest.testTracking-result.xml");
	}

	/** test support for unusual parameter and return types */
	@Test
	public void testComplexParameters() throws Exception {
		//test logging of actual type of argument, when it differs from the method signature.
		ServiceB arg = new SubServiceB();
		assertSame("argument value is returned unmodified", arg, service.getWeirdParametersTracked(this, arg, Param.Value_1, "hello", "world"));
		assertNull("null values allowed", service.getWeirdParametersTracked(null, null, null));
		
		arg = new ServiceB();
		assertSame("argument value is returned unmodified", arg, service.getWeirdParametersTracked(this, arg, Param.Value_2, null, "value"));
		
		//flush and verify the log data.
		manager.flush();
		assertEventDataMatches("BehaviorAdviceTest.testComplexParameters-result.xml");
	}
	
	/** test that event tracking responds graceful to application exceptions */
	@Test
	public void testExceptionHandling() throws Exception {
		try {
			service.throwExceptionTracked(1972);
			fail("application exception should have been thrown");
		} catch (RuntimeException expected) {
			assertSame("original application exception is returned", ServiceA.ERROR, expected);
		}
		
		manager.flush();
		assertEventDataMatches("BehaviorAdviceTest.testExceptionHandling-result.xml");
	}
	
	/** test support for methods without a return value and without parameters */
	@Test
	public void testVoidReturn() throws Exception {
		service.getVoidReturnTracked();
		manager.flush();
		assertEventDataMatches("BehaviorAdviceTest.testVoidReturn-result.xml");
	}

	/** test support for primitive types and varargs arrays */
	@Test
	public void testPrimitiveTypes() throws Exception {
		assertEquals(6, service.getPrimitiveTracked(1, 2, 3, -4, 1, 3));
		//now test a really long array to test length limiting.
		int[] data = new int[102];
		for (int i = 0; i < data.length; ++i)
			data[i] = i;
		
		assertEquals(5151, service.getPrimitiveTracked(data));
		manager.flush();
		assertEventDataMatches("BehaviorAdviceTest.testPrimitiveTypes-result.xml");
	}
	
	/**
	 * Spring bean class that depends on another spring bean (ServiceB).
	 * Some method calls in this class are tracked, some aren't.
	 */
	public static class ServiceA {
		
		public static final RuntimeException ERROR = new RuntimeException("i'm a bad application");
		
		private ServiceB serviceB;

		@Required
		public void setServiceB(ServiceB inst) {
			this.serviceB = inst;
		}
		
		/**
		 * A service method that also calls a tracked method on another service class.
		 * This method will be configured for behavior tracking.
		 */
		public String getTracked(String param) {
			return "serviceA:" + serviceB.getTracked();
		}

		/** test varargs and primitive type support in logging */
		public int getPrimitiveTracked(int... params) {
			int sum = 0;
			for (int i : params)
				sum += i;
			return sum;
		}
		
		/**
		 * A service method that also calls a monitored method on another service class.
		 * This method will NOT be configured for behavior tracking.
		 */
		public String getUntracked() {
			return "serviceA:" + serviceB.getTracked();
		}

		/**
		 * Tests support for logging tracking events with custom parameter types and variable
		 * argument lists.
		 */
		public ServiceB getWeirdParametersTracked(BehaviorAdviceTest arg0, ServiceB arg1, Param enumParam, String... argN) {
			return arg1;
		}
		
		/**
		 * Tests support for void return type.
		 */
		public void getVoidReturnTracked() {
		}
		
		/**
		 * Tests support for both primitive parameter types and error handling.
		 */
		public void throwExceptionTracked(long ignored) {
			throw ERROR;
		}
	}
	
	public static enum Param {
		Value_1, Value_2;
	}
	
	public static class ServiceB {

		private volatile int hitCount = 0;
		
		public Integer getTracked() {
			return hitCount++;
		}
		
	}
	
	public static class SubServiceB extends ServiceB {}
	
}
