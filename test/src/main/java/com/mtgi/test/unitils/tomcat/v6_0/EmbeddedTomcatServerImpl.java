package com.mtgi.test.unitils.tomcat.v6_0;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StoreBase;
import org.apache.catalina.startup.Embedded;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.tomcat.util.net.JIoEndpoint;

import com.mtgi.test.unitils.tomcat.EmbeddedTomcatServer;

public class EmbeddedTomcatServerImpl implements EmbeddedTomcatServer {

	private Embedded server;
	private EmbeddedConnector httpConnector;
	private Host host;
	private File catalinaHome;
	private boolean started;
	private boolean autostart;
	
	private MemoryStore sessions;
	private PersistentManager sessionManager;
	
	private Properties savedProperties;
	
	public EmbeddedTomcatServerImpl(File homeDir, boolean autoStart) throws Exception {
		autostart = autoStart;
		catalinaHome = initHome(homeDir);
		initServer();
	}

	private void initServer() throws Exception {
		//point catalina at the provided home directory
		server = new Embedded();
		server.setCatalinaHome(catalinaHome.getAbsolutePath());

		Engine engine = server.createEngine();
		engine.setName("embedded");
		server.addEngine(engine);
		
		host = server.createHost("localhost", new File(catalinaHome, "webapps").getAbsolutePath());
		engine.addChild(host);
		engine.setDefaultHost(host.getName());

		//bind to an available port
		httpConnector = new EmbeddedConnector();
		server.addConnector(httpConnector);
		
		//disable session persistence on restart
		sessions = new MemoryStore();
		sessionManager = new PersistentManager();
		sessionManager.setDistributable(false);
		sessionManager.setSaveOnRestart(false);
		sessionManager.setStore(sessions);
		
		Context root = server.createContext("", new File(catalinaHome, "conf").getAbsolutePath());
		root.setManager(sessionManager);
		host.addChild(root);
	}
	
	public File getCatalinaHome() {
		return catalinaHome;
	}
	
	public boolean isAutostart() {
		return autostart;
	}
	
	public void deployDescriptor(String contextRoot, File descriptorFile) {
		Context context = newContext(contextRoot, descriptorFile.getParentFile());
		context.setAltDDName(descriptorFile.getAbsolutePath());
		host.addChild(context);
	}

	public void deployExploded(File explodedDir) {
		Context context = newContext("/" + explodedDir.getName(), explodedDir);
		host.addChild(context);
	}
	
	private Context newContext(String path, File docBase) {
		Context context = server.createContext(path, docBase.getAbsolutePath());
		context.setLoader(new WebappLoader(Thread.currentThread().getContextClassLoader()));
		context.setManager(sessionManager);
		return context;
	}
	
	private File initHome(File homeDir) throws IOException {
		//copy default configuration into home directory
		URL defaultWebXml = getClass().getResource("conf/web.xml");
		if (defaultWebXml == null)
			throw new IOException("Could not load default web.xml");

		File conf = new File(homeDir, "conf");
		if (!conf.isDirectory() && !conf.mkdirs())
			throw new IOException("Could not create config directory " + conf.getAbsolutePath());
		
		FileOutputStream fos = new FileOutputStream(new File(conf, "web.xml"));
		try {
			InputStream ios = defaultWebXml.openStream();
			try {
				byte[] buf = new byte[512];
				for (int b = ios.read(buf); b >= 0; b = ios.read(buf))
					if (b > 0)
						fos.write(buf, 0, b);
			} finally {
				ios.close();
			}
		} finally {
			fos.close();
		}
		
		return homeDir;
	}

	public int getHttpPort() {
		if (!isStarted())
			throw new IllegalStateException("Cannot determine server port until after the server is started");
		return httpConnector.getLocalPort();
	}

	public void start() throws LifecycleException {
		if (!started) {
			savedProperties = System.getProperties();
			System.setProperties(new Properties(savedProperties));
			server.start();
			started = true;
		}
	}

	public void destroy() throws Exception {
		if (started) {
			server.stop();
			sessions.clear();
			started = false;
			System.setProperties(savedProperties);
			initServer();
		}
	}

	public boolean isStarted() {
		return started;
	}
	
	/**
	 * Identical to HTTP 1.1 Connector, except instead of a well-defined listener port
	 * it binds to the first available port at startup.  This port can then
	 * be accessed with a call to {@link #getLocalPort()}.
	 */
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
	
	private static class MemoryStore extends StoreBase {
		
		private Map<String,Session> sessions = Collections.synchronizedMap(new HashMap<String,Session>());
		
		public void clear() {
			sessions.clear();
		}

		public int getSize() {
			return sessions.size();
		}

		public String[] keys() {
			return sessions.keySet().toArray(new String[sessions.size()]);
		}

		public Session load(String id) {
			return sessions.get(id);
		}

		public void remove(String id) {
			sessions.remove(id);
		}

		public void save(Session session) {
			sessions.put(session.getId(), session);
		}
	}
}
