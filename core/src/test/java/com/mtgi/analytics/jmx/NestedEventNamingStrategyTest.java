package com.mtgi.analytics.jmx;

import static org.junit.Assert.assertEquals;

import javax.management.MalformedObjectNameException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.aop.BehaviorTrackingAdvice;
import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

public class NestedEventNamingStrategyTest {

	private NestedEventNamingStrategy inst;

	@Before
	public void setUp() {
		EventTypeNamingStrategy parent = new EventTypeNamingStrategy();
		parent.afterPropertiesSet();
		inst = (NestedEventNamingStrategy)parent.getStrategy(BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE);
	}
	
	@After
	public void tearDown() {
		inst = null;
	}
	
	@Test
	public void testRootEvent() throws MalformedObjectNameException {
		BehaviorEvent event = new TestEvent(null, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate", "testApp", null, null);
		event.addData().addElement("sql").setText("insert into FOO values (1, 2)");
		assertEquals("root sql event name computed correctly",
				"testApp:type=jdbc-monitor,name=executeUpdate",
				inst.getObjectName(event, null).toString());
	}

	@Test
	public void testShallowEvent() throws MalformedObjectNameException {
		BehaviorEvent parent = new TestEvent(null, BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		BehaviorEvent event = new TestEvent(parent, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate", "testApp", null, null);
		event.addData().addElement("sql").setText("insert into FOO values (1, 2)");
		assertEquals("nested sql event name computed correctly",
				"testApp:type=method-monitor,package=com.mtgi,group=analytics.test,class=SomeType,name=someMethod,nestedType=jdbc,nestedName=executeUpdate",
				inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testDeepNesting() throws MalformedObjectNameException {
		BehaviorEvent parent = new TestEvent(null, BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		BehaviorEvent event = new TestEvent(parent, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate", "testApp", null, null);
		for (int i = 2; i <= 3; ++i)
			event = new TestEvent(event, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate_" + i, "testApp", null, null);

		assertEquals("deep nested sql event name computed correctly",
				"testApp:type=method-monitor,package=com.mtgi,group=analytics.test,class=SomeType,name=someMethod," +
				"nestedType=jdbc,nestedName=executeUpdate," +
				"nestedType[2]=jdbc,nestedName[2]=executeUpdate_2," +
				"nestedType[3]=jdbc,nestedName[3]=executeUpdate_3",
				inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testNestingOverflow() throws MalformedObjectNameException {
		BehaviorEvent parent = new TestEvent(null, BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		BehaviorEvent event = new TestEvent(parent, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate", "testApp", null, null);
		for (int i = 2; i <= 4; ++i)
			event = new TestEvent(event, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate_" + i, "testApp", null, null);

		assertEquals("nesting overflow event name falls back on simple naming scheme",
				"testApp:type=jdbc-monitor,name=executeUpdate_4",
				inst.getObjectName(event, null).toString());
	}
	
}
