package com.mtgi.analytics;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.unitils.spring.annotation.SpringBeanByType;

public class BehaviorTrackingManagerTest extends JdbcEventTestCase {

	@SpringBeanByType
	private TaskExecutor executor;
	@SpringBeanByType
	private MockSessionContext sessionContext;
	@SpringBeanByType
	private BehaviorTrackingManagerImpl manager;
	@SpringBeanByType
	private BehaviorEventPersister persister;
	
	@After
	public void waitForQueue() throws InterruptedException {
		
		RequestContextHolder.resetRequestAttributes();
		sessionContext.reset();
		manager.flush();
		
		//we add a wait job to the end of the queue to make sure
		//any async operations left over from the last test are finished
		flushTaskExecutions();
	}

	@Test
	public void testCreateEvent() {

		assertEquals("no events pending", 0, manager.getEventsPendingFlush());
		
		sessionContext.setContextUserId("foodaddy");
		sessionContext.setContextSessionId("anklebone");
		
		//test basic creation of a new root event.
		BehaviorEvent event = manager.createEvent("foo", "testEvent");
		assertNotNull("event created", event);
		
		assertEquals("testBT", event.getApplication());
		assertEquals("foo", event.getType());
		assertEquals("testEvent", event.getName());
		assertEquals("foodaddy", event.getUserId());
		assertEquals("anklebone", event.getSessionId());
		assertNull(event.getParent());
		assertNull(event.getData());
		assertNull(event.getError());
		assertNull(event.getStart());
		assertNull(event.getDuration());
		assertTrue(event.isRoot());
		assertFalse(event.isStarted());
		assertFalse(event.isEnded());
		
		//start the event.
		manager.start(event);
		
		//test the nesting of events
		BehaviorEvent child = manager.createEvent("foo", "childEvent");
		assertNotNull("event created", child);
		assertEquals("testBT", child.getApplication());
		assertEquals("foo", child.getType());
		assertEquals("childEvent", child.getName());
		assertEquals("foodaddy", child.getUserId());
		assertEquals("anklebone", child.getSessionId());
		assertSame(event, child.getParent());
		assertNull(child.getData());
		assertNull(child.getError());
		assertNull(child.getStart());
		assertNull(child.getDuration());
		assertFalse(child.isRoot());
		assertFalse(child.isStarted());
		assertFalse(child.isEnded());
		
		//go two deep
		manager.start(child);
		
		BehaviorEvent grandChild = manager.createEvent("foo", "grandKid");
		assertEquals("testBT", grandChild.getApplication());
		assertEquals("foo", grandChild.getType());
		assertEquals("grandKid", grandChild.getName());
		assertEquals("foodaddy", grandChild.getUserId());
		assertEquals("anklebone", grandChild.getSessionId());
		assertSame(child, grandChild.getParent());
		assertFalse(grandChild.isRoot());
		
		manager.start(grandChild);
		manager.stop(grandChild);
		
		assertEquals("grandChild now pending flush", 1, manager.getEventsPendingFlush());
		
		manager.stop(child);
		assertEquals("child now pending flush", 2, manager.getEventsPendingFlush());
		
		//verify that the event stack has popped all the way up to the root now.
		BehaviorEvent child2 = manager.createEvent("foo", "child2");
		assertNotNull("event created", child2);
		assertEquals("testBT", child2.getApplication());
		assertEquals("foo", child2.getType());
		assertEquals("child2", child2.getName());
		assertEquals("foodaddy", child2.getUserId());
		assertEquals("anklebone", child2.getSessionId());
		assertSame("event stack has popped back up to root", event, child2.getParent());
		assertNull(child2.getData());
		assertNull(child2.getError());
		assertNull(child2.getStart());
		assertNull(child2.getDuration());
		assertFalse(child2.isRoot());
		assertFalse(child2.isStarted());
		assertFalse(child2.isEnded());
		
		manager.start(child2);
		manager.stop(child2);
		
		assertEquals("second child now pending flush", 3, manager.getEventsPendingFlush());
		
		manager.stop(event);
		assertEquals("all events now pending flush", 4, manager.getEventsPendingFlush());
		manager.flush();
		
		assertEquals("event queue flushed", 0, manager.getEventsPendingFlush());
		
		//verify that we get another root event.
		BehaviorEvent newRoot = manager.createEvent("bar", "baz");
		assertNull("new event is root", newRoot.getParent());
		assertTrue(newRoot.isRoot());
	}

