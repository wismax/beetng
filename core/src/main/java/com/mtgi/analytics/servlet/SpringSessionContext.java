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
 
package com.mtgi.analytics.servlet;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mtgi.analytics.SessionContext;

/**
 * The default {@link SessionContext} implementation for web applications, which uses Spring request context
 * to lookup user name and session ID.  Requires that the spring {@link RequestContextListener} is registered
 * in the web application.
 */
public class SpringSessionContext implements SessionContext {

	public String getContextSessionId() {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		return attributes == null ? null : attributes.getSessionId();
	}

	public String getContextUserId() {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		return attributes == null ? null : attributes.getRequest().getRemoteUser();
	}

}
