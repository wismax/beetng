package com.mtgi.analytics.aop;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Required;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.EventDataElement;

/**
 * Aspect-Oriented "around" advisor that logs method invocations
 * to a backing instance of {@link BehaviorTrackingManager}.  Method
 * parameters and result value are included in the event data.
 * The eventType attribute of generated events is set to <code>method</code>
 * unless overridden with {@link #setEventType(String)}.
 */
public class BehaviorTrackingAdvice implements MethodInterceptor {

	private String eventType = "method";
	private BehaviorTrackingManager trackingManager;
	
	private String application;
	
	public String getApplication() {
		return application;
	}

	@Required
	public void setApplication(String application) {
		this.application = application;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@Required
	public void setTrackingManager(BehaviorTrackingManager manager) {
		this.trackingManager = manager;
	}
	
	public BehaviorTrackingManager getTrackingManager() {
		return trackingManager;
	}
	

	public Object invoke(MethodInvocation invocation) throws Throwable {

		Method m = invocation.getMethod();
		String eventName = m.getDeclaringClass().getName() + "." + m.getName();

		BehaviorEvent event = trackingManager.createEvent(eventType, eventName);

		//log method parameters.  would be nice if we could figure
		//out parameter names here.
		EventDataElement data = event.addData();
		EventDataElement parameters = data.addElement("parameters");
		Class<?>[] sig = m.getParameterTypes();
		Object[] args = invocation.getArguments();
		
		for (int i = 0; i < sig.length; ++i) {
			Object val = args[i];
			logValue(parameters, "param", sig[i], val);
		}
		
		trackingManager.start(event);
		try {
			Object ret = invocation.proceed();
			logValue(data, "result", m.getReturnType(), ret);
			return ret;
		} catch (Throwable error) {
			event.setError(error);
			throw error;
		} finally {
			trackingManager.stop(event);
		}
	}
	
	/**
	 * marshal the given value to a String.  Only java builtin types,
	 * and arrays of those types, are converted; everything else is
	 * represented as "[qualified.type.name]" to avoid invoking
	 * costly (or buggy) toString() methods.
	 */
	private static final void logValue(EventDataElement parent, String elementName, Class<?> expectedType, Object arg) {
		EventDataElement element = parent.addElement(elementName);
		if (arg == null)
			return;
		
		Class<?> type = arg.getClass();
		//log the concrete type of the argument if it differs from the expected type (i.e. is a subclass)
		//the primitive type checks avoid logging redundant type info for autoboxed values
		if (type != expectedType && !(expectedType.isPrimitive() || type.isPrimitive()))
			element.add("type", type.getName());
		
		//TODO: use annotations or some other configuration for custom
		//parameter logging?
		String value = "{object}";
		if (type.isArray()) {
			if (shouldLog(type.getComponentType()))
				value = toStringArray(arg);
		} else {
			if (shouldLog(type))
				value = arg.toString();
		}
		element.setText(value);
	}

	protected static final String toStringArray(Object array) {
		StringBuffer ret = new StringBuffer("[");
		int len = Array.getLength(array);
		int maxLen = Math.min(len, 100);
		for (int i = 0; i < maxLen; ++i) {
			if (i > 0)
				ret.append(", ");
			ret.append(String.valueOf(Array.get(array, i)));
		}
		if (maxLen < len)
			ret.append(", ... (").append(len - maxLen).append(" more)");
		ret.append("]");
		return ret.toString();
	}
	
	private static final boolean shouldLog(Class<?> type) {
		return (type.isPrimitive()) || type.isEnum() || type.getName().startsWith("java.lang");
	}
	
}