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
 
 /**
 * 
 */
package com.mtgi.analytics.aop.config.v11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorEventPersister;

/** a dummy persister implementation which just accumulates events in memory */
public class TestPersister implements BehaviorEventPersister {

	private List<BehaviorEvent> events = Collections.synchronizedList(new ArrayList<BehaviorEvent>());

	public synchronized ArrayList<BehaviorEvent> events() {
		return new ArrayList<BehaviorEvent>(events);
	}
	
	public synchronized int count() {
		return events.size();
	}
	
	public synchronized int persist(Queue<BehaviorEvent> events) {
		this.events.addAll(events);
		int c = events.size();
		events.clear();
		return c;
	}
	
}