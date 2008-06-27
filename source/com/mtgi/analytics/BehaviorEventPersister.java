package com.mtgi.analytics;

import java.util.Queue;

/**
 * Stores instances of {@link BehaviorEvent} to a behavior tracking database.
 */
public interface BehaviorEventPersister {
	/**
	 * Drain the given event queue, persisting all instances to the
	 * database.  This operation is recursive; each event on the queue
	 * and all of its children are persisted to the database.  This
	 * method returns when the queue is empty.
	 * @return the number of persisted events
	 */
	public int persist(Queue<BehaviorEvent> events);
}
