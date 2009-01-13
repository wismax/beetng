package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_NAMESPACE;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_SCHEDULER;
import static com.mtgi.analytics.aop.config.v11.SchedulerActivationPostProcessor.configureTriggerDefinition;
import static com.mtgi.analytics.aop.config.v11.SchedulerActivationPostProcessor.registerPostProcessor;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;

public class BtXmlPersisterBeanDefinitionParser<P extends XmlBehaviorEventPersisterImpl> extends BtPersisterDefinitionParser<P> {

	private static final String[] PROPS = { "file", "binary", "compress" };
	
	private static final String CONFIG_PERSISTER = CONFIG_NAMESPACE + ".btPersister";
	private static final String CONFIG_ROTATE_TRIGGER = CONFIG_NAMESPACE + ".btRotateTrigger";
	
	public BtXmlPersisterBeanDefinitionParser() {
		this(XmlBehaviorEventPersisterImpl.class);
	}

	@SuppressWarnings("unchecked")
	protected BtXmlPersisterBeanDefinitionParser(Class<? extends XmlBehaviorEventPersisterImpl> type) {
		super(CONFIG_PERSISTER, (Class<P>)type);
	}

	@Override
	protected BeanDefinition decorate(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
		for (String p : PROPS)
			overrideAttribute(p, template, element);
		String rotateSchedule = element.getAttribute("rotate-schedule");
		if (rotateSchedule != null) {
			//schedule periodic log rotation with Quartz
			BeanDefinition trigger = factory.getBeanDefinition(CONFIG_ROTATE_TRIGGER);
			configureTriggerDefinition(trigger, rotateSchedule, parserContext.getReaderContext().generateBeanName(trigger) + "_rotate");
			registerPostProcessor(parserContext, factory, CONFIG_SCHEDULER, CONFIG_ROTATE_TRIGGER);
		}
		return super.decorate(factory, template, element, parserContext);
	}

}