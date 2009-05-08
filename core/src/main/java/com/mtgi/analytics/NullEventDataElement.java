package com.mtgi.analytics;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

public class NullEventDataElement extends EventDataElement {

	private static final long serialVersionUID = -6242814010746885962L;
	
	public static final NullEventDataElement INSTANCE = new NullEventDataElement();
	
	private NullEventDataElement() {
		super("event-data");
	}

	@Override
	public boolean isEmpty() { return true; }
	@Override
	public boolean isNull() { return true; }

	@Override
	protected Iterator<EventDataElement> iterate() {
		return Collections.<EventDataElement>emptySet().iterator();
	}
	@Override
	public Iterator<EventDataElement> iterateChildren() {
		return Collections.<EventDataElement>emptySet().iterator();
	}
	@Override
	public Iterator<? extends Entry<String, Object>> iterateProperties() {
		return Collections.<Entry<String, Object>>emptySet().iterator();
	}
	
	@Override
	public void setText(String text) {}
	@Override
	public void add(String name, Object value) {}
	@Override
	public void addElement(EventDataElement child) {}
	@Override
	public EventDataElement addElement(String name) {
		return this;
	}
	@Override
	protected EventDataElement initialize(BehaviorEvent event) {
		return this;
	}
	@Override
	protected void setNext(EventDataElement parent, EventDataElement next) {}

}
