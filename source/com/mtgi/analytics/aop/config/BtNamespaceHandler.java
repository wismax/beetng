package com.mtgi.analytics.aop.config;

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
