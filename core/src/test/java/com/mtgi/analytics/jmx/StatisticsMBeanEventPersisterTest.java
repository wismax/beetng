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
 
package com.mtgi.analytics.jmx;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedDeploy;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

@EmbeddedTomcat(start=true)
@EmbeddedDeploy(value="com/mtgi/analytics/jmx/testApp")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class StatisticsMBeanEventPersisterTest {

	private WebClient webClient;
	
	@EmbeddedTomcat
	protected EmbeddedTomcatServer server;

	@TestDataSource
	private DataSource db;
	private Connection conn;
	
	@Before
	public void setUp() throws SQLException {
		webClient = new WebClient();
		conn = db.getConnection();
	}
	
	@After
	public void tearDown() throws SQLException {
		webClient = null;
		if (conn != null)
			conn.close();
		conn = null;
	}

	@Test
	public void testMbeanRegistration() 
		throws FailingHttpStatusCodeException, IOException, SQLException, JMException, InterruptedException 
	{
		ObjectName[] all = { 
			ObjectName.getInstance(
					"testApp:type=http-request-monitor,name=/testApp/test/invoke.do"),
			ObjectName.getInstance(
					"testApp:type=http-request-monitor,name=/testApp/test/invoke.do," +
					"nested=method_com.mtgi.analytics.jmx.StatisticsMBeanEventPersisterTest$Service.save"),
			ObjectName.getInstance(
					"testApp:type=http-request-monitor,name=/testApp/test/invoke.do," +
					"nested=method_com.mtgi.analytics.jmx.StatisticsMBeanEventPersisterTest$Service.save," +
					"nested[2]=jdbc_executeBatch")
		};
		
		MBeanServer jmx = JmxUtils.locateMBeanServer();
		for (ObjectName on : all)
			assertFalse("statistics mbean " + on + " is not yet registered", jmx.isRegistered(on));
		
		assertEquals("no data in test db yet", 0, countRecords());
		webClient.getPage("http://localhost:8888/testApp/test/invoke.do?id=1&value=hello");
		assertEquals("test service invoked", 1, countRecords());
		
		//verify that we receive updated statistics for each of our monitored events
		for (ObjectName on : all)
			waitForCount(on, 1);
		
		//post again, and verify that the now-registered mbeans are updated.
		webClient.getPage("http://localhost:8888/testApp/test/invoke.do?id=2&value=world");
		assertEquals("test service invoked", 2, countRecords());
		for (ObjectName on : all)
			waitForCount(on, 2);
	}

	private void waitForCount(ObjectName id, int count) throws JMException, InterruptedException {
		MBeanServer jmx = JmxUtils.locateMBeanServer();
		long start = System.currentTimeMillis();
		int actual = -1;
		do {
			if (jmx.isRegistered(id))
				actual = ((Number)jmx.getAttribute(id, "Count")).intValue();
			if (actual < count)
				Thread.sleep(10);
		} while (actual < count && (System.currentTimeMillis() - start) < 300000);
		assertEquals("events received for " + id, actual, count);
	}
	
	private int countRecords() throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("select count(*) from Data");
			try {
				rs.next();
				return rs.getInt(1);
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}
	
	public static class Controller implements org.springframework.web.servlet.mvc.Controller {

		private Service service;
		
		public void setService(Service service) {
			this.service = service;
		}

		public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
			service.save(new Long(request.getParameter("id")), request.getParameter("value"));
			return null;
		}
		
	}

	public static class Service {
		
		private HibernateTemplate dao;

		public void setDao(HibernateTemplate dao) {
			this.dao = dao;
		}

		public void save(Long id, String value) {
			dao.save(new Data(id, value));
		}
	}

	@Entity @Table(name="Data")
	public static class Data {

		@Id public Long key;
		@Column public String value;
		
		public Data() {}
		public Data(Long key, String value) {
			this.key = key;
			this.value = value;
		}
		
	}
}
