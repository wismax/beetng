package com.mtgi.analytics;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.LinkedList;

import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChainingEventPersisterTest {

	private LinkedList<BehaviorEvent> events;
	private ChainingEventPersisterImpl inst;
	private ArrayList<BehaviorEventPersister> delegates;
	private BehaviorEventPersister mock;
	
	@Before
	public void setUp() {
		mock = createMock(BehaviorEventPersister.class);
		delegates = new ArrayList<BehaviorEventPersister>();
		delegates.add(mock);
		
		inst = new ChainingEventPersisterImpl();
		inst.setDelegates(delegates);
		
		events = new LinkedList<BehaviorEvent>();
	}
	
	@After
	public void tearDown() {
		delegates = null;
		inst = null;
		events = null;
		mock = null;
	}
	
	@Test
	public void testSingleDelegate() {
		mock.persist(same(events));
		expectLastCall().once();
		replay(mock);
		
		inst.persist(events);
		verify(mock);
	}
	
	@Test
	public void testChaining() {
		//append a second persister to the chain to verify that all get notified in the correct order.
		BehaviorEventPersister second = createMock(BehaviorEventPersister.class);
		delegates.add(second);

		//we program the first mock persister to add a marker event to the queue.
		//this is normally not allowed, but it helps us to verify that the persisters are
		//invoked in the correct order.
		final BehaviorEvent marker = new BehaviorEvent(null, "test", "markerEvent", "testApp", null, null);
		
		//the first persister adds the marker event to the queue, only if it is empty.
		mock.persist(same(events));
		expectLastCall()
			.andAnswer(new IAnswer<Object>() {
							public Object answer() {
								assertTrue(events.isEmpty());
								events.add(marker);
								return null;
							}
						})
			.once();
		
		//the second persister adds the marker event to the queue, only if there is already a marker event there.
		second.persist(same(events));
		expectLastCall()
			.andAnswer(new IAnswer<Object>() {
							public Object answer() {
								assertEquals("marker event added", 1, events.size());
								assertSame(marker, events.peek());
								events.add(marker);
								return null;
							}
						})
			.once();
		
		replay(mock, second);
		
		inst.persist(events);
		
		verify(mock, second);
		assertEquals("both persisters were invoked in the proper order", 2, events.size());
	}

	@Test
	public void testErrorHandling() {
		//append a second persister to the chain to verify that all get notified in the correct order.
		BehaviorEventPersister second = createMock(BehaviorEventPersister.class);
		delegates.add(second);

		//the first persister will raise an error on invocation.  this should not interrupt service
		//to the second instance.
		mock.persist(same(events));
		expectLastCall().andThrow(new RuntimeException("boom")).once();
		
		//the second persister is isolated from the error in the first.
		second.persist(same(events));
		expectLastCall().once();
		
		replay(mock, second);
		
		inst.persist(events);
		
		verify(mock, second);
	}
}
