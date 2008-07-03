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
		assertSame("argument value is returned unmodified", arg, service.getWeirdParametersTracked(this, arg, "hello", "world"));
		assertNull("null values allowed", service.getWeirdParametersTracked(null, null));
		
		arg = new ServiceB();
		assertSame("argument value is returned unmodified", arg, service.getWeirdParametersTracked(this, arg, null, "value"));
		
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
		public ServiceB getWeirdParametersTracked(BehaviorAdviceTest arg0, ServiceB arg1, String... argN) {
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
	
	public static class ServiceB {

		private volatile int hitCount = 0;
		
		public Integer getTracked() {
			return hitCount++;
		}
		
	}
	
	public static class SubServiceB extends ServiceB {}
	
}
