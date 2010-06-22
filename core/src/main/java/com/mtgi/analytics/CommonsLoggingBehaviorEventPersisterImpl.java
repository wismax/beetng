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

import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An event persister which logs all incoming events using commons-logging.
 * Mainly intended for development and diagnostic purposes only.
 */
public class CommonsLoggingBehaviorEventPersisterImpl implements BehaviorEventPersister {

	private static final Log log = LogFactory.getLog(CommonsLoggingBehaviorEventPersisterImpl.class);

	public void persist(Queue<BehaviorEvent> events) {
		for (BehaviorEvent currEvent : events) {
			log.info(currEvent);
		}
	}

	public void setLevel(String level) {
		// do nothing
	}

}
