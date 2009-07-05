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
 
package com.mtgi.analytics.aop.config.v11;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;

import com.mtgi.analytics.BehaviorTrackingManager;
import com.mtgi.analytics.aop.config.DisabledBehaviorTrackingManager;
import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

/** decorates the annotated datasource bean definition with behavior tracking */
public class BtDataSourceBeanDefinitionDecorator implements BeanDefinitionDecorator {

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		BeanDefinition delegate = definition.getBeanDefinition();
		BeanDefinitionBuilder wrapper = BeanDefinitionBuilder.rootBeanDefinition(DataSourceFactory.class);
		wrapper.addPropertyReference("trackingManager", node.getNodeValue());
		wrapper.addPropertyValue("targetDataSource", delegate);
		wrapper.addPropertyValue("singleton", delegate.isSingleton());
		return new BeanDefinitionHolder(wrapper.getRawBeanDefinition(), definition.getBeanName());
	}

	/**
	 * Defers initialization of the DataSource until after the concrete type of the
	 * BehaviorTrackingManager is known.  This allows us to skip BehaviorTracking instrumentation
	 * altogether if we know the manager is a {@link DisabledBehaviorTrackingManager}.
	 */
	public static class DataSourceFactory implements FactoryBean {
		
		private BehaviorTrackingManager trackingManager;
		private DataSource targetDataSource;
		private DataSource dataSource;
		private boolean singleton;
		
		@Required
		public void setTrackingManager(BehaviorTrackingManager trackingManager) {
			this.trackingManager = trackingManager;
		}
		@Required
		public void setTargetDataSource(DataSource targetDataSource) {
			this.targetDataSource = targetDataSource;
		}
		@Required
		public void setSingleton(boolean singleton) {
			this.singleton = singleton;
		}
		
		public Object getObject() throws Exception {
			return getDelegate();
		}
		public Class<?> getObjectType() {
			return dataSource == null ? DataSource.class : dataSource.getClass();
		}
		public boolean isSingleton() {
			return singleton;
		}

		private synchronized DataSource getDelegate() {
			if (dataSource == null) {
				if (trackingManager instanceof DisabledBehaviorTrackingManager) {
					dataSource =  targetDataSource;
				} else {
					BehaviorTrackingDataSource wrapper = new BehaviorTrackingDataSource();
					wrapper.setTrackingManager(trackingManager);
					wrapper.setTargetDataSource(targetDataSource);
					wrapper.afterPropertiesSet();
					dataSource = wrapper;
				}
			}
			return dataSource;
		}
	}
	
}