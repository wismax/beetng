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

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_MBEAN_PERSISTER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_TEMPLATE;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;
import com.mtgi.analytics.jmx.StatisticsMBeanEventPersisterImpl;

/** 
 * Parses the <code>bt:mbean-persister</code> tag to produce an {@link StatisticsMBeanEventPersisterImpl} bean,
 * for inclusion in an enclosing <code>bt:manager</code> tag or as a standalone managed bean.
 */
public class BtMBeanPersisterBeanDefinitionParser extends TemplateBeanDefinitionParser 
{
	public BtMBeanPersisterBeanDefinitionParser() {
		super(CONFIG_TEMPLATE, CONFIG_MBEAN_PERSISTER);
	}

	@Override
	protected void transform(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
		String id = overrideAttribute("id", template, element);
		overrideProperty("server", template, element, true);
		if (parserContext.isNested()) {
			if (id == null)
				id = parserContext.getReaderContext().generateBeanName(template);
			BtManagerBeanDefinitionParser.registerNestedBean(new BeanDefinitionHolder(template, id), "persister", parserContext);
		}
	}
}
