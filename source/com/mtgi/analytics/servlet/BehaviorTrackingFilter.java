package com.mtgi.analytics.servlet;

import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.web.context.WebApplicationContext;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.EventDataElement;

/**
 * <p>A servlet filter which logs all activity to an instance of {@link BehaviorTrackingManager}
 * in the application's Spring context.  All request parameters and any specific
 * response status code is included in the event data.</p>
 * 
 * <p>If there is only one BehaviorTrackingManager in the Spring context, that
 * instance is used automatically.  If there is more than one, which manager the
 * filter should use is configured using the init parameter <code>com.mtgi.analytics.manager</code>.</p>
 * 
 * <p>By default all events generated by this filter will have a type of <code>http-request</code>.
 * An alternate type value can be specified using the filter parameter
 * <code>com.mtgi.analytics.servlet.event</code>.</p>
 */
public class BehaviorTrackingFilter implements Filter {

	/** filter parameter specifying the bean name of the BehaviorTrackingManager instance to use in the application spring context. */
	public static final String PARAM_MANAGER_NAME = "com.mtgi.analytics.manager";
	/** filter parameter specifying the eventType value to use when logging behavior tracking events. */
	public static final String PARAM_EVENT_TYPE = "com.mtgi.analytics.servlet.event";
	/** filter parameter specifying a list of parameters to include in logging; defaults to all if unspecified */
	public static final String PARAM_PARAMETERS_INCLUDE = "com.mtgi.analytics.parameters.include";

	public static final String ATT_FILTER_REGISTERED = BehaviorTrackingFilter.class.getName() + ".count";
	
	public static boolean isFiltered(ServletContext context) {
		Integer count = (Integer)context.getAttribute(ATT_FILTER_REGISTERED);
		return count != null && count > 0;
	}
	
	private ServletContext servletContext;
	private ServletRequestBehaviorTrackingAdapter delegate;
	
	public void destroy() {
		delegate = null;
		Integer count = (Integer)servletContext.getAttribute(ATT_FILTER_REGISTERED);
		if (count == null || count == 1)
			servletContext.removeAttribute(ATT_FILTER_REGISTERED);
		else
			servletContext.setAttribute(ATT_FILTER_REGISTERED, count - 1);
	}

	public void init(FilterConfig config) throws ServletException {
		servletContext = config.getServletContext();
		WebApplicationContext context = getRequiredWebApplicationContext(servletContext);
		String managerName = config.getInitParameter(PARAM_MANAGER_NAME);
		
		BehaviorTrackingManager manager;
		if (managerName == null) {
			//if there is no bean name configured, we assume there
			//must be exactly one such bean in the application context.
			Map<?,?> managers = context.getBeansOfType(BehaviorTrackingManager.class);
			if (managers.isEmpty())
				throw new ServletException("Unable to find a bean of class " + BehaviorTrackingManager.class.getName() + " in the Spring application context; perhaps it has not been configured?");
			if (managers.size() > 1)
				throw new ServletException("More than one instance of " + BehaviorTrackingManager.class.getName() + " in Spring application context; you must specify which to use with the filter parameter " + PARAM_MANAGER_NAME);
			
			manager = (BehaviorTrackingManager)managers.values().iterator().next();
		} else {
			//lookup the specified bean name.
			manager = (BehaviorTrackingManager)context.getBean(managerName, BehaviorTrackingManager.class);
		}

		//see if there is an event type name configured.
		String eventType = config.getInitParameter(PARAM_EVENT_TYPE);
		String params = config.getInitParameter(PARAM_PARAMETERS_INCLUDE);
		String[] parameters = params == null ? null : params.split("[\\r\\n\\s,;]+");

		delegate = new ServletRequestBehaviorTrackingAdapter(eventType, manager, parameters, null);

		//increment count of tracking filters registered in the servlet context.  the filter
		//and alternative request listener check this attribute to make sure both are not registered at once.
		Integer count = (Integer)servletContext.getAttribute(ATT_FILTER_REGISTERED);
		servletContext.setAttribute(ATT_FILTER_REGISTERED, count == null ? 1 : count + 1);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//wrap the response so that we can intercept response status if the application
		//sets it.
		BehaviorTrackingResponse btr = new BehaviorTrackingResponse((HttpServletResponse)response);
		BehaviorEvent event = delegate.start(request);
		try {
			chain.doFilter(request, btr);
			
			//log response codes.
			EventDataElement data = event.getData();
			data.add("response-status", btr.status);
			data.add("response-message", btr.message);
			
			//if an error code is being sent back, populate the 'error' field of the event with relevant info.
			if (btr.status != null && btr.status >= 400)
				event.setError(btr.status + ": " + btr.message);
			
		} catch (Throwable error) {
			//log exception messages to event data.
			handleServerError(event, error);
		} finally {
			delegate.stop(event);
		}
	}
	
	private static final void handleServerError(BehaviorEvent event, Throwable e) throws ServletException, IOException {

		event.addData().add("response-status", 500);
		
		if (e instanceof ServletException) {
			ServletException se = (ServletException)e;
			Throwable cause = se.getRootCause();
			if (cause != null)
				event.setError(cause);
			else
				event.setError(se);
			throw se;
		} else {
			event.setError(e);
		}
		
		//propagate exception
		if (e instanceof IOException)
			throw (IOException)e;
		if (e instanceof RuntimeException)
			throw (RuntimeException)e;
		//should not get this far in normal execution, but cover this case anyway..
		throw new ServletException(e);
	}
	
	private static class BehaviorTrackingResponse extends HttpServletResponseWrapper {

		Integer status;
		String message;
		
		protected BehaviorTrackingResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(int status, String message) throws IOException {
			this.status = status;
			this.message = message;
			super.sendError(status, message);
		}

		@Override
		public void sendError(int status) throws IOException {
			this.status = status;
			super.sendError(status);
		}

		@Override
		public void setStatus(int status, String message) {
			this.status = status;
			this.message = message;
			super.setStatus(status, message);
		}

		@Override
		public void setStatus(int status) {
			this.status = status;
			super.setStatus(status);
		}
		
	}
}
