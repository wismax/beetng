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
