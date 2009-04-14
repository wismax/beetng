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

import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.EventDataElement;

/**
 * <p>Facilitates behavior tracking for servlet requests via calls to 
 * {@link #start(ServletRequest)} and {@link #stop(BehaviorEvent)}.  The relative
 * verbosity of event logging is configured in the {@link #ServletRequestBehaviorTrackingAdapter(String, BehaviorTrackingManager, String[]) constructor}.</p>
 * 
 * <p>This class abstracts the details of tracking servlet requests, so that
 * it can be reused by delegation in both listeners and filters.</p>
 */
public class ServletRequestBehaviorTrackingAdapter {
	
	private String eventType;
	private BehaviorTrackingManager manager;
	private String[] parameters;
	private Pattern[] uriPatterns;
	
	public ServletRequestBehaviorTrackingAdapter(String eventType, BehaviorTrackingManager manager, String[] parameters, Pattern[] uriPatterns) {
		this.eventType = StringUtils.hasText(eventType) ? eventType: "http-request";
		this.manager = manager;
		this.parameters = parameters;
		this.uriPatterns = uriPatterns;
	}

	public BehaviorEvent start(ServletRequest request) {
		
		HttpServletRequest req = (HttpServletRequest)request;
		if (!match(req))
			return null;

		//use the request path as an event name, excluding proto, host, and query string.
		String eventName = req.getRequestURI();
		BehaviorEvent event = manager.createEvent(eventType, eventName);

		//log relevant request data and parameters to the event.
		EventDataElement data = event.addData();
		data.add("uri", eventName);
		data.add("protocol", req.getProtocol());
		data.add("method", req.getMethod());
		data.add("remote-address", req.getRemoteAddr());
		data.add("remote-host", req.getRemoteHost());
		
		EventDataElement parameters = data.addElement("parameters");
		if (this.parameters != null) {
			//include only configured parameters
			for (String name : this.parameters) {
				String[] values = request.getParameterValues(name);
				if (values != null)
					addParameter(parameters, name, values);
			}
		} else {
			//include all parameters
			for (Enumeration<?> params = request.getParameterNames(); params.hasMoreElements(); ) {
				String name = (String)params.nextElement();
				String[] values = request.getParameterValues(name);
				addParameter(parameters, name, values);
			}
		}
		
		manager.start(event);
		return event;
	}
	
	public void stop(BehaviorEvent event) {
		if (event != null) //event may be null if match() returned false at the start of the request.
			manager.stop(event);
	}
	
	protected boolean match(HttpServletRequest request) {
		if (uriPatterns == null)
			return true;
		for (Pattern p : uriPatterns)
			if (p.matcher(request.getRequestURI()).matches())
				return true;
		return false;
	}
	
	private static final void addParameter(EventDataElement parameters, String name, String[] values) {
		EventDataElement param = parameters.addElement("param");
		param.add("name", name);
		for (String v : values)
			param.addElement("value").setText(v);
	}
	
}
