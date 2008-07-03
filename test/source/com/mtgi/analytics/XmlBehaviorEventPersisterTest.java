package com.mtgi.analytics;

import static com.mtgi.analytics.BehaviorEventSerializer.XS_DATE_FORMAT;
import static org.apache.commons.io.IOUtils.readLines;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.xml.fastinfoset.tools.FI_SAX_XML;

public class XmlBehaviorEventPersisterTest {

	private XmlBehaviorEventPersisterImpl persister;
	private File file;
	
	@Before
	public void setUp() throws Exception {
		file = File.createTempFile("perf", ".bxml");
		file.deleteOnExit();
		//initialize the persister to point to the test logger.
		persister = new XmlBehaviorEventPersisterImpl();
		persister.setFile(file.getAbsolutePath());
		persister.afterPropertiesSet();
	}

	@After
	public void tearDown() throws Exception {
		try {
			persister.destroy();
		} finally {
			persister = null;
			file.delete();
		}
	}
	
	@Test
	public void testEmptyQueue() {
		assertEquals("no events to persist", 0, persister.persist(new LinkedList<BehaviorEvent>()));
		assertEquals("no events written to log", 0, file.length());
		assertEquals(0, persister.getFileSize());
		assertFalse("persister defaults to plain text format", persister.isBinary());
	}
	
	@Test @SuppressWarnings("unchecked")
	public void testNestedEvents() throws InterruptedException, IOException {
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			events.add(JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter));
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);

		assertEquals("entire event tree persisted", 39, persister.persist(queue));
		assertEquals("persister reports correct file size", file.length(), persister.getFileSize());
		
		//now perform verification of log data against the expected results.
		List<String> actualLines = (List<String>)FileUtils.readLines(file);
		assertEquals("every event was written", 39, actualLines.size());

		//flatten the event data tree into an ordered list, as we expect to find it in the log.
		for (int i = 0; i < events.size(); ++i)
			events.addAll(events.get(i).getChildren());

		//read up the expected results for comparison.
		InputStream expectedData = XmlBehaviorEventPersisterTest.class.getResourceAsStream("XmlBehaviorEventPersisterTest.testNestedEvents-result.xml");
		assertNotNull("expected results resource found in test environment", expectedData);
		List<String> expectedLines = (List<String>)readLines(expectedData);
		expectedData.close();

		//compare the logged data line by line.
		assertEquals("expected log count matches actual", expectedLines.size(), actualLines.size());
		for (int i = 0; i < expectedLines.size(); ++i) {
			
			String actual = actualLines.get(i);
			//we have to strip data that varies from test run to test run out before comparing.
			String expectedStripped = stripVariableData(expectedLines.get(i));
			String actualStripped = stripVariableData(actual);
			assertEquals("log line[" + i + "] matches expected", expectedStripped, actualStripped);
			
			//now check data against source event so that time-sensitive info is checked.
			BehaviorEvent evt = events.get(i);
			assertNotNull("event was given an id", evt.getId());
			assertTrue("log contains id", actual.contains("id=\"" + evt.getId() + "\""));
			
			BehaviorEvent parent = evt.getParent();
			if (parent == null)
				assertFalse("log does not contain parent id", actual.contains("parent-id"));
			else
				assertTrue("log contains parent reference", actual.contains("parent-id=\"" + parent.getId() + "\""));
			
			assertTrue("log records time correctly", actual.contains("<start>" + XS_DATE_FORMAT.format(evt.getStart()) + "</start>"));
			assertTrue("log records duration correctly", actual.contains("<duration-ms>" + evt.getDuration() + "</duration-ms>"));
		}
	}
	
	@Test @SuppressWarnings("unchecked")
	public void testBinaryXml() throws Exception {
		
		//switch to binary format.
		persister.setBinary(true);
		persister.rotateLog();
		assertTrue("persister switched to binary", persister.isBinary());
		
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			events.add(JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter));
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);

		assertEquals("entire event tree persisted", 39, persister.persist(queue));
		assertEquals("persister reports correct file size", file.length(), persister.getFileSize());
		
		//convert binary to flat xml for comparison.  this also verifies that 'rotateLog'
		//leaves a well-formed XML document in its wake.
		String path = persister.rotateLog();
		File source = new File(path);
		assertTrue("log archive data exists", source.isFile());
		assertFalse("archive does not point to active log", source.getCanonicalPath().equals(file.getCanonicalPath()));
		
		File dest = File.createTempFile("perf", ".xml");
		dest.deleteOnExit();
		
		FileInputStream fis = new FileInputStream(source);
		FileOutputStream fos = new FileOutputStream(dest);
		FI_SAX_XML converter = new FI_SAX_XML();
		converter.parse(fis, fos);
		fis.close();
		fos.close();
		
		//now perform verification of log data against the expected results.
		List<String> actualLines = (List<String>)FileUtils.readLines(dest);
		assertEquals("every event was written, including log close", 40, actualLines.size());

		final String expectedOpen = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><event-log>";
		assertEquals("well-formed open", 
					 expectedOpen, actualLines.get(0).substring(0, expectedOpen.length()));
		assertEquals("well-formed close", "</event-log>", actualLines.get(actualLines.size() - 1));
		
		//flatten the event data tree into an ordered list, as we expect to find it in the log.
		for (int i = 0; i < events.size(); ++i)
			events.addAll(events.get(i).getChildren());

		//read up the expected results for comparison.
		InputStream expectedData = XmlBehaviorEventPersisterTest.class.getResourceAsStream("XmlBehaviorEventPersisterTest.testBinaryXml-result.xml");
		assertNotNull("expected results resource found in test environment", expectedData);
		List<String> expectedLines = (List<String>)readLines(expectedData);
		expectedData.close();

		//compare the logged data line by line.
		assertEquals("expected log count matches actual", expectedLines.size(), actualLines.size());
		for (int i = 0; i < expectedLines.size(); ++i) {
			
			String actual = actualLines.get(i);
			//we have to strip data that varies from test run to test run out before comparing.
			String expectedStripped = stripVariableData(expectedLines.get(i));
			String actualStripped = stripVariableData(actual);
			assertEquals("log line[" + i + "] matches expected", expectedStripped, actualStripped);

			//last line is log close and doesn't correspond to an event.
			if (i < expectedLines.size() - 1) {
				//now check data against source event so that time-sensitive info is checked.
				BehaviorEvent evt = events.get(i);
				assertNotNull("event was given an id", evt.getId());
				assertTrue("log contains id", actual.contains("id=\"" + evt.getId() + "\""));
				
				BehaviorEvent parent = evt.getParent();
				if (parent == null)
					assertFalse("log does not contain parent id", actual.contains("parent-id"));
				else
					assertTrue("log contains parent reference", actual.contains("parent-id=\"" + parent.getId() + "\""));
				
				assertTrue("log records time correctly", actual.contains("<start>" + XS_DATE_FORMAT.format(evt.getStart()) + "</start>"));
				assertTrue("log records duration correctly", actual.contains("<duration-ms>" + evt.getDuration() + "</duration-ms>"));
			}
		}
	}
	
	/**
	 * String time-specific data out of event logging so that we can make useful assertions.
	 */
	private static String stripVariableData(String logLine) {
		logLine = logLine.replaceAll("(?:parent-)?id=\".+?\"", "");
		logLine = logLine.replaceAll("<start>.+?</start>", "");
		logLine = logLine.replaceAll("<duration-ms>.+?</duration-ms>", "");
		return logLine;
	}
}
