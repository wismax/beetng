package com.mtgi.analytics;

import java.util.Collection;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * A delegating persister that invokes persistence operations on a group
 * of other persisters.  Useful for broadcasting events to several different
 * persister operations.
 */
public class ChainingEventPersisterImpl implements BehaviorEventPersister {

	private static final Log log = LogFactory.getLog(ChainingEventPersisterImpl.class);

	private Collection<BehaviorEventPersister> delegates;

	/** 
	 * specify the list of delegate persisters to be invoked.  this collection is iterated in 
	 * natural order on each call to {@link #persist(Queue)}. 
	 */
	@Required
	public void setDelegates(Collection<BehaviorEventPersister> delegates) {
		this.delegates = delegates;
	}

	public void persist(Queue<BehaviorEvent> events) {
		//TODO: perform invocations through TaskExecutor to allow multi-threading.
		for (BehaviorEventPersister delegate : delegates) {
			try {
				delegate.persist(events);
			} catch (Exception e) {
				log.error("Error persisting with " + delegate, e);
			}
		}
	}

}
