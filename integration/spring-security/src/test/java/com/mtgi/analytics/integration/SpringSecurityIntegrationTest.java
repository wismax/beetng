package com.mtgi.analytics.integration;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="SpringSecurityIntegrationTest-applicationContext.xml",
		loader=SpringSecurityIntegrationTest.TestLoader.class)
public class SpringSecurityIntegrationTest {

	@Autowired
	private TestBean bean;
	
	@Test
	public void testInitialization() {
		assertNotNull(bean.getDataTracked());
	}
	
	public static class TestBean {
		public String getDataTracked() {
			return String.valueOf(System.currentTimeMillis());
		}
	}
	
	public static class TestLoader extends AbstractContextLoader {

		public ConfigurableApplicationContext loadContext(String... locations) throws Exception {
			return new ClassPathXmlApplicationContext(locations);
		}

		@Override
		protected String getResourceSuffix() {
			return null;
		}
		
		
	}
	
	public static class MockUserService implements UserDetailsService {

		public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
			UserDetails udx = createMock(UserDetails.class);
			expect(udx.getUsername()).andReturn(userName).anyTimes();
			return udx;
		}
		
	}
	
}
