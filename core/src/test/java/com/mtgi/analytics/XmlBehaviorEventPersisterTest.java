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
 
package com.mtgi.analytics;

import static org.apache.commons.io.IOUtils.readLines;
import static org.custommonkey.xmlunit.XMLUnit.buildTestDocument;
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

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	public void testEmptyQueue() throws IOException {
		persister.persist(new LinkedList<BehaviorEvent>());
		assertTrue("empty event-log declaration written", 
					FileUtils.readFileToString(file)
						.matches("<\\?xml .*?\\?><event-log"));
		assertEquals(file.length(), persister.getFileSize());
		assertFalse("persister defaults to plain text format", persister.isBinary());
	}
	
	@Test
	public void testNestedEvents() throws InterruptedException, IOException, XMLStreamException, SAXException {
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		final ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter, events);
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);

		persister.persist(queue);
		assertEquals("queue unmodified by persistence operation", 39, queue.size());
		assertEquals("persister reports correct file size", file.length(), persister.getFileSize());
		
		//now perform verification of log data against the expected results.
		String fileName = persister.rotateLog();
		Document actualXML = buildTestDocument(FileUtils.readFileToString(new File(fileName)));
		
		//read up the expected results for comparison.
		InputStream expectedData = XmlBehaviorEventPersisterTest.class.getResourceAsStream("XmlBehaviorEventPersisterTest.testNestedEvents-result.xml");
		Document controlXML = buildTestDocument(new InputSource(expectedData));
		expectedData.close();
		
		XMLUnit.setIgnoreWhitespace(true);

		//compare the logged data to our expectations, ignoring time-sensitive values.
		Diff diff = new Diff(controlXML, actualXML);
		diff.overrideDifferenceListener(new DifferenceListener() {
			
			//filter out artificial differences in id, start time, duration
			public int differenceFound(Difference diff) {
				
				Node cn = diff.getControlNodeDetail().getNode();
				Node tn = diff.getTestNodeDetail().getNode();
				
				if (cn != null && tn != null) {
					short ctype = cn.getNodeType();
					short ttype = tn.getNodeType();
					if (ctype == ttype) {
						if (ctype == Node.ATTRIBUTE_NODE) {
							if (cn.getNodeName().equals("id") || cn.getNodeName().equals("parent-id")) {
								//we can at least verify that the logged ID matches the ID assigned to our data model.
								int index = -1;
								for (Node n = ((Attr)cn).getOwnerElement(); n != null; n = n.getPreviousSibling())
									if (n.getNodeType() == Node.ELEMENT_NODE)
										++index;
								BehaviorEvent event = events.get(index);
								if (cn.getNodeName().equals("id")) {
									assertEquals("logged event has same id as data model", event.getId().toString(), tn.getNodeValue());
								} else {
									BehaviorEvent parent = event.getParent();
									assertNotNull("node " + event.getId() + " has parent", parent);
									assertEquals("logged event has same parent-id as data model", parent.getId().toString(), tn.getNodeValue());
								}
								return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
							}
						} else if (ctype == Node.TEXT_NODE) {
							String cname = cn.getParentNode().getNodeName();
							String tname = tn.getParentNode().getNodeName();
							if (cname.equals(tname)) {
								//TODO: sanity check values.
								if ("duration-ns".equals(cname) || "start".equals(cname))
									return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
							}
						}
					}
				}
				
				return RETURN_ACCEPT_DIFFERENCE;
			}

			public void skippedComparison(Node n0, Node n1) {}
			
		});
		
		if (!diff.similar())
			fail(diff.appendMessage(new StringBuffer()).toString());
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

		persister.persist(queue);
		assertEquals("queue unmodified by persistence operation", 39, queue.size());
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
		for (int i = 0; i < expectedLines.size() - 1; ++i) {
			
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
			assertTrue("log records duration correctly", actual.contains("<duration-ns>" + evt.getDurationNs() + "</duration-ns>"));
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

		persister.persist(queue);
		assertEquals("queue unmodified by persistence operation", 39, queue.size());
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
				assertTrue("log records duration correctly", actual.contains("<duration-ns>" + evt.getDurationNs() + "</duration-ns>"));
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
		logLine = logLine.replaceAll("<duration-ns>.+?</duration-ns>", "");
		return logLine;
	}
}
