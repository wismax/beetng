/* 
 * Copyright 2008-2009 the original author or authors.
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
 
package com.mtgi.analytics;

import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLIdentical;

import java.io.IOException;

import javax.xml.stream.XMLOutputFactory;

import org.custommonkey.xmlunit.Diff;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class EventDataElementSerializerTest {

	private static EventDataElementSerializer serializer;
	private static boolean isJava5;

	@BeforeClass
	public static void init() {
		serializer = new EventDataElementSerializer(XMLOutputFactory.newInstance());
		isJava5 = System.getProperty("java.version").startsWith("1.5");
	}
	
	@AfterClass
	public static void destroy() {
		serializer = null;
	}
	
	@Test
	public void testElementNames() throws SAXException, IOException {
		//test conversion of input text into valid XML element names.
		EventDataElement root = new EventDataElement("-0&9!@#$#$!!$");
		root.addElement("--Leading&*(Trailing_7-#")
			.addElement("9-foo@bar.baz");
		assertXMLIdentical(new Diff("<?xml version='1.0'?><data><Leading-Trailing-7><foo-bar-baz/></Leading-Trailing-7></data>", serializer.serialize(root, true)), true);
	}
	
	@Test
	public void testProperties() throws SAXException, IOException {
		//test serialization of properties to elements.
		EventDataElement root = new EventDataElement("event-data");
		root.add("foo", new Double(1.5));
		root.add("---bar-7", "hello");
		root.add("baz", null);
		root.add("qux 8", "world&escaped\nmaybe?"); //newline value should be normalized.

		if (isJava5) {
			assertXMLIdentical(new Diff("<?xml version='1.0'?><event-data foo=\"1.5\" bar-7=\"hello\" qux-8=\"world&amp;escaped&#10;maybe?\"></event-data>", 
										serializer.serialize(root, true)), 
								true);
		} else {
			assertXMLIdentical(new Diff("<?xml version='1.0'?><event-data foo=\"1.5\" bar-7=\"hello\" qux-8=\"world&amp;escaped maybe?\"></event-data>", 
										serializer.serialize(root, true)), 
							   true);
		}
	}
	
	@Test
	public void testDuplicateProperties() throws SAXException, IOException {
		//test overriding of prior property values.
		EventDataElement root = new EventDataElement("event-data");
		root.add("foo", new Double(1.5));
		root.add("---bar-7", "hello");
		root.add("baz", null);
		root.add("qux 8", "world&escaped\nmaybe?"); //newline value should be normalized.
		root.add("foo", new Double(2.7));
		root.add("baz", "updated");
		root.add("baz", null);
		root.add("foo", new Double(4.7));
		if (isJava5) {
			assertXMLIdentical(new Diff("<?xml version='1.0'?><event-data foo=\"4.7\" bar-7=\"hello\" qux-8=\"world&amp;escaped&#10;maybe?\"></event-data>", 
					serializer.serialize(root, true)), 
		   true);
		} else {
			assertXMLIdentical(new Diff("<?xml version='1.0'?><event-data foo=\"4.7\" bar-7=\"hello\" qux-8=\"world&amp;escaped maybe?\"></event-data>", 
										serializer.serialize(root, true)), 
							   true);
		}
	}
	
	@Test
	public void testMixed() throws SAXException, IOException {
		//test a complex case mixing text, properties, and nested elements.
		EventDataElement root = new EventDataElement("-0&9!@#$#$!!$");
		root.addElement("--Leading&*(Trailing_7-#").setText("<hello&amp;world>");
		EventDataElement child = root.addElement("9-foo@bar.baz");
		child.add("hello", "world");
		child.setText("bar");
		child.addElement("baz-quux").addElement("first");
		child.addElement("baz-quux").add("second", "");
		assertXMLIdentical(new Diff("<?xml version='1.0'?>" +
					"<data>" +
						"<Leading-Trailing-7>&lt;hello&amp;amp;world&gt;</Leading-Trailing-7>" +
						"<foo-bar-baz hello=\"world\">" +
							"bar" +
							"<baz-quux><first/></baz-quux>" +
							"<baz-quux second=\"\"></baz-quux>" +
						"</foo-bar-baz>" +
					"</data>", 
					serializer.serialize(root, true)), true);
	}

	@Test
	public void testNull() {
		assertNull("null data serializes as null string", serializer.serialize(null, true));
	}
	
	@Test
	public void testEmpty() throws SAXException, IOException {
		EventDataElement element = new EventDataElement("event-data");
		assertXMLIdentical(new Diff("<?xml version='1.0'?><event-data/>", serializer.serialize(element, true)), true);
	}
	
}
