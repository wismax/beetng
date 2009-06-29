package com.mtgi.test.unitils.tomcat.annotations;

import static com.mtgi.test.unitils.tomcat.annotations.TomcatVersion.v6_0;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;

/**
 * <p>Mark a test that requires an embedded Tomact server.  Typically this will
 * be used in conjunction with one or more {@link DeployDescriptor} or {@link DeployExploded}
 * annotations to deploy test applications to the embedded server.  Requires that the
 * Unitils {@link TomcatModule} has been enabled.</p>
 * 
 * <p>This annotation can occur in several places:
 * <ul>
 *   <li>On a class, enables the embedded server for all tests in the class</li>
 *   <li>On a test method, enables the embedded server for that test method</li>
 *   <li>On a field or setter of type {@link EmbeddedTomcatServer}, enables injection of 
 *   	a {@link EmbeddedTomcatServer} instance into that field prior to test runs.</li>
 *   <li>On a field or setter of type {@link String}, injects the base URL at which the
 *      test server can be reached.  <emphasis>Only works if {@link #start() autostart}
 *      has been enabled for the server</emphasis>.</li>
 * </ul>
 * </p>
 * 
 * @see DeployDescriptor
 * @see DeployExploded
 */
@Target({TYPE,METHOD,FIELD}) @Retention(RUNTIME) @Documented
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
