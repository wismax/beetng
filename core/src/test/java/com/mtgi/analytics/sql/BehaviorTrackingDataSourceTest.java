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
			"drop sequence SEQ_BEHAVIOR_TRACKING_EVENT; " +
			"create sequence SEQ_BEHAVIOR_TRACKING_EVENT; "
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
		//test simple static insert / update.
		assertFalse(stmt.execute("insert into TEST_TRACKING values (1, 'hello', null)"));

		//test batching.  each batch should create one event.
		stmt.addBatch("insert into TEST_TRACKING values (3, 'batch', '1')");
		stmt.addBatch("insert into TEST_TRACKING values (4, 'batch', '2')");
		stmt.executeBatch();

		assertFalse(stmt.execute("insert into TEST_TRACKING values (2, 'goodbye', null)"));
		assertEquals(4, stmt.executeUpdate("update TEST_TRACKING set DESCRIPTION = 'world'"));
		
		//test query.
		ResultSet rs = stmt.executeQuery("select ID from TEST_TRACKING order by ID");
		int index = 0;
		long[] keys = { 1L, 2L, 3L, 4L };
		while (rs.next())
			assertEquals(keys[index++], rs.getLong(1));
		rs.close();
		assertEquals(4, index);
		
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

		//test support for batching.  each batch should log 1 event.
		stmt.setLong(1, 3);
		stmt.setString(2, "batch");
		stmt.setObject(3, "1", Types.VARCHAR);
		stmt.addBatch();
		
		stmt.setLong(1, 4);
		stmt.setString(2, "batch");
		stmt.setObject(3, "2", Types.VARCHAR);
		stmt.addBatch();
		stmt.executeBatch();
		
		//back to a regular old update.
		stmt.setLong(1, 2);
		stmt.setObject(2, "goodbye", Types.VARCHAR);
		stmt.setNull(3, Types.VARCHAR);
		assertEquals(1, stmt.executeUpdate());
		
		stmt = conn.prepareStatement("update TEST_TRACKING set DESCRIPTION = 'world'");
		assertEquals(4, stmt.executeUpdate());

		stmt = conn.prepareStatement("select ID from TEST_TRACKING order by ID");
		ResultSet rs = stmt.executeQuery();
		int index = 0;
		long[] keys = { 1L, 2L, 3L, 4L };
		while (rs.next())
			assertEquals(keys[index++], rs.getLong(1));
		rs.close();
		assertEquals(4, index);
		
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
