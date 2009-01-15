package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser.overrideProperty;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mtgi.analytics.JdbcBehaviorEventPersisterImpl;

public class BtJdbcPersisterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		overrideProperty("id-sql", builder.getRawBeanDefinition(), element, false);
		NodeList nodes = element.getElementsByTagNameNS("*", "data-source");
		if (nodes.getLength() == 1) {
			Element ds = (Element)nodes.item(0);
			ds.setAttribute("name", "dataSource");
			parserContext.getDelegate().parsePropertyElement(ds, builder.getRawBeanDefinition());
		}
		BtManagerBeanDefinitionParser.registerNestedBean(builder.getRawBeanDefinition(), "persister", parserContext);
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
