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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.FrameworkServlet;

import com.mtgi.analytics.BehaviorEvent;

/**
 * Logs behavior tracking events for incoming servlet requests.  This listener is activated by the
 * <code>bt:http-requests</code> Spring XML tag.  It is an alternative to {@link BehaviorTrackingFilter}
 * registration in <code>web.xml</code>.  BehaviorTrackingFilter is more flexible, but is slightly more
 * complex to configure.  At most one of these methods (bt:http-requests or BehaviorTrackingFilter) should
 * be used in a given application, otherwise errors will be thrown during application startup.
 */
public class BehaviorTrackingListener implements ServletRequestListener, ServletContextListener {

	private static final Log log = LogFactory.getLog(BehaviorTrackingListener.class);
	
	private static final String ATT_EVENTS = BehaviorTrackingListener.class.getName() + ".events";

	private ServletRequestBehaviorTrackingAdapter[] adapters = null;
	private boolean initialized = false;
	
	public void contextInitialized(ServletContextEvent event) {}
	public void contextDestroyed(ServletContextEvent event) {
		initialized = false;
		if (adapters != null) {
			log.info("BehaviorTracking for HTTP servlet requests stopped");
			adapters = null;
		}
	}

	public void requestInitialized(ServletRequestEvent event) {
		checkInit(event);
		if (adapters != null) {
			BehaviorEvent[] events = new BehaviorEvent[adapters.length];
			ServletRequest request = event.getServletRequest();
			request.setAttribute(ATT_EVENTS, events);
			
			for (int i = 0; i < adapters.length; ++i)
				try {
					events[i] = adapters[i].start(request);
				} catch (Exception e) {
					log.error("Error starting http event", e);
				}
		}
	}

	public void requestDestroyed(ServletRequestEvent event) {
		if (adapters != null) {
			ServletRequest request = event.getServletRequest();
			BehaviorEvent[] events = (BehaviorEvent[])request.getAttribute(ATT_EVENTS);
			if (events == null) {
				log.error("no behavior events stored in the current request (" + ((HttpServletRequest)request).getRequestURI());
			} else {
				request.removeAttribute(ATT_EVENTS);
				for (int i = adapters.length - 1; i >= 0; --i)
					try {
						adapters[i].stop(events[i]);
					} catch (Exception e) {
						log.error("Error stopping http event", e);
					}
			}
		}
	}

	private synchronized void checkInit(ServletRequestEvent event) {
		if (!initialized) {

			ServletContext context = event.getServletContext();
			boolean hasFilter = BehaviorTrackingFilter.isFiltered(context);
			ArrayList<ServletRequestBehaviorTrackingAdapter> beans = new ArrayList<ServletRequestBehaviorTrackingAdapter>();

			//find registered request adapters in all mvc servlet contexts.
			for (Enumeration<?> atts = context.getAttributeNames(); atts.hasMoreElements(); ) {
				String name = (String)atts.nextElement();
				if (name.startsWith(FrameworkServlet.SERVLET_CONTEXT_PREFIX)) {
					Object value = context.getAttribute(name);
					if (value instanceof ListableBeanFactory)
						addRequestAdapters(beans, (ListableBeanFactory)value, hasFilter);
				}
			}

			//look for shared application context, loaded by ContextLoaderListener.
			ListableBeanFactory parent = WebApplicationContextUtils.getWebApplicationContext(context);
			if (parent != null)
				addRequestAdapters(beans, parent, hasFilter);
			
			if (!beans.isEmpty()) {
				adapters = beans.toArray(new ServletRequestBehaviorTrackingAdapter[beans.size()]);
				log.info("BehaviorTracking for HTTP servlet requests started");
			}
			
			initialized = true;
		}
	}
	
	private static void addRequestAdapters(Collection<ServletRequestBehaviorTrackingAdapter> accum, ListableBeanFactory beanFactory, boolean hasFilter) {
		@SuppressWarnings("unchecked")
		Collection<ServletRequestBehaviorTrackingAdapter> beans = beanFactory.getBeansOfType(ServletRequestBehaviorTrackingAdapter.class, false, false).values();
		if (!beans.isEmpty()) {
			if (hasFilter)
				throw new IllegalStateException("You have configured both BehaviorTrackingFilters and BehaviorTrackingListeners in the same web application.  Only one of these methods may be used in a single application.");
			accum.addAll(beans);
		}
	}
}
