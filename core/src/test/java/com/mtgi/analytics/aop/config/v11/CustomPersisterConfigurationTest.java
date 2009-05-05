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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.impl.SchedulerRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorEventPersister;
import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.JAASSessionContext;
import com.mtgi.analytics.SessionContext;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/CustomPersisterConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class CustomPersisterConfigurationTest {

	@SpringBeanByName
	private TestBean testBean;
	
	@SpringBeanByName
	private BehaviorTrackingManagerImpl defaultTrackingManager;

	@Test
	public void testDefaultConfiguration() throws Exception {
		assertNotNull("default tracking manager configured", defaultTrackingManager);
		assertEquals("application name set", "testApp", defaultTrackingManager.getApplication());
		assertEquals("custom persister type provided", TestPersister.class, defaultTrackingManager.getPersister().getClass());

		//verify that the custom persister has been created
		TestPersister persister = (TestPersister)defaultTrackingManager.getPersister();
		assertSame("application dependency injected into persister", testBean, persister.innerBean);
		assertEquals("literal property injected into persister", "Hello from testland", persister.prop);

		//verify proper configuration of log flush and rotation using private task executor and scheduler instances
		TaskExecutor executor = defaultTrackingManager.getExecutor();
		assertEquals("default executor type provided", ThreadPoolTaskExecutor.class, executor.getClass());

		//test the state of the global scheduler configuration.
		Scheduler sched = (Scheduler)persister.beanFactory.getBean(ConfigurationConstants.CONFIG_SCHEDULER);
		assertEquals("scheduler has expected name", "BehaviorTrackingScheduler", sched.getSchedulerName());
		
		List<String> triggers = Arrays.asList(sched.getTriggerNames("BehaviorTracking"));
		assertEquals("flush job scheduled", 1, triggers.size());
		assertTrue("flush job scheduled", triggers.contains("defaultTrackingManager_flush_trigger"));

		SchedulerRepository repos = SchedulerRepository.getInstance();
		Collection<?> schedulers = repos.lookupAll();
		if (!schedulers.isEmpty()) {
			//prior to 2.5, spring registered the scheduler in the global repository.
			//if that's the case, verify that's the only scheduler created.
			assertEquals("private scheduler created", 1, schedulers.size());
			assertSame(sched, schedulers.iterator().next());
		}
		
		//verify the default session context implementation has been selected.
		SessionContext context = defaultTrackingManager.getSessionContext();
		assertNotNull(context);
		assertTrue("JAAS context used by default", context instanceof JAASSessionContext);
	}
	
	public static class TestBean {
	}
	
	public static class TestPersister implements BehaviorEventPersister, BeanFactoryAware {

		private String prop;
		private TestBean innerBean;
		private BeanFactory beanFactory;
		
		public void setBeanFactory(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		public void setProp(String prop) {
			this.prop = prop;
		}

		public void setInnerBean(TestBean innerBean) {
			this.innerBean = innerBean;
		}

		public void persist(Queue<BehaviorEvent> events) {
		}
		
	}
}
