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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.jmx.StatisticsMBeanEventPersisterImpl;

/** 
 * Parses the <code>bt:mbean-persister</code> tag to produce an {@link StatisticsMBeanEventPersisterImpl} bean,
 * for inclusion in an enclosing <code>bt:manager</code> tag or as a standalone managed bean.
 */
public class BtMBeanPersisterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		if (parserContext.isNested()) {
			BeanDefinition def = builder.getBeanDefinition();
			String id = element.hasAttribute("id") ? element.getAttribute("id")
												   : parserContext.getReaderContext().generateBeanName(def);
			BeanDefinitionHolder holder = new BeanDefinitionHolder(def, id);
			BtManagerBeanDefinitionParser.registerNestedBean(holder, "persister", parserContext);
		}
	}

	@Override
	protected Class<?> getBeanClass(Element element) {
		return StatisticsMBeanEventPersisterImpl.class;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

}
