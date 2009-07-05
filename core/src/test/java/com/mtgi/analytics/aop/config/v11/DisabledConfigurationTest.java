package com.mtgi.analytics.aop.config.v11;

import static org.junit.Assert.*;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.aop.config.DisabledBehaviorTrackingManager;
import com.mtgi.analytics.servlet.ServletRequestBehaviorTrackingAdapter;
import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public class DisabledConfigurationTest {

	@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/DisabledConfigurationTest-applicationContext.xml")
	protected ApplicationContext appContext;

	@SpringBeanByType
	private DataSource dataSource;
	
	@SpringBeanByType
	private TestBean testBean;
	
	@SpringBeanByName
	private BehaviorTrackingManager defaultTrackingManager;
	
	@Test
	public void testHttpRequestListenersDisabled() {
		Map<?,?> requestListeners = appContext.getBeansOfType(ServletRequestBehaviorTrackingAdapter.class);
		assertEquals("no request listeners registered in app context", 0, requestListeners.size());
		
	}
	
	@Test
	public void testAopDisabled() {
		assertSame("test bean has not be wrapped", TestBean.class, testBean.getClass());
	}
	
	@Test
	public void testDataSourceTrackingDisabled() {
		assertFalse("data source has not been wrapped", dataSource instanceof BehaviorTrackingDataSource);
	}

	@Test
	@SuppressWarnings("serial")
	public void testDummyManager() {
		assertSame("manager is of dummy type", DisabledBehaviorTrackingManager.class, defaultTrackingManager.getClass());
		try {
			defaultTrackingManager.createEvent("hello", "world");
			fail("manager should be disabled");
		} catch (UnsupportedOperationException expected) {
		}
		
		try {
			defaultTrackingManager.start(new BehaviorEvent(null, "hello", "world", "testApp", null, null) {});
			fail("manager should be disabled");
		} catch (UnsupportedOperationException expected) {
		}
		
		try {
			defaultTrackingManager.stop(new BehaviorEvent(null, "hello", "world", "testApp", null, null) {});
			fail("manager should be disabled");
		} catch (UnsupportedOperationException expected) {
		}
	}
	
	public static class TestBean {
		public String getValueTracked() {
			return "hello";
		}
	}
}
