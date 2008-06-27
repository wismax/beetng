package com.mtgi.analytics;

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EventDataElementSerializerTest {

	private static EventDataElementSerializer serializer;

	@BeforeClass
	public static void init() {
		serializer = new EventDataElementSerializer(DocumentBuilderFactory.newInstance());
	}
	
	@AfterClass
	public static void destroy() {
		serializer = null;
	}
	
	@Test
	public void testElementNames() {
		//test conversion of input text into valid XML element names.
		EventDataElement root = new EventDataElement("-0&9!@#$#$!!$");
		root.addElement("--Leading&*(Trailing_7-#")
			.addElement("9-foo@bar.baz");
		assertEquals("<?xml version=\"1.0\"?>\n<data><Leading-Trailing-7><foo-bar-baz/></Leading-Trailing-7></data>", 
					 serializer.serialize(root));
	}
	
	@Test
	public void testProperties() {
		//test serialization of properties to elements.
		EventDataElement root = new EventDataElement("event-data");
		assertNull(root.put("foo", new Double(1.5)));
		assertNull(root.put("---bar-7", "hello"));
		assertNull(root.put("baz", null));
		assertNull(root.put("qux", "hello"));
		assertEquals("hello", root.put("qux", "world&escaped\nmaybe?"));
		assertEquals("<?xml version=\"1.0\"?>\n<event-data><foo>1.5</foo><bar-7>hello</bar-7><baz/><qux>world&amp;escaped\nmaybe?</qux></event-data>", 
					 serializer.serialize(root));
	}
	
	@Test
	public void testMixed() {
		//test a complex case mixing text, properties, and nested elements.
		EventDataElement root = new EventDataElement("-0&9!@#$#$!!$");
		root.addElement("--Leading&*(Trailing_7-#").setText("<hello&amp;world>");
		EventDataElement child = root.addElement("9-foo@bar.baz");
		child.put("hello", "world");
		child.setText("bar");
		child.addElement("baz-quux").put("first", null);
		child.addElement("baz-quux").put("second", null);
		assertEquals("<?xml version=\"1.0\"?>\n" +
					"<data>" +
						"<Leading-Trailing-7>&lt;hello&amp;amp;world&gt;</Leading-Trailing-7>" +
						"<foo-bar-baz>" +
							"bar" +
							"<hello>world</hello>" +
							"<baz-quux><first/></baz-quux>" +
							"<baz-quux><second/></baz-quux>" +
						"</foo-bar-baz>" +
					"</data>", 
					serializer.serialize(root));
	}
	
	@Test
	public void testNull() {
		assertNull("null data serializes as null string", serializer.serialize(null));
	}
	
	@Test
	public void testEmpty() {
		EventDataElement element = new EventDataElement("event-data");
		assertEquals("<?xml version=\"1.0\"?>\n<event-data/>", serializer.serialize(element));
	}
}
