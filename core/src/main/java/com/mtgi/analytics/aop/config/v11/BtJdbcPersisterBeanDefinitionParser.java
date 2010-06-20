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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mtgi.analytics.JdbcBehaviorEventPersisterImpl;
import com.mtgi.util.BeanDefinitionReaderUtilsWrapper;

/** 
 * Parses the <code>bt:jdbc-persister</code> tag to produce an {@link JdbcBehaviorEventPersisterImpl} bean,
 * for inclusion in an enclosing <code>bt:manager</code> tag or as a standalone managed bean.
 */
public class BtJdbcPersisterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String ATT_INCREMENT = "increment";
	private static final String ELT_ID_SQL = "id-sql";

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		
		//configure ID generator settings
		NodeList children = element.getElementsByTagNameNS("*", ELT_ID_SQL);
		if (children.getLength() == 1) {
			BeanDefinition def = builder.getRawBeanDefinition();
			MutablePropertyValues props = def.getPropertyValues();
			
			Element child = (Element)children.item(0);
			String sql = child.getTextContent();
			if (StringUtils.hasText(sql))
				props.addPropertyValue("idSql", sql);
			
			if (child.hasAttribute(ATT_INCREMENT))
				props.addPropertyValue("idIncrement", child.getAttribute(ATT_INCREMENT));
		}
		
		//configure nested dataSource
		NodeList nodes = element.getElementsByTagNameNS("*", "data-source");
		if (nodes.getLength() == 1) {
			Element ds = (Element)nodes.item(0);
			ds.setAttribute("name", "dataSource");
			parserContext.getDelegate().parsePropertyElement(ds, builder.getRawBeanDefinition());
		}
		
		//push persister into parent manager bean, if applicable
		if (parserContext.isNested()) {
			AbstractBeanDefinition def = builder.getBeanDefinition();
			String id = element.hasAttribute("id") ? element.getAttribute("id")
												   : BeanDefinitionReaderUtilsWrapper.generateBeanName(def, parserContext.getReaderContext().getRegistry(), true);
			BeanDefinitionHolder holder = new BeanDefinitionHolder(def, id);
			BtManagerBeanDefinitionParser.registerNestedBean(holder, "persister", parserContext);
		}
	}

	@Override
	protected Class<?> getBeanClass(Element element) {
		return JdbcBehaviorEventPersisterImpl.class;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

}
