package com.mtgi.test.unitils.spring;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.unitils.core.TestListener;
import org.unitils.spring.SpringModule;

public class SpringLifecycleModule extends SpringModule {

	@Override
	public TestListener getTestListener() {
		return new Listener();
	}

	public class Listener extends SpringModule.SpringTestListener {

		@Override
		public void beforeTestSetUp(Object testObject, Method testMethod) {
			if (isApplicationContextConfiguredFor(testObject)) {
				ApplicationContext ctx = getApplicationContext(testObject);
				if (ctx instanceof ConfigurableApplicationContext) {
					ConfigurableApplicationContext cac = (ConfigurableApplicationContext)ctx;
					if (!cac.isActive())
						cac.refresh();
				}
			}
			super.beforeTestSetUp(testObject, testMethod);
		}

		@Override
		public void afterTestTearDown(Object testObject, Method testMethod) {
			if (isApplicationContextConfiguredFor(testObject)) {
				ApplicationContext ctx = getApplicationContext(testObject);
				if (ctx instanceof ConfigurableApplicationContext)
					((ConfigurableApplicationContext)ctx).close();
			}
		}
		
	}
	
}
