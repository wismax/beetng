package com.mtgi.analytics.jmx;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mtgi.analytics.BehaviorEvent;

public class StatisticsMBeanTest {

	private StatisticsMBean inst;
	
	@Before
	public void setUp() {
		inst = new StatisticsMBean();
	}
	
	@After
	public void tearDown() {
		inst = null;
	}
	
	@Test
	public void testDefaults() {
		assertStats(inst, null, 0.0, 0, 0, 0, null, 0.0);
	}
	
	@Test
	public void testAccumulation() {
		for (int i = 0; i < DATA_SET.length; ++i) {
			Date now = new Date();
			TestEvent event = new TestEvent(now, DATA_SET[i]);
			inst.add(event);
			assertStats(inst, now, AVERAGE[i], i + 1, 0, MAX[i], MIN[i], STDEV[i]);
		}
	}
	
	@Test
	public void testReset() {
		//accumulate a bunch of data
		Date now = new Date();
		for (int i = 0; i < DATA_SET.length; ++i) {
			TestEvent event = new TestEvent(now, DATA_SET[i]);
			inst.add(event);
		}
		//check values just for fun.
		final int count = DATA_SET.length;
		assertStats(inst, now, AVERAGE[count - 1], count, 0, MAX[count - 1], MIN[count - 1], STDEV[count - 1]);
		
		//reset and verify we're back to defaults
		inst.reset();
		
		assertStats(inst, null, 0.0, 0, 0, 0, null, 0.0);
	}
	
	@Test
	public void testOrdering() {
		Date now = new Date();
		inst.add(new TestEvent(now, 1L));
		assertEquals("last invocation set", now, inst.getLastInvocation());
		assertEquals(1, inst.getCount());
		assertEquals(1.0, inst.getAverageTime(), 0.0000000001);
		
		inst.add(new TestEvent(new Date(now.getTime() - 10), 2L));
		assertEquals("out-of-order event does not update last invocation", now, inst.getLastInvocation());
		assertEquals(2, inst.getCount());
		assertEquals(1.5, inst.getAverageTime(), 0.0000000001);
		
		Date update = new Date(now.getTime() + 1);
		inst.add(new TestEvent(update, 2L));
		assertEquals("in-order event does update last invocation", update, inst.getLastInvocation());
		assertEquals(3, inst.getCount());
		assertEquals(1.6666666666, inst.getAverageTime(), 0.0000000001);
	}
	
	private static final void assertStats(StatisticsMBean inst, Date last, double average, long count, long errors, long max, Long min, double stddev) {
		assertEquals(count, inst.getCount());
		assertEquals(0, inst.getErrorCount());
		assertEquals(last, inst.getLastInvocation());
		assertEquals(max, inst.getMaxTime());
		assertEquals(min, inst.getMinTime());
		assertEquals(average, inst.getAverageTime(), 0.0000000001);
		assertEquals(stddev, inst.getStandardDeviation(), 0.0000000001);
	}
	
	private static final long[] DATA_SET = {
		10, 100, 49, 70, 4, 150, 14, 1, 13, 1000
	};
	private static final Long[] MIN = {
		10L, 10L, 10L, 10L, 4L, 4L, 4L, 1L, 1L, 1L
	};
	private static final long[] MAX = {
		10, 100, 100, 100, 100, 150, 150, 150, 150, 1000
	};
	private static final double[] AVERAGE = {
		10.0, 55.0, 53.0, 57.25, 46.6, 63.833333333333, 56.71428571428, 49.75, 45.66666666666, 141.1
	};
	private static final double[] STDEV = {
		0.0,
		45.0,
		36.851051545376556,
		32.751908341347075,
		36.219331854687766,
		50.775376797113864,
		50.13899048990654,
		50.39035125894639,
		48.89217160696019,
		290.03291192552615
	};

	public static class TestEvent extends BehaviorEvent {

		private static final long serialVersionUID = 6984000927990213680L;

		private long duration;
		private Date startDate;

		public TestEvent(Date startDate, long duration) {
			super(null, "stats", "test", "testApp", null, null);
			this.duration = duration;
			this.startDate = startDate;
		}
		
		@Override
		public Date getStart() {
			return startDate;
		}

		@Override
		public Long getDuration() {
			return duration;
		}
		
	}
	
}
