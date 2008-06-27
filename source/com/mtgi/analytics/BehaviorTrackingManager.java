package com.mtgi.analytics;

/**
 * Top-level application interface for logging behavior tracking
 * events to the BehaviorTracking database.  
 * 
 * Most applications should <i>never</i> have to call a method on this
 * interface or {@link BehaviorEvent} directly.  Rather, behavior tracking
 * should be managed automatically as an Aspect or using a servlet filter.
 * 
 * The lifecyle of a behavior tracking event has four steps:
 * <ol>
 * <li>An event is created using {@link #createEvent(String, String)}</li>
 * <li>The event is populated with event metadata, like parameter values, using the {@link BehaviorEvent} API methods</li>
 * <li>The event begins measuring execution time with a call to {@link #start(BehaviorEvent)}</li>
 * <li>The execution time measurement is stopped with a call to {@link #stop(BehaviorEvent)}, and the finished event is written to the behavior tracking database.</li>
 * </ol>.
 * 
 * @see SessionContext
 */
public interface BehaviorTrackingManager {
	
	/**
	 * Create a new event of the given type and name.  If another event
	 * is still pending on the current thread, the pending event
	 * will become the {@link BehaviorEvent#getParent() parent} of the newly created event.
	 * The returned event will also reflect the user Id and session Id associated
	 * with the current thread, if any.  
	 * 
	 * The returned event does <i>not</i>
	 * become active until {@link #start(BehaviorEvent)} is called, which
	 * should be done after all event metadata has been gathered.
	 * 
	 * @see SessionContext
	 * @see #start(BehaviorEvent)
	 */
	public BehaviorEvent createEvent(String type, String name);

	/**
	 * Set the given event as the currently executing event for this thread,
	 * and start measuring event time.
	 * @throws IllegalStateException if this event has already been started or stopped.
	 */
	public void start(BehaviorEvent event);

	/**
	 * Stop measuring execution time for the given event, and set the current
	 * event for this thread to be the parent of the given event, if any.
	 * The event data will subsequently be logged to the behavior tracking database.
	 * @throws IllegalStateException if this event has not been started, has already been stopped, or is not the currently executing event on the calling thread.
	 */
	public void stop(BehaviorEvent event);
	
}
