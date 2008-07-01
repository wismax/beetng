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

@SpringApplicationContext({"com/mtgi/analytics/aop/BehaviorAdviceTest-applicationContext.xml", "com/mtgi/analytics/aop/applicationContext-test.xml"})
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

	@Test
	public void testTracking() throws Exception {
		assertEquals("this call should log 2 events", "serviceA:0", service.getTracked("hello"));
		assertEquals("this call should log only 1 event", "serviceA:1", service.getUntracked());
		
		//make sure all event data has been pushed to the test database
		manager.flush();
		assertEventDataMatches("BehaviorAdviceTest.testTracking-result.xml");
	}
	
	/**
	 * Spring bean class that depends on another spring bean (ServiceB).
	 * Some method calls in this class are tracked, some aren't.
	 */
	public static class ServiceA {
		
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
		
	}
	
	public static class ServiceB {

		private volatile int hitCount = 0;
		
		public Integer getTracked() {
			return hitCount++;
		}
		
	}
	
}
