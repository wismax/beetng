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
	
	public static final String DEFAULT_EVENT_TYPE = "http-request";
	
	public static final String PARAMETERS_ELEMENT = "parameters";
	public static final String PARAM_ELEMENT = "param";
	public static final String VALUE_ELEMENT = "value";
	public static final String NAME_ATTRIBUTE = "name";
	
	private String eventType;
	private BehaviorTrackingManager manager;
	private String[] parameters;
	private String[] nameParameters;
	private Pattern[] uriPatterns;
	
	public ServletRequestBehaviorTrackingAdapter(String eventType, BehaviorTrackingManager manager, String[] parameters, String[] nameParameters, Pattern[] uriPatterns) {
		this.eventType = StringUtils.hasText(eventType) ? eventType: DEFAULT_EVENT_TYPE;
		this.manager = manager;
		this.parameters = parameters;
		this.nameParameters = nameParameters;
		this.uriPatterns = uriPatterns;
	}

	public BehaviorEvent start(ServletRequest request) {
		
		HttpServletRequest req = (HttpServletRequest)request;
		if (!match(req))
			return null;

		String eventName = getEventName(req); 
		BehaviorEvent event = manager.createEvent(eventType, eventName);

		//log relevant request data and parameters to the event.
		EventDataElement data = event.addData();
		data.add("uri", req.getRequestURI());
		data.add("protocol", req.getProtocol());
		data.add("method", req.getMethod());
		data.add("remote-address", req.getRemoteAddr());
		data.add("remote-host", req.getRemoteHost());
		
		if (this.parameters != null) {
			EventDataElement parameters = data.addElement(PARAMETERS_ELEMENT);
			//include only configured parameters
			for (String name : this.parameters) {
				String[] values = request.getParameterValues(name);
				if (values != null)
					addParameter(parameters, name, values);
			}
		} else {
			EventDataElement parameters = data.addElement(PARAMETERS_ELEMENT);
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
	
	protected String getEventName(HttpServletRequest request) {
		//use the request path as an event name, excluding proto, host, and query string.
		String eventName = request.getRequestURI();
		//optionally use important parameters as part of the event name
		if (nameParameters != null) {
			char sep = '?';
			StringBuffer buf = new StringBuffer(eventName);
			for (String name : this.nameParameters) {
				String[] values = request.getParameterValues(name);
				if (values != null) {
					for (String v : values) {
						buf.append(sep).append(name).append('=').append(v);
						sep = '&';
					}
				}
			}
			eventName = buf.toString();
		}

		return eventName;
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
		EventDataElement param = parameters.addElement(PARAM_ELEMENT);
		param.add(NAME_ATTRIBUTE, name);
		for (String v : values)
			param.addElement(VALUE_ELEMENT).setText(v);
	}
	
}
