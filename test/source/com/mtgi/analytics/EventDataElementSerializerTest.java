package com.mtgi.analytics;

import static org.junit.Assert.*;

import javax.xml.stream.XMLOutputFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EventDataElementSerializerTest {

	private static EventDataElementSerializer serializer;

	@BeforeClass
	public static void init() {
		serializer = new EventDataElementSerializer(XMLOutputFactory.newInstance());
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
		assertEquals("<?xml version='1.0' encoding='utf-8'?><data><Leading-Trailing-7><foo-bar-baz/></Leading-Trailing-7></data>", 
					 serializer.serialize(root, true));
	}
	
	@Test
	public void testProperties() {
		//test serialization of properties to elements.
		EventDataElement root = new EventDataElement("event-data");
		root.add("foo", new Double(1.5));
		root.add("---bar-7", "hello");
		root.add("baz", null);
		root.add("qux", "world&escaped\nmaybe?");
		assertEquals("<?xml version='1.0' encoding='utf-8'?><event-data><foo>1.5</foo><bar-7>hello</bar-7><baz/><qux>world&amp;escaped\nmaybe?</qux></event-data>", 
					 serializer.serialize(root, true));
	}
	
	@Test
	public void testMixed() {
		//test a complex case mixing text, properties, and nested elements.
		EventDataElement root = new EventDataElement("-0&9!@#$#$!!$");
		root.addElement("--Leading&*(Trailing_7-#").setText("<hello&amp;world>");
		EventDataElement child = root.addElement("9-foo@bar.baz");
		child.add("hello", "world");
		child.setText("bar");
		child.addElement("baz-quux").add("first", null);
		child.addElement("baz-quux").add("second", null);
		assertEquals("<?xml version='1.0' encoding='utf-8'?>" +
					"<data>" +
						"<Leading-Trailing-7>&lt;hello&amp;amp;world&gt;</Leading-Trailing-7>" +
						"<foo-bar-baz>" +
							"bar" +
							"<hello>world</hello>" +
							"<baz-quux><first/></baz-quux>" +
							"<baz-quux><second/></baz-quux>" +
						"</foo-bar-baz>" +
					"</data>", 
					serializer.serialize(root, true));
	}
	
	@Test
	public void testNull() {
		assertNull("null data serializes as null string", serializer.serialize(null, true));
	}
	
	@Test
	public void testEmpty() {
		EventDataElement element = new EventDataElement("event-data");
		assertEquals("<?xml version='1.0' encoding='utf-8'?><event-data/>", serializer.serialize(element, true));
	}
}
