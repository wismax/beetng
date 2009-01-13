package com.mtgi.analytics.aop.config.v11;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
import com.mtgi.analytics.aop.config.v10.BtAdviceBeanDefinitionParser;

/**
 * <code>NamespaceHandler</code> allowing for the configuration of
 * declarative behavior tracking management using XML.
 */
public class BtNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("config", new BtConfigBeanDefinitionParser());
		registerBeanDefinitionParser("manager", new BtManagerBeanDefinitionParser());
		registerBeanDefinitionParser("xml-persister", new BtXmlPersisterBeanDefinitionParser<XmlBehaviorEventPersisterImpl>());
		registerBeanDefinitionParser("session-context", new BtSessionContextDefinitionParser());
//		registerBeanDefinitionParser("jdbc-persister", new BtAdviceBeanDefinitionParser());
//		registerBeanDefinitionParser("custom-persister", new BtAdviceBeanDefinitionParser());

		//register tracking-manager attribute for decorating standard bean definitions
		registerBeanDefinitionDecoratorForAttribute("tracking-manager", new BtDataSourceBeanDefinitionDecorator());
		
		//stand-alone advice element, which is unchanged from 1.0 config semantics
		registerBeanDefinitionParser("advice", new BtAdviceBeanDefinitionParser());
	}

}
