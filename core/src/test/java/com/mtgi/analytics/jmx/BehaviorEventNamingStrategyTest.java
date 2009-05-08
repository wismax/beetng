package com.mtgi.analytics.jmx;

import static org.junit.Assert.*;

import javax.management.MalformedObjectNameException;

import org.junit.Test;

import com.mtgi.analytics.BehaviorEvent;

public class BehaviorEventNamingStrategyTest {

	@Test
	public void testConventions() throws MalformedObjectNameException {
		BehaviorEventNamingStrategy inst = new BehaviorEventNamingStrategy();
		BehaviorEvent event = new TestEvent("myApp", "some-type", "myName");
		assertEquals("event name is generated", 
					 "myApp:type=some-type-monitor,name=myName",
					 inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testQuotes() throws MalformedObjectNameException {
		BehaviorEventNamingStrategy inst = new BehaviorEventNamingStrategy();
		BehaviorEvent event = new TestEvent("myApp*test", "some type", "myName:is?special");
		assertEquals("event name is generated", 
					 "\"myApp\\*test\":type=\"some type-monitor\",name=\"myName:is\\?special\"",
					 inst.getObjectName(event, null).toString());
	}
	
	private static class TestEvent extends BehaviorEvent {
		private static final long serialVersionUID = -631522178925423193L;

		public TestEvent(String app, String type, String name) {
			super(null, type, name, app, null, null, null);
		}
	}
}
