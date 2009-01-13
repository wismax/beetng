package com.mtgi.analytics.aop.config.v11;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;

/**
 * A bean factory post-processor which schedules a trigger with a Quartz scheduler.
 * Note that the source of the scheduler and the trigger does not necessarily
 * have to be the bean factory that is being processed.  This is intended to assist in
 * processing of a {@link TemplateBeanDefinitionParser}, in which beans are
 * defined in a source template factory and then promoted out into a target factory after
 * transformation.
 * 
 * @see TemplateBeanDefinitionParser
 */
public class SchedulerActivationPostProcessor implements BeanFactoryPostProcessor {

	private BeanFactory sourceFactory; 
	private String schedulerName;
	private String triggerName;
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Scheduler scheduler = (Scheduler)sourceFactory.getBean(schedulerName, Scheduler.class);
		Trigger trigger = (Trigger)sourceFactory.getBean(triggerName, Trigger.class);
		try {
			if (trigger instanceof JobDetailAwareTrigger) {
				JobDetail job = ((JobDetailAwareTrigger)trigger).getJobDetail();
				scheduler.addJob(job, false);
			}
			scheduler.scheduleJob(trigger);
		} catch (SchedulerException e) {
			throw new SchedulingException("error scheduling trigger [" + trigger + "]", e);
		}
	}

	public void setSourceFactory(BeanFactory sourceFactory) {
		this.sourceFactory = sourceFactory;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	/**
	 * Convenience method to register a {@link SchedulerActivationPostProcessor} in the given BeanFactory
	 * parse context with the given properties.
	 * @param parseContext the target bean factory in this context will have a {@link SchedulerActivationPostProcessor} registered
	 * @param sourceFactory the source for both the named scheduler and trigger instances
	 * @param schedulerName the name of the Quartz {@link Scheduler} in <code>sourceFactory</code> to use
	 * @param triggerName the name of the Quarty {@link Trigger} in <code>sourceFactory</code> that must be scheduled
	 */
	public static void registerPostProcessor(ParserContext parseContext, BeanFactory sourceFactory, String schedulerName, String triggerName) {
		BeanDefinitionBuilder scheduleBootstrap = BeanDefinitionBuilder.rootBeanDefinition(SchedulerActivationPostProcessor.class);
		scheduleBootstrap.addPropertyValue("sourceFactory", sourceFactory);
		scheduleBootstrap.addPropertyValue("schedulerName", schedulerName);
		scheduleBootstrap.addPropertyValue("triggerName", triggerName);
		parseContext.getReaderContext().registerWithGeneratedName(scheduleBootstrap.getBeanDefinition());
	}
	
	/**
	 * Convenience method to override a CronTrigger bean definition with the given cron expression
	 * and base name.
	 */
	public static void configureTriggerDefinition(BeanDefinition trigger, String cronExpression, String name) {
		MutablePropertyValues props = trigger.getPropertyValues();
		props.removePropertyValue("cronExpression");
		props.addPropertyValue("cronExpression", cronExpression);
		props.addPropertyValue("name", name + "_trigger");
		
		unwrapInnerBean(trigger, "jobDetail").getPropertyValues().addPropertyValue("name", name + "_job");
	}

	public static BeanDefinition unwrapInnerBean(BeanDefinition parent, String property) {
		Object value = parent.getPropertyValues().getPropertyValue(property).getValue();
		if (value == null)
			return null;
		if (value instanceof BeanDefinition)
			return (BeanDefinition)value;
		if (value instanceof BeanDefinitionHolder)
			return ((BeanDefinitionHolder)value).getBeanDefinition();
		throw new IllegalArgumentException("Don't know how to convert " + value.getClass() + " into a BeanDefinition for property " + property);
	}
}
