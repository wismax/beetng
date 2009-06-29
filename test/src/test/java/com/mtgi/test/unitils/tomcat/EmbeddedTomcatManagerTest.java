package com.mtgi.test.unitils.tomcat;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.mtgi.test.unitils.tomcat.annotations.DeployDescriptor;
import com.mtgi.test.unitils.tomcat.annotations.DeployExploded;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

public class EmbeddedTomcatManagerTest {

	private EmbeddedTomcatManager manager;
	private WebClient webClient;

	@Before
	public void setUp() {
		manager = new EmbeddedTomcatManager();
		webClient = new WebClient();
	}
	
	@After
	public void tearDown() {
		manager.invalidateAll();
		manager = null;
		webClient = null;
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
	
	@Test
	public void testConfigureDeployments() throws Exception {
		AnnotatedClass inst = new AnnotatedClass();
		
		EmbeddedTomcatServer server = manager.getInstance(inst);
		assertNotNull("server returned", server);
		assertTrue("server is configured autostart", server.isAutostart());
		assertFalse("server has not started", server.isStarted());
		
		Method method = inst.getClass().getMethod("testMethod");
		manager.configureDeployments(inst, method);
		
		assertTrue("server has been started", server.isStarted());
		
		//verify that all four requested deployments are active
		final String base = "http://localhost:" + server.getHttpPort();
		
		Page page = webClient.getPage(base + "/desc/echo?hello=a");
		assertEquals("a", page.getWebResponse().getContentAsString());
		
		page = webClient.getPage(base + "/desc2/echo?hello=b");
		assertEquals("b", page.getWebResponse().getContentAsString());
		
		page = webClient.getPage(base + "/exploded");
		assertEquals("boom", page.getWebResponse().getContentAsString());
		
		page = webClient.getPage(base + "/exploded2");
		assertEquals("pow", page.getWebResponse().getContentAsString());
	}

	public static class UnannotatedClass {}
	
	@EmbeddedTomcat(start=true)
	//test both relative and absolute classpath specifications
	@DeployDescriptor(contextRoot="/desc", webXml="com/mtgi/test/unitils/tomcat/web.xml")
	@DeployExploded("exploded")
	public static class AnnotatedClass {

		@DeployDescriptor(contextRoot="/desc2", webXml="web.xml")
		@DeployExploded("com/mtgi/test/unitils/tomcat/exploded2")
		public void testMethod() {}
	}
	
	public static class EchoServlet extends HttpServlet {

		private static final long serialVersionUID = -2945940741900800819L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.getWriter().print(req.getParameter("hello"));
		}
		
	}
	
}
