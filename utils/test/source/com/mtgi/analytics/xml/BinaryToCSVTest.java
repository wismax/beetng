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
