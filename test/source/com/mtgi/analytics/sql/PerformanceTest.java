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
 
package com.mtgi.analytics.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;

import com.mtgi.analytics.AbstractPerformanceTestCase;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class PerformanceTest extends AbstractPerformanceTestCase {

	public PerformanceTest() {
		super(5); //each test job generates five BT events.
	}
	
	@TestDataSource
	private DataSource dataSource;
	
	private ClassPathXmlApplicationContext context;
	
	@Before
	public void initTestTable() throws SQLException{
		context = new ClassPathXmlApplicationContext("com/mtgi/analytics/sql/PerformanceTest-tracking.xml");
		
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute("create table TEST_TRACKING (" +
				"	ID			IDENTITY," +
				"	NUM			NUMERIC(16)		NOT NULL," +
				"	NAME		VARCHAR(16)		NOT NULL," +
				"	DESCRIPTION	VARCHAR(32) 	NULL" +
				")");
		stmt.close();
		conn.close();
	}

	@After
	public void dropTestTable() throws SQLException {
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute("drop table TEST_TRACKING");
		stmt.close();
		conn.close();
		
		context.destroy();
		context = null;
	}
	
	@Test
	public void testPerformance() throws Throwable {
		TestJob basisJob = new TestJob((DataSource)context.getBean("dataSource"));
		TestJob testJob = new TestJob((DataSource)context.getBean("instrumentedDataSource"));

		testPerformance(basisJob, testJob);
	}
	
	public static class TestJob implements Runnable {

		private static volatile long SEQ = 0;
		private DataSource ds;
		
		public TestJob(DataSource service) {
			this.ds = service;
		}

		public void run() {
			try {
				//method call results in two events logged.
				Connection conn = ds.getConnection();
				try {
					PreparedStatement ps = conn.prepareStatement("insert into TEST_TRACKING (NUM, NAME, DESCRIPTION) values (?, ?, ?)");
					long s1 = ++SEQ, s2 = ++SEQ, s3 = ++SEQ, s4 = ++SEQ;
					ps.setLong(1, s1);
					ps.setString(2, "foo" + SEQ);
					ps.setString(3, "adesc");
					ps.executeUpdate();
					
					ps.setLong(1, s2);
					ps.setString(2, "bar" + SEQ);
					ps.setString(3, "bdesc");
					ps.executeUpdate();
					
					ps.setLong(1, s3);
					ps.setString(2, "bar" + SEQ);
					ps.setString(3, "bdesc");
					ps.addBatch();
					
					ps.setLong(1, s4);
					ps.setString(2, "bar" + SEQ);
					ps.setString(3, "bdesc");
					ps.addBatch();
					ps.executeBatch();
					ps.close();
					
					Statement s = conn.createStatement();
					s.executeUpdate("delete from TEST_TRACKING where ID in (" + s1 + "," + s2 + ")");
					
					s.addBatch("delete from TEST_TRACKING where ID = " + s3);
					s.addBatch("delete from TEST_TRACKING where ID = " + s4);
					s.executeBatch();
					s.close();

				} finally {
					conn.close();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
