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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MainTest {

	private PrintStream oldErr;
	private PrintStream oldOut;
	private ByteArrayOutputStream err;

	@Before
	public void setUp() {
		oldErr = System.err;
		oldOut = System.out;
		err = new ByteArrayOutputStream();
		System.setErr(new PrintStream(err));
		System.setOut(System.err);
		System.err.flush();
		err.reset();
	}

	@After
	public void tearDown() {
		System.setErr(oldErr);
		System.setOut(oldOut);
	}
	
	@Test
	public void testHelpMessages() throws Exception {
		String[] tools = { "xml", "xslt", "csv" };
		String[] expected = { 
			"Usage: -tool xml [input] [output]",
			"Usage: -tool xslt -split [xpath] -xsl [xsl source] [-format text|xml|html] [input] [output]",
			"Usage: -tool csv [input] [output]"
		};

		for (int i = 0; i < tools.length; ++i) {
			err.reset();
			String[] args = { "-tool", tools[i], "-?" };
			assertEquals("Tool returned non-zero status", 1, Main.process(args));
			System.err.flush();
			assertEquals("Tool help message displayed", expected[i], new String(err.toByteArray()).trim());
		}
	}
	
	@Test
	public void testInvalidArgument() throws Exception {
		String[] args = { "-tool", "foo", "input", "output" };
		assertEquals("Tool returned non-zero status", 1, Main.process(args));
		System.err.flush();
		assertEquals("Tool help message displayed", 
				"Usage: -tool xml|xslt|csv [-help|--help|-?] [tool options]", 
				new String(err.toByteArray()).trim());
	}
	
	@Test
	public void testMissingArgument() throws Exception {
		String[] args = { "input", "output" };
		assertEquals("Tool returned non-zero status", 1, Main.process(args));
		System.err.flush();
		assertEquals("Default xml tool help message displayed", 
				"Usage: -tool xml|xslt|csv [-help|--help|-?] [tool options]", 
				new String(err.toByteArray()).trim());
	}
}
