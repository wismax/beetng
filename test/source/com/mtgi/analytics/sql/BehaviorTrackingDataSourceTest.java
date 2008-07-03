package com.mtgi.analytics.sql;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.JdbcEventTestCase;
import com.mtgi.analytics.MockSessionContext;

@SpringApplicationContext("com/mtgi/analytics/sql/BehaviorTrackingDataSourceTest-applicationContext.xml")
public class BehaviorTrackingDataSourceTest extends JdbcEventTestCase {

	@SpringBeanByType
	private MockSessionContext sessionContext;
	
	@SpringBeanByType
	private BehaviorTrackingManagerImpl manager;
	
	@Before
	public void initTestTable() throws SQLException{
		sessionContext.setContextUserId("dbUser");
		sessionContext.setContextSessionId("ABCDEF123");
		stmt.execute("create table TEST_TRACKING (" +
				"	ID			NUMERIC(8) 		PRIMARY KEY," +
				"	NAME		VARCHAR(16)		NOT NULL," +
				"	DESCRIPTION	VARCHAR(32) 	NULL" +
				")");
		
		//because we've put behavior tracking at the DataSource level, all of our
		//test setup will have generated a bunch of behavior tracking events.
		//so we extract an untracked connection
		//and delete all of those events before the test starts.  it's much easier
		//to validate the data this way.
		manager.flush();
		Statement cleanup = ((ConnectionProxy)conn).getTargetConnection().createStatement();
		cleanup.execute(
			"delete from BEHAVIOR_TRACKING_EVENT; " +
			"drop sequence S_BEHAVIOR_TRACKING_EVENT; " +
			"create sequence S_BEHAVIOR_TRACKING_EVENT; "
		);
		cleanup.close();
	}
	
	@After
	public void dropTestTable() throws SQLException {
		sessionContext.reset();
		stmt.execute("drop table TEST_TRACKING");
	}
	
	@Test
	public void testStaticSql() throws Exception {
		//test simple static insert, update, deletes.
		assertFalse(stmt.execute("insert into TEST_TRACKING values (1, 'hello', null)"));
		assertFalse(stmt.execute("insert into TEST_TRACKING values (2, 'goodbye', null)"));
		assertEquals(2, stmt.executeUpdate("update TEST_TRACKING set DESCRIPTION = 'world'"));
		
		ResultSet rs = stmt.executeQuery("select ID from TEST_TRACKING order by ID");
		int index = 0;
		long[] keys = { 1L, 2L };
		while (rs.next())
			assertEquals(keys[index++], rs.getLong(1));
		rs.close();
		assertEquals(2, index);
		
		manager.flush();
		assertEventDataMatches("BehaviorTrackingDataSourceTest.testStaticSql-result.xml");
	}
	
	@Test
	public void testPreparedStatement() throws Exception {
		//test tracking through the prepared statement API, which should also
		//log parameters in the events.
		PreparedStatement stmt = conn.prepareStatement("insert into TEST_TRACKING values (?, ?, ?)");
		stmt.setLong(1, 1);
		stmt.setString(2, "hello");
		stmt.setObject(3, null, Types.VARCHAR);
		assertEquals(1, stmt.executeUpdate());

		stmt.setLong(1, 2);
		stmt.setObject(2, "goodbye", Types.VARCHAR);
		stmt.setNull(3, Types.VARCHAR);
		assertEquals(1, stmt.executeUpdate());
		
		stmt = conn.prepareStatement("update TEST_TRACKING set DESCRIPTION = 'world'");
		assertEquals(2, stmt.executeUpdate());

		stmt = conn.prepareStatement("select ID from TEST_TRACKING order by ID");
		ResultSet rs = stmt.executeQuery();
		int index = 0;
		long[] keys = { 1L, 2L };
		while (rs.next())
			assertEquals(keys[index++], rs.getLong(1));
		rs.close();
		assertEquals(2, index);
		
		manager.flush();
		assertEventDataMatches("BehaviorTrackingDataSourceTest.testPreparedStatement-result.xml");
	}

	@Test
	public void testExceptionHandling() throws Exception {
		try {
			stmt.execute("select BARF from VOID where DEATH RULES order by CHAOS");
			fail("sql exception should have been raised");
		} catch (SQLException expected) {
		}
		manager.flush();
		assertEventDataMatches("BehaviorTrackingDataSourceTest.testExceptionHandling-result.xml");
	}
}
