package com.mtgi.analytics.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.aop.BehaviorTrackingAdvice;
import com.mtgi.analytics.servlet.ServletRequestBehaviorTrackingAdapter;
import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

/**
 * <p>A delegating naming strategy for instances of {@link BehaviorEvent} which chooses
 * the actual {@link ObjectNamingStrategy} implementation to use based on the value
 * of {@link BehaviorEvent#getType()}.  If there is no registered strategy for the
 * given event type, a default strategy configured by {@link #setDefaultNamingStrategy(ObjectNamingStrategy)}
 * is used.</p>
 * 
 * <p>If no custom strategies are registered, default behavior for each event type is as follows:
 * <dl>
 * 	<dd>http-request</dd><dt>{@link BehaviorEventNamingStrategy}</dt>
 * 	<dd>method</dd><dt>{@link MethodNamingStrategy}</dt>
 * 	<dd>jdbc</dd><dt>{@link NestedEventNamingStrategy}</dt>
 * 	<dd>all other event types</dd><dt>{@link BehaviorEventNamingStrategy}</dt>
 * </dl>
 * </p>
 */
public class EventTypeNamingStrategy implements ObjectNamingStrategy, InitializingBean {

	private Map<String,ObjectNamingStrategy> namingStrategies;
	private ObjectNamingStrategy defaultNamingStrategy;
	
	public void setNamingStrategies(Map<String, ObjectNamingStrategy> namingStrategies) {
		this.namingStrategies = namingStrategies;
	}
	
	protected ObjectNamingStrategy getStrategy(String type) {
		return namingStrategies.get(type);
	}

	public void setDefaultNamingStrategy(ObjectNamingStrategy defaultNamingStrategy) {
		this.defaultNamingStrategy = defaultNamingStrategy;
	}

	public void afterPropertiesSet() {
		if (namingStrategies == null) {
			HashMap<String,ObjectNamingStrategy> namingStrategies = new HashMap<String,ObjectNamingStrategy>();
			
			//simple stateless naming strategies are effective for method calls and http URIs
			namingStrategies.put(BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, new MethodNamingStrategy());
			namingStrategies.put(ServletRequestBehaviorTrackingAdapter.DEFAULT_EVENT_TYPE, new BehaviorEventNamingStrategy());
			
			//sql events are hard to name effectively -- use the 'nested' strategy to name them
			//subordinate to whatever event triggered them.
			NestedEventNamingStrategy nestedStrat = new NestedEventNamingStrategy();
			nestedStrat.setParentStrategy(this);
			namingStrategies.put(BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, nestedStrat);
			
			setNamingStrategies(namingStrategies);
		}
		if (defaultNamingStrategy == null) 
			//fall through strategy just uses obvious mapping of "application:type=event-type,name=event-name"
			setDefaultNamingStrategy(new BehaviorEventNamingStrategy());
	}

	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException 
	{
		BehaviorEvent event = (BehaviorEvent)managedBean;
		ObjectNamingStrategy delegate = getStrategy(event.getType());
		if (delegate == null)
			delegate = defaultNamingStrategy;
		return delegate.getObjectName(event, null);
	}

}
