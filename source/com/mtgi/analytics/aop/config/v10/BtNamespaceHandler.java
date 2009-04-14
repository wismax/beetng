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
 
package com.mtgi.analytics.aop.config.v10;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <code>NamespaceHandler</code> allowing for the configuration of
 * declarative behavior tracking management using either XML or using annotations.
 *
 * <p>This namespace handler is the central piece of functionality in the
 * Spring 2 behavior tracking management facilities and offers two appraoched
 * to declaratively manage transactions.
 *
 * <p>One approach uses behavior tracking semantics defined in XML using the
 * <code>&lt;bt:advice&gt;</code> elements, the other uses annotations
 * in combination with the <code>&lt;bt:annotation-driven&gt;</code> element.
 *
 * @author Shawn Cao
 */
public class BtNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("advice", new BtAdviceBeanDefinitionParser());
		//registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		
	}

}
