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

import java.util.Queue;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jmx.support.JmxUtils;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorEventPersister;
import com.mtgi.analytics.BehaviorTrackingManagerImpl;
import com.mtgi.analytics.ChainingEventPersisterImpl;
import com.mtgi.analytics.XmlBehaviorEventPersisterImpl;
import com.mtgi.analytics.jmx.StatisticsMBeanEventPersisterImpl;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/PersisterChainConfigurationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class PersisterChainConfigurationTest {

	@SpringBeanByName
	private BehaviorTrackingManagerImpl multiTracking;

	@SpringApplicationContext
	private ConfigurableApplicationContext spring;
	
	@SpringBeanByName
	private CustomPersister customPersister;
	
	@After
	public void cleanup() {
		spring.close();
	}
	
	@Test
	public void testXmlPersisterConfiguration() throws Exception {
		assertNotNull("custom tracking manager configured", multiTracking);
		assertEquals("application name set", "testApp", multiTracking.getApplication());
		assertEquals("correct persister type provided", ChainingEventPersisterImpl.class, multiTracking.getPersister().getClass());

		ChainingEventPersisterImpl persister = (ChainingEventPersisterImpl)multiTracking.getPersister();
		BehaviorEventPersister[] delegates = persister.getDelegates().toArray(new BehaviorEventPersister[0]);
		assertEquals("all delegates registered", 3, delegates.length);
		assertEquals("first delegate has correct type", XmlBehaviorEventPersisterImpl.class, delegates[0].getClass());
		assertEquals("second delegate has correct type", StatisticsMBeanEventPersisterImpl.class, delegates[1].getClass());
		assertSame("third delegate is reference to top-level bean", customPersister, delegates[2]);
		
		//verify that MBeans have been registered
        MBeanServer server = JmxUtils.locateMBeanServer();
        assertNotNull("manager mbean found", server.getMBeanInfo(new ObjectName("testApp:package=com.mtgi.analytics,group=multiTracking,name=BeetManager")));
	}

	public static class CustomPersister implements BehaviorEventPersister {
		public void persist(Queue<BehaviorEvent> events) {
		}
	}
}
