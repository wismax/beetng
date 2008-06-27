package com.mtgi.analytics;

import static com.mtgi.analytics.Log4jBehaviorEventLayout.XS_DATE_FORMAT;
import static org.apache.commons.io.IOUtils.readLines;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Log4jBehaviorEventPersisterTest {

	private Log4jBehaviorEventPersisterImpl persister;

	private WriterAppender appender;
	private StringWriter buffer;
	
	@Before
	public void setUp() throws XMLStreamException {
		
		//configure the logger with the custom Layout and a special appender that will help us monitor the results.
		Logger logger = Logger.getLogger(Log4jBehaviorEventPersisterTest.class);
		buffer = new StringWriter();
		appender = new WriterAppender(new Log4jBehaviorEventLayout(), buffer);
		appender.setImmediateFlush(true);
		appender.setLayout(new Log4jBehaviorEventLayout());
		appender.activateOptions();
		logger.addAppender(appender);
		
		//initialize the persister to point to the test logger.
		persister = new Log4jBehaviorEventPersisterImpl();
		persister.setCategory(Log4jBehaviorEventPersisterTest.class.getName());
	}

	@After
	public void tearDown() {
		persister = null;

		Logger logger = Logger.getLogger(Log4jBehaviorEventPersisterTest.class);
		logger.removeAppender(appender);
		
		appender = null;
		buffer = null;
	}
	
	@Test
	public void testEmptyQueue() {
		assertEquals("no events to persist", 0, persister.persist(new LinkedList<BehaviorEvent>()));
		buffer.flush();
		assertEquals("no events written to log", 0, buffer.toString().length());
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
		
		//now perform verification of log data against the expected results.
		buffer.flush();
		List<String> actualLines = (List<String>)readLines(new StringReader(buffer.toString()));
		assertEquals("every event was written", 39, actualLines.size());

		//flatten the event data tree into an ordered list, as we expect to find it in the log.
		for (int i = 0; i < events.size(); ++i)
			events.addAll(events.get(i).getChildren());

		//read up the expected results for comparison.
		InputStream expectedData = Log4jBehaviorEventPersisterTest.class.getResourceAsStream("Log4jBehaviorEventPersisterTest.testNestedEvents-result.xml");
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
