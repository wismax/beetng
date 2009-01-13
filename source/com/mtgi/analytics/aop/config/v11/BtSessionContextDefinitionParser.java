package com.mtgi.analytics.aop.config.v11;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class BtSessionContextDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		//session-context element is really just a standard bean class definition, so delegate to default behavior.
		BeanDefinitionHolder ret = parserContext.getDelegate().parseBeanDefinitionElement(element);
		if (ret != null) {
			//add parsed session context element to containing manager definition.
			return BtManagerBeanDefinitionParser.registerNestedBean(ret.getBeanDefinition(), "sessionContext", parserContext);
		}
		//maybe a parse error -- bean definition not created.
		return null;
	}

}