package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_NAMESPACE;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_SCHEDULER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_TEMPLATE;
import static com.mtgi.analytics.aop.config.v11.SchedulerActivationPostProcessor.configureTriggerDefinition;
import static com.mtgi.analytics.aop.config.v11.SchedulerActivationPostProcessor.registerPostProcessor;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;

public class BtXmlPersisterBeanDefinitionParser extends TemplateBeanDefinitionParser 
{
	private static final String[] PROPS = { "file", "binary", "compress" };
	
	private static final String CONFIG_PERSISTER = CONFIG_NAMESPACE + ".btPersister";
	public static final String CONFIG_ROTATE_TRIGGER = CONFIG_NAMESPACE + ".btRotateTrigger";
	
	public BtXmlPersisterBeanDefinitionParser() {
		super(CONFIG_TEMPLATE, CONFIG_PERSISTER);
	}

	@Override
	protected BeanDefinition decorate(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
		for (String p : PROPS)
			overrideProperty(p, template, element, false);
		//schedule periodic log rotation with Quartz
		String rotateSchedule = element.getAttribute("rotate-schedule");
		if (rotateSchedule != null)
			configureLogRotation(parserContext, factory, rotateSchedule);
		BtManagerBeanDefinitionParser.registerNestedBean(template, "persister", parserContext);
		return super.decorate(factory, template, element, parserContext);
	}

	public static void configureLogRotation(ParserContext parserContext, ConfigurableListableBeanFactory factory, String schedule) {
		BeanDefinition trigger = factory.getBeanDefinition(CONFIG_ROTATE_TRIGGER);
		configureTriggerDefinition(trigger, schedule, parserContext.getReaderContext().generateBeanName(trigger) + "_rotate");
		registerPostProcessor(parserContext, factory, CONFIG_SCHEDULER, CONFIG_ROTATE_TRIGGER);
	}
}