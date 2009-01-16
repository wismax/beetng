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
}
