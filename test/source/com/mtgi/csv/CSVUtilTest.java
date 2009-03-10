package com.mtgi.csv;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;

public class CSVUtilTest {

	@Test
	public void testEscapeCsv() throws IOException, ParserConfigurationException {
		assertEquals("xml is serialized, with internal quotes stuttered",
					 "\"\r<foo bar=\"\"&quot;b\\ar&quot;\"\"><baz>inner\rlines\rnormalized\rcompletely</baz></foo>\"",
					 CSVUtil.quoteCSV("\n<foo bar=\"&quot;b\\ar&quot;\"><baz>inner\rlines\r\nnormalized\ncompletely</baz></foo>"));
	}
	
	@Test
	public void testNullCsv() throws IOException {
		assertEquals("Null CSV is converted to blank", "", CSVUtil.quoteCSV(null));
	}
	
	@Test
	public void testEmptyCsv() throws IOException {
		assertEquals("Empty CSV is quoted", "\"\"", CSVUtil.quoteCSV(""));
	}
	
	@Test
	public void testEscapeCsvAppendable() throws IOException, ParserConfigurationException {
		StringWriter writer = new StringWriter();
		assertSame("reference returned", 
				   writer, CSVUtil.quoteCSV("\n<foo bar=\"&quot;b\\ar&quot;\"><baz>inner\rlines\r\nnormalized\ncompletely</baz></foo>", writer));
		assertEquals("xml is serialized, with internal quotes stuttered",
					 "\"\r<foo bar=\"\"&quot;b\\ar&quot;\"\"><baz>inner\rlines\rnormalized\rcompletely</baz></foo>\"",
					 writer.toString());
	}
	
	@Test
	public void testNullCsvAppendable() throws IOException {
		StringWriter writer = new StringWriter();
		assertSame("reference returned", writer, CSVUtil.quoteCSV(null, writer));
		assertEquals("Null CSV is converted to blank", "", writer.toString());
	}
	
	@Test
	public void testEmptyCsvAppendable() throws IOException {
		StringWriter writer = new StringWriter();
		assertSame("reference returned", writer, CSVUtil.quoteCSV("", writer));
		assertEquals("Empty CSV is quoted", "\"\"", writer.toString());
	}
	
}
