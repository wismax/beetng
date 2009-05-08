package com.mtgi.analytics.aop.config.v11;

import static org.junit.Assert.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorTrackingManager;

@SpringApplicationContext("com/mtgi/analytics/aop/config/v11/MBeanServerRegistrationTest-applicationContext.xml")
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class MBeanServerRegistrationTest {

	@SpringBeanByName
	private MBeanServer firstServer;
	@SpringBeanByName
	private MBeanServer secondServer;
	
	@SpringBeanByName
	private BehaviorTrackingManager manager1;
	@SpringBeanByName
	private BehaviorTrackingManager manager2;
	
	@Test
	public void testServerRegistrations() throws Exception {
		assertNotSame("different servers created", firstServer, secondServer);
		
		ObjectName m1 = new ObjectName("testApp:package=com.mtgi.analytics,group=manager1,name=BeetManager"),
			m2 = new ObjectName("testApp:package=com.mtgi.analytics,group=manager2,name=BeetManager");
        assertNotNull("first server contains mbeans for manager1", firstServer.getMBeanInfo(m1));
        assertNotNull("first server contains mbeans for manager2", firstServer.getMBeanInfo(m2));
        
        //create some events and see where the statistics beans land
        BehaviorEvent event = manager1.createEvent("test", "ello");
        manager1.start(event);
        manager1.stop(event);
        firstServer.invoke(m1, "flush", null, null);
        
        event = manager2.createEvent("test", "goodbye");
        manager2.start(event);
        manager2.stop(event);
        firstServer.invoke(m2, "flush", null, null);
        
        ObjectName stats1 = new ObjectName("testApp:type=test-monitor,name=ello");
        ObjectName stats2 = new ObjectName("testApp:type=test-monitor,name=goodbye");
        
        assertTrue("statistics on manager1 sent to first mbean server", firstServer.isRegistered(stats1));
        assertFalse("statistics on manager1 sent to first mbean server", firstServer.isRegistered(stats2));
        
        assertTrue("statistics on manager2 sent to second server", secondServer.isRegistered(stats2));
        assertFalse("statistics on manager2 sent to second server", secondServer.isRegistered(stats1));
	}
}
