package com.mtgi.analytics.aop.config.v11;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
