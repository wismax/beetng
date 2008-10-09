package com.mtgi.analytics;

import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An event persister which discards all incoming events.  Intended for testing
 * and diagnostic purposes only.
 */
public class NullBehaviorEventPersisterImpl implements BehaviorEventPersister {

	private static final Log log = LogFactory.getLog(NullBehaviorEventPersisterImpl.class);
	
	public int persist(Queue<BehaviorEvent> events) {
		int count = events.size();
		events.clear();
		log.info("Discarded " + count + " events");
		return count;
	}

}