	/** test use of default SpringSessionContext when none is specified in configuration */
	@Test
	public void testDefaultSessionContext() throws Exception {
		BehaviorTrackingManagerImpl impl = new BehaviorTrackingManagerImpl();
		impl.setApplication(manager.getApplication());
		impl.setExecutor(executor);
		impl.setPersister(persister);
		impl.setFlushThreshold(5);
		impl.afterPropertiesSet();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteUser("testUser");
		ServletRequestAttributes atts = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(atts);
		
		BehaviorEvent event = impl.createEvent("foo", "testEvent");
		assertNotNull("event created", event);
		
		assertEquals("testBT", event.getApplication());
		assertEquals("foo", event.getType());
		assertEquals("testEvent", event.getName());
		assertEquals("testUser", event.getUserId());
		assertEquals(request.getSession().getId(), event.getSessionId());
		assertNull(event.getParent());
		assertNull(event.getData());
		assertNull(event.getError());
		assertNull(event.getStart());
		assertNull(event.getDuration());
		assertTrue(event.isRoot());
		assertFalse(event.isStarted());
		assertFalse(event.isEnded());
		
		//start the event.
		impl.start(event);
		impl.stop(event);
		
		assertEquals("all events now pending flush", 1, impl.getEventsPendingFlush());
		impl.flush();
		
		assertEquals("event queue flushed", 0, impl.getEventsPendingFlush());
	}

	@Test
	public void testStartEvent() {
		//test basic creation of a new root event.
		BehaviorEvent event = manager.createEvent("foo", "root");
		assertNull(event.getParent());

		//create another root event
		BehaviorEvent dup = manager.createEvent("foo", "secondRoot");
		assertNull(dup.getParent());
		
		long start = System.currentTimeMillis();
		manager.start(event);

		assertTrue("event started", event.isStarted());
		assertFalse("event running", event.isEnded());
		assertTrue("event started at correct time", event.getStart().getTime() >= start);
		
		try {
			manager.start(dup);
			fail("attempt to start an event that is not a child of the current event should fail");
		} catch (IllegalStateException expected) {}
		
		assertTrue("event started", event.isStarted());
		assertFalse("event running", event.isEnded());
		assertFalse("second event is *not* started", dup.isStarted());
		
		try {
			manager.start(event);
			fail("duplicate start on new event should fail");
		} catch (IllegalStateException expected) {}
		
		assertTrue("event started", event.isStarted());
		assertFalse("event running", event.isEnded());
		
		manager.stop(event);
		assertTrue(event.isEnded());

		Date origStart = event.getStart();
		try {
			manager.start(event);
			fail("attempt to re-start completed event should fail");
		} catch (IllegalStateException expected) {}

		assertSame("start date unchanged", origStart, event.getStart());
		assertTrue("event still ended", event.isEnded());
		
		//attempt to start a new event should be ok now.
		manager.start(dup);
		assertTrue("dup event is running now", dup.isStarted());
		assertFalse("dup event is running now", dup.isEnded());
		
		BehaviorEvent child = manager.createEvent("foo", "child");
		assertSame(dup, child.getParent());
		
		manager.start(child);
		manager.stop(child);
		manager.stop(dup);
		
		assertTrue("second event is finished", dup.isEnded());
	}
	
