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
		String[] tools = { "xml", "xslt" };
		String[] expected = { 
			"Usage: -tool xml [input] [output]",
			"Usage: -tool xslt -split [xpath] -xsl [xsl source] [-format text|xml|html] [input] [output]"
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
				"Usage: -tool xml|xslt [-help|--help|-?] [tool options]", 
				new String(err.toByteArray()).trim());
	}
	
	@Test
	public void testMissingArgument() throws Exception {
		String[] args = { "input", "output" };
		assertEquals("Tool returned non-zero status", 1, Main.process(args));
		System.err.flush();
		assertEquals("Default xml tool help message displayed", 
				"Usage: -tool xml|xslt [-help|--help|-?] [tool options]", 
				new String(err.toByteArray()).trim());
	}
}
