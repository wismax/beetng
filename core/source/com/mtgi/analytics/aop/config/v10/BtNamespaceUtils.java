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

import org.springframework.core.Conventions;

public class BtNamespaceUtils {
	public static final String TRACKING_MANAGER_ATTRIBUTE = "tracking-manager";

	public static final String TRACKING_MANAGER_PROPERTY =
			Conventions.attributeNameToPropertyName(TRACKING_MANAGER_ATTRIBUTE);

	public static final String TRACKING_ATTRIBUTE_SOURCE = "trackingAttributeSource";

	public static final String TRACKING_MANAGER_APPLICATION_ATTRIBUTE = "application";
	
	public static final String TRACKING_MANAGER_APPLICATION_PROPERTY =
		Conventions.attributeNameToPropertyName(TRACKING_MANAGER_APPLICATION_ATTRIBUTE);

//	private static final String ANNOTATION_TRACKING_ATTRIBUTE_SOURCE_CLASS_NAME =
//			"org.springframework.transaction.annotation.AnnotationTransactionAttributeSource";
//
//
//	public static Class getAnnotationTransactionAttributeSourceClass() {
//		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_15) {
//			throw new IllegalStateException(
//					"AnnotationTransactionAttributeSource is only available on Java 1.5 and higher");
//		}
//		try {
//			return ClassUtils.forName(
//					ANNOTATION_TRACKING_ATTRIBUTE_SOURCE_CLASS_NAME, BtNameSpaceUtils.class.getClassLoader());
//		}
//		catch (Throwable ex) {
//			throw new IllegalStateException("Unable to load Java 1.5 dependent class [" +
//					ANNOTATION_TRACKING_ATTRIBUTE_SOURCE_CLASS_NAME + "]", ex);
//		}
//	}
}
