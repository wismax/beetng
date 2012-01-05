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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.JAASSessionContext;
import com.mtgi.analytics.SessionContext;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
import com.mtgi.test.util.IOUtils;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/DefaultConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class DefaultConfigurationTest {

	@SpringBeanByName
	private ThreadPoolTaskExecutor testExecutor;

	@SpringBeanByName
	private BehaviorTrackingManagerImpl defaultTrackingManager;

	@SpringBeanByName
	private Scheduler testScheduler;

	@SpringApplicationContext
	private ConfigurableApplicationContext spring;
	
	@AfterClass
    public static void deletePersisterFiles() {
	    IOUtils.deleteAllFilesStartingWith(new File("."), "beet.bxml.gz");
	}

	@Test
	public void testDefaultConfiguration() throws Exception {
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

		assertNotSame("private executor created", testExecutor, executor);

		String[] groups = testScheduler.getTriggerGroupNames();
		for (String g : groups) {
			String[] names = testScheduler.getTriggerNames(g);
			assertEquals("no triggers scheduled in application scheduler for " + g + ": " + Arrays.asList(names), 0, names.length);
		}

		//test the state of the global scheduler configuration.
		SchedulerFactory factory = new StdSchedulerFactory();
		Scheduler sched = factory.getScheduler("BeetScheduler");
		assertNotSame("private scheduler instance initialized", testScheduler, sched);
		
		List<String> triggers = Arrays.asList(sched.getTriggerNames("beet"));
		assertEquals("flush and rotate jobs scheduled", 2, triggers.size());
		assertTrue("flush job scheduled", triggers.contains("defaultTrackingManager_flush_trigger"));
		assertTrue("rotate job scheduled", triggers.contains("org.springframework.scheduling.quartz.CronTriggerBean_rotate_trigger"));
		
		Collection<?> schedulers = factory.getAllSchedulers();
		assertEquals("private scheduler and application scheduler created", 2, schedulers.size());
		assertTrue(schedulers.contains(sched));
		assertTrue(schedulers.contains(testScheduler));
		
		//verify the default session context implementation has been selected.
		SessionContext context = defaultTrackingManager.getSessionContext();
		assertNotNull(context);
		assertTrue("JAAS context used by default", context instanceof JAASSessionContext);
	}
	
	@Test
	public void testCleanup() throws Exception {
		assertFalse("test executor is running", testExecutor.getThreadPoolExecutor().isShutdown());
		assertFalse("private executor is running", ((ThreadPoolTaskExecutor)defaultTrackingManager.getExecutor()).getThreadPoolExecutor().isShutdown());

		Scheduler sched = new StdSchedulerFactory().getScheduler("BeetScheduler");
		assertTrue("private scheduler is running", sched.isStarted());
		assertFalse("private scheduler is running", sched.isShutdown());
		
		spring.close();
		
		assertTrue("test executor is stopped", testExecutor.getThreadPoolExecutor().isShutdown());
		assertTrue("private executor is stopped", ((ThreadPoolTaskExecutor)defaultTrackingManager.getExecutor()).getThreadPoolExecutor().isShutdown());
		assertTrue("private scheduler is stopped", sched.isShutdown());
	}
	
}
