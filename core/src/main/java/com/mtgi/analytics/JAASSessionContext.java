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

package com.mtgi.analytics;

import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

public class JAASSessionContext implements SessionContext {

	public String getContextSessionId() {
		//TODO: ?
		return null;
	}

	public String getContextUserId() {
		Subject subj = Subject.getSubject(AccessController.getContext());
		if (subj == null)
			return null;
		Set<Principal> princ = subj.getPrincipals();
		if (princ == null || princ.isEmpty())
			return null;
		return princ.iterator().next().getName();
	}

}
