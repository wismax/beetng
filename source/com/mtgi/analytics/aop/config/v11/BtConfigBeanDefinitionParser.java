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
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the <code>bt:config</code> tag.  This tag does not export a bean definition by itself, but is rather just
 * a container for multiple <code>bt:manager</code> tags (in the rare cases where such a thing is desired).
 */
public class BtConfigBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		CompositeComponentDefinition component = new CompositeComponentDefinition(element.getNodeName(), parserContext.extractSource(element));
		parserContext.pushContainingComponent(component);
		try {
			NodeList children = element.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
					parserContext.getDelegate().parseCustomElement((Element)node, null);
			}
			//no actual bean generated for bt:config.
			return null;
		} finally {
			parserContext.popAndRegisterContainingComponent();
		}
	}

}
