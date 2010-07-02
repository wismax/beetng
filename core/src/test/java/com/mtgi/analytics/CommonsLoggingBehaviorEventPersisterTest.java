/* 
 * Copyright 2008-2010 the original author or authors.
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommonsLoggingBehaviorEventPersisterTest {

	private CommonsLoggingBehaviorEventPersisterImpl inst;
	
	@Before
	public void setUp() {
		inst = new CommonsLoggingBehaviorEventPersisterImpl();
	}
	
	@After
	public void tearDown() {
		inst = null;
	}
	
	@Test
	public void testEmptyQueue(){
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>();
		inst.persist(queue);
		assertTrue("queue unmodified", queue.isEmpty());
	}
	
	@Test
	public void testNestedEvents() throws Exception  {
		//we reuse the test event creation code from jdbc persister test to get ourselves an interesting dataset.
		ArrayList<BehaviorEvent> events = new ArrayList<BehaviorEvent>();
		int[] counter = { 0 };
		for (int i = 0; i < 3; ++i)
			SequenceStyleJdbcBehaviorEventPersisterTest.createEvent(null, 1, 3, 3, counter, events);
		LinkedList<BehaviorEvent> queue = new LinkedList<BehaviorEvent>(events);
		inst.persist(queue);
		assertEquals("queue unmodified by persistence operation", 39, queue.size());
	}
	
}
