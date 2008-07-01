package com.mtgi.analytics.aop;

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
	
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@Required
	public void setTrackingManager(BehaviorTrackingManager manager) {
		this.trackingManager = manager;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {

		Method m = invocation.getMethod();
		String eventName = m.getDeclaringClass().getName() + "." + m.getName();

		BehaviorEvent event = trackingManager.createEvent(eventType, eventName);

		//log method parameters.  would be nice if we could figure
		//out parameter names here.
		EventDataElement data = event.addData();
		EventDataElement parameters = data.addElement("parameters");
		Class[] sig = m.getParameterTypes();
		Object[] args = invocation.getArguments();
		
		for (int i = 0; i < sig.length; ++i) {
			EventDataElement param = parameters.addElement("parameter");
			Object val = args[i];
			String type = val == null ? sig[i].getName()
									  : val.getClass().getName();

			param.put("type", type);
			param.put("value", toString(val));
		}
		
		trackingManager.start(event);
		try {
			Object ret = invocation.proceed();
			data.put("result", toString(ret));
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
	private static final String toString(Object arg) {
		if (arg == null)
			return null;
		
		Class type = arg.getClass();
		if (type.isPrimitive())
			return arg.toString();
		
		String typeName = type.isArray() ? type.getComponentType().getName() 
										 : type.getName();
		if (typeName.startsWith("java.lang"))
			return arg.toString();
		
		//TODO: use annotations or some other configuration for custom
		//parameter logging?
		return "[" + type.getName() + "]";
	}
	
}