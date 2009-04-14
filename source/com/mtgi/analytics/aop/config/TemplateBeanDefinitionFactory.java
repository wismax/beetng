/* 
 * Copyright 2008-2009 the original author or authors.
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
 
package com.mtgi.analytics.aop.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * <p>Bootstraps a bean from one {@link BeanFactory} into another.  The intent is that we can
 * "embed" one Spring bean factory inside another, and use instances of this class to promote
 * public beans out of the embedded factory.</p>
 * 
 * <p>Not intended to be used directly in spring configuration files, but rather indirectly via
 * {@link TemplateBeanDefinitionParser} subclasses.</p>
 * 
 * @see TemplateBeanDefinitionParser
 */
public class TemplateBeanDefinitionFactory implements FactoryBean, DisposableBean {

	private BeanFactory beanFactory;
	private String beanName;
	
	public Object getObject() throws Exception {
		if (beanFactory == null || beanName == null)
			throw new FactoryBeanNotInitializedException();
		return beanFactory.getBean(beanName);
	}

	public Class<?> getObjectType() {
		return beanFactory == null ? null : beanFactory.getType(beanName);
	}

	public boolean isSingleton() {
		return beanFactory.isSingleton(beanName);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void destroy() throws Exception {
		if (beanFactory instanceof DisposableBean)
			((DisposableBean)beanFactory).destroy();
		else if (beanFactory instanceof ConfigurableBeanFactory)
			((ConfigurableBeanFactory)beanFactory).destroySingletons();
	}

}
