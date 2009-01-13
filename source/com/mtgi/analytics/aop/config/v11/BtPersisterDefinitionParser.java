package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_TEMPLATE;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.BehaviorEventPersister;
import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;

public class BtPersisterDefinitionParser<P extends BehaviorEventPersister> 
	extends TemplateBeanDefinitionParser 
{
	public BtPersisterDefinitionParser(String templateId) {
		this(templateId, null);
	}
	
	protected BtPersisterDefinitionParser(String templateId, Class<P> type) {
		super(CONFIG_TEMPLATE, templateId);
	}

	@Override
	protected BeanDefinition decorate(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
		BtManagerBeanDefinitionParser.registerNestedBean(template, "persister", parserContext);
		return template;
	}
}
