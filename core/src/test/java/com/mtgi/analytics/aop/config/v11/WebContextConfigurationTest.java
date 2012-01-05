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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mock.web.MockServletContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.unitils.UnitilsJUnit4TestClassRunner;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.SessionContext;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
import com.mtgi.analytics.servlet.SpringSessionContext;
import com.mtgi.test.util.IOUtils;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public class WebContextConfigurationTest {

	private ThreadPoolTaskExecutor testExecutor;
	private BehaviorTrackingManagerImpl defaultTrackingManager;
	private Scheduler testScheduler;
	private ConfigurableWebApplicationContext spring;
	
    @AfterClass
    public static void deletePersisterFiles() {
        IOUtils.deleteAllFilesStartingWith(new File("."), "beet.bxml.gz");
    }
	
    @Before
	public void setUp() {
		//we shirk the usual Unitils spring initialization for us, so that we can select a non-default
		//spring application context.  The web context should switch the default initialization behavior
		//for the bt:manager tag.
		spring = new XmlWebApplicationContext();
		spring.setServletContext(new MockServletContext());
		spring.setConfigLocations(new String[]{ "com/mtgi/analytics/aop/config/v11/DefaultConfigurationTest-applicationContext.xml" });
		spring.refresh();
		
		testExecutor = (ThreadPoolTaskExecutor)spring.getBean("testExecutor");
		defaultTrackingManager = (BehaviorTrackingManagerImpl)spring.getBean("defaultTrackingManager");
		testScheduler = (Scheduler)spring.getBean("testScheduler");
	}
	
	@After
	public void tearDown() {
		try {
			spring.close();
		} finally {
			spring = null;
			testScheduler = null;
			defaultTrackingManager = null;
			testExecutor = null;
		}
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
		
		//verify the appropriate default session context implementation has been selected.
		SessionContext context = defaultTrackingManager.getSessionContext();
		assertNotNull(context);
		assertTrue("web-sensitive context used by default", context instanceof SpringSessionContext);
	}
	
}
