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
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;

/**
 * Generic parser for inner bean definitions enclosed by a <code>bt:manager</code> tag.  These bean definitions
 * are generally the same as standard Spring XML bean definitions, with a <code>class</code> attribute and nested
 * <code>property</code> elements rather than custom XML attributes.  At time of writing, this parser handles both
 * <code>bt:custom-persister</code> and <code>bt:session-context</code> tags.
 */
public class BtInnerBeanDefinitionParser implements BeanDefinitionParser {

	private String property;

	/** @param property the property of {@link BehaviorTrackingManagerImpl} that should receive this bean definition. */
	public BtInnerBeanDefinitionParser(String property) {
		this.property = property;
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		//no custom attributes, delegate to default definition parser.
		BeanDefinitionHolder ret = parserContext.getDelegate().parseBeanDefinitionElement(element);
		if (ret != null) {
			//add parsed inner bean to containing manager definition if applicable
			if (!BtManagerBeanDefinitionParser.registerNestedBean(ret, property, parserContext)) {
				
				//add bean to global registry
				BeanDefinition def = ret.getBeanDefinition();
				
				String id = element.getAttribute("id");
				if (StringUtils.hasText(id))
					parserContext.getRegistry().registerBeanDefinition(id, def);
				else
					parserContext.getReaderContext().registerWithGeneratedName(def);

				return def;
			}
				
		}
		//global bean definition not created, probably some parse error.
		return null;
	}

}
