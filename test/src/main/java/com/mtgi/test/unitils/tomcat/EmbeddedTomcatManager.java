package com.mtgi.test.unitils.tomcat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitils.core.UnitilsException;
import org.unitils.core.util.AnnotatedInstanceManager;

import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

public class EmbeddedTomcatManager extends AnnotatedInstanceManager<EmbeddedTomcatServer, EmbeddedTomcat> {
	
	private static final Log log = LogFactory.getLog(EmbeddedTomcatManager.class);
	
	public EmbeddedTomcatManager(Class<EmbeddedTomcatServer> instanceClass, Class<EmbeddedTomcat> annotationClass) {
		super(instanceClass, annotationClass);
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

	@Override
	public void invalidateInstance(Class<?>... testClasses) {
		for (Class<?> t : testClasses) {
			try {
				EmbeddedTomcatServer server = instances.get(t);
				if (server != null)
					server.destroy();
			} catch (Exception e) {
				log.warn("Error shutting down server for " + t.getName(), e);
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
		String implClassName = "com.mtgi.test.unitils.tomcat." + version + ".EmbeddedTomcatServerImpl";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<? extends EmbeddedTomcatServer> implClass = null;
		try {
			implClass = (Class<? extends EmbeddedTomcatServer>) loader.loadClass(implClassName);
			return implClass.newInstance();
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
		return ret;
	}

	public static File getDeployableResource(String resourcePath) throws IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
		if (url == null)
			throw new FileNotFoundException("Unable to locate classpath resource at " + resourcePath);
		
		return urlToFile(url);
	}

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
