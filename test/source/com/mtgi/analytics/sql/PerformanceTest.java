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
		super(3); //each test job generates three BT events.
	}
	
	@TestDataSource
	private DataSource dataSource;
	
	@Before
	public void initTestTable() throws SQLException{
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		stmt.execute("create table TEST_TRACKING (" +
				"	ID			NUMERIC(8) 		PRIMARY KEY," +
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
	}
	
	@Test
	public void testPerformance() throws Throwable {
		ClassPathXmlApplicationContext context = 
			new ClassPathXmlApplicationContext("com/mtgi/analytics/sql/PerformanceTest-tracking.xml");
		
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
					PreparedStatement ps = conn.prepareStatement("insert into TEST_TRACKING values (?, ?, ?)");
					long s1 = SEQ++, s2 = SEQ++;
					ps.setLong(1, s1);
					ps.setString(2, "foo" + SEQ);
					ps.setString(3, "adesc");
					ps.executeUpdate();
					
					ps.setLong(1, s2);
					ps.setString(2, "bar" + SEQ);
					ps.setString(3, "bdesc");
					ps.executeUpdate();
					ps.close();
					
					Statement s = conn.createStatement();
					s.executeUpdate("delete from TEST_TRACKING where ID in (" + s1 + "," + s2 + ")");
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
