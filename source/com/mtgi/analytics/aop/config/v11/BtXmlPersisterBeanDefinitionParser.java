package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_NAMESPACE;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_SCHEDULER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_TEMPLATE;
import static com.mtgi.analytics.aop.config.v11.SchedulerActivationPostProcessor.configureTriggerDefinition;
import static com.mtgi.analytics.aop.config.v11.SchedulerActivationPostProcessor.registerPostProcessor;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;

/** 
 * Parses the <code>bt:xml-persister</code> tag to produce an {@link XmlBehaviorEventPersisterImpl} bean,
 * for inclusion in an enclosing <code>bt:manager</code> tag or as a standalone managed bean.
 */
public class BtXmlPersisterBeanDefinitionParser extends TemplateBeanDefinitionParser 
{
	private static final String[] PROPS = { "file", "binary", "compress" };
	
	public static final String CONFIG_PERSISTER = CONFIG_NAMESPACE + ".btPersister";
	public static final String CONFIG_ROTATE_TRIGGER = CONFIG_NAMESPACE + ".btRotateTrigger";
	
	public BtXmlPersisterBeanDefinitionParser() {
		super(CONFIG_TEMPLATE, CONFIG_PERSISTER);
	}

	@Override
	protected void transform(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
		String id = overrideAttribute("id", template, element);
		for (String p : PROPS)
			overrideProperty(p, template, element, false);
		//schedule periodic log rotation with Quartz
		String rotateSchedule = element.getAttribute("rotate-schedule");
		if (rotateSchedule != null)
			configureLogRotation(parserContext, factory, rotateSchedule);
		if (parserContext.isNested()) {
			if (id == null)
				id = parserContext.getReaderContext().generateBeanName(template);
			BtManagerBeanDefinitionParser.registerNestedBean(new BeanDefinitionHolder(template, id), "persister", parserContext);
		}
	}

	public static void configureLogRotation(ParserContext parserContext, ConfigurableListableBeanFactory factory, String schedule) {
		BeanDefinition trigger = factory.getBeanDefinition(CONFIG_ROTATE_TRIGGER);
		configureTriggerDefinition(trigger, schedule, parserContext.getReaderContext().generateBeanName(trigger) + "_rotate");
		registerPostProcessor(parserContext, factory, CONFIG_SCHEDULER, CONFIG_ROTATE_TRIGGER);
	}
}