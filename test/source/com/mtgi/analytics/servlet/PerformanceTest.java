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
 
package com.mtgi.analytics.servlet;

import static org.junit.Assert.*;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.NameValuePair;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.SubmitMethod;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.mtgi.analytics.AbstractPerformanceTestCase;
import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedDeploy;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
@EmbeddedTomcat(start=true)
@EmbeddedDeploy(contextRoot="/app", value="com/mtgi/analytics/servlet/PerformanceTest-web.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class PerformanceTest extends AbstractPerformanceTestCase {

	@EmbeddedTomcat
	protected EmbeddedTomcatServer server;
	
	public PerformanceTest() {
		super(1, 1, 100); //each test job generates one BT event.
	}
	
	@After
	public void destroyContext() {
		server = null;
	}
	
	@Test
	public void testPerformance() throws Throwable {
		TestJob basisJob = new TestJob("/basis/ping");
		TestJob testJob = new TestJob("/test/ping");
		testPerformance(basisJob, testJob);
	}
	
	public static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 7753583670712327245L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			String count = req.getParameter("count");
			resp.setContentType("text/plain");
			resp.getWriter().print("success[" + count + "]");
		}
		
	}
	
	public static class TestJob implements Runnable {
		
		private volatile int iteration = 0;
		private URL url;
		private WebClient webClient;
		private WebRequestSettings request;
		private NameValuePair countParam;
		
		public TestJob(String servletPath) throws MalformedURLException {
			url = new URL("http://localhost:8888/app" + servletPath);
			webClient = new WebClient();
			request = new WebRequestSettings(url);
			request.setEncodingType(FormEncodingType.URL_ENCODED);
			request.setSubmitMethod(SubmitMethod.POST);
			NameValuePair[] parameters = {
					new NameValuePair("param1", "hello"),
					new NameValuePair("param1", "world"),
					new NameValuePair("param2", String.valueOf(System.currentTimeMillis())),
					new NameValuePair("param3", "72<"),
					countParam = new NameValuePair("count", "")
			};
			request.setRequestParameters(asList(parameters));
		}

		public void run() {
			String count = String.valueOf(++iteration);
			countParam.setValue(count);
			try {
				TextPage result = (TextPage)webClient.getPage(request);
				assertEquals("servlet loaded successfully", "success[" + count + "]", result.getContent());
			} catch (Throwable e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
}
