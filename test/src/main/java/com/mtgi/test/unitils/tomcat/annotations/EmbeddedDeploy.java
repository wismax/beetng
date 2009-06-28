package com.mtgi.test.unitils.tomcat.annotations;

public @interface EmbeddedDeploy {

	public String contextRoot() default "";
	public String value() default "";
	
}
