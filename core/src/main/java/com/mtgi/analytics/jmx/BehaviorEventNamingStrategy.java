package com.mtgi.analytics.jmx;

import static com.mtgi.jmx.export.naming.AppendNamingStrategy.quote;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.naming.ObjectNamingStrategy;

import com.mtgi.analytics.BehaviorEvent;

/**
 * A simple naming strategy for {@link BehaviorEvent} instances based on <code>application</code>,
 * <code>type</code>, and <code>name</code> attributes.  If a given event has application value
 * <code>testApp</code>, type value <code>testType</code>, and name value <code>testName</code>, then
 * its {@link ObjectName} will be <pre>testApp:type=testType-monitor,name=testName</pre>.
 */
public class BehaviorEventNamingStrategy implements ObjectNamingStrategy {

	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException 
	{
		BehaviorEvent event = (BehaviorEvent)managedBean;
		StringBuffer name = new StringBuffer();
		name.append(quote(event.getApplication()));
		name.append(":type=").append(quote(event.getType() + "-monitor"));
		name.append(",name=").append(quote(getEventName(event)));
		return ObjectName.getInstance(name.toString());
	}
	
	protected String getEventName(BehaviorEvent event) throws MalformedObjectNameException {
		return event.getName();
	}
	
}
