package com.mtgi.test.unitils.tomcat.annotations;

import com.mtgi.test.unitils.tomcat.TomcatVersion;

public @interface EmbeddedTomcat {

	boolean start() default false;	
	TomcatVersion version() default TomcatVersion.v5_5;
	
}
