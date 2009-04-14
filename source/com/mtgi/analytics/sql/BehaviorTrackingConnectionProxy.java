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
