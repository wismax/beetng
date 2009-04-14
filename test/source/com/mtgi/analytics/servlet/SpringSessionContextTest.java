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
