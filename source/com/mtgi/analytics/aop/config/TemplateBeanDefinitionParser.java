package com.mtgi.analytics.aop.config;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.Conventions;
import org.springframework.core.io.DefaultResourceLoader;
import org.w3c.dom.Element;

/**
 * Base class to assist in building Spring XML extensions.  Out of the box, Spring's Extensible XML Authoring
 * support is powerful, but requires a lot of parser coding, and a lot of exposure to the sometimes
 * arcane BeanDefinition API.  <code>TemplateBeanDefinitionParser</code> allows subclasses to read
 * complex BeanDefinitions from an embedded Spring XML configuration file and then modify them according
 * to runtime configuration values.  This is often much more concise than manually constructing BeanDefinitions 
 * from scratch.
 * 
 * <p>Subclasses specify a classpath resource containing the template XML bean definitions in the constructor.
 * Subclasses should then override {@link #decorate(ConfigurableBeanFactory, BeanDefinition, Element, ParserContext)}
 * to transform the template bean definition according to runtime values.</p>
 */
public class TemplateBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private String templateResource;
	private String templateId;

	/**
	 * @param templateResource qualified classpath resource containing the template XML configuration
	 * @param templateId bean name to fetch out of the template XML configuration
	 */
	public TemplateBeanDefinitionParser(String templateResource, String templateId) {
		this.templateResource = templateResource;
		this.templateId = templateId;
	}

	@Override
	protected Class<?> getBeanClass(Element element) {
		return TemplateBeanDefinitionFactory.class;
	}

	@Override
	protected final void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		//if we have multiple nested bean definitions, we only parse the template factory
		//once.  this allows configuration changes made by enclosing bean parsers to be inherited
		//by contained beans, which is quite useful.
		DefaultListableBeanFactory templateFactory = findEnclosingTemplateFactory(parserContext);
		TemplateComponentDefinition tcd = null;
		if (templateFactory == null) {
			
			//no nesting -- load the template XML configuration from the classpath.
			final ConfigurableBeanFactory parentFactory = (ConfigurableBeanFactory)parserContext.getRegistry();
			templateFactory = new DefaultListableBeanFactory(parentFactory);
			DefaultResourceLoader loader = new DefaultResourceLoader();
	
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(templateFactory);
			reader.setResourceLoader(loader);
			reader.setEntityResolver(new ResourceEntityResolver(loader));
			reader.loadBeanDefinitions(templateResource);

			//push component definition onto the parser stack for the benefit of
			//nested bean definitions.
			tcd = newComponentDefinition(element.getNodeName(), parserContext.extractSource(element), templateFactory);
			parserContext.pushContainingComponent(tcd);
		}

		try {
			//allow subclasses to apply overrides to the template bean definition.
			BeanDefinition def = templateFactory.getBeanDefinition(templateId);
			decorate(templateFactory, def, element, parserContext);
	
			//setup our factory bean to instantiate the modified bean definition upon request.
			builder.addPropertyValue("beanFactory", templateFactory);
			builder.addPropertyValue("beanName", templateId);
			builder.getRawBeanDefinition().setAttribute("id", def.getAttribute("id"));
		} finally {
			if (tcd != null)
				parserContext.popContainingComponent();
		}
	}

	/**
	 * Hook by which subclasses can modify template configuration values.  Default behavior does nothing.
	 * @param template the template bean definition
	 * @param factory the bean factory from which <code>template</code> was loaded
	 * @param element XML configuration fragment containing overrides that should be applied to the template
	 * @param parserContext XML parse context supplying the configuration values
	 */
	protected BeanDefinition decorate(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
		return template;
	}

	protected TemplateComponentDefinition newComponentDefinition(String name, Object source, DefaultListableBeanFactory factory) {
		return new TemplateComponentDefinition(name, source, factory);
	}
	
	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		String id = (String)definition.getAttribute("id");
		return id == null ? super.resolveId(element, definition, parserContext) : id;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
	
	/**
	 * Convenience method to update a template bean definition from overriding XML data.  
	 * If <code>overrides</code> contains attribute <code>attribute</code>, transfer that
	 * attribute onto <code>template</code>, overwriting the default value.
	 */
	public static String overrideAttribute(String attribute, BeanDefinition template, Element overrides) {
		String value = (String)template.getAttribute(attribute);
		if (overrides.hasAttribute(attribute)) {
			value = overrides.getAttribute(attribute);
			template.setAttribute(attribute, value);
		}
		return value;
	}
	
	/**
	 * Convenience method to update a template bean definition from overriding XML data.  
	 * If <code>overrides</code> contains attribute <code>attribute</code>, transfer that
	 * attribute as a bean property onto <code>template</code>, overwriting the default value.
	 * @param reference if true, the value of the attribute is to be interpreted as a runtime bean name reference; otherwise it is interpreted as a literal value
	 */
	public static boolean overrideProperty(String attribute, BeanDefinition template, Element overrides, boolean reference) {
		if (overrides.hasAttribute(attribute)) {
			String propName = Conventions.attributeNameToPropertyName(attribute);
			Object value = overrides.getAttribute(attribute);
			if (reference)
				value = new RuntimeBeanReference(value.toString());
			
			MutablePropertyValues props = template.getPropertyValues();
			props.removePropertyValue(propName);
			props.addPropertyValue(propName, value);
			return true;
		}
		return false;
	}

	private static DefaultListableBeanFactory findEnclosingTemplateFactory(ParserContext context) {
		if (context.isNested()) {
			CompositeComponentDefinition parent = context.getContainingComponent();
			if (parent instanceof TemplateComponentDefinition)
				return ((TemplateComponentDefinition)parent).getTemplateFactory();
		}
		return null;
	}
	
	public static class TemplateComponentDefinition extends CompositeComponentDefinition {

		private DefaultListableBeanFactory templateFactory;
		
		public TemplateComponentDefinition(String name, Object source, DefaultListableBeanFactory factory) {
			super(name, source);
			this.templateFactory = factory;
		}

		public DefaultListableBeanFactory getTemplateFactory() {
			return templateFactory;
		}
	}

}
