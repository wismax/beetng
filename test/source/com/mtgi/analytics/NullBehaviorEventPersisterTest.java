package com.mtgi.analytics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NullBehaviorEventPersisterTest {

	private NullBehaviorEventPersisterImpl inst;
	
	@Before
	public void setUp() {
		inst = new NullBehaviorEventPersisterImpl();
	}
	
	@After
	public void tearDown() {
		inst = null;
	}
	
	@Test
	public void testEmptyQueue(){
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>();
		assertEquals("no events persisted", 0, inst.persist(queue));
		assertEquals("no events added", 0, queue.size());
	}
	
	@Test
	public void testNestedEvents() throws Exception  {
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			JdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter, events);
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);
		assertEquals("all events persisted", 39, inst.persist(queue));
		assertEquals("queue has been cleared", 0, queue.size());
	}
	
}
