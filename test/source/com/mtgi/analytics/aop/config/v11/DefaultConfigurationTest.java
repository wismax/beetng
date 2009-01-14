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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;

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
	
	@Test
	public void testDefaultConfiguration() throws Exception {
		assertNotNull("default tracking manager configured", defaultTrackingManager);
		assertEquals("application name set", "testApp", defaultTrackingManager.getApplication());
		assertEquals("default persister type provided", XmlBehaviorEventPersisterImpl.class, defaultTrackingManager.getPersister().getClass());

		XmlBehaviorEventPersisterImpl persister = (XmlBehaviorEventPersisterImpl)defaultTrackingManager.getPersister();
		assertTrue(persister.isBinary());
		assertTrue(persister.isCompress());
		assertTrue("default file name [" + persister.getFile() + "]", new File(persister.getFile()).getName().startsWith("behavior-tracking"));

		TaskExecutor executor = defaultTrackingManager.getExecutor();
		assertEquals("default executor type provided", ThreadPoolTaskExecutor.class, executor.getClass());

		assertNotSame("private executor created", testExecutor, executor);

		String[] groups = testScheduler.getTriggerGroupNames();
		for (String g : groups) {
			String[] names = testScheduler.getTriggerNames(g);
			assertEquals("no triggers scheduled in application scheduler for " + g + ": " + Arrays.asList(names), 0, names.length);
		}

		SchedulerFactory factory = new StdSchedulerFactory();
		Scheduler sched = factory.getScheduler("BehaviorTrackingScheduler");
		assertNotSame("private scheduler instance initialized", testScheduler, sched);
		
		List<String> triggers = Arrays.asList(sched.getTriggerNames("BehaviorTracking"));
		assertEquals("flush and rotate jobs scheduled", 2, triggers.size());
		assertTrue("flush job scheduled", triggers.contains("defaultTrackingManager_flush_trigger"));
		assertTrue("rotate job scheduled", triggers.contains("org.springframework.scheduling.quartz.CronTriggerBean_rotate_trigger"));
		
		Collection<?> schedulers = factory.getAllSchedulers();
		assertEquals("private scheduler and application scheduler created", 2, schedulers.size());
		assertTrue(schedulers.contains(sched));
		assertTrue(schedulers.contains(testScheduler));
	}
	
	@Test
	public void testCleanup() throws Exception {
		assertFalse("test executor is running", testExecutor.getThreadPoolExecutor().isShutdown());
		assertFalse("private executor is running", ((ThreadPoolTaskExecutor)defaultTrackingManager.getExecutor()).getThreadPoolExecutor().isShutdown());

		Scheduler sched = new StdSchedulerFactory().getScheduler("BehaviorTrackingScheduler");
		assertTrue("private scheduler is running", sched.isStarted());
		assertFalse("private scheduler is running", sched.isShutdown());
		
		spring.close();
		
		assertTrue("test executor is stopped", testExecutor.getThreadPoolExecutor().isShutdown());
		assertTrue("private executor is stopped", ((ThreadPoolTaskExecutor)defaultTrackingManager.getExecutor()).getThreadPoolExecutor().isShutdown());
		assertTrue("private scheduler is stopped", sched.isShutdown());
	}
	
}
