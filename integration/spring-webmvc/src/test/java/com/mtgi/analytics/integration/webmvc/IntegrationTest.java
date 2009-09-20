package com.mtgi.analytics.integration.webmvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.unitils.UnitilsJUnit4TestClassRunner;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorEventPersister;
import com.mtgi.test.unitils.tomcat.annotations.DeployExploded;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public class IntegrationTest {

	@EmbeddedTomcat(start=true) 
	private String serverAddress;
	
	@Test @DeployExploded("integrationTest")
	public void testEvents() throws Exception {

		WebClient client = new WebClient();
		String timestamp = String.valueOf(System.currentTimeMillis());
		Page page = client.getPage(serverAddress + "/integrationTest/test/ping/" + timestamp);
		assertEquals("servlet hit successfully", "PING", page.getWebResponse().getContentAsString());
		TestPersister.waitForEvents();
		
		BehaviorEvent event = TestPersister.LAST.persisted.get(0);
		assertEquals("http-request", event.getType());
		assertEquals("beet-webmvc-test", event.getApplication());
		assertNull(event.getError());
		assertNull(event.getParent());
		assertEquals("/integrationTest/test/ping/" + timestamp, event.getName());
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
	
	public static class TestController implements Controller {
		public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.getWriter().print("PING");
			return null;
		}
	}
	
}
