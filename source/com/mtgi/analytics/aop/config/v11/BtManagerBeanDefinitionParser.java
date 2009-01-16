package com.mtgi.analytics.aop.config.v11;

import static com.mtgi.analytics.aop.config.v11.BtXmlPersisterBeanDefinitionParser.CONFIG_PERSISTER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_EXECUTOR;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_MANAGER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_NAMESPACE;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_SCHEDULER;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_SESSION_CONTEXT;
import static com.mtgi.analytics.aop.config.v11.ConfigurationConstants.CONFIG_TEMPLATE;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
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
	 * Bean name reference to an implementation of {@link BehaviorEventPersister} defined in the application context.
	 * A private instance of {@link XmlBehaviorEventPersisterImpl} is created if none is specified.  This attribute
	 * cannot be used in combination with a nested persister tag (<code>xml-persister</code>, <code>jdbc-persister</code>,
	 * <code>custom-persister</code>).
	 * @see BehaviorTrackingManagerImpl#setPersister(BehaviorEventPersister)
	 */
	public static final String ATT_PERSISTER = "persister";

	public BtManagerBeanDefinitionParser() {
		super(CONFIG_TEMPLATE, CONFIG_MANAGER);
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

		if (element.hasAttribute(ATT_SCHEDULER))
			factory.registerAlias(element.getAttribute(ATT_SCHEDULER), CONFIG_SCHEDULER);
		
		if (element.hasAttribute(ATT_TASK_EXECUTOR))
			factory.registerAlias(element.getAttribute(ATT_TASK_EXECUTOR), CONFIG_EXECUTOR);
		
		if (element.hasAttribute(ATT_PERSISTER)) {
			//override default persister definition with reference
			def.addNestedProperty(ATT_PERSISTER);
			factory.registerAlias(element.getAttribute(ATT_PERSISTER), CONFIG_PERSISTER);
		}

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
			//custom persister not registered.  schedule default log rotation trigger.
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
			BeanDefinition managerDef = parserContext.getContainingBeanDefinition();

			MutablePropertyValues props = managerDef.getPropertyValues();
			props.removePropertyValue(parentProperty);
			props.addPropertyValue(parentProperty, nested);
			
			((ManagerComponentDefinition)parent).addNestedProperty(parentProperty);
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