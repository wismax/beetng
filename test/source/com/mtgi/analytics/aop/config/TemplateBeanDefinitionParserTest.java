package com.mtgi.analytics.aop.config;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.w3c.dom.Element;

@SpringApplicationContext("com/mtgi/analytics/aop/config/TemplateBeanDefinitionParserTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class TemplateBeanDefinitionParserTest {

	@SpringBeanByName
	private Embed embeddedDefaults;
	@SpringBeanByName
	private Embed embeddedCustomized;

	@SpringApplicationContext
	private ConfigurableApplicationContext context;
	
	@Test
	public void testTemplateBeans() {
		assertEquals("Template bean has been provided with default values", "default", embeddedDefaults.getInner().getData());
		assertEquals("Customized values override template values", "customized", embeddedCustomized.getInner().getData());
	}
	
	@Test
	public void testCleanUp() {
		assertFalse(embeddedDefaults.getInner().destroyed);
		context.close();
		assertTrue(embeddedDefaults.getInner().destroyed);
	}

	/** a general-purpose bean class, created by our test parser {@link EmbedBeanDefintionParser} */
	public static class Embed {
		private InnerBean inner;

		public InnerBean getInner() {
			return inner;
		}

		public void setInner(InnerBean inner) {
			this.inner = inner;
		}
	}
	
	public static class InnerBean implements DisposableBean {
		private String data;
		boolean destroyed = false;
		
		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}
		
		public void destroy() {
			destroyed = true;
		}
	}

	/**
	 * An instance of {@link TemplateBeanDefinitionParser} which initializes an instance of {@link Embed}
	 * using the embedded spring configuration file <code>com/mtgi/analytics/aop/config/TemplateBeanDefinitionParserTest-embeddedApplicationContext.xml</code>.
	 * Properties found in the runtime configuration file override those in the embedded configuration file.
	 */
	public static class EmbedBeanDefintionParser extends TemplateBeanDefinitionParser {
		public EmbedBeanDefintionParser() {
			super("com/mtgi/analytics/aop/config/TemplateBeanDefinitionParserTest-embeddedApplicationContext.xml", 
				  "com.mtgi.analytics.embedded");
		}

		@Override
		protected void transform(ConfigurableListableBeanFactory factory, BeanDefinition template, Element element, ParserContext parserContext) {
			if (element.hasAttribute("inner")) {
				//replace template inner bean configuration with substituted value at runtime.
				factory.registerAlias(element.getAttribute("inner"), "com.mtgi.analytics.inner");
			}
		}
	}
	
	/** namespace handler, auto-wired to the test namespace by test/config/spring.handlers */
	public static class NamespaceHandler extends NamespaceHandlerSupport {
		public void init() {
			registerBeanDefinitionParser("embed", new EmbedBeanDefintionParser());
		}
	}
	
}
