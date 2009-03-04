package com.mtgi.analytics.servlet;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
	
	@SuppressWarnings("unchecked")
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		WebApplicationContext spring = WebApplicationContextUtils.getWebApplicationContext(context);
		if (spring != null) {
			Collection<ServletRequestBehaviorTrackingAdapter> beans = spring.getBeansOfType(ServletRequestBehaviorTrackingAdapter.class, false, false).values();
			if (!beans.isEmpty()) {
				if (BehaviorTrackingFilter.isFiltered(context))
					throw new IllegalStateException("You have configured both BehaviorTrackingFilters and BehaviorTrackingListeners in the same web application.  Only one of these methods may be used in a single application.");
				adapters = beans.toArray(new ServletRequestBehaviorTrackingAdapter[beans.size()]);
				log.info("BehaviorTracking for HTTP servlet requests started");
			}
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		if (adapters != null) {
			log.info("BehaviorTracking for HTTP servlet requests stopped");
			adapters = null;
		}
	}

	public void requestInitialized(ServletRequestEvent event) {
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
				for (int i = 0; i < adapters.length; ++i)
					try {
						adapters[i].stop(events[i]);
					} catch (Exception e) {
						log.error("Error stopping http event", e);
					}
			}
		}
	}

}