	@Test
	public void testStopEvent() {
		BehaviorEvent event = manager.createEvent("foo", "root");
		BehaviorEvent dup = manager.createEvent("foo", "secondRoot");
		
		manager.start(event);

		try {
			manager.stop(dup);
			fail("attempt to stop an event other than the running one should fail");
		} catch (IllegalStateException expected) {}
		
		assertTrue("event started", event.isStarted());
		assertFalse("event running", event.isEnded());
		assertFalse("second event is *not* started", dup.isStarted());
		
		manager.stop(event);
		assertTrue(event.isEnded());

		//attempt to start a new event should be ok now.
		manager.start(dup);
		assertTrue("dup event is running now", dup.isStarted());
		assertFalse("dup event is running now", dup.isEnded());
		
		BehaviorEvent child = manager.createEvent("foo", "child");
		assertSame(dup, child.getParent());
		
		manager.start(child);
		
		try {
			manager.stop(dup);
			fail("attempt to stop an event while its children are in progress should fail");
		} catch (IllegalStateException expected) {}

		assertTrue(dup.isStarted());
		assertTrue(child.isStarted());
		assertFalse(child.isEnded());
		assertFalse(dup.isEnded());
			
		manager.stop(child);
		assertTrue(child.isEnded());
		
		try {
			manager.stop(event);
			fail("attempt to re-stop a finished event should fail");
		} catch (IllegalStateException expected) {}
		
		manager.stop(dup);
		assertTrue("second event is finished", dup.isEnded());
	}
	
	@Test
	public void testAutoFlush() throws SQLException, InterruptedException {
		//configured flush threshold is five.  build up a backlog of events exceeding it.
		for (int i = 0; i < 3; ++i) {
			BehaviorEvent evt = manager.createEvent("app", "event[" + i + "]");
			manager.start(evt);
			manager.stop(evt);
		}
		
		assertEquals("no events persisted yet", 0, countEventsOfType("app"));
		
		BehaviorEvent root = manager.createEvent("app", "event[3]");
		manager.start(root);
		
		//start and stop nested events, pushing past the flush threshold.
		for (int i = 0; i < 2; ++i) {
			BehaviorEvent evt = manager.createEvent("app", "child[" + i + "]");
			manager.start(evt);
			manager.stop(evt);
		}
		
		flushTaskExecutions();
		assertEquals("completed events have been persisted", 5, countEventsOfType("app"));
		assertEquals("one flush event persisted", 1, countEventsOfType("behavior-tracking"));
		
		manager.stop(root);
		flushTaskExecutions();
		assertEquals("extra flush not triggered", 5, countEventsOfType("app"));
		assertEquals("flush event persisted", 1, countEventsOfType("behavior-tracking"));
		
		manager.flush();
		flushTaskExecutions();
		assertEquals("extra event flushed", 6, countEventsOfType("app"));
		assertEquals("flush event persisted", 2, countEventsOfType("behavior-tracking"));
		
		String[] events = { "event[0]", "event[1]", "event[2]", "event[3]", "child[0]", "child[1]" };
		ResultSet rs = stmt.executeQuery("select event_name from BEHAVIOR_TRACKING_EVENT where event_type='app' order by event_id");
		for (int i = 0; i < events.length; ++i) {
			assertTrue("got event[" + i + "]", rs.next());
			assertEquals("event[" + i + "] has correct name", events[i], rs.getString(1));
		}
		assertFalse(rs.next());
		rs.close();
	}
	
