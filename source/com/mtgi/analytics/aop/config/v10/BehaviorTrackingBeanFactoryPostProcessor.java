package com.mtgi.analytics.aop.config.v10;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.BehaviorTrackingManagerImpl;

/** intelligently assigns a matching BehaviorTrackingManager instance to a BehaviorTrackingAdvice bean, creating one if necessary */
public class BehaviorTrackingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private String application;
	
	public void setApplication(String application) {
		this.application = application;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		try {
			BeanDefinition def = beanFactory.getBeanDefinition("defaultTrackingManager");
			if (def != null)
				return; //nothing to do.
		} catch (NoSuchBeanDefinitionException notFound) {
		}

		//no default BehaviorTrackingManager instances defined, choose an available instance or create one.
		String[] matches = beanFactory.getBeanNamesForType(BehaviorTrackingManager.class, false, false);
		if (matches.length > 0) {
			beanFactory.registerAlias(matches[0], "defaultTrackingManager");
		} else {
			if (application == null)
				throw new BeanInitializationException("'application' is required on bt:advice when not specified anywhere else in the configuration");

			BehaviorTrackingManagerImpl inst = new BehaviorTrackingManagerImpl();
			inst.setApplication(application);
			beanFactory.autowireBeanProperties(inst, ConfigurableListableBeanFactory.AUTOWIRE_BY_TYPE, false);
			try {
				inst.afterPropertiesSet();
			} catch (Exception e) {
				throw new BeanInitializationException("Error initializing default tracking manager instance", e);
			}
			beanFactory.registerSingleton("defaultTrackingManager", inst);
		}
		
	}

}
