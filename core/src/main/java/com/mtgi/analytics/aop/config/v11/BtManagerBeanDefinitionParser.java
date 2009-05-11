/* 
 * Copyright 2008-2009 the original author or authors.
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
 
package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.*;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mtgi.analytics.BehaviorEventPersister;
import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.SessionContext;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
import com.mtgi.analytics.aop.BehaviorTrackingAdvice;
import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;
import com.mtgi.analytics.servlet.SpringSessionContext;

/**
 * Parser for &lt;bt:manager&gt; configuration tags, the most significant tag in behavior tracking configuration.
 * Each such tag will configure an instance of {@link BehaviorTrackingManagerImpl} and bind it into the Spring application
 * context.  This includes configuring an implementation of {@link BehaviorEventPersister}, {@link SessionContext},
 * registering AOP advice for tracking method calls, and automatically scheduling event flushes and log rotation
 * according to sensible default values.
 */
public class BtManagerBeanDefinitionParser extends TemplateBeanDefinitionParser  {

	/** The bean name of the bt manager instance, defaults to <code>defaultTrackingManager</code>. */
	public static final String ATT_ID = "id";
	/** 
	 * The name of the application, required to be specified at runtime.  
	 * @see BehaviorTrackingManagerImpl#setApplication(String) 
	 */
	public static final String ATT_APPLICATION = "application";
	/** 
	 * Bean name reference to a TaskExecutor for executing event flushes and other scheduled operations.  
	 * A private instance is created if one is not specified.
	 * @see BehaviorTrackingManagerImpl#setExecutor(org.springframework.core.task.TaskExecutor)
	 */
	public static final String ATT_TASK_EXECUTOR = "task-executor";
	/** @see BehaviorTrackingManagerImpl#setFlushThreshold(int)  */
	public static final String ATT_FLUSH_THRESHOLD = "flush-threshold";
	/** 
	 * Bean name reference to a Quartz Scheduler used for scheduled operations like event flush and log rotation.
	 * A private instance is created if one is not specified.
	 */
	public static final String ATT_SCHEDULER = "scheduler";
	/**
	 * Quartz Cron Expression identifying how often behavior events are flushed to the persister.  Defaults
	 * to once every 5 minutes if unspecified.
	 */
	public static final String ATT_FLUSH_SCHEDULE = "flush-schedule";
	/**
	 * AOP method pattern identifying which methods should be logged as behavior tracking events.  Defaults to
	 * null if unspecified.
	 */
	public static final String ATT_METHOD_EXPRESSION = "track-method-expression";
	/**
	 * Set to true/false to enable JMX mbean registration for this manager.  Defaults to true if unspecified.
	 */
	public static final String ATT_REGISTER_MBEANS = "register-mbeans";
	/**
	 * When {@link #ATT_REGISTER_MBEANS} is specified, identifies a reference
	 * to the MBeanServer to target for registration.  Defaults to the platform 
	 * MBean server if unspecified.
	 */
	public static final String ATT_MBEAN_SERVER = "mbean-server";
	
	/**
	 * Bean name reference to an implementation of {@link BehaviorEventPersister} defined in the application context.
	 * A private instance of {@link XmlBehaviorEventPersisterImpl} is created if none is specified.  This attribute
	 * cannot be used in combination with a nested persister tag (<code>xml-persister</code>, <code>jdbc-persister</code>,
	 * <code>custom-persister</code>).
	 * @see BehaviorTrackingManagerImpl#setPersister(BehaviorEventPersister)
	 */
	public static final String ATT_PERSISTER = "persister";
	/**
	 * Bean name reference to an implementation of {@link SessionContext} defined in the application context.
	 * A private instance is created if none is specified.  This attribute
	 * cannot be used in combination with a nested bt:session-context tag.
	 * @see BehaviorTrackingManagerImpl#setSessionContext(SessionContext)
	 */
	public static final String ATT_SESSION_CONTEXT = "session-context";

	private static final String AOP_REGISTRATION = "registerAspectJAutoProxyCreatorIfNecessary";
	
	//we want to support both 2.0 and 2.5 deployments without
	//too much hassle; unfortunately, an incompatible API change in
	//AopNamespaceUtils means we have to use reflection to do so.
	private Method aopRegistration;
	
	public BtManagerBeanDefinitionParser() {
		super(CONFIG_TEMPLATE, CONFIG_MANAGER);
		
		try {
			//try Spring 2.5 method signature first.
			aopRegistration = AopNamespaceUtils.class.getMethod(AOP_REGISTRATION, ParserContext.class, Element.class);
		} catch (NoSuchMethodException allowed) {
			//fallback to 2.0 signature
			try {
				aopRegistration = AopNamespaceUtils.class.getMethod(AOP_REGISTRATION, ParserContext.class, Object.class);
			} catch (NoSuchMethodException e) {
				throw new org.springframework.beans.factory.BeanDefinitionStoreException("Could not find method " + AopNamespaceUtils.class.getName() + "." + AOP_REGISTRATION + " with a recognized signature; are you using an unsupported version of Spring?");
			}
		}
	}

