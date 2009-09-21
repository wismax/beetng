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
		if (element.hasAttribute(BtNamespaceConstants.ATT_TRACKING_MANAGER)) {
			builder.addPropertyReference(BtNamespaceConstants.PROP_TRACKING_MANAGER, element.getAttribute(BtNamespaceConstants.ATT_TRACKING_MANAGER));
			//if there is an explicit tracking manager reference, there's no need to register our post-processor.
			return;
		} else {
			//add reference to default tracking manager alias.  this will result in a runtime
			//error if the configuration is inconsistent
			builder.addPropertyReference(BtNamespaceConstants.PROP_TRACKING_MANAGER, "defaultTrackingManager");
		}
	}

}
