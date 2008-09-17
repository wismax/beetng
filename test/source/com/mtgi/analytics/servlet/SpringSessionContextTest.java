package com.mtgi.analytics.servlet;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SpringSessionContextTest {

	private SpringSessionContext inst;
	
	@Before
	public void setUp() {
		inst = new SpringSessionContext();
	}
	
	@After
	public void tearDown() {
		inst = null;
		RequestContextHolder.resetRequestAttributes();
	}
	
	@Test
	public void testUnauthenticated() {
		assertNull("no user bound to thread", inst.getContextUserId());
		assertNull("no session bound to thread", inst.getContextSessionId());
	}
	
	@Test
	public void testAuthenticated() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteUser("testUser");
		ServletRequestAttributes atts = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(atts);
		
		assertEquals("context inherits user from request attributes", "testUser", inst.getContextUserId());
		assertEquals("context inherits session from request attributes", request.getSession().getId(), inst.getContextSessionId());
	}
	
}
