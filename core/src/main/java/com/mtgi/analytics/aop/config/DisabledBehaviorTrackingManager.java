package com.mtgi.analytics.aop.config;

import org.springframework.beans.factory.BeanNameAware;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;

/**
 * A dummy implementation of {@link BehaviorTrackingManager} which cannot be used at runtime.
 */
public class DisabledBehaviorTrackingManager implements BehaviorTrackingManager, BeanNameAware {

	private String beanName;
	
	public DisabledBehaviorTrackingManager() {}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/** @throws UnsupportedOperationException if called */
	public BehaviorEvent createEvent(String type, String name) {
		return fail();
	}

	/** @throws UnsupportedOperationException if called */
	public void start(BehaviorEvent event) {
		fail();
	}

	/** @throws UnsupportedOperationException if called */
	public void stop(BehaviorEvent event) {
		fail();
	}

	private <T> T fail() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("beet manager " + beanName + " is disabled");
	}
}
