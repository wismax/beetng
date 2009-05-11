/* 
 * Copyright 2008-2009 the original author or authors.
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
 
package com.mtgi.analytics.aop.config.v11;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.SessionContext;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/SessionContextConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class SessionContextConfigurationTest {

	@SpringBeanByName
	private BehaviorTrackingManagerImpl defaultTrackingManager;
	
	@SpringBeanByName
	private TestBean testBean;

	@Test
	public void testConfiguration() throws Exception {
		assertNotNull("default tracking manager configured", defaultTrackingManager);
		assertEquals("application name set", "testApp", defaultTrackingManager.getApplication());
		assertEquals("default persister type provided", XmlBehaviorEventPersisterImpl.class, defaultTrackingManager.getPersister().getClass());

		//verify that the default xml persister configuration has been applied.
		XmlBehaviorEventPersisterImpl persister = (XmlBehaviorEventPersisterImpl)defaultTrackingManager.getPersister();
		assertTrue(persister.isBinary());
		assertTrue(persister.isCompress());
		assertTrue("default file name [" + persister.getFile() + "]", new File(persister.getFile()).getName().startsWith("beet"));

		//verify proper configuration of log flush and rotation using private task executor and scheduler instances
		TaskExecutor executor = defaultTrackingManager.getExecutor();
		assertEquals("default executor type provided", ThreadPoolTaskExecutor.class, executor.getClass());

		//test the state of the global scheduler configuration.
		SchedulerFactory factory = new StdSchedulerFactory();
		Scheduler sched = factory.getScheduler("BeetScheduler");
		
		List<String> triggers = Arrays.asList(sched.getTriggerNames("beet"));
		assertEquals("flush and rotate jobs scheduled", 2, triggers.size());
		assertTrue("flush job scheduled", triggers.contains("defaultTrackingManager_flush_trigger"));
		assertTrue("rotate job scheduled", triggers.contains("org.springframework.scheduling.quartz.CronTriggerBean_rotate_trigger"));
		
		Collection<?> schedulers = factory.getAllSchedulers();
		assertEquals("private scheduler created", 1, schedulers.size());
		assertTrue(schedulers.contains(sched));
		
		//verify the default session context implementation has been selected.
		SessionContext context = defaultTrackingManager.getSessionContext();
		assertNotNull(context);
		
		assertTrue("application context class used", context instanceof TestContext);
		assertSame("application dependency injected into context", testBean, ((TestContext)context).innerBean);
		assertEquals("literal property injected into context", "Hello from testland", ((TestContext)context).prop);
	}
	
	public static class TestBean {
	}
	
	public static class TestContext implements SessionContext {

		private String prop;
		private TestBean innerBean;
		
		public String getContextSessionId() {
			return "12345";
		}

		public String getContextUserId() {
			return "test";
		}

		public void setProp(String prop) {
			this.prop = prop;
		}

		public void setInnerBean(TestBean innerBean) {
			this.innerBean = innerBean;
		}
		
	}
}
