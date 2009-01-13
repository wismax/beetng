package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_EXECUTOR;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_MANAGER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_NAMESPACE;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_SCHEDULER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_TEMPLATE;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mtgi.analytics.aop.BehaviorTrackingAdvice;
import com.mtgi.analytics.aop.config.TemplateBeanDefinitionParser;

public class BtManagerBeanDefinitionParser extends TemplateBeanDefinitionParser  {

	public static final String ATT_ID = "id";
	public static final String ATT_APPLICATION = "application";
	public static final String ATT_TASK_EXECUTOR = "task-executor";
	public static final String ATT_FLUSH_THRESHOLD = "flush-threshold";
	public static final String ATT_SCHEDULER = "scheduler";
	public static final String ATT_FLUSH_SCHEDULE = "flush-schedule";
	public static final String ATT_METHOD_EXPRESSION = "track-method-expression";

	public BtManagerBeanDefinitionParser() {
		super(CONFIG_TEMPLATE, CONFIG_MANAGER);
	}

	@Override
	protected BeanDefinition decorate(ConfigurableListableBeanFactory factory,
			BeanDefinition template, Element element,
			ParserContext parserContext) {
		
		String managerId = overrideAttribute(ATT_ID, template, element);
		if (managerId == null)
			template.setAttribute(ATT_ID, managerId = "defaultTrackingManager");

		overrideProperty(ATT_APPLICATION, template, element, false);
		overrideProperty(ATT_FLUSH_THRESHOLD, template, element, false);

		if (element.hasAttribute(ATT_SCHEDULER))
			factory.registerAlias(element.getAttribute(ATT_SCHEDULER), CONFIG_SCHEDULER);
		
		if (element.hasAttribute(ATT_TASK_EXECUTOR))
			factory.registerAlias(element.getAttribute(ATT_TASK_EXECUTOR), CONFIG_EXECUTOR);

		//handle AOP configuration if needed
		if (element.hasAttribute(ATT_METHOD_EXPRESSION)) {
			//activate global AOP proxying if it hasn't already been done (borrowed logic from AopNamespaceHandler / config element parser)
			AopNamespaceUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext, element);
			
			//register pointcut definition for the provided expression.
			RootBeanDefinition pointcut = new RootBeanDefinition(AspectJExpressionPointcut.class);
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
		ManagerComponentDefinition def = new ManagerComponentDefinition(element.getTagName(), parserContext.extractSource(element));
		parserContext.pushContainingComponent(def);
		try {
			//descend on nested child nodes to pick up persister and session context configuration
			NodeList children = element.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					BeanDefinition child = parserContext.getDelegate().parseCustomElement((Element)node, template);
					if (child != null) {
						String childName = (String)child.getAttribute("id");
						if (StringUtils.hasText(childName))
							parserContext.getRegistry().registerBeanDefinition(childName, child);
						else
							parserContext.getReaderContext().registerWithGeneratedName(child);
					}
				}
			}
		} finally {
			parserContext.popAndRegisterContainingComponent();
		}
		
		if (!def.nestedProperties.contains("persister")) {
			//custom persister not registered.  schedule default log rotation trigger.
			BtXmlPersisterBeanDefinitionParser.configureLogRotation(parserContext, factory, null);
		}
		
		return super.decorate(factory, template, element, parserContext);
	}
	
	protected static BeanDefinition registerNestedBean(BeanDefinition nested, String parentProperty, ParserContext parserContext) {
		//add parsed session context element to containing manager definition.
		CompositeComponentDefinition parent = parserContext.getContainingComponent();
		if (parent instanceof ManagerComponentDefinition) {
			//push the inner session context bean into the parent manager bean.
			BeanDefinition managerDef = parserContext.getContainingBeanDefinition();

			MutablePropertyValues props = managerDef.getPropertyValues();
			props.removePropertyValue(parentProperty);
			props.addPropertyValue(parentProperty, nested);
			
			((ManagerComponentDefinition)parent).addNestedProperty(parentProperty);
			
			//bean definition has been handled, nothing to do.
			return null;
		}
		
		//bean is not nested inside bt:manager, return it to be registered further up the stack.
		return nested;
	}
	
	public static class ManagerComponentDefinition extends CompositeComponentDefinition {

		private Set<String> nestedProperties = new HashSet<String>();
		
		public ManagerComponentDefinition(String name, Object source) {
			super(name, source);
		}
		
		public void addNestedProperty(String property) {
			nestedProperties.add(property);
		}

	}
}
