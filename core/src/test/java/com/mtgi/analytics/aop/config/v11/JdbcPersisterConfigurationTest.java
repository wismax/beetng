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

import javax.sql.DataSource;

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
import com.mtgi.analytics.JAASSessionContext;
import com.mtgi.analytics.JdbcBehaviorEventPersisterImpl;
import com.mtgi.analytics.SessionContext;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/JdbcPersisterConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class JdbcPersisterConfigurationTest {

	@SpringBeanByName
	private BehaviorTrackingManagerImpl defaultTrackingManager;

	@SpringBeanByName
	private DataSource unitilsDS;
	
	@Test
	public void testConfiguration() throws Exception {
		assertNotNull("default tracking manager configured", defaultTrackingManager);
		assertEquals("application name set", "testApp", defaultTrackingManager.getApplication());
		assertEquals("default persister type provided", JdbcBehaviorEventPersisterImpl.class, defaultTrackingManager.getPersister().getClass());

		//verify that the custom jdbc persister configuration has been applied.
		JdbcBehaviorEventPersisterImpl persister = (JdbcBehaviorEventPersisterImpl)defaultTrackingManager.getPersister();
		assertSame("correct datasource injected into persister", unitilsDS, persister.getDataSource());
		assertEquals("custom id sql injected into persister", "select next value for SEQ_BEHAVIOR_TRACKING_EVENT from INFORMATION_SCHEMA.SYSTEM_SEQUENCES", persister.getIdSql());
		
		//verify proper configuration of log flush and rotation using private task executor and scheduler instances
		TaskExecutor executor = defaultTrackingManager.getExecutor();
		assertEquals("default executor type provided", ThreadPoolTaskExecutor.class, executor.getClass());

		//test the state of the global scheduler configuration.
		SchedulerFactory factory = new StdSchedulerFactory();
		Scheduler sched = factory.getScheduler("BeetScheduler");
		
		List<String> triggers = Arrays.asList(sched.getTriggerNames("beet"));
		assertEquals("flush job scheduled", 1, triggers.size());
		assertTrue("flush job scheduled", triggers.contains("defaultTrackingManager_flush_trigger"));
		
		Collection<?> schedulers = factory.getAllSchedulers();
		assertEquals("private scheduler created", 1, schedulers.size());
		assertTrue(schedulers.contains(sched));
		
		//verify the default session context implementation has been selected.
		SessionContext context = defaultTrackingManager.getSessionContext();
		assertNotNull(context);
		assertTrue("JAAS context used by default", context instanceof JAASSessionContext);
	}
	
}
