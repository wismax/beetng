package com.mtgi.test.unitils.tomcat;

import static org.unitils.util.ReflectionUtils.getFieldsAssignableFrom;
import static org.unitils.util.ReflectionUtils.getSettersAssignableFrom;
import static org.unitils.util.ReflectionUtils.setFieldAndSetterValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.unitils.core.Module;
import org.unitils.core.TestListener;
import org.unitils.core.UnitilsException;

import com.mtgi.test.unitils.tomcat.annotations.EmbeddedTomcat;

/**
 * Unitils module which adds support for an embedded Tomcat 6 server,
 * to which test applications can be deployed.  Most useful in combination
 * with a tool like HtmlUnit for testing server responses.
 */
public class TomcatModule implements Module {

	private EmbeddedTomcatManager manager;
	
	public void init(Properties config) {
		manager = new EmbeddedTomcatManager();
	}
	public void afterInit() {}

	public TestListener getTestListener() {
		return new Listener();
	}

	@SuppressWarnings("unchecked") 
	private static void inject(Object into, Object value, Class<?> fieldType, Class<? extends Annotation> marker) {
		Set<Field> fields = getFieldsAssignableFrom(into.getClass(), fieldType, false);
		Set<Method> setters = getSettersAssignableFrom(into.getClass(), fieldType, false);
		filter(marker, fields, setters);
		setFieldAndSetterValue(into, fields, setters, value);
	}
	
	private static void filter(Class<? extends Annotation> marker, Collection<? extends AnnotatedElement>... sets) {
		for (Collection<? extends AnnotatedElement> c : sets)
			for (Iterator<? extends AnnotatedElement> it = c.iterator(); it.hasNext(); )
				if (it.next().getAnnotation(marker) == null)
					it.remove();
	}
	
	public class Listener extends TestListener {

		@Override
		public void beforeTestSetUp(Object testObject, Method testMethod) {
			try {
				EmbeddedTomcatServer server = manager.configureDeployments(testObject, testMethod);
				if (server != null) {
					//inject server and/or base url into marked fields.
					inject(testObject, server, EmbeddedTomcatServer.class, EmbeddedTomcat.class);
					if (server.isStarted()) {
						String baseUrl = "http://localhost:" + server.getHttpPort();
						inject(testObject, baseUrl, String.class, EmbeddedTomcat.class);
					}
				}
			} catch (Exception e) {
				throw new UnitilsException(e);
			}
		}

		@Override
		public void afterTestTearDown(Object testObject, Method testMethod) {
			if (manager.isServerInitialized(testObject))
				try {
					manager.getInstance(testObject).destroy();
				} catch (Exception e) {
					throw new UnitilsException(e);
				} finally {
					//clean up injections
					inject(testObject, null, EmbeddedTomcatServer.class, EmbeddedTomcat.class);
					inject(testObject, null, String.class, EmbeddedTomcat.class);
				}
		}
		
	}
	
}
