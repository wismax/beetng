package com.mtgi.test.unitils.tomcat;

import java.io.File;

public interface EmbeddedTomcatServer {

	public void start();
	public void destroy();
	
	public int getHttpPort();
	
	public void deployDescriptor(String contextRoot, File explodedDir);
	
}
