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
 
package com.mtgi.analytics.aop.config.v10;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.aop.BehaviorTrackingAdvice;

public class BtAdviceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return BehaviorTrackingAdvice.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		if (element.hasAttribute(BtNamespaceUtils.TRACKING_MANAGER_ATTRIBUTE)) {
			if (element.hasAttribute(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_ATTRIBUTE))
				throw new IllegalArgumentException("Cannot specify both 'tracking-manager' and 'application' at the same time");
			builder.addPropertyReference(BtNamespaceUtils.TRACKING_MANAGER_PROPERTY, element.getAttribute(BtNamespaceUtils.TRACKING_MANAGER_ATTRIBUTE));
			//if there is an explicit tracking manager reference, there's no need to register our post-processor.
			return;
		} 
		
		String application = null;
		if (element.hasAttribute(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_ATTRIBUTE)) {
			application = element.getAttribute(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_ATTRIBUTE);
			builder.addPropertyValue(BtNamespaceUtils.TRACKING_MANAGER_APPLICATION_PROPERTY, application);
		}
		//add reference to default tracking manager alias, which will be configured by our bean factory post-processor.
		builder.addPropertyReference(BtNamespaceUtils.TRACKING_MANAGER_PROPERTY, "defaultTrackingManager");

		//add post-processor to check manager configuration.
		if (!parserContext.getRegistry().containsBeanDefinition("behaviorTrackingProcessor")) {
			BeanDefinitionBuilder processor = BeanDefinitionBuilder.rootBeanDefinition(BehaviorTrackingBeanFactoryPostProcessor.class);
			if (application != null)
				processor.addPropertyValue("application", application);
			parserContext.getRegistry().registerBeanDefinition("behaviorTrackingProcessor", processor.getBeanDefinition());
		}
	}

}
