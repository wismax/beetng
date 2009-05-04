package com.mtgi.analytics.jmx;

import java.util.Date;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.mtgi.analytics.BehaviorEvent;

@ManagedResource
public class StatisticsMBean {

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

	@ManagedAttribute
	public long getCount() {
		return count;
	}

	@ManagedAttribute
	public long getErrorCount() {
		return errorCount;
	}

	@ManagedAttribute
	public double getAverageTime() {
		return averageTime;
	}

	@ManagedAttribute
	public long getMaxTime() {
		return maxTime;
	}

	@ManagedAttribute
	public Long getMinTime() {
		return minTime;
	}

	@ManagedAttribute
	public double getStandardDeviation() {
		return standardDeviation;
	}

	@ManagedAttribute
	public Date getLastInvocation() {
		return lastInvocation;
	}
	
	@ManagedOperation
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
		
		long duration = event.getDuration();
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
		
		if (event.getError() != null)
			++errorCount;
	}
	
}
