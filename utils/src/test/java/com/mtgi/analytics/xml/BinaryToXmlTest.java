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
 
package com.mtgi.analytics.xml;

import static com.mtgi.analytics.xml.BinaryToXSLTTest.getResource;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLIdentical;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class BinaryToXmlTest {

	@Test
	public void testConversion() throws Exception {
		
		File in = getResource(BinaryToXmlTest.class, "BinaryToXSLTTest-input.bxml");
		File expectedData = getResource(BinaryToXmlTest.class, "BinaryToXmlTest-output.xml");
		File out = File.createTempFile("BinaryToXmlTest", ".xml");
		out.deleteOnExit();
		
		String[] args = { 
			"-tool", "xml", 
			in.getAbsolutePath(), out.getAbsolutePath() 
		};
		assertEquals("process completes successfully", 0, Main.process(args));
		assertTrue("sanity check: some data generated", out.length() > 0);
		
		BufferedReader expected = new BufferedReader(new FileReader(expectedData));
		try {
			BufferedReader actual = new BufferedReader(new FileReader(out));
			try {
				assertXMLIdentical(new Diff(expected, actual), true);
			} finally {
				actual.close();
			}
		} finally {
			expected.close();
		}
		
		out.delete();
	}
}