	@Test
	public void testSuspendAndResume() throws InterruptedException, SQLException {
		
		assertFalse("manager is not in suspended state", manager.isSuspended());
		
		//configured flush threshold is five.  build up a backlog of events exceeding it.
		for (int i = 0; i < 3; ++i) {
			BehaviorEvent evt = manager.createEvent("app", "event[" + i + "]");
			manager.start(evt);
			manager.stop(evt);
		}
		
		assertEquals("no events persisted yet", 0, countEventsOfType("app"));
		
		BehaviorEvent root = manager.createEvent("app", "event[3]");
		manager.start(root);
		
		assertNotNull("status message returned on suspend", manager.suspend());
		assertTrue("manager is now suspended", manager.isSuspended());
		
		//start and stop nested events, pushing past the flush threshold.
		for (int i = 0; i < 2; ++i) {
			BehaviorEvent evt = manager.createEvent("app", "child[" + i + "]");
			manager.start(evt);
			manager.stop(evt);
		}
		
		flushTaskExecutions();
		assertEquals("no events persisted yet", 0, countEventsOfType("app"));
		
		assertNotNull("status message returned on resume", manager.resume());
		assertFalse("event logging resumed", manager.isSuspended());
		
		for (int i = 0; i < 2; ++i) {
			BehaviorEvent evt = manager.createEvent("app", "child[" + (i + 2) + "]");
			manager.start(evt);
			manager.stop(evt);
		}
		
		flushTaskExecutions();
		assertEquals("completed events have been persisted", 5, countEventsOfType("app"));
		assertEquals("one flush event persisted", 1, countEventsOfType("behavior-tracking"));
		
		manager.stop(root);
		flushTaskExecutions();
		assertEquals("extra flush not triggered", 5, countEventsOfType("app"));
		assertEquals("still only one flush event", 1, countEventsOfType("behavior-tracking"));
		
		manager.flush();
		flushTaskExecutions();
		assertEquals("extra event flushed", 6, countEventsOfType("app"));
		assertEquals("new flush event persisted", 2, countEventsOfType("behavior-tracking"));
		
		//verify that only events fired outside of suspended period were logged
		String[] events = { "event[0]", "event[1]", "event[2]", "event[3]", "child[2]", "child[3]" };
		ResultSet rs = stmt.executeQuery("select event_name from BEHAVIOR_TRACKING_EVENT where event_type='app' order by event_id");
		for (int i = 0; i < events.length; ++i) {
			assertTrue("got event[" + i + "]", rs.next());
			assertEquals("event[" + i + "] has correct name", events[i], rs.getString(1));
		}
		assertFalse(rs.next());
		rs.close();
	}
	
	@Test
	public void testThreadSafety() throws InterruptedException, SQLException {
		
		EventGenerator[] threads = new EventGenerator[20];
		Semaphore in = new Semaphore(0),
				  out = new Semaphore(0);
		for (int i = 0; i < threads.length; ++i) {
			threads[i] = new EventGenerator("thread[" + i + "]", in, out);
			threads[i].start();
		}

		//release the threads to do their work
		in.release(threads.length);
		//wait for all to finish
		out.acquire(threads.length);
		
		//let them all quiesce.
		for (int i = 0; i < threads.length; ++i) {
			threads[i].join(10000);
			assertFalse("thread[" + i + "] has exited", threads[i].isAlive());
		}

		//make sure lingering autoflushes are done, and perform a manual flush to pick up stragglers.
		flushTaskExecutions();
		manager.flush();
		assertEquals("no uncommitted events remain", 0, manager.getEventsPendingFlush());
		
		//do an initial count to see how we look.
		ResultSet rs = stmt.executeQuery("select count(event_id) from BEHAVIOR_TRACKING_EVENT where event_type != 'behavior-tracking'");
		assertTrue(rs.next());
		int ret = rs.getInt(1);
		rs.close();
		assertEquals("all threads' events are committed", 39 * threads.length, ret);
		
		//let each thread verify that all of its data was committed
		for (EventGenerator g : threads)
			g.verifyEvents();
	}
	
	/** verify that synchronous event persistence works as expected (though a warning should be logged) */
	@Test
	public void testSynchronousFlush() throws Exception {
		SyncTaskExecutor ste = new SyncTaskExecutor();
		BehaviorTrackingManagerImpl impl = new BehaviorTrackingManagerImpl();
		impl.setExecutor(ste);
		impl.setApplication(manager.getApplication());
		impl.setPersister(persister);
		impl.setFlushThreshold(2);
		impl.afterPropertiesSet();
		
		BehaviorEvent root = impl.createEvent("test", "hello");
		impl.start(root);
		
		//flush should be handled synchronously from inside call to stop().
		for (int i = 0; i < 6; ++i) {
			BehaviorEvent child = impl.createEvent("test", "foo");
			child.addData().add("index", i);
			impl.start(child);
			impl.stop(child);
		}
		
		impl.stop(root);
	}
	
