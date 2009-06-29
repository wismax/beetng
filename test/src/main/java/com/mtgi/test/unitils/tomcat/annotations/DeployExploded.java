package com.mtgi.test.unitils.tomcat.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Deploy an exploded WAR directory to an {@link EmbeddedTomcat} server.
 */
@Target({TYPE,METHOD}) @Retention(RUNTIME) @Documented
public @interface DeployExploded {
	/** classpath resource identifying the root of the exploded web application directory. */
	String value();
}
