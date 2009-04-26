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

/**
 * String constants for values embedded in <code>com/mtgi/analytics/aop/config/v11/applicationContext.xml</code>.
 */
public interface ConfigurationConstants {
	/** classpath resource location for the embedded template configuration used by {@link BtManagerBeanDefinitionParser} */
	public static final String CONFIG_TEMPLATE = "com/mtgi/analytics/aop/config/v11/applicationContext.xml";
	/** prefix used on all bean names found in the {@link #CONFIG_TEMPLATE template configuration} */
	public static final String CONFIG_NAMESPACE = "com.mtgi.analytics";

	/** bean name for the default <code>bt:manager</code> configuration */
	public static final String CONFIG_MANAGER = CONFIG_NAMESPACE + ".btManager";
	/** bean name for the default <code>bt:session-context</code> configuration */
	public static final String CONFIG_SESSION_CONTEXT = CONFIG_NAMESPACE + ".btSessionContext";
	/** bean name for the default <code>bt:persister</code> configuration */
	public static final String CONFIG_PERSISTER = CONFIG_NAMESPACE + ".btPersister";
	
	/** bean name for the default private Quartz Scheduler instance used by both <code>bt:manager</code> and <code>bt:persister</code> */
	public static final String CONFIG_SCHEDULER = CONFIG_NAMESPACE + ".btScheduler";
	/** bean name for the default private TaskExecutor instance used by the private scheduler and <code>bt:manager</code> */
	public static final String CONFIG_EXECUTOR = CONFIG_NAMESPACE + ".btExecutor";

	/** bean name for an MBeanExporter used to auto-register beet JMX features */
	public static final String CONFIG_MBEAN_EXPORTER = CONFIG_NAMESPACE + ".btMBeanExporter";
	/** bean name for the naming strategy used to register MBeans */
	public static final String CONFIG_NAMING_STRATEGY = CONFIG_NAMESPACE + ".btJmxNamingStrategy";
}
