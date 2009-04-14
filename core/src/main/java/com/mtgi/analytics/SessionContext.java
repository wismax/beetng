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
