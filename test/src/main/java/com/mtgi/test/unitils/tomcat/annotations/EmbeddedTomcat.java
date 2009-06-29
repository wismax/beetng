package com.mtgi.test.unitils.tomcat.annotations;

import static com.mtgi.test.unitils.tomcat.annotations.TomcatVersion.v6_0;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;

/**
 * <p>Mark a test class that requires an embedded Tomact server.  Requires that the
 * Unitils {@link TomcatModule} has been enabled.</p>
 * 
 * <p>This annotation can also be used to mark a field of a test class, which must be
 * of type {@link EmbeddedTomcatServer}.  In this case, an instance of {@link EmbeddedTomcatServer}
 * will be injected into that field prior to test runs.</p>
 * 
 * @see DeployDescriptor
 */
@Target({TYPE,FIELD}) @Retention(RUNTIME) @Documented
public @interface EmbeddedTomcat {

	/**
	 * Indicate whether the embedded server should be started automatically prior to each test
	 * case.  Default is false, meaning the test case will have to call {@link EmbeddedTomcatServer#start}
	 * on an injected instance of {@link EmbeddedTomcatServer}.
	 */
	boolean start() default false;
	
	/**
	 * The version of the embedded tomcat server.  Supported versions are enumerated
	 * in {@link TomcatVersion}.
	 */
	TomcatVersion version() default v6_0;
	
}