	private int countEventsOfType(String type) throws SQLException {
		ResultSet rs = stmt.executeQuery("select count(event_id) from BEHAVIOR_TRACKING_EVENT where event_type = '" + type + "'");
		assertTrue(rs.next());
		int ret = rs.getInt(1);
		rs.close();
		return ret;
	}
	
	private void flushTaskExecutions() throws InterruptedException {
		Waiter waiter = new Waiter();
		executor.execute(waiter);
		waiter.waitFor();
	}
	
	private static class Waiter implements Runnable {
		
		private Object sync = new Object();
		private boolean finished = false;
		
		public void waitFor() throws InterruptedException {
			synchronized (sync) {
				if (!finished)
					sync.wait(60000);
				assertTrue("timed out waiting for queue to flush", finished);
			}
		}
		
		public void run() {
			synchronized (sync) {
				finished = true;
				sync.notifyAll();
			}
		}
	}
	
	private class EventGenerator extends Thread {
		
		private Semaphore in;
		private Semaphore out;
		
		public EventGenerator(String name, Semaphore in, Semaphore out) {
			super(name);
			this.in = in;
			this.out = out;
		}
		
		public void verifyEvents() throws SQLException {

			ResultSet l0 = stmt.executeQuery(
					"select * from BEHAVIOR_TRACKING_EVENT " +
					"where event_type='level-0' " +
					"and event_name like '" + getName() + "%' " +
					"order by event_id"
			);
			for (int a = 0; a < 3; ++a) {
				assertTrue("found event[" + a + "] at level 0 for " + getName(), l0.next());
				long l0_id = l0.getLong("event_id");
				
				ResultSet l1 = stmt.executeQuery(
						"select * from BEHAVIOR_TRACKING_EVENT " +
						"where event_type='level-1' " +
						"and event_name like '" + getName() + "%' " +
						"and parent_event_id=" + l0_id +
						"order by event_id"
				);
				
				for (int b = 0; b < 3; ++b) {
					assertTrue("found event[" + b + "] at level 1 for " + getName(), l1.next());
					long l1_id = l1.getLong("event_id");
					
					ResultSet l2 = stmt.executeQuery(
							"select * from BEHAVIOR_TRACKING_EVENT " +
							"where event_type='level-2' " +
							"and event_name like '" + getName() + "%' " +
							"and parent_event_id=" + l1_id +
							"order by event_id"
					);
					
					for (int c = 0; c < 3; ++c) {
						assertTrue("found event[" + c + "] at level 2 for " + getName(), l2.next());
					}
					
					assertFalse("no more l2 events", l2.next());
					l2.close();
				}
				
				assertFalse("no more l1 events", l1.next());
				l1.close();
			}
			
			assertFalse("no more l0 events", l0.next());
			l0.close();
		}

		@Override
		public void run() {
			
			try {
				in.acquire(1);
			
				for (int a = 0; a < 3; ++a) {
					BehaviorEvent l0 = manager.createEvent("level-0", getName() + "[" + a + "]");
					manager.start(l0);
					for (int b = 0; b < 3; ++b) {
						BehaviorEvent l1 = manager.createEvent("level-1", getName() + "[" + b + "]");
						manager.start(l1);
						for (int c = 0; c < 3; ++c) {
							BehaviorEvent l2 = manager.createEvent("level-2", getName() + "[" + c + "]");
							manager.start(l2);
							Thread.sleep(10);
							manager.stop(l2);
						}
						manager.stop(l1);
					}
					manager.stop(l0);
				}
				
			} catch (InterruptedException failure) {
				throw new RuntimeException(failure);
			} finally {
				out.release(1);
			}
			
		}
	}
}
