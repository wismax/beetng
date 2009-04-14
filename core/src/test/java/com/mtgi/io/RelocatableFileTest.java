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
 
package com.mtgi.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RelocatableFileTest {

	private File source;
	private RelocatableFile inst;
	
	private ObjectOutputStream output;
	private ByteArrayOutputStream buffer;
	
	@Before
	public void setUp() throws IOException {
		source = File.createTempFile("RelocatableFileTest", ".tmp");
		inst = new RelocatableFile(source);
		
		buffer = new ByteArrayOutputStream();
		output = new ObjectOutputStream(buffer);
	}
	
	@After
	public void tearDown() throws IOException {
		if (source.exists())
			assertTrue(source.delete());
		source = null;
		inst = null;
		output = null;
		buffer = null;
	}
	
	@Test
	public void testEmptyFile() throws IOException, ClassNotFoundException {
		output.writeObject(inst);
		output.close();
		
		assertTrue("original file still here", inst.getLocalFile().isFile());
		assertEquals("source file is empty", 0, inst.getLocalFile().length());
		
		RelocatableFile transferred = (RelocatableFile)new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray())).readObject();
		assertNotNull("file loaded", transferred);
		assertNotNull("local file defined", transferred.getLocalFile());
		assertTrue("local file exists", transferred.getLocalFile().isFile());
		assertFalse("reloaded file points to a new path", 
					inst.getLocalFile().getCanonicalPath().equals(transferred.getLocalFile().getCanonicalPath()));
		
		assertEquals("transferred file is empty", 0, transferred.getLocalFile().length());
		assertTrue("transferred file is closed", transferred.getLocalFile().delete());
	}

	@Test
	public void testMissingFile() throws IOException {
		assertTrue(inst.getLocalFile().delete());
		try {
			output.writeObject(inst);
			fail("attempt to write missing file should result in error");
		} catch (FileNotFoundException expected) {
		}
	}
	
	@Test
	public void testRelocate() throws IOException, ClassNotFoundException {
		FileUtils.writeStringToFile(source, "here is some data");
		
		output.writeObject(inst);
		output.close();
		
		assertTrue("original file still here", inst.getLocalFile().isFile());
		assertEquals("source file is unchanged", "here is some data", FileUtils.readFileToString(inst.getLocalFile()));
		
		RelocatableFile transferred = (RelocatableFile)new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray())).readObject();
		assertNotNull("file loaded", transferred);
		assertNotNull("local file defined", transferred.getLocalFile());
		assertTrue("local file exists", transferred.getLocalFile().isFile());
		assertFalse("reloaded file points to a new path", 
					inst.getLocalFile().getCanonicalPath().equals(transferred.getLocalFile().getCanonicalPath()));

		assertEquals("transferred file contains source data", "here is some data", FileUtils.readFileToString(transferred.getLocalFile()));
		assertTrue("transferred file is closed", transferred.getLocalFile().delete());
	}
	
}
