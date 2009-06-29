package com.mtgi.test.unitils.tomcat.v6_0;

import java.io.File;
import java.io.IOException;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Embedded;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.net.JIoEndpoint;

import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;

public class EmbeddedTomcatServerImpl implements EmbeddedTomcatServer {

	private Embedded server;
	private EmbeddedConnector httpConnector;
	private Host host;
	private File catalinaHome;
	private boolean started;

	public EmbeddedTomcatServerImpl() throws Exception {
		
		catalinaHome = new File(".");
		
		server = new Embedded();
		Engine engine = server.createEngine();
		server.addEngine(engine);
		
		host = server.createHost("localhost", catalinaHome.getAbsolutePath());
		engine.addChild(host);

		httpConnector = new EmbeddedConnector();
		server.addConnector(httpConnector);
	}
	
	public void deployDescriptor(String contextRoot, File descriptorFile) {
		StandardContext context = new StandardContext();
		context.setPath(contextRoot);
		context.setDocBase(descriptorFile.getParentFile().getAbsolutePath());
		context.setAltDDName(descriptorFile.getAbsolutePath());
		
		ContextConfig config = new ContextConfig();
		((Lifecycle) context).addLifecycleListener(config);

		host.addChild(context);
	}

	public void deployExploded(File explodedDir) {
		// TODO Auto-generated method stub

	}

	public void destroy() throws LifecycleException {
		if (started) {
			server.stop();
			started = false;
		}
	}

	public int getHttpPort() {
		if (!isStarted())
			throw new IllegalStateException("Cannot determine server port until after the server is started");
		return httpConnector.getLocalPort();
	}

	public void start() throws LifecycleException {
		if (!started) {
			server.start();
			started = true;
		}
	}

	public boolean isStarted() {
		return started;
	}
	
	public static class EmbeddedConnector extends Connector {

		private EmbeddedProtocolAdapter localProtocolAdapter;
		
		public EmbeddedConnector() throws Exception {
			super("HTTP/1.1");
			this.protocolHandler = localProtocolAdapter = new EmbeddedProtocolAdapter();
			setPort(0);
			setSecure(false);
		}
		
		public int getLocalPort() {
			return localProtocolAdapter.getLocalPort();
		}
		
		public static class EmbeddedProtocolAdapter extends Http11Protocol {

			private Endpoint localEndpoint;
			
			public EmbeddedProtocolAdapter() {
				super();
				this.endpoint = localEndpoint = new Endpoint();
			}
			
			public int getLocalPort() {
				return localEndpoint.getLocalPort();
			}

			public static class Endpoint extends JIoEndpoint {
				public int getLocalPort() {
					return serverSocket == null ? -1 : serverSocket.getLocalPort();
				}
			}
			
		}
	}
}
