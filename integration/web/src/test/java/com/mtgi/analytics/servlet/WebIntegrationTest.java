package com.mtgi.analytics.servlet;

import static com.mtgi.test.unitils.tomcat.EmbeddedTomcatManager.getDeployableResource;
import static com.mtgi.test.util.IOUtils.createTempDir;
import static com.mtgi.test.util.IOUtils.delete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.unitils.thirdparty.org.apache.commons.io.FileUtils.copyDirectory;
import static org.unitils.thirdparty.org.apache.commons.io.FileUtils.copyFileToDirectory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorEventPersister;
import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public class WebIntegrationTest {

	@EmbeddedTomcat
	private EmbeddedTomcatServer server;
	private File appDir;
	private TestLoader classLoader;

	@Before
	public void setUp() throws Exception {
		
		//locate beet-web archive
	    File targetDir = new File("target");
	    File[] jars = targetDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("beet-web-") && name.endsWith(".jar");
            }
        });
	    assertEquals("one beet-web-*.jar file found in target directory", 1, jars.length);
		File library = jars[0];
		assertTrue("found beet-web archive in local build directory " + library.getAbsolutePath(), library.isFile());

		//install test class loader to filter out global beet configuration files, simulating the situation
		//where beet.jar is in some shared directory
		classLoader = TestLoader.push();

		//create exploded deployment dir, including the beet-web module in WEB-INF/lib
		appDir = createTempDir("webIntegrationTest");
		File src = getDeployableResource(WebIntegrationTest.class, "webIntegrationTest");
		copyDirectory(src, appDir);

		File libDir = new File(appDir, "WEB-INF/lib");
		assertTrue(libDir.mkdirs());
		copyFileToDirectory(library, libDir);

		//add exploded dir to server and start up
		server.deployExploded(appDir);
		server.start();
	}
	
	@After
	public void tearDown() throws Exception {
		TestPersister.LAST = null;
		try {
			if (server != null)
				server.destroy();
		} finally {
			if (appDir != null)
				delete(appDir);
			appDir = null;
			server = null;
			if (classLoader != null)
				classLoader.pop();
			classLoader = null;
		}
	}
	
	@Test
	public void testRegisterListener() throws Exception {
		WebClient client = new WebClient();
		String timestamp = String.valueOf(System.currentTimeMillis());
		Page page = client.getPage("http://localhost:" + server.getHttpPort() + "/" + appDir.getName() + "/test/" + timestamp);
		assertEquals("servlet hit successfully", "PING", page.getWebResponse().getContentAsString());
		TestPersister.waitForEvents();
		
		BehaviorEvent event = TestPersister.LAST.persisted.get(0);
		assertEquals("http-request", event.getType());
		assertEquals("beet-web-test", event.getApplication());
		assertNull(event.getError());
		assertNull(event.getParent());
		assertEquals("/" + appDir.getName() + "/test/" + timestamp, event.getName());
	}
	
	public static class TestPersister implements BehaviorEventPersister {
		
		static TestPersister LAST;
		List<BehaviorEvent> persisted = new ArrayList<BehaviorEvent>();
		
		public void persist(Queue<BehaviorEvent> events) {
			synchronized (TestPersister.class) {
				LAST = this;
				persisted.addAll(events);
				if (!events.isEmpty())
					TestPersister.class.notifyAll();
			}
		}
		
		public static void waitForEvents() throws InterruptedException {
			synchronized (TestPersister.class) {
				long start = System.currentTimeMillis();
				while ((LAST == null || LAST.persisted.isEmpty()) && (System.currentTimeMillis() - start < 10000)) {
					TestPersister.class.wait(10000);
				}
			}
			assertTrue("events received", LAST != null && !LAST.persisted.isEmpty());
		}
	}
	
	/**
	 * Utility class which wraps the entire classloader hierarchy in non-URLClassLoader instances.
	 * This effectively prevents Tomcat from scanning the system classpath for TLD files, so that
	 * beet.jar cannot automatically register its event listener.
	 * 
	 * This simulates the condition where beet.jar is found in a custom application classloader (like
	 * WebLogic APP-INF/lib).
	 */
	public static class TestLoader extends ClassLoader {

		public static TestLoader push() {
			ClassLoader parent = Thread.currentThread().getContextClassLoader();
			TestLoader loader = wrap(parent);
			Thread.currentThread().setContextClassLoader(loader);
			return loader;
		}
		
		private static TestLoader wrap(ClassLoader loader) {
			if (loader == null || loader instanceof TestLoader)
				return (TestLoader)loader;
			return new TestLoader(wrap(loader.getParent()), loader);
		}
		
		public void pop() {
			Thread.currentThread().setContextClassLoader(getDelegate());
		}
		
		private ClassLoader delegate;
		
		public TestLoader(ClassLoader parent, ClassLoader delegate) {
			super(parent);
			this.delegate = delegate;
		}
		
		public ClassLoader getDelegate() {
			return delegate;
		}

		public void clearAssertionStatus() {
			delegate.clearAssertionStatus();
		}

		public URL getResource(String name) {
			return delegate.getResource(name);
		}

		public InputStream getResourceAsStream(String name) {
			return delegate.getResourceAsStream(name);
		}

		public Enumeration<URL> getResources(String name) throws IOException {
			return delegate.getResources(name);
		}

		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return delegate.loadClass(name);
		}

		public void setClassAssertionStatus(String className, boolean enabled) {
			delegate.setClassAssertionStatus(className, enabled);
		}

		public void setDefaultAssertionStatus(boolean enabled) {
			delegate.setDefaultAssertionStatus(enabled);
		}

		public void setPackageAssertionStatus(String packageName,
				boolean enabled) {
			delegate.setPackageAssertionStatus(packageName, enabled);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			return delegate.loadClass(name);
		}

		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			return delegate.getResources(name);
		}

	}
	
	public static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 2471997577714152932L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.getWriter().print("PING");
		}
		
	}
	
}
