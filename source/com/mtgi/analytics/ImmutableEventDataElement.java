package com.mtgi.analytics;

public class ImmutableEventDataElement extends EventDataElement {

	private static final long serialVersionUID = 2051877886306623711L;

	public ImmutableEventDataElement(String name) {
		super(name);
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public void add(String name, Object value) {
		throw new UnsupportedOperationException("Cannot add data to this element");
	}

	@Override
	public void addElement(EventDataElement child) {
		throw new UnsupportedOperationException("Cannot add data to this element");
	}

	@Override
	public EventDataElement addElement(String name) {
		throw new UnsupportedOperationException("Cannot add data to this element");
	}

	@Override
	protected void setNext(EventDataElement parent, EventDataElement next) {
		throw new UnsupportedOperationException("Cannot add data to this element");
	}

	@Override
	public void setText(String text) {
		throw new UnsupportedOperationException("Cannot add data to this element");
	}

	
}