	@Override
	protected void transform(ConfigurableListableBeanFactory factory,
			BeanDefinition template, Element element,
			ParserContext parserContext) {

		ManagerComponentDefinition def = (ManagerComponentDefinition)parserContext.getContainingComponent();
		
		String managerId = overrideAttribute(ATT_ID, template, element);
		if (managerId == null)
			template.setAttribute(ATT_ID, managerId = "defaultTrackingManager");

		overrideProperty(ATT_APPLICATION, template, element, false);
		overrideProperty(ATT_FLUSH_THRESHOLD, template, element, false);

		//wake up MBeanExporter if we're going to be doing MBean registration.
		if ("true".equalsIgnoreCase(element.getAttribute(ATT_REGISTER_MBEANS))) {
			AbstractBeanDefinition exporter = (AbstractBeanDefinition)factory.getBeanDefinition(CONFIG_MBEAN_EXPORTER);
			exporter.setLazyInit(false);

			//append manager ID to mbean name, in case of multiple managers in a single application.
			BeanDefinition naming = factory.getBeanDefinition(CONFIG_NAMING_STRATEGY);
			naming.getPropertyValues().addPropertyValue("value", managerId);
		}

		//prefer references to beans in the parent factory if they've been specified
		if (element.hasAttribute(ATT_MBEAN_SERVER))
			factory.registerAlias(element.getAttribute(ATT_MBEAN_SERVER), CONFIG_MBEAN_SERVER);
		
		if (element.hasAttribute(ATT_SCHEDULER))
			factory.registerAlias(element.getAttribute(ATT_SCHEDULER), CONFIG_SCHEDULER);
		
		if (element.hasAttribute(ATT_TASK_EXECUTOR))
			factory.registerAlias(element.getAttribute(ATT_TASK_EXECUTOR), CONFIG_EXECUTOR);
		
		//make note of external persister element so that we don't activate log rotation.
		if (element.hasAttribute(ATT_PERSISTER)) {
			def.addNestedProperty(ATT_PERSISTER);
			MutablePropertyValues props = template.getPropertyValues();
			props.removePropertyValue(ATT_PERSISTER);
			props.addPropertyValue(ATT_PERSISTER, new RuntimeBeanReference(element.getAttribute(ATT_PERSISTER)));
		}
		
		if (element.hasAttribute(ATT_SESSION_CONTEXT)) {
			//override default session context with reference
			def.addNestedProperty("sessionContext");
			factory.registerAlias(element.getAttribute(ATT_SESSION_CONTEXT), CONFIG_SESSION_CONTEXT);
		}

		//handle AOP configuration if needed
		if (element.hasAttribute(ATT_METHOD_EXPRESSION)) {
			//activate global AOP proxying if it hasn't already been done (borrowed logic from AopNamespaceHandler / config element parser)
			activateAopProxies(parserContext, element);
			
			//register pointcut definition for the provided expression.
			RootBeanDefinition pointcut = new RootBeanDefinition(AspectJExpressionPointcut.class);
			//rely on deprecated method to maintain spring 2.0 support
			pointcut.setSingleton(false);
			pointcut.setSynthetic(true);
			pointcut.getPropertyValues().addPropertyValue("expression", element.getAttribute(ATT_METHOD_EXPRESSION));

			//create implicit pointcut advice bean.
			RootBeanDefinition advice = new RootBeanDefinition(BehaviorTrackingAdvice.class);
			overrideProperty(ATT_APPLICATION, advice, element, false);
			advice.getPropertyValues().addPropertyValue("trackingManager", new RuntimeBeanReference(managerId));

			//register advice, pointcut, and advisor entry to bind the two together.
			XmlReaderContext ctx = parserContext.getReaderContext();
			String pointcutId = ctx.registerWithGeneratedName(pointcut);
			String adviceId = ctx.registerWithGeneratedName(advice);
			
			RootBeanDefinition advisorDefinition = new RootBeanDefinition(DefaultBeanFactoryPointcutAdvisor.class);
			advisorDefinition.getPropertyValues().addPropertyValue("adviceBeanName", new RuntimeBeanNameReference(adviceId));
			advisorDefinition.getPropertyValues().addPropertyValue("pointcut", new RuntimeBeanReference(pointcutId));
			ctx.registerWithGeneratedName(advisorDefinition);
		}

		//configure flush trigger and job to be globally unique based on manager name.
		BeanDefinition flushTrigger = factory.getBeanDefinition("com.mtgi.analytics.btFlushTrigger");
		SchedulerActivationPostProcessor.configureTriggerDefinition(flushTrigger, element.getAttribute(ATT_FLUSH_SCHEDULE), managerId + "_flush");

		//set up a post-processor to register the flush job with the selected scheduler instance.  the job and scheduler
		//come from the template factory, but the post-processor runs when the currently-parsing factory is finished.
		SchedulerActivationPostProcessor.registerPostProcessor(parserContext, factory, CONFIG_SCHEDULER, CONFIG_NAMESPACE + ".btFlushTrigger");
		
		//ManagerComponentDefinition is a flag to nested parsers that they should push their parsed bean definitions into
		//the manager bean definition.  for example, see BtPersisterBeanDefinitionParser.
		//descend on nested child nodes to pick up persister and session context configuration
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String namespaceUri = node.getNamespaceURI();
				NamespaceHandler handler = parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(namespaceUri);
				ParserContext nestedCtx = new ParserContext(parserContext.getReaderContext(), parserContext.getDelegate(), template);
				nestedCtx.pushContainingComponent(def);
				handler.parse((Element)node, nestedCtx);
			}
		}
		
		if (!def.nestedProperties.contains(ATT_PERSISTER)) {
			//no persister registered.  schedule default log rotation trigger.
			BtXmlPersisterBeanDefinitionParser.configureLogRotation(parserContext, factory, null);
		}
		
		if (!def.nestedProperties.contains("sessionContext")) {
			//custom session context not registered.  select appropriate default class
			//depending on whether we are in a web context or not.
			if (parserContext.getReaderContext().getReader().getResourceLoader() instanceof WebApplicationContext) {
				BeanDefinition scDef = factory.getBeanDefinition(CONFIG_SESSION_CONTEXT);
				scDef.setBeanClassName(SpringSessionContext.class.getName());
			}
		}
	}
	
	/** overridden to return an instance of {@link ManagerComponentDefinition} */
	@Override
	protected TemplateComponentDefinition newComponentDefinition(String name,
			Object source, DefaultListableBeanFactory factory) {
		return new ManagerComponentDefinition(name, source, factory);
	}
	
	protected void activateAopProxies(ParserContext context, Element source) {
		try {
			aopRegistration.invoke(null, context, source);
		} catch (Exception e) {
			throw new BeanDefinitionStoreException("Error activating AOP proxies while parsing bt:manager definition", e);
		}
	}
	
	/** 
	 * called by nested tags to push inner beans into the enclosing {@link BehaviorTrackingManagerImpl}.
	 * @return <span>true if the inner bean was added to an enclosing {@link BehaviorTrackingManagerImpl}.  Otherwise, the bean definition
	 * is not nested inside a &lt;bt:manager&gt; tag and therefore will have to be registered as a global bean in the application
	 * context.</span>
	 */
	protected static boolean registerNestedBean(BeanDefinitionHolder nested, String parentProperty, ParserContext parserContext) {
		//add parsed inner bean element to containing manager definition; e.g. persister or SessionContext impls.
		CompositeComponentDefinition parent = parserContext.getContainingComponent();
		if (parent instanceof ManagerComponentDefinition) {
			//we are nested; add to enclosing bean def.
			ManagerComponentDefinition mcd = (ManagerComponentDefinition)parent;
			BeanDefinition managerDef = parserContext.getContainingBeanDefinition();

			MutablePropertyValues props = managerDef.getPropertyValues();
			PropertyValue current = props.getPropertyValue(parentProperty);
			boolean innerBean = true;
			
			if (current != null) {
				//if the original value is a reference, replace it with an alias to the nested bean definition.
				//this means the nested bean takes the place of the default definition
				//in other places where it might be referenced, as well as during mbean export
				Object value = current.getValue();
				DefaultListableBeanFactory factory = mcd.getTemplateFactory();
				if (value instanceof RuntimeBeanReference) {
					String ref = ((RuntimeBeanReference)value).getBeanName();
					
					if (factory.getBeanDefinition(ref) == nested.getBeanDefinition()) {
						//the nested definition is the same as the default definition
						//by reference, so we don't need to make it an inner bean definition.
						innerBean = false;
					}
				}
			}
			if (innerBean)
				props.addPropertyValue(parentProperty, nested);
			mcd.addNestedProperty(parentProperty);
			return true;
		}
		//bean is not nested inside bt:manager
		return false;
	}

	/** 
	 * Specialized {@link TemplateComponentDefinition} for the <code>bt:manager</code> config tag.
	 * Adds some extra validation to make sure that duplicate definitions are not given for dependencies
	 * (for example, specifying a persister both as a nested element and as a reference attribute).
	 */
	public static class ManagerComponentDefinition extends TemplateComponentDefinition {

		protected ManagerComponentDefinition(String name, Object source,
				DefaultListableBeanFactory factory) {
			super(name, source, factory);
		}

		private Set<String> nestedProperties = new HashSet<String>();

		public void addNestedProperty(String property) {
			if (!nestedProperties.add(property))
				throw new IllegalArgumentException("Property " + property + " specified more than once");
		}

	}

}