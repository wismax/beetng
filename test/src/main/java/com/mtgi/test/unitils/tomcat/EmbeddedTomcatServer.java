package com.mtgi.test.unitils.tomcat;

import java.io.File;

/**
 * Control API for an embedded Tomcat instance.  Instances of this
 * class may be configured and/or injected into test cases using the
 * {@link EmbeddedTomcat} annotation.
 */
public interface EmbeddedTomcatServer {

	/**
	 * Start the server, if it is not already running.
	 * @throws Exception if the server could not be started
	 */
	public void start() throws Exception;
	/**
	 * Shutdown the server, if it is already running.  The server may
	 * be restarted with a call to {@link #start()}.
	 * @throws Exception if there was an error shutting the server down
	 */
	public void destroy() throws Exception;
	/**
	 * @return true if the server has been started with a call to {@link #start()}, false otherwise
	 */
	public boolean isStarted();

	/**
	 * Get the HTTP port for the server, if it is running.  It is invalid to call
	 * this method before the server has been started.
	 * @throws IllegalStateException if the server is not yet running
	 */
	public int getHttpPort();
	
	/**
	 * Deploy an exploded web application directory to this server.
	 */
	public void deployExploded(File explodedDir);
	/**
	 * Deploy a standalone web.xml descriptor at the given context root to this server.
	 */
	public void deployDescriptor(String contextRoot, File descriptorFile);
	
}
