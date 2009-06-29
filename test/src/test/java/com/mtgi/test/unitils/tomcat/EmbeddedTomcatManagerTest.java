package com.mtgi.test.unitils.tomcat;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

public class EmbeddedTomcatManagerTest {

	private EmbeddedTomcatManager manager;

	@Before
	public void setUp() {
		manager = new EmbeddedTomcatManager();
	}
	
	@After
	public void tearDown() {
		manager.invalidateAll();
		manager = null;
	}
	
	@Test
	public void testAnnotatedTestClass() throws Exception {
		AnnotatedClass testCase = new AnnotatedClass();
		assertFalse("no instances yet configured", manager.isServerInitialized(testCase));
		
		EmbeddedTomcatServer server = manager.getInstance(testCase);
		assertNotNull("instance created on demand", server);
		assertTrue("instance has correct version", server instanceof com.mtgi.test.unitils.tomcat.v6_0.EmbeddedTomcatServerImpl);
		assertFalse("instance not yet started", server.isStarted());
		
		File homeDir = server.getCatalinaHome();
		assertTrue("server home directory assigned", homeDir.isDirectory());
		assertEquals("home directory is in tmp", new File(System.getProperty("java.io.tmpdir")), homeDir.getParentFile());
		
		server.start();
		assertTrue("instance is now started", server.isStarted());
		
		assertTrue("manager now reports configured instance", manager.isServerInitialized(testCase));
		assertSame("instance is cached between invocations", server, manager.getInstance(testCase));
		assertTrue("instance still running", server.isStarted());
		
		manager.invalidateInstance(AnnotatedClass.class);
		
		assertFalse("instance has been removed", manager.isServerInitialized(testCase));		
		assertFalse("server has been shut down", server.isStarted());
		
		assertFalse("home directory has been deleted", homeDir.exists());
	}
	
	@Test
	public void testUnannotatedClass() {
		UnannotatedClass testCase = new UnannotatedClass();
		assertFalse("no instances configured", manager.isServerInitialized(testCase));
		assertNull("no instance created", manager.getInstance(testCase));
	}

	public static class UnannotatedClass {}
	
	@EmbeddedTomcat
	public static class AnnotatedClass {}
	
}
