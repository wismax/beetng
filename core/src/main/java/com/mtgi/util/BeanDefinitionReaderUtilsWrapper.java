/* 
 * Copyright 2008-2010 the original author or authors.
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

package com.mtgi.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Utility class to abstract the differences between Spring 2.0 and Spring
 * {2.5,3.0}.
 */
public class BeanDefinitionReaderUtilsWrapper {

	private final static Log log = LogFactory.getLog(BeanDefinitionReaderUtilsWrapper.class);
	private final static String METHOD_GENERATE_BEAN_NAME = "generateBeanName";

	public static String generateBeanName(AbstractBeanDefinition definition, BeanDefinitionRegistry registry,
			boolean isInnerBean) {

		// Spring >= 2.5 args
		Class<?>[] parameters = new Class[3];
		parameters[0] = BeanDefinition.class;
		parameters[1] = BeanDefinitionRegistry.class;
		parameters[2] = Boolean.TYPE;

		Method generateBeanNameMethod = null;
		try {
			try {
				// try to see if the Spring >= 2.5 signature exists
				generateBeanNameMethod = BeanDefinitionReaderUtils.class.getMethod(METHOD_GENERATE_BEAN_NAME,
						parameters);
				log.trace("Found Spring 2.5+ BeanDefinitionReaderUtils#generateBeanName");

			} catch (NoSuchMethodException nsme) {
				// Spring < 2.5 args
				parameters[0] = AbstractBeanDefinition.class;

				try {
					// try to see if the Spring < 2.5 signature exists
					generateBeanNameMethod = BeanDefinitionReaderUtils.class.getMethod(METHOD_GENERATE_BEAN_NAME,
							parameters);
					log.trace("Found Spring <2.5 BeanDefinitionReaderUtils#generateBeanName");

				} catch (NoSuchMethodException nsme2) {
					nsme2.printStackTrace();
				}
			}

		} catch (SecurityException se) {
			se.printStackTrace();
		}

		if (generateBeanNameMethod != null) {
			Object[] args = new Object[3];
			args[0] = definition;
			args[1] = registry;
			args[2] = isInnerBean;

			try {
				return (String) generateBeanNameMethod.invoke(null, args);

			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();

			} catch (IllegalAccessException iae) {
				iae.printStackTrace();

			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}

		return null;
	}
}
