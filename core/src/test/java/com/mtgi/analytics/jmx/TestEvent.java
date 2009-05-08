/**
 * 
 */
package com.mtgi.analytics.jmx;

import com.mtgi.analytics.BehaviorEvent;

public class TestEvent extends BehaviorEvent {

	private static final long serialVersionUID = -4116444699206367496L;

	public TestEvent(BehaviorEvent parent, String type, String name, String application, String userId, String sessionId) {
		super(parent, type, name, application, userId, sessionId, null);
	}
	
}