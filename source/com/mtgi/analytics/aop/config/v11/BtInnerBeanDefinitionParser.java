package com.mtgi.analytics.aop.config.v11;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class BtInnerBeanDefinitionParser implements BeanDefinitionParser {

	private String property;
	
	public BtInnerBeanDefinitionParser(String property) {
		this.property = property;
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		//session-context element is really just a standard bean class definition, so delegate to default behavior.
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
		//global bean definition not created.
		return null;
	}

}
