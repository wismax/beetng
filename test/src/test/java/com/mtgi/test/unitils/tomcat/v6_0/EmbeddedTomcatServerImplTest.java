package com.mtgi.test.unitils.tomcat.v6_0;

import static com.mtgi.test.unitils.tomcat.EmbeddedTomcatManager.urlToFile;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class EmbeddedTomcatServerImplTest {

	private EmbeddedTomcatServerImpl server;
	private WebClient webClient;

	@Before
	public void setUp() throws Exception {
		server = new EmbeddedTomcatServerImpl();
		webClient = new WebClient();
	}
	
	@After
	public void tearDown() throws Exception {
		if (server != null) 
			try {
				server.destroy();
			} finally {
				server = null;
				webClient = null;
			}
	}
	
	@Test
	public void testLifecycle() throws Exception {
		
		assertFalse("server is not started after creation", server.isStarted());
		try {
			server.getHttpPort();
			fail("cannot get server port until the server is running");
		} catch (IllegalStateException expected) {}
		
		server.start();

		assertTrue("server is running now", server.isStarted());
		int port = server.getHttpPort();
		
		//verify that we can make a connection
		Socket conn = new Socket("localhost", port);
		conn.close();

		server.destroy();
		assertFalse("server is stopped again", server.isStarted());
		
		try {
			server.getHttpPort();
			fail("cannot get server port after shutdown");
		} catch (IllegalStateException expected) {}
		
		try {
			new Socket("localhost", port);
			fail("server should have been destroyed");
		} catch (IOException expected) {}
		
		//run through lifecycle again to verify we can start/stop several times
		server.start();
		assertTrue("server is running again", server.isStarted());
		
		conn = new Socket("localhost", server.getHttpPort());
		conn.close();

		server.destroy();
		assertFalse("server is stopped again", server.isStarted());
		
		try {
			server.getHttpPort();
			fail("cannot get server port after shutdown");
		} catch (IllegalStateException expected) {}
		
		try {
			new Socket("localhost", port);
			fail("server should have been destroyed");
		} catch (IOException expected) {}
	}
	
	@Test
	public void testDeployDescriptor() throws Exception {
		server.deployDescriptor("/test", urlToFile(getClass().getResource("EmbeddedTomcatServerImplTest.testDeployDescriptor-web.xml")));
		server.start();
		
	    Page result = webClient.getPage("http://localhost:" + server.getHttpPort() + "/test/ping");
	    assertEquals("servlet was hit successfully", "PING", result.getWebResponse().getContentAsString());
	    
	    //verify that deployments do *not* persist across restarts
	    server.destroy();
	    server.start();
	    try {
	    	webClient.getPage("http://localhost:" + server.getHttpPort() + "/test/ping");
	    	fail("test app should have been undeployed on shutdown");
	    } catch (FailingHttpStatusCodeException expected) {
	    	assertEquals("context should have been undeployed", 404, expected.getStatusCode());
	    }
	}
	
	public static class PingServlet extends HttpServlet {

		private static final long serialVersionUID = -6427195340620769344L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.getWriter().print("PING");
		}
		
	}
	
}
