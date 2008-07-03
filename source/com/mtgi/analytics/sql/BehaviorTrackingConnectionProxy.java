package com.mtgi.analytics.sql;

import org.springframework.jdbc.datasource.ConnectionProxy;

/**
 * Extends the standard connection interface with the ability to suspend
 * or resume event broadcasting.
 */
public interface BehaviorTrackingConnectionProxy extends ConnectionProxy {
	/** halt generating tracking events on this connection */
	public void suspendTracking();
	/** resume generating tracking events on this connection */
	public void resumeTracking();
}
