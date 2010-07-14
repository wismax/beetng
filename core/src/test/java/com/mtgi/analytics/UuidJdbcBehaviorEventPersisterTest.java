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
 
package com.mtgi.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByName;

public class UuidJdbcBehaviorEventPersisterTest extends JdbcEventTestCase {

	@SpringBeanByName
	private UuidJdbcBehaviorEventPersisterImpl uuidJdbcPersistenceManager;

	public UuidJdbcBehaviorEventPersisterTest() {
		super("uuid");
	}

	@Test
	public void testEmptyQueue() throws SQLException {
		uuidJdbcPersistenceManager.persist(new LinkedList<BehaviorEvent>());
		
		ResultSet rs = stmt.executeQuery("select count(*) from BEHAVIOR_TRACKING_EVENT");
		assertTrue(rs.next());
		assertEquals("no events in db", 0, rs.getInt(1));
		rs.close();
	}

	@Test
	public void testNestedEvents() throws SQLException, Exception  {
		//we construct a dataload with important properties for the implementation:
		// * null and non-null event data
		// * events with properties and events without
		// * events with children and events without
		// * events with errors and events without

		//create a tree of 39 events that cover the properties above.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			createEvent(null, 1, 3, 3, counter, events);
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);

		uuidJdbcPersistenceManager.persist(queue);
		assertEquals("queue unmodified by persistence operation", 39, queue.size());

		// DEBUG
		// this.dumpDBUnitFlatXmlDataSet(new File("uuid-before.xml"));

		// use the sequence-style dbunit check, so modify the table so as to match the expected result
		this.runResourceScript("alter/alter-uuid-to-seq-style.sql");

		// DEBUG
		// this.dumpDBUnitFlatXmlDataSet(new File("uuid-after.xml"));

		//use the dbunit API to do a full check of data contents.  we can't use simple
		//annotations because we have to exclude variable date columns from comparison.
		assertEventDataMatches("JdbcBehaviorEventPersisterTest.testNestedEvents-result.xml");
		
		//because the DBUnit dataset cannot contain static event times and durations,
		//we do some extra verification of our own here.  we also do sanity checks, like
		//making sure child events start after their parents and end before them.
		for (BehaviorEvent e : events)
			verifyEvent(e);
	}

	/**
	 * Recursively verify that correct values for the given behavior event can be found
	 * in the database.
	 */
	private void verifyEvent(BehaviorEvent event) throws SQLException {
		assertNotNull("event was persisted", event.getId());
		Long eventId = getEventId(event.getId().toString());
		ResultSet data = getEventData(eventId);

		if (event.getParent() == null) {
			assertEquals(0, data.getLong("PARENT_EVENT_ID"));
			assertTrue("no parent for event " + event.getId(), data.wasNull());
		} else {
			Long parentId = data.getLong("PARENT_EVENT_ID");
			assertEquals(event.getParent().getId().toString(), getEventUuid(parentId));
		}
		assertEquals(event.getType(), data.getString("EVENT_TYPE"));
		assertEquals(event.getName(), data.getString("EVENT_NAME"));
		assertEquals(event.getApplication(), data.getString("APPLICATION"));
		assertEquals(event.getStart().getTime(), data.getTimestamp("EVENT_START").getTime());
		assertEquals(event.getDurationNs(), (Long)data.getLong("DURATION_NS"));
		assertEquals(event.getError(), data.getString("ERROR"));
		assertEquals(event.getUserId(), data.getString("USER_ID"));
		assertEquals(event.getSessionId(), data.getString("SESSION_ID"));
		
		data.close();

		long total = event.getDurationNs();
			
		ResultSet rs = stmt.executeQuery("select sum(duration_ns) from BEHAVIOR_TRACKING_EVENT where PARENT_EVENT_ID = " + eventId);
		assertTrue(rs.next());
		long childTotal = rs.getLong(1);
		assertTrue("child duration is less than parent event duration", childTotal <= total);
	}

	private ResultSet getEventData(Long id) throws SQLException {
		ResultSet ret = stmt.executeQuery("select * from BEHAVIOR_TRACKING_EVENT where EVENT_ID = " + id);
		assertTrue("found event with id " + id, ret.next());
		return ret;
	}

	private Long getEventId(String uuid) throws SQLException {
		ResultSet idRet = stmt.executeQuery("select ID from TMP_CONVERSION_ID where EVENT_UUID = '" + uuid + '\'');
		assertTrue("found event id with uuid " + uuid, idRet.next());
		Long id = idRet.getLong("ID");
		return id;
	}

	private String getEventUuid(Long id) throws SQLException {
		ResultSet idRet = stmt.executeQuery("select EVENT_UUID from TMP_CONVERSION_ID where ID = " + id);
		assertTrue("found event uuid with id " + id, idRet.next());
		String uuid = idRet.getString("EVENT_UUID");
		return uuid;
	}

	/**
	 * Recursively create an event tree with variable data.
	 * @param parent the parent of the new event, or null.
	 * @param depth the depth of the subtree to create.
	 * @param maxDepth the maximum depth of the tree
	 * @param maxChildren the number of children for each nested event.
	 * @param counter maintains a count of the total number of events created.
	 */
	protected static BehaviorEvent createEvent(BehaviorEvent parent, int depth, int maxDepth, int maxChildren, int[] counter, List<BehaviorEvent> accum) 
		throws InterruptedException
	{
		//our range of test event data.  the sizes are (mostly) coprime so that
		//we should generate a bunch of different combinations.
		final String[] types = { "request", "app" };
		final String[] names = { "hello", "goodbye", "world" };
		final String[] apps = { "test" };
		final String[] users = { "me", "you", "him", "her", null };
		final String[] sessions = { "1", "2" };

		int index = counter[0];
		counter[0] = counter[0] + 1;

		//one out of five events has null user/session.
		String user = select(users, index);
		String session = (user == null ? null : user + select(sessions, index));
		
		BehaviorEvent ret = new BehaviorEvent(parent, select(types, index), select(names, index), 
				select(apps, index), user, session);
		ret.start();
		
		//give the events some duration to make things more interesting.
		// adds a minimal wait because this unit test cannot support two events with the same eventStart + the TSC on my machine sucks 
		Thread.sleep((long)(Math.random() * 25) + 40);
		
		//even numbered events have an error.
		if ( (index % 2) == 0 )
			ret.setError("error[" + index + "]");
		//two out of every three events have a data element.
		if ( ((index + 2) % 3) != 0 )
			ret.addData().add("key", "value<" + index + ">");

		//descend to create child events.
		if (depth < maxDepth) {
			for (int c = 0; c < maxChildren; ++c) {
				createEvent(ret, depth + 1, maxDepth, maxChildren, counter, accum);
			}
		}
		
		ret.stop();
		accum.add(ret);
		return ret;
	}
	
	private static final String select(String[] array, int index) {
		return array[index % array.length];
	}
}
