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
