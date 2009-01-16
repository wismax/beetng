package com.mtgi.analytics.aop.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/** 
 * Re-runs all other post-processors on the given bean factory on 
 * another target bean factory, for chaining factory post-process
 * operations across multiple unrelated factories.
 * @see TemplateBeanDefinitionParser
 */
public class ChainingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private ConfigurableListableBeanFactory targetFactory;
	
	public void setTargetFactory(ConfigurableListableBeanFactory targetFactory) {
		this.targetFactory = targetFactory;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (!targetFactory.equals(beanFactory)) {
			String[] bfpps = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, false, false);
			for (String name : bfpps) {
				BeanFactoryPostProcessor delegate = (BeanFactoryPostProcessor)beanFactory.getBean(name);
				if (delegate != this)
					delegate.postProcessBeanFactory(targetFactory);
			}
		}
	}
}
