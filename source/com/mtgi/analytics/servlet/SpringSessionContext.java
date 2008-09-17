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
