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
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

public class BinaryToCSVTest {

	@Test
	public void testConversion() throws Exception {
		
		File in = getResource(BinaryToCSVTest.class, "BinaryToXSLTTest-input.bxml");
		File expectedData = getResource(BinaryToCSVTest.class, "BinaryToCSVTest-output.csv");
		File out = File.createTempFile("BinaryToXmlTest", ".xml");
		out.deleteOnExit();
		
		String[] args = { 
			"-tool", "csv", 
			in.getAbsolutePath(), out.getAbsolutePath() 
		};
		assertEquals("process completes successfully", 0, Main.process(args));
		assertTrue("sanity check: some data generated", out.length() > 0);
		
		BufferedReader expected = new BufferedReader(new FileReader(expectedData));
		try {
			BufferedReader actual = new BufferedReader(new FileReader(out));
			try {
				int lineNo = 1;
				for (String line = expected.readLine(); line != null; line = expected.readLine()) 
					assertEquals("line " + lineNo++, line, actual.readLine());
				assertNull("no data remains in output", actual.readLine());
			} finally {
				actual.close();
			}
		} finally {
			expected.close();
		}
		
		out.delete();
	}
}
