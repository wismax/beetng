package com.mtgi.analytics.aop.config.v11;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
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

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/XmlPersisterConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class XmlPersisterConfigurationTest {

	@SpringBeanByName
	private ThreadPoolTaskExecutor testExecutor;

	@SpringBeanByName
	private BehaviorTrackingManagerImpl xmlTracking;

	@SpringBeanByName
	private Scheduler testScheduler;

	@SpringApplicationContext
	private ConfigurableApplicationContext spring;
	
	@After
	public void cleanup() {
		spring.close();
	}
	
	@Test
	public void testXmlPersisterConfiguration() throws Exception {
		assertNotNull("custom tracking manager configured", xmlTracking);
		assertEquals("application name set", "testApp", xmlTracking.getApplication());
		assertEquals("correct persister type provided", XmlBehaviorEventPersisterImpl.class, xmlTracking.getPersister().getClass());

		XmlBehaviorEventPersisterImpl persister = (XmlBehaviorEventPersisterImpl)xmlTracking.getPersister();
		assertFalse("default setting overridden", persister.isBinary());
		assertFalse("default setting overridden", persister.isCompress());
		assertTrue("custom file name [" + persister.getFile() + "]", new File(persister.getFile()).getName().startsWith("xml-tracking"));
		
		TaskExecutor executor = xmlTracking.getExecutor();
		assertSame("application executor is used", testExecutor, executor);

		List<String> triggers = Arrays.asList(testScheduler.getTriggerNames("BehaviorTracking"));
		assertEquals("flush and rotate jobs scheduled in application scheduler", 2, triggers.size());
		assertTrue("flush job scheduled", triggers.contains("xmlTracking_flush_trigger"));
		assertTrue("rotate job scheduled", triggers.contains("org.springframework.scheduling.quartz.CronTriggerBean_rotate_trigger"));

		SchedulerFactory factory = new StdSchedulerFactory();
		assertEquals("private scheduler was not created", 1, factory.getAllSchedulers().size());
		assertSame(testScheduler, factory.getAllSchedulers().iterator().next());
	}
	
}
