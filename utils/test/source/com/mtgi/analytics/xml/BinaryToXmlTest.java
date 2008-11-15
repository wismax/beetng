package com.mtgi.analytics.xml;

import static com.mtgi.analytics.xml.BinaryToXSLTTest.getResource;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

				int line = 1;
				for (String s = expected.readLine(); s != null; s = expected.readLine()) {
					assertEquals("line " + line++ + " matches", s, actual.readLine());
				}
				assertNull("no data remains in results", actual.readLine());
				
			} finally {
				actual.close();
			}
		} finally {
			expected.close();
		}
		
		out.delete();
	}
}
