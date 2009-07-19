package com.mtgi.analytics.jmx;

import static java.text.MessageFormat.format;

import java.util.Date;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.mtgi.analytics.BehaviorEvent;

/**
 * Tracks aggregate statistics for a class of events.  Data points are added
 * with {@link #add(BehaviorEvent)}, after which various statistics can be queried
 * with calls like {@link #getAverageTime()} or {@link #getStandardDeviation()}.
 * Statistics can be reset with {@link #reset()}.
 * 
 * @see StatisticsMBeanEventPersisterImpl
 */
@ManagedResource(description="Aggregate statistics for a single type of behavior tracking event")
public class StatisticsMBean {

	public static enum Units {
		seconds(1e-9), 
		milliseconds(1e-6), 
		nanoseconds(1) { public double convertNanos(double identity) { return identity; } };
		
		private double ratio;
		private Units(double ratio) {
			this.ratio = ratio;
		}
		
		public double convertNanos(double nanos) {
			return ratio * nanos;
		}
	}
	
	private Units units = Units.milliseconds;
	
	private long count;
	private long errorCount;

	private double averageTime;
	private long maxTime;
	private Long minTime;
	
	//the product of sample count and variance, an auxiliary value used to efficiently compute stddev
	private double nVariance;
	private double standardDeviation;

	private Date lastInvocation;
	//TODO: throughput statistics, histogram ?

	@ManagedAttribute(description="The unit of measure for attribute values; supported units are seconds, milliseconds, and nanoseconds")
	public String getUnits() {
		return units.toString();
	}
	
	public void setUnits(String units) {
		units = units.toLowerCase();
		Units v = null;
		for (Units u : Units.values())
			if (u.name().startsWith(units)) {
				v = u;
				break;
			}
		if (v == null)
			throw new IllegalArgumentException("Unknown unit '" + units + "'; only seconds, milliseconds, nanoseconds are supported");
		this.units = v;
	}
	
	@ManagedAttribute(description="The number of events received")
	public long getCount() {
		return count;
	}

	@ManagedAttribute(description="The number of received events that ended in error")
	public long getErrorCount() {
		return errorCount;
	}

	@ManagedAttribute(description="The average event execution time")
	public double getAverageTime() {
		return units.convertNanos(averageTime);
	}

	@ManagedAttribute(description="The maximum event execution time")
	public double getMaxTime() {
		return units.convertNanos(maxTime);
	}

	@ManagedAttribute(description="The minimum event execution time")
	public Double getMinTime() {
		return minTime == null ? null : units.convertNanos(minTime);
	}

	@ManagedAttribute(description="The standard deviation of event execution time")
	public double getStandardDeviation() {
		return units.convertNanos(standardDeviation);
	}

	@ManagedAttribute(description="The start time of the last event recorded")
	public Date getLastInvocation() {
		return lastInvocation;
	}
	
	@ManagedOperation(description="Reset all tracked statistics for this type of event to their starting values")
	public synchronized void reset() {
		count = errorCount = maxTime = 0;
		averageTime = standardDeviation = nVariance = 0;
		minTime = null;
		lastInvocation = null;
	}
	
	public synchronized void add(BehaviorEvent event) {
		Date start = event.getStart();
		if (lastInvocation == null || lastInvocation.before(start))
			lastInvocation = start;
		
		if (event.getError() != null)
			++errorCount;
		
		add(event.getDurationNs());
	}
	
	public synchronized void add(long duration) {
		maxTime = Math.max(maxTime, duration);
		if (count == 0) {
			minTime = duration;
			averageTime = duration;
			count = 1;
		} else {
			minTime = Math.min(minTime, duration);

			//use running total algorithms for avg and stddev to reduce error
			double delta = duration - averageTime;
			nVariance = nVariance + ((double)count * delta * delta) / (double)++count;
			standardDeviation = Math.sqrt(nVariance / (double)count);
			averageTime = averageTime + (duration - averageTime) / count;
		}
	}
	
	@Override
	public String toString() {
		return format("[average:\t{0}\n max:\t\t{1}\n min:\t\t{2}\n dev:\t\t{3}\n count:\t\t{4}]", 
				averageTime, maxTime, minTime, standardDeviation, count);
	}
}
