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