package com.mtgi.analytics;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class BehaviorEventTest {

	@Test
	public void testStart() {
		BehaviorEvent event = new BehaviorEvent(null, "request", "/foo", "test", "me", "1");
		assertNull("no start time yet", event.getStart());
		assertNull("no duration", event.getDuration());
		assertFalse(event.isStarted());
		assertFalse(event.isEnded());
		
		long start = System.currentTimeMillis();
		event.start();
		Date date = event.getStart();
		assertNotNull("start time logged", date);
		assertTrue(date.getTime() >= start);
		
		assertTrue(event.isStarted());
		assertFalse(event.isEnded());
		assertNull(event.getDuration());
		
		try {
			event.start();
			fail("attempt to start event twice should raise exception");
		} catch (IllegalStateException expected) {
		}

		//no change in state.
		assertSame("start date unchanged", date, event.getStart());
		assertTrue(event.isStarted());
		assertFalse(event.isEnded());
		assertNull(event.getDuration());
	}
	
	@Test
	public void testStop() throws InterruptedException {
		BehaviorEvent event = new BehaviorEvent(null, "request", "/foo", "test", "me", "1");
		assertFalse(event.isStarted());
		assertFalse(event.isEnded());

		try {
			event.stop();
			fail("attempt to stop an unstarted event should raise an exception");
		} catch (IllegalStateException expected) {
		}
		
		assertFalse(event.isStarted());
		assertFalse(event.isEnded());
		assertNull(event.getStart());
		assertNull(event.getDuration());
		
		event.start();
		
		Date date = event.getStart();

		Thread.sleep(110);
		assertTrue(event.isStarted());
		assertFalse(event.isEnded());
		assertNotNull(date);
		assertNull(event.getDuration());

		event.stop();
		
		assertTrue(event.isStarted());
		assertTrue(event.isEnded());
		
		Long duration = event.getDuration();
		assertNotNull("duration has been logged", duration);
		assertTrue("duration greater than sleep [" + duration + "]", duration >= 100L);
		assertTrue("duration within reasonable limits [" + duration + "]", duration <= 250L);
		assertSame("start date is unchanged", date, event.getStart());
		
		try {
			event.stop();
			fail("attempt to stop event twice should raise exception");
		} catch (IllegalStateException expected) {
		}
		
		//no state change from duplicate stop.
		assertTrue(event.isStarted());
		assertTrue(event.isEnded());
		assertSame(date, event.getStart());
		assertSame(duration, event.getDuration());

		try {
			event.start();
			fail("attempt to start a stopped event should raise exception");
		} catch (IllegalStateException expected) {
		}
		
		//no state change from duplicate start.
		assertTrue(event.isStarted());
		assertTrue(event.isEnded());
		assertSame(date, event.getStart());
		assertSame(duration, event.getDuration());
	}
	
	@Test
	public void testNesting() {
		BehaviorEvent root = new BehaviorEvent(null, "request", "/foo", "test", "me", "1");
		assertTrue("event with no parent is root", root.isRoot());
		assertEquals("one node in tree", 1, root.getTreeSize());
		
		try {
			new BehaviorEvent(root, "request", "/bad", "test", "me", "1");
			fail("should not be able to construct child event before parent is started");
		} catch (IllegalStateException expected) {
		}
		
		assertEquals("still one node in tree", 1, root.getTreeSize());
		root.start();
		
		BehaviorEvent c10 = new BehaviorEvent(root, "request", "/bad", "test", "me", "1");
		assertSame("child points to parent", root, c10.getParent());
		assertEquals("now two nodes in tree", 2, root.getTreeSize());
		
		assertFalse("child node is not a root", c10.isRoot());
		assertEquals(1, c10.getTreeSize());
		
		BehaviorEvent c11 = new BehaviorEvent(root, "request", "/bad", "test", "me", "1");
		assertSame("child points to parent", root, c11.getParent());
		assertFalse("child node is not a root", c11.isRoot());
		assertEquals("three nodes in tree", 3, root.getTreeSize());
		assertEquals(1, c11.getTreeSize());
		assertEquals(1, c10.getTreeSize());
		
		root.stop();
		assertTrue("root event stopped", root.isEnded());
		
		try {
			new BehaviorEvent(root, "request", "/bad", "test", "me", "1");
			fail("attempt to add a new child to a completed event should fail");
		} catch (IllegalStateException ise) {
		}
		
		assertTrue("root event stopped", root.isEnded());
		assertEquals("still three nodes in tree", 3, root.getTreeSize());
	}

	@Test
	public void testCtor() {
		//test read-only properties supplied in constructor.
		BehaviorEvent event = new BehaviorEvent(null, "application", "hello.world", "test", "me", "1");
		assertNull("no parent", event.getParent());
		assertEquals("application", event.getType());
		assertEquals("hello.world", event.getName());
		assertEquals("test", event.getApplication());
		assertEquals("me", event.getUserId());
		assertEquals("1", event.getSessionId());
	}
	
	@Test
	public void testEventDataElement() {
		BehaviorEvent event = new BehaviorEvent(null, "application", "hello.world", "test", "me", "1");
		assertNull("no event data yet created", event.getData());
		
		EventDataElement data = event.addData();
		assertNotNull("event data element created", data);
		assertEquals("data has correct name", "event-data", data.getName());
		assertFalse("no children", data.iterateChildren().hasNext());
		assertFalse("no properties", data.iterateProperties().hasNext());

		assertSame("event now has data", data, event.getData());
		assertSame("duplicate add has no effect", data, event.addData());
		assertSame("duplicate add has no effect", data, event.getData());
	}
	
	@Test
	public void testToString() {
		BehaviorEvent event = new BehaviorEvent(null, "application", "hello.world", "test", "me", "1");
		//a trivial example, with several null attributes and no parent.
		assertEquals("behavior-event: id=\"null\" " +
					 "type=\"application\" name=\"hello.world\" application=\"test\" " +
					 "start=\"null\" duration-ms=\"null\" " +
					 "user-id=\"me\" session-id=\"1\" " +
					 "error=\"null\"", 
					 event.toString());
		event.setId(1);
		event.start();

		//a more complete example, with parent id, error message, and time statistics.
		BehaviorEvent child = new BehaviorEvent(event, "child", "foo", "test", "you", "2");
		child.addData().setText("ignored");
		child.setId(2);
		child.start();
		child.setError("boom");
		child.stop();
		event.stop();
		
		String start = child.getStart().toString();
		String duration = child.getDuration().toString();
		
		assertEquals("behavior-event: id=\"2\" parent-id=\"1\" " +
				 "type=\"child\" name=\"foo\" application=\"test\" " +
				 "start=\"" + start + "\" duration-ms=\"" + duration + "\" " +
				 "user-id=\"you\" session-id=\"2\" " +
				 "error=\"boom\"", 
				 child.toString());
	}
}
