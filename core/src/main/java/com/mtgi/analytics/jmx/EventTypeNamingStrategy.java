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
 * 	<dd>nested event (regardless of type)</dd><dt>{@link NestedEventNamingStrategy}</dt>
 * 	<dd>http-request</dd><dt>{@link BehaviorEventNamingStrategy}</dt>
 * 	<dd>method</dd><dt>{@link MethodNamingStrategy}</dt>
 * 	<dd>jdbc</dd><dt>{@link BehaviorEventNamingStrategy}</dt>
 * 	<dd>all other event types</dd><dt>{@link BehaviorEventNamingStrategy}</dt>
 * </dl>
 * </p>
 */
public class EventTypeNamingStrategy implements ObjectNamingStrategy, InitializingBean {

	private Map<String,ObjectNamingStrategy> namingStrategies;
	private ObjectNamingStrategy nestedNamingStrategy;
	private ObjectNamingStrategy defaultNamingStrategy;
	
	public void setNamingStrategies(Map<String, ObjectNamingStrategy> namingStrategies) {
		this.namingStrategies = namingStrategies;
	}
	
	public void setDefaultNamingStrategy(ObjectNamingStrategy defaultNamingStrategy) {
		this.defaultNamingStrategy = defaultNamingStrategy;
	}

	public void setNestedNamingStrategy(ObjectNamingStrategy nestedNamingStrategy) {
		this.nestedNamingStrategy = nestedNamingStrategy;
	}

	public void afterPropertiesSet() {
		if (namingStrategies == null) {
			HashMap<String,ObjectNamingStrategy> namingStrategies = new HashMap<String,ObjectNamingStrategy>();
			BehaviorEventNamingStrategy simple = new BehaviorEventNamingStrategy();
			
			//simple stateless naming strategies are effective for method calls and http URIs
			namingStrategies.put(BehaviorTrackingAdvice.DEFAULT_EVENT_TYPE, new MethodNamingStrategy());
			namingStrategies.put(ServletRequestBehaviorTrackingAdapter.DEFAULT_EVENT_TYPE, simple);
			namingStrategies.put(BehaviorTrackingDataSource.DEFAULT_EVENT_TYPE, simple);
			
			setNamingStrategies(namingStrategies);
		}
		
		if (nestedNamingStrategy == null) {
			NestedEventNamingStrategy nestedStrat = new NestedEventNamingStrategy();
			nestedStrat.setParentStrategy(this);
			nestedNamingStrategy = nestedStrat;
		}
		
		if (defaultNamingStrategy == null) 
			//fall through strategy just uses obvious mapping of "application:type=event-type,name=event-name"
			setDefaultNamingStrategy(new BehaviorEventNamingStrategy());
	}

	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException 
	{
		BehaviorEvent event = (BehaviorEvent)managedBean;
		return getStrategy(event).getObjectName(event, null);
	}
	
	protected ObjectNamingStrategy getNestedNamingStrategy() {
		return nestedNamingStrategy;
	}

	protected ObjectNamingStrategy getStrategy(BehaviorEvent event) {
		if (event.getParent() == null) {
			ObjectNamingStrategy strat = namingStrategies.get(event.getType());
			return strat == null ? defaultNamingStrategy : strat;
		} else {
			return nestedNamingStrategy;
		}
	}

}
