package com.mtgi.analytics.jmx;

import static org.junit.Assert.*;

import javax.management.MalformedObjectNameException;

import org.junit.Test;

import com.mtgi.analytics.BehaviorEvent;

public class MethodNamingStrategyTest {

	@Test
	public void testDefaultPackage() throws MalformedObjectNameException {
		MethodNamingStrategy inst = new MethodNamingStrategy();
		BehaviorEvent event = new TestEvent("MyType.myMethod");
		assertEquals("methods in default package are parsed correctly", 
					 "testApp:type=method-monitor,package=[default],class=MyType,name=myMethod",
					 inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testPackageDepthClasses() throws MalformedObjectNameException {
		MethodNamingStrategy inst = new MethodNamingStrategy();

		BehaviorEvent event = new TestEvent("com.mtgi.MyType.myMethod");
		assertEquals("methods at max package depth are parsed correctly", 
					 "testApp:type=method-monitor,package=com.mtgi,class=MyType,name=myMethod",
					 inst.getObjectName(event, null).toString());
		
		event = new TestEvent("com.MyType.myMethod");
		assertEquals("methods below max package depth are parsed correctly", 
					 "testApp:type=method-monitor,package=com,class=MyType,name=myMethod",
					 inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testDeepClasses() throws MalformedObjectNameException {
		MethodNamingStrategy inst = new MethodNamingStrategy();
		BehaviorEvent event = new TestEvent("com.mtgi.module.MyType.myMethod");
		assertEquals("methods deeper than max package depth are parsed correctly", 
					 "testApp:type=method-monitor,package=com.mtgi,group=module,class=MyType,name=myMethod",
					 inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testNoPackageDepth() throws MalformedObjectNameException {
		MethodNamingStrategy inst = new MethodNamingStrategy();
		inst.setPackageDepth(0);
		BehaviorEvent event = new TestEvent("com.mtgi.module.MyType.myMethod");
		assertEquals("methods with no max package depth are parsed correctly", 
					 "testApp:type=method-monitor,package=com.mtgi.module,class=MyType,name=myMethod",
					 inst.getObjectName(event, null).toString());
	}
	
	@Test
	public void testArgumentChecking() throws MalformedObjectNameException {
		MethodNamingStrategy inst = new MethodNamingStrategy();
		BehaviorEvent event = new TestEvent("myMethod");
		try {
			inst.getObjectName(event, null);
			fail("invalid input should raise an error");
		} catch (IllegalArgumentException expected) {}
	}
	
	private static class TestEvent extends BehaviorEvent {
		private static final long serialVersionUID = -631522178925423193L;

		public TestEvent(String name) {
			super(null, "method", name, "testApp", null, null, null);
		}
	}
}
