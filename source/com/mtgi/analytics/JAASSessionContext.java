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
