package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser.overrideProperty;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
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
		
		if (parserContext.isNested()) {
			AbstractBeanDefinition def = builder.getBeanDefinition();
			String id = element.hasAttribute("id") ? element.getAttribute("id")
												   : BeanDefinitionReaderUtils.generateBeanName(def, parserContext.getReaderContext().getRegistry(), true);
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
