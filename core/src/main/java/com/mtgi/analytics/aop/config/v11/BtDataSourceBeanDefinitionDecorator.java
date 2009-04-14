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

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;

import com.mtgi.analytics.sql.BehaviorTrackingDataSource;

/** decorates the annotated datasource bean definition with behavior tracking */
public class BtDataSourceBeanDefinitionDecorator implements BeanDefinitionDecorator {

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		BeanDefinitionBuilder wrapper = BeanDefinitionBuilder.rootBeanDefinition(BehaviorTrackingDataSource.class);
		wrapper.addPropertyReference("trackingManager", node.getNodeValue());
		wrapper.addPropertyValue("targetDataSource", definition.getBeanDefinition());
		return new BeanDefinitionHolder(wrapper.getRawBeanDefinition(), definition.getBeanName());
	}

}
