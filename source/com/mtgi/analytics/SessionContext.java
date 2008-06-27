package com.mtgi.analytics;

/**
 * Provides contextual information for behavior tracking purposes about a user
 * (or other principal) acting on an application.  Usually principal
 * name and session ID can be determined using standard java.security
 * and servlet APIs; this interface provides a layer of abstraction so
 * that custom application authentication schemes can be integrated into
 * the behavior tracking library.
 * 
 * @see BehaviorTrackingManager
 */
public interface SessionContext {
	/** get the user ID currently associated with the calling thread, if any */
	public String getContextUserId();
	/** 
	 * If the calling thread is currently working on an authenticated session,
	 * return the session ID.  Otherwise return null.
	 */
	public String getContextSessionId();
}
