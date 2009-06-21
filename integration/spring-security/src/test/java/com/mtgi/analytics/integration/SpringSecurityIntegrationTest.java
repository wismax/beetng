package com.mtgi.analytics.integration;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="SpringSecurityIntegrationTest-applicationContext.xml")
public class SpringSecurityIntegrationTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private TestBean bean;

	@Before
	public void initSecurity() {
		SecurityContext context = createMock(SecurityContext.class);
		Authentication auth = createMock(Authentication.class);
		GrantedAuthority[] privs = {
			createMock(GrantedAuthority.class)	
		};
		
		expect(privs[0].getAuthority()).andReturn("ROLE_USER").anyTimes();
		expect(auth.getAuthorities()).andReturn(privs).anyTimes();
		expect(auth.getName()).andReturn("testUser").anyTimes();
		expect(auth.isAuthenticated()).andReturn(true).anyTimes();
		expect(context.getAuthentication()).andReturn(auth).anyTimes();
		replay(context, auth, privs[0]);
		
		SecurityContextHolder.setContext(context);
	}

	@After
	public void clearSecurity() {
		SecurityContextHolder.clearContext();
	}
	
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
			
			GrantedAuthority[] auth = {
				createMock(GrantedAuthority.class)	
			};
			expect(auth[0].getAuthority()).andReturn("ROLE_USER").anyTimes();
			
			UserDetails udx = createMock(UserDetails.class);
			expect(udx.getUsername()).andReturn(userName).anyTimes();
			expect(udx.getAuthorities()).andReturn(auth).anyTimes();
			replay(udx);
			return udx;
		}
		
	}
	
}
