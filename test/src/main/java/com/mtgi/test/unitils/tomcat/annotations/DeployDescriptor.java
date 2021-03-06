package com.mtgi.test.unitils.tomcat.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Configuration a webapp descriptor for automatic deployment into an
 * {@link EmbeddedTomcat} server instance.
 */
@Target({TYPE,METHOD}) @Retention(RUNTIME) @Documented
public @interface DeployDescriptor {

	/**
	 * The context root at which the given application should be deployed.
	 */
	public String contextRoot();
	/**
	 * The classpath resource location at which the web.xml descriptor can be found.
	 * The resource will attempt to be located relative to the package of the test
	 * class; if it cannot be found there, it will be scanned relative to the
	 * Thread context classloader.
	 */
	public String webXml();
	
}
