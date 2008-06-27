package com.mtgi.analytics;

import static java.util.UUID.randomUUID;

import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * <p>A trivial persister which logs instances of {@link BehaviorEvent} to a Log4j logger category at INFO severity.  
 * The default category is <code>com.mtgi.analytics.BehaviorEventPersister</code>,
 * which can be overridden with {@link #setCategory(String)}.</p>
 * 
 * <p>Intended for use with {@link Log4jBehaviorEventLayout}, which formats BehaviorEvent objects
 * as XML messages.  Default Log4j layouts will just log the output of {@link BehaviorEvent#toString()},
 * which is fairly useful in itself but does not include call parameters or other such detailed data.</p>
 */
public class Log4jBehaviorEventPersisterImpl implements BehaviorEventPersister {

	private Logger log = Logger.getLogger(BehaviorEventPersister.class);
	
	/** Override the logger to which BehaviorEvents are written */
	public void setCategory(String name) {
		log = Logger.getLogger(name);
	}
	
	public int persist(Queue<BehaviorEvent> events) {
		int count = 0;
		while (!events.isEmpty()) {
			BehaviorEvent event = events.remove();
			if (event.getId() != null)
				throw new IllegalStateException("Event " + event.getId() + " already persisted");
			
			event.setId(randomUUID());
			log.info(event);
			
			++count;
			//push children on queue for logging.
			events.addAll(event.getChildren());
		}
		return count;
	}

}