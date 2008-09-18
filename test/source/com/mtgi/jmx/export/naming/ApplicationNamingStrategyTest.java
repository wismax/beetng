package com.mtgi.jmx.export.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;

import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;

public class ApplicationNamingStrategyTest {

	@Test
	public void testNamingStrategy() throws MalformedObjectNameException {
		
		MetadataNamingStrategy delegate = new MetadataNamingStrategy();
		delegate.setAttributeSource(new AnnotationJmxAttributeSource());
		
		ApplicationNamingStrategy ans = new ApplicationNamingStrategy();
		ans.setDelegate(delegate);
		ans.setApplication("testApplication");
		
		ObjectName name = ans.getObjectName(new XmlBehaviorEventPersisterImpl(), "testPersister");
		assertNotNull("name is constructed", name);
		assertEquals("name has been transformed correctly",
					 "testApplication:package=com.mtgi,group=analytics,name=BehaviorTrackingLog", name.toString());
		assertEquals("package name quoted properly", "com.mtgi", name.getKeyProperty("package"));
	}
	
	@Test
	public void testQuoting() {
		
		String[] text = { 
			"no_Quotes;",
			"multi\nline",
			"white space",
			"comma,comma",
			"colon:not_allowed",
			"what?",
			"\"quotes",
			"not=equal",
			"back\\slash"
		};
		String[] expected = {
			"no_Quotes;",
			"\"multi\\nline\"",
			"\"white space\"",
			"\"comma,comma\"",
			"\"colon:not_allowed\"",
			"\"what\\?\"",
			"\"\\\"quotes\"",
			"\"not=equal\"",
			"\"back\\\\slash\""
		};
		for (int i = 0; i < text.length; ++i)
			assertEquals(expected[i], ApplicationNamingStrategy.quote(text[i]));
	}
}
