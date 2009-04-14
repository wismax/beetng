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

public class MockSessionContext implements SessionContext {

	private String contextSessionId;
	private String contextUserId;
	
	public String getContextSessionId() {
		return contextSessionId;
	}
	public void setContextSessionId(String contextSessionId) {
		this.contextSessionId = contextSessionId;
	}
	public String getContextUserId() {
		return contextUserId;
	}
	public void setContextUserId(String contextUserId) {
		this.contextUserId = contextUserId;
	}

	public void reset() {
		contextSessionId = contextUserId = null;
	}
}
