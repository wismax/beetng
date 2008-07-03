package com.mtgi.analytics.aop.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.aop.BehaviorTrackingAdvice;

public class BehaviorTrackingBeanFactoryPostPocessor implements BeanFactoryPostProcessor {

	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BehaviorTrackingAdvice advice = (BehaviorTrackingAdvice) beanFactory.getBean("btAdvice");
		if (advice == null || advice.getTrackingManager() != null)
			return;
		advice.setTrackingManager(getDefaultBehaviorTrackingManager(beanFactory));
	}

	/**
	 * provide a default BehaviorTrackingManager instance
	 * @param beanFactory
	 * @return a BehaviorTrackingAdvice instance
	 * @throws BeansException
	 */
	protected BehaviorTrackingManager getDefaultBehaviorTrackingManager(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		BehaviorTrackingManager manager = (BehaviorTrackingManager) getFirstExistingBean(beanFactory,
				BehaviorTrackingManager.class);
		if (manager == null) {
			manager = (BehaviorTrackingManager) beanFactory.autowire(BehaviorTrackingManagerImpl.class,
					ConfigurableListableBeanFactory.AUTOWIRE_AUTODETECT, true);
			beanFactory.registerSingleton("manager", manager);
		}
		
		return manager;
	}

	/**
	 * Try to find the first bean of a given type
	 * 
	 * @param beanFactory
	 * @param clz -
	 *            given type
	 * @return the first bean instance if existing; null - if not existing
	 * @throws BeansException
	 */
	protected Object getFirstExistingBean(ConfigurableListableBeanFactory beanFactory, Class clz) throws BeansException {
		String[] beanNames = beanFactory.getBeanNamesForType(clz);
		if (beanNames.length > 0) {
			return beanFactory.getBean(beanNames[0]);
		}
		return null;
	}
}
