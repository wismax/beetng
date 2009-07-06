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
		
		root.start();
		
		BehaviorEvent c10 = new BehaviorEvent(root, "request", "/bad", "test", "me", "1");
		assertSame("child points to parent", root, c10.getParent());
		
		assertFalse("child node is not a root", c10.isRoot());
		
		BehaviorEvent c11 = new BehaviorEvent(root, "request", "/bad", "test", "me", "1");
		assertSame("child points to parent", root, c11.getParent());
		assertFalse("child node is not a root", c11.isRoot());
		
		root.stop();
		assertTrue("root event stopped", root.isEnded());
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
		
		EventDataElement element = event.getData();
		assertNotNull("data element not null", element);
		assertTrue("no event data yet created", element.isNull());
		assertSame("getData always returns same element", element, event.getData());
		
		//starting data element should be immutable.
		try {
			element.add("foo", "bar");
			fail("starting data element should be shared, immutable instance");
		} catch (UnsupportedOperationException expected) {}
		
		try {
			element.addElement("test");
			fail("starting data element should be shared, immutable instance");
		} catch (UnsupportedOperationException expected) {}
		
		try {
			element.addElement(new EventDataElement("child"));
			fail("starting data element should be shared, immutable instance");
		} catch (UnsupportedOperationException expected) {}
		
		try {
			element.setText("fail");
			fail("starting data element should be shared, immutable instance");
		} catch (UnsupportedOperationException expected) {}

		try {
			element.setNext(element, new EventDataElement("foo"));
			fail("starting data element should be shared, immutable instance");
		} catch (UnsupportedOperationException expected) {}

		//add data should lazily initialize a mutable data element.
		EventDataElement data = event.addData();
		assertNotNull("event data element created", data);
		assertNotSame("new event data element returned", data, element);
		assertEquals("data has correct name", "event-data", data.getName());
		assertTrue("new data is empty", data.isEmpty());
		assertFalse("new data is not null", data.isNull());
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
					 "start=\"null\" duration-ns=\"null\" " +
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
		String duration = child.getDurationNs().toString();
		
		assertEquals("behavior-event: id=\"2\" parent-id=\"1\" " +
				 "type=\"child\" name=\"foo\" application=\"test\" " +
				 "start=\"" + start + "\" duration-ns=\"" + duration + "\" " +
				 "user-id=\"you\" session-id=\"2\" " +
				 "error=\"boom\"", 
				 child.toString());
	}
}
