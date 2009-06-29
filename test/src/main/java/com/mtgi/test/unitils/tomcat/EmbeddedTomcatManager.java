package com.mtgi.test.unitils.tomcat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitils.core.UnitilsException;
import org.unitils.core.util.AnnotatedInstanceManager;

import com.mtgi.test.unitils.tomcat.annotations.DeployDescriptor;
import com.mtgi.test.unitils.tomcat.annotations.DeployExploded;
import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

public class EmbeddedTomcatManager extends AnnotatedInstanceManager<EmbeddedTomcatServer, EmbeddedTomcat> {
	
	private static final Log log = LogFactory.getLog(EmbeddedTomcatManager.class);
	
	public EmbeddedTomcatManager() {
		super(EmbeddedTomcatServer.class, EmbeddedTomcat.class);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				invalidateAll();
			}
		});
	}

	@Override
	public EmbeddedTomcatServer getInstance(Object testObject) {
		return super.getInstance(testObject);
	}

	public boolean isServerInitialized(Object testObject) {
		return testObject != null && instances.containsKey(testObject.getClass());
	}

	public EmbeddedTomcatServer configureDeployments(Object testObject, Method testMethod) throws Exception {

		EmbeddedTomcatServer server = getInstance(testObject);
		if (server != null) {
			Class<?> testClass = testObject.getClass();
			
			ArrayList<DeployDescriptor> descs = new ArrayList<DeployDescriptor>();
			DeployDescriptor ann = testObject.getClass().getAnnotation(DeployDescriptor.class);
			if (ann != null)
				descs.add(ann);
			ann = testMethod.getAnnotation(DeployDescriptor.class);
			if (ann != null)
				descs.add(ann);
			
			if (!descs.isEmpty())
				for (DeployDescriptor dep : descs)
					server.deployDescriptor(dep.contextRoot(), getDeployableResource(testClass, dep.webXml()));

			ArrayList<DeployExploded> apps = new ArrayList<DeployExploded>();
			DeployExploded app = testObject.getClass().getAnnotation(DeployExploded.class);
			if (app != null)
				apps.add(app);
			app = testMethod.getAnnotation(DeployExploded.class);
			if (app != null)
				apps.add(app);
			
			if (!apps.isEmpty())
				for (DeployExploded dep : apps)
					server.deployExploded(getDeployableResource(testClass, dep.value()));

			if (server.isAutostart())
				server.start();
		}
		return server;
	}
	
	@Override
	public void invalidateInstance(Class<?>... testClasses) {
		for (Class<?> t : testClasses) {
			try {
				EmbeddedTomcatServer server = instances.get(t);
				if (server != null) {
					try {
						server.destroy();
					} catch (Exception e) {
						log.warn("Error shutting down server for " + t.getName(), e);
					} finally {
						File home = server.getCatalinaHome();
						delete(home);
					}
				}
			} catch (Exception e) {
				log.warn("Error cleaning up server for " + t.getName(), e);
			} finally {
				super.invalidateInstance(t);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void invalidateAll() {
		Set<Class<?>> keys = instances.keySet();
		Class[] testClasses = keys.toArray(new Class[keys.size()]);
		invalidateInstance(testClasses);
	}

	@Override @SuppressWarnings("unchecked")
	protected EmbeddedTomcatServer createInstanceForValues(Object testObject, Class<?> testClass, List<String> values) {

		String version = values.get(0);
		boolean autoStart = Boolean.valueOf(values.get(1));

		String implClassName = "com.mtgi.test.unitils.tomcat." + version + ".EmbeddedTomcatServerImpl";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		try {
			Class<? extends EmbeddedTomcatServer> implClass = (Class<? extends EmbeddedTomcatServer>) loader.loadClass(implClassName);
			Constructor<? extends EmbeddedTomcatServer> con = implClass.getConstructor(File.class, Boolean.TYPE);
			return con.newInstance(createTempDir("embeddedTomcat"), autoStart);
		} catch (ClassNotFoundException e) {
			throw new UnitilsException("Unable to locate server class " + implClassName);
		} catch (Exception e) {
			throw new UnitilsException("Error initializing server class " + implClassName, e);
		}
		
	}

	@Override
	protected List<String> getAnnotationValues(EmbeddedTomcat annotation) {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(annotation.version().toString());
		ret.add(String.valueOf(annotation.start()));
		return ret;
	}

	public static File getDeployableResource(Class<?> testClass, String resourcePath) throws IOException {
		URL url = testClass.getResource(resourcePath);
		if (url == null)
			return getDeployableResource(resourcePath);
		return urlToFile(url);
	}
	
	/**
	 * Translate the given classpath resource name into a local File.
	 */
	public static File getDeployableResource(String resourcePath) throws IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
		if (url == null)
			throw new FileNotFoundException("Unable to locate classpath resource at " + resourcePath);
		
		return urlToFile(url);
	}

	/**
	 * Create an empty temporary directory with the given basename.
	 */
	public static File createTempDir(String name) throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		for (int counter = new Random().nextInt() & 0xffff; true; ++counter) {
			File dir = new File(tmp, name + counter + ".dir");
			if (!dir.exists()) {
				if (!dir.mkdirs())
					throw new IOException("Unable to create temporary directory " + dir.getAbsolutePath());
				return dir;
			}
		}
	}
	
	/**
	 * Recursively delete the given filesystem path and its children.
	 */
	public static void delete(File dir) throws IOException {
		if (dir.isDirectory())
			for (File child : dir.listFiles())
				delete(child);
		
		if (dir.exists() && !dir.delete())
			throw new IOException("Unable to delete " + dir.getAbsolutePath());
	}
	
	/**
	 * Convert a file-scheme URL to its equivalent File representation.
	 * @throws IOException if the URL is invalid, or if the file does not exist.
	 */
	public static File urlToFile(URL url) throws IOException {
		String ext = url.toExternalForm();
		ext = ext.replaceFirst("^file:/+", "/");
		ext = URLDecoder.decode(ext, "UTF-8");
		
		File location = new File(ext);
		if (!location.exists())
			throw new FileNotFoundException("No resource found at path " + location.getAbsolutePath());
		
		return location;
	}
	
}
