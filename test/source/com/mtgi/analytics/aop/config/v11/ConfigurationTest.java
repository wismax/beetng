package com.mtgi.analytics.aop.config.v11;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/ConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class ConfigurationTest {

	@SpringBeanByName
	private BehaviorTrackingManagerImpl defaultTrackingManager;
	//private BehaviorTrackingManagerImpl xmlManager;
	
	@Test
	public void testDefaultConfiguration() {
		assertNotNull("default tracking manager configured", defaultTrackingManager);
		assertEquals("application name set", "testApp", defaultTrackingManager.getApplication());
		assertEquals("default persister type provided", XmlBehaviorEventPersisterImpl.class, defaultTrackingManager.getPersister().getClass());
		
		XmlBehaviorEventPersisterImpl persister = (XmlBehaviorEventPersisterImpl)defaultTrackingManager.getPersister();
		assertTrue(persister.isBinary());
		assertTrue(persister.isCompress());
		
		TaskExecutor executor = defaultTrackingManager.getExecutor();
		assertEquals("default executor type provided", ThreadPoolTaskExecutor.class, executor.getClass());
	}
	
}
