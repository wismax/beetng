package com.mtgi.analytics.jmx;

import static org.junit.Assert.*;

import javax.management.MalformedObjectNameException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.aop.BehaviorTrackingAdvice;
import com.mtgi.analytics.servlet.ServletRequestBehaviorTrackingAdapter;
import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

public class EventTypeNamingStrategyTest {

	private EventTypeNamingStrategy inst;

	@Before
	public void setUp() {
		inst = new EventTypeNamingStrategy();
		inst.afterPropertiesSet();
	}
	
	@After
	public void tearDown() {
		inst = null;
	}
	
	@Test
	public void testMethodEvents() throws MalformedObjectNameException {
		BehaviorEvent event = new TestEvent(null, BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		assertEquals("method event name computed correctly",
				"testApp:type=method-monitor,package=com.mtgi,group=analytics.test,class=SomeType,name=someMethod",
				inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testNestedMethodEvents() throws MalformedObjectNameException {
		BehaviorEvent event = new TestEvent(null, ServletRequestBehaviorTrackingAdapter.DEFAULT_EVENT_TYPE, "/request/uri?param=value", "testApp", null, null);
		event = new TestEvent(event, BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		assertEquals("request event name computed correctly",
				"testApp:type=http-request-monitor,name=\"/request/uri\\?param=value\",nested=method_com.mtgi.analytics.test.SomeType.someMethod",
				inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testHttpRequestEvents() throws MalformedObjectNameException {
		BehaviorEvent event = new TestEvent(null, ServletRequestBehaviorTrackingAdapter.DEFAULT_EVENT_TYPE, "/request/uri?param=value", "testApp", null, null);
		assertEquals("request event name computed correctly",
				"testApp:type=http-request-monitor,name=\"/request/uri\\?param=value\"",
				inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testRootSqlEvents() throws MalformedObjectNameException {
		BehaviorEvent event = new TestEvent(null, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate", "testApp", null, null);
		event.addData().addElement("sql").setText("insert into FOO values (1, 2)");
		assertEquals("root sql event name computed correctly",
				"testApp:type=jdbc-monitor,name=executeUpdate",
				inst.getObjectName(event, null).toString());
	}

	@Test
	public void testNestedSqlEvents() throws MalformedObjectNameException {
		BehaviorEvent parent = new TestEvent(null, BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		BehaviorEvent event = new TestEvent(parent, BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, "executeUpdate", "testApp", null, null);
		event.addData().addElement("sql").setText("insert into FOO values (1, 2)");
		assertEquals("nested sql event name computed correctly",
				"testApp:type=method-monitor,package=com.mtgi,group=analytics.test,class=SomeType,name=someMethod,nested=jdbc_executeUpdate",
				inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testCustomTypeEvents() throws MalformedObjectNameException {
		BehaviorEvent event = new TestEvent(null, "custom", "com.mtgi.analytics.test.SomeType.someMethod", "testApp", null, null);
		assertEquals("custom event name computed correctly",
				"testApp:type=custom-monitor,name=com.mtgi.analytics.test.SomeType.someMethod",
				inst.getObjectName(event, null).toString());
	}
}
