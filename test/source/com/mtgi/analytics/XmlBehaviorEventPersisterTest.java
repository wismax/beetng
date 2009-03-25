package com.mtgi.analytics;

import static org.apache.commons.io.IOUtils.readLines;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mtgi.io.RelocatableFile;
import com.sun.xml.fastinfoset.tools.FI_SAX_XML;

public class XmlBehaviorEventPersisterTest {

	private static final Pattern DATE_PATTERN = Pattern.compile("<start>\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\+|-\\d\\d:\\d\\d</start>");
	
	private XmlBehaviorEventPersisterImpl persister;
	private File file;
	
	@Before
	public void setUp() throws Exception {
		file = File.createTempFile("perf", ".bxml");
		file.delete();
		//initialize the persister to point to the test logger.
		persister = new XmlBehaviorEventPersisterImpl();
		persister.setFile(file.getAbsolutePath());
		persister.afterPropertiesSet();
		file = new File(persister.getFile());
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
			JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter, events);
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);

		assertEquals("entire event tree persisted", 39, persister.persist(queue));
		assertEquals("persister reports correct file size", file.length(), persister.getFileSize());
		
		//now perform verification of log data against the expected results.
		List<String> actualLines = (List<String>)FileUtils.readLines(file);
		assertEquals("every event was written", 39, actualLines.size());

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
			
			assertTrue("log records time correctly", DATE_PATTERN.matcher(actual).find());
			assertTrue("log records duration correctly", actual.contains("<duration-ms>" + evt.getDuration() + "</duration-ms>"));
		}
	}
	
	@Test @SuppressWarnings("unchecked")
	public void testCompress() throws Exception {
		persister.setBinary(true);
		persister.setCompress(true);
		persister.rotateLog();
		file = new File(persister.getFile());
		assertTrue("persister switched to binary", persister.isBinary());
		
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter, events);
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);

		assertEquals("entire event tree persisted", 39, persister.persist(queue));
		assertEquals("persister reports correct file size", file.length(), persister.getFileSize());
		
		//unzip and convert binary to flat xml for comparison.  this also verifies that 'rotateLog'
		//leaves a well-formed XML document in its wake.
		String path = persister.rotateLog();
		File source = new File(path);
		assertTrue("log archive data exists", source.isFile());
		assertFalse("archive does not point to active log", source.getCanonicalPath().equals(file.getCanonicalPath()));
		
		File dest = File.createTempFile("perf", ".xml");
		dest.deleteOnExit();
		
		InputStream fis = new GZIPInputStream(new FileInputStream(source));
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
				
				assertTrue("log records time correctly", DATE_PATTERN.matcher(actual).find());
				assertTrue("log records duration correctly", actual.contains("<duration-ms>" + evt.getDuration() + "</duration-ms>"));
			}
		}
	}
	
	@Test @SuppressWarnings("unchecked")
	public void testBinaryXml() throws Exception {
		
		//switch to binary format.
		persister.setBinary(true);
		persister.rotateLog();
		file = new File(persister.getFile());
		assertTrue("persister switched to binary", persister.isBinary());
		
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter, events);
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
				
				assertTrue("log records time correctly", DATE_PATTERN.matcher(actual).find());
				assertTrue("log records duration correctly", actual.contains("<duration-ms>" + evt.getDuration() + "</duration-ms>"));
			}
		}
	}
	
	/** 
	 * if the persister was not shut down cleanly, we might have an old log file without a timestamp laying around.
	 * make sure log rotation doesn't clobber it.
	 */
	@Test
	public void testOfflineConfigurationChange() throws Exception {
		
		//generate an old log file with some data, *not* the one currently referenced by our persister.
		File staleFile = new File(persister.getFile() + ".gz");
		staleFile.deleteOnExit();
		assertFalse("sanity check: " + staleFile.getAbsolutePath() + " does not exist", staleFile.exists());
		
		byte[] data = { 0xC, 0xA, 0xF, 0xE, 0xB, 0xA, 0xB, 0xE };
		FileOutputStream fos = new FileOutputStream(staleFile);
		try {
			fos.write(data);
			fos.flush();
			fos.getFD().sync();
		} finally {
			IOUtils.closeQuietly(fos);
		}
		assertEquals("sanity check: stale log file has content", 8, staleFile.length());
		assertFalse("sanity check: persister currently points to different location", staleFile.equals(new File(persister.getFile())));

		//change logging config, such that the next log rotation would cause us to clobber the
		//pre-existing file 'staleFile'.  staleFile should be preserved in a new location with a timestamp
		//suffix.
		persister.setCompress(true);
		persister.rotateLog();
		File newPath = new File(persister.getFile());
		assertEquals("persister now points to stale log file location after rotate", staleFile, newPath);

		//we should be able to locate a backup file in the parent dir somewhere.
		final String backupPrefix = staleFile.getName();
		class BackupLocator implements FilenameFilter {
			File backup = null;
			int matchCount = 0;

			public boolean accept(File dir, String name) {
				if (name.length() > backupPrefix.length() && name.startsWith(backupPrefix)) {
					++matchCount;
					backup = new File(dir, name);
				}
				return false;
			}
		};
		BackupLocator locator = new BackupLocator();
		File dir = staleFile.getParentFile();
		dir.listFiles(locator);
		
		assertEquals("exactly one backup log file found", 1, locator.matchCount);
		//verify backup contents
		FileInputStream fis = new FileInputStream(locator.backup);
		try {
			for (int i = 0; i < data.length; ++i)
				assertEquals("@" + i + " matches", data[i], (byte)fis.read());
			assertEquals("no data remains", -1, fis.read());
		} finally {
			IOUtils.closeQuietly(fis);
			locator.backup.delete();
		}
	}
	
	@Test
	public void testListLogFiles() throws Exception {
		//switch to binary format.
		persister.setBinary(true);
		file = new File(persister.rotateLog());
		
		//rotate again so that we have multiple matching files, with different extensions.
		String path = persister.rotateLog();
		File file2 = new File(path);

		//test list wrapper.
		StringBuffer fileData = persister.listLogFiles();
		assertNotNull("file list returned", fileData);

		String data = fileData.toString();
		int offset = data.indexOf(file.getName());
		assertTrue("row for first file found", offset > 0);
		String rowData = "<tr><td>" + file.getName() + "</td><td>" + file.length() + "</td><td>" + new Date(file.lastModified()) + "</td></tr>";
		assertEquals(rowData, data.subSequence(offset - 8, offset - 8 + rowData.length()));

		offset = data.indexOf(file2.getName());
		rowData = "<tr><td>" + file2.getName() + "</td><td>" + file2.length() + "</td><td>" + new Date(file2.lastModified()) + "</td></tr>";
		assertEquals(rowData, data.subSequence(offset - 8, offset - 8 + rowData.length()));
		
		//test download
		RelocatableFile data0 = persister.downloadLogFile(file.getName());
		assertNotNull("file data found", data0);
		assertEquals(file, data0.getLocalFile());
		
		RelocatableFile data1 = persister.downloadLogFile(file2.getName());
		assertNotNull("file data found", data1);
		assertEquals(file2, data1.getLocalFile());
		
		//delete a file and verify that it disappears from the list.
		file2.delete();
		fileData = persister.listLogFiles();
		assertNotNull("file list returned", fileData);
		data = fileData.toString();
		assertTrue("first file still in list", data.indexOf(file.getName()) > 0);
		assertTrue("second file is gone", data.indexOf(file2.getName()) < 0);
		
		try {
			persister.downloadLogFile(file2.getName());
			fail("attempt to download missing file should fail");
		} catch (FileNotFoundException expected) {}
		
		data0 = persister.downloadLogFile(file.getName());
		assertNotNull("existing file data found", data0);
		assertEquals(file, data0.getLocalFile());
		
		try {
			persister.downloadLogFile("junk.junk");
			fail("attempt to access illegal file should fail");
		} catch (IllegalArgumentException expected) {}
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
