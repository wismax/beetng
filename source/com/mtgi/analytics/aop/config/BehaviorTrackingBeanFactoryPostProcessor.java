package com.mtgi.analytics.aop.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.aop.BehaviorTrackingAdvice;

public class BehaviorTrackingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BehaviorTrackingAdvice advice = (BehaviorTrackingAdvice) beanFactory.getBean("btAdvice");
		if (advice == null)
			return;
		BehaviorTrackingManager manager = advice.getTrackingManager();
		if (manager == null) {
			manager = getDefaultBehaviorTrackingManager(beanFactory, advice.getApplication());
		}

		if (!checkConsistenceOfApplication(advice, manager)) {
			throw new BeanInitializationException(
					"The application value in Advice bean is inconsistent with the one in BehaviorTrackingManager bean!"
				);
		}

		advice.setTrackingManager(manager);
	}

	/**
	 * provide a default BehaviorTrackingManager instance
	 * 
	 * @param beanFactory
	 * @return a BehaviorTrackingAdvice instance
	 * @throws BeansException
	 */
	protected BehaviorTrackingManager getDefaultBehaviorTrackingManager(ConfigurableListableBeanFactory beanFactory,
			String application) throws BeansException {
		BehaviorTrackingManager manager = (BehaviorTrackingManager) getFirstExistingBean(beanFactory,
				BehaviorTrackingManager.class);
		if (manager == null) {
			manager = (BehaviorTrackingManager) beanFactory.createBean(BehaviorTrackingManagerImpl.class,
					ConfigurableListableBeanFactory.AUTOWIRE_AUTODETECT, false);
			BehaviorTrackingManagerImpl managerImpl = (BehaviorTrackingManagerImpl) manager;
			managerImpl.setApplication(application);
			beanFactory.registerSingleton("defaultTrackingManager", manager);
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

	/**
	 * check whether advice and manager define the same application name
	 * 
	 * @param advice
	 * @param manager
	 * @return false - application names are different; <br>
	 *         true - 1. application names are the same <br>
	 *         2.manager is not a instance of BehaviorTrackingManagerImpl <br>
	 *         3.BehaviorTrackingManager has no application defined, one will be
	 *         assiged from the advice
	 */
	protected boolean checkConsistenceOfApplication(BehaviorTrackingAdvice advice, BehaviorTrackingManager manager) {
		if (manager instanceof BehaviorTrackingManagerImpl) {
			BehaviorTrackingManagerImpl managerImpl = (BehaviorTrackingManagerImpl) manager;
			String managerApp = managerImpl.getApplication();
			String adviceApp = advice.getApplication();
			if (managerApp != null) {
				return managerApp.equalsIgnoreCase(adviceApp);
			}
			//else
			managerImpl.setApplication(adviceApp);
		}
		return true;
	}
}
