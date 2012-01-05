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

import org.junit.Ignore;
import org.junit.Test;

import com.mtgi.analytics.test.AbstractPerformanceTestCase;
import com.mtgi.analytics.test.AbstractSpringTestCase;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public class PerformanceTest extends AbstractPerformanceTestCase {

	//our test case actually generates 5 events, so the cumulative overhead
	//is somewhat higher than what is allowed in the AOP perf test.
	private static final long AVERAGE_OVERHEAD_NS = 400000;
	private static final long WORST_OVERHEAD_NS = 100000;
	
	private static final long TIME_BASIS = 450000;
	
	private static final String[] BASIS_CONFIG = { 
		"com/mtgi/analytics/sql/PerformanceTest-basis.xml" 
	};
	private static final String[] TEST_CONFIG = { 
		"com/mtgi/analytics/sql/PerformanceTest-basis.xml",
		"com/mtgi/analytics/sql/PerformanceTest-tracking.xml"
	};
	
	public PerformanceTest() {
		super(10, 50, TIME_BASIS, AVERAGE_OVERHEAD_NS, WORST_OVERHEAD_NS); //each test job generates 6 BT events.
	}
	
	@Test
    @Ignore
	public void testPerformance() throws Throwable {
		TestJob basisJob = new TestJob("dataSource", BASIS_CONFIG);
		TestJob testJob = new TestJob("instrumentedDataSource", TEST_CONFIG);
		testPerformance(basisJob, testJob);
	}
	
	public static class TestJob extends AbstractSpringTestCase<DataSource> {

		private static final long serialVersionUID = 930701941418132948L;
		private static volatile long SEQ = 0;
		
		public TestJob(String beanName, String[] configFiles) {
			super(beanName, DataSource.class, configFiles);
		}

		@Override
		public void setUp() throws Throwable {
			super.setUp();
			Connection conn = bean.getConnection();
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

		public void run() {
			try {
				//method call results in two events logged.
				Connection conn = bean.getConnection();
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
