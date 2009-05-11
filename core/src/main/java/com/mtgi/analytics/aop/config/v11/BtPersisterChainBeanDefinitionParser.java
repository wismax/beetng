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

import static com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser.findEnclosingTemplateFactory;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mtgi.analytics.ChainingEventPersisterImpl;
import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser.TemplateComponentDefinition;

/** 
 * Parses the <code>bt:persister-chain</code> tag to produce a {@link ChainingEventPersisterImpl} bean,
 * for inclusion in an enclosing <code>bt:manager</code> tag or as a standalone managed bean.
 */
public class BtPersisterChainBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override @SuppressWarnings("unchecked")
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		//propagate shared template factory into sub-components if necessary.  this would not be
		//necessary if parserContext gave us access to the entire component stack instead of just the
		//top.  In that case, deeply nested children could search up the stack until they found
		//the template.  perhaps a future version of the Spring API will support this.
		DefaultListableBeanFactory template = findEnclosingTemplateFactory(parserContext);
		if (template != null)
			parserContext.pushContainingComponent(new TemplateComponentDefinition(element.getNodeName(), parserContext.extractSource(element), template));

		try {
			//parse delegate persister definitions
			ManagedList persisters = new ManagedList();
			persisters.setSource(element);
			
			NodeList nodes = element.getChildNodes();
			for (int i = 0; i < nodes.getLength(); ++i) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String namespaceUri = node.getNamespaceURI();
					NamespaceHandler handler = parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(namespaceUri);
					Object def = handler == null ? parserContext.getDelegate().parsePropertySubElement((Element)node, builder.getRawBeanDefinition()) 
												 : handler.parse((Element)node, parserContext);
					persisters.add(def);
				}
			}
			builder.addPropertyValue("delegates", persisters);
		} finally {
			if (template != null)
				parserContext.popContainingComponent();
		}
			
		//register persister implementation with parent.
		if (parserContext.isNested()) {
			AbstractBeanDefinition def = builder.getBeanDefinition();
			String id = element.hasAttribute("id") ? element.getAttribute("id")
													: parserContext.getReaderContext().generateBeanName(def);
			BeanDefinitionHolder holder = new BeanDefinitionHolder(def, id);
			BtManagerBeanDefinitionParser.registerNestedBean(holder, "persister", parserContext);
		}
	}

	@Override
	protected Class<?> getBeanClass(Element element) {
		return ChainingEventPersisterImpl.class;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

}
