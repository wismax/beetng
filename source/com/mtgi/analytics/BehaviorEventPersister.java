package com.mtgi.analytics;

import java.util.Queue;

/**
 * Stores instances of {@link BehaviorEvent} to a behavior tracking database.
 */
public interface BehaviorEventPersister {
	/**
	 * Drain the given event queue, persisting all instances to the
	 * database.  Persisters must support the persisting of child events
	 * before their parents, since this is generally the natural order
	 * in which events are completed.  This method returns when the queue 
	 * is empty.
	 * @return the number of persisted events
	 */
	public int persist(Queue<BehaviorEvent> events);
}
