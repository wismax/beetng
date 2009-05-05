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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.mtgi.analytics.aop.config.v10.BtAdviceBeanDefinitionParser;

/**
 * <code>NamespaceHandler</code> allowing for the configuration of
 * declarative behavior tracking management using XML.
 */
public class BtNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("config", new BtConfigBeanDefinitionParser());
		registerBeanDefinitionParser("manager", new BtManagerBeanDefinitionParser());
		registerBeanDefinitionParser("persister-chain", new BtPersisterChainBeanDefinitionParser());
		registerBeanDefinitionParser("xml-persister", new BtXmlPersisterBeanDefinitionParser());
		registerBeanDefinitionParser("jdbc-persister", new BtJdbcPersisterBeanDefinitionParser());
		registerBeanDefinitionParser("mbean-persister", new BtMBeanPersisterBeanDefinitionParser());
		registerBeanDefinitionParser("custom-persister", new BtInnerBeanDefinitionParser("persister"));
		registerBeanDefinitionParser("session-context", new BtInnerBeanDefinitionParser("sessionContext"));
		registerBeanDefinitionParser("http-requests", new BtHttpRequestsBeanDefinitionParser());

		//register tracking-manager attribute for decorating standard bean definitions
		registerBeanDefinitionDecoratorForAttribute("tracking-manager", new BtDataSourceBeanDefinitionDecorator());
		
		//stand-alone advice element, which is unchanged from 1.0 config semantics
		registerBeanDefinitionParser("advice", new BtAdviceBeanDefinitionParser());
	}

}
