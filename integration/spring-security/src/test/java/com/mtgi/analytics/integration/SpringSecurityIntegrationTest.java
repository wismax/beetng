package com.mtgi.analytics.integration;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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

import com.mtgi.analytics.BehaviorEvent;
import com.mtgi.analytics.BehaviorEventPersister;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "SpringSecurityIntegrationTest-applicationContext.xml")
public class SpringSecurityIntegrationTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private TestBean bean;

    @Before
    public void initSecurity() {
        SecurityContext context = createMock(SecurityContext.class);
        Authentication auth = createMock(Authentication.class);
        GrantedAuthority[] privs = { createMock(GrantedAuthority.class) };

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

            GrantedAuthority[] auth = { createMock(GrantedAuthority.class) };
            expect(auth[0].getAuthority()).andReturn("ROLE_USER").anyTimes();

            UserDetails udx = createMock(UserDetails.class);
            expect(udx.getUsername()).andReturn(userName).anyTimes();
            expect(udx.getAuthorities()).andReturn(auth).anyTimes();
            replay(udx);
            return udx;
        }

    }

    public static class TestPersister implements BehaviorEventPersister {

        static TestPersister LAST;
        List<BehaviorEvent> persisted = new ArrayList<BehaviorEvent>();

        public void persist(Queue<BehaviorEvent> events) {
            synchronized (TestPersister.class) {
                LAST = this;
                persisted.addAll(events);
                if (!events.isEmpty())
                    TestPersister.class.notifyAll();
            }
        }

        public static void waitForEvents() throws InterruptedException {
            synchronized (TestPersister.class) {
                long start = System.currentTimeMillis();
                while ((LAST == null || LAST.persisted.isEmpty()) && (System.currentTimeMillis() - start < 10000)) {
                    TestPersister.class.wait(10000);
                }
            }
            assertTrue("events received", LAST != null && !LAST.persisted.isEmpty());
        }
    }
}
