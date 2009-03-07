package com.mtgi.analytics;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Semi-structured (XML-like) data about a {@link BehaviorEvent}.
 * 
 * @see BehaviorEvent#getData()
 */
public class EventDataElement implements Serializable {

	private static final long serialVersionUID = -7479072851562747744L;
	
	private String name;
	private String text;

	private LinkedHashMap<String,Object> properties;

	//simple linked-list structure for child data elements, which has proven
	//faster than java collections in hitting our performance test targets
	private EventDataElement firstChild = ListHead.INSTANCE;
	private EventDataElement lastChild = ListHead.INSTANCE;
	
	private EventDataElement nextSibling;
	
	public EventDataElement(String name) {
		this.name = name;
	}
	
	/**
	 * Get the name of this element
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the text of this element, if any
	 */
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Set a named attribute on this element.  Values may be null.
	 */
	public void add(String name, Object value) {
		if (properties == null)
			properties = new LinkedHashMap<String,Object>();
		properties.put(name, value);
	}

	/**
	 * Add a child element with the given name to this element.
	 */
	public EventDataElement addElement(String name) {
		EventDataElement ret = new EventDataElement(name);
		lastChild.setNext(this, ret);
		return ret;
	}
	
	public void addElement(EventDataElement child) {
		lastChild.setNext(this, child);
	}
	
	public boolean isEmpty() {
		return text == null && properties == null && firstChild == ListHead.INSTANCE;
	}
	
	public Iterator<Map.Entry<String,Object>> iterateProperties() {
		return properties == null ? Collections.<Map.Entry<String,Object>>emptySet().iterator()
								  : properties.entrySet().iterator();
	}
	
	public Iterator<EventDataElement> iterateChildren() {
		return firstChild == ListHead.INSTANCE ? Collections.<EventDataElement>emptyList().iterator() 
											   : new ChildIterator();
	}
	
	/** set the next sibling in the linked list of children under <code>parent</code>. */
	protected void setNext(EventDataElement parent, EventDataElement next) {
		parent.lastChild = this.nextSibling = next;
	}
	
	/**
	 * Return a concrete, fully-realized instance of this data, performing any deferred initialization
	 * of internal data structures.  This implementation simply returns "this".
	 * @param event the parent event
	 */
	protected EventDataElement dereference(BehaviorEvent event) {
		return this;
	}
	
	private class ChildIterator implements Iterator<EventDataElement> {

		private EventDataElement current = firstChild;
		
		public boolean hasNext() {
			return current != null;
		}

		public EventDataElement next() {
			if (current == null)
				throw new NoSuchElementException();
			EventDataElement ret = current;
			current = ret.nextSibling;
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static class ListHead extends EventDataElement {
		
		private static final long serialVersionUID = 1511666816688289823L;
		
		static final ListHead INSTANCE = new ListHead();
		
		private ListHead() {
			super("");
		}
		@Override
		protected void setNext(EventDataElement parent, EventDataElement next) {
			parent.firstChild = parent.lastChild = next;
		}
		
		private Object readResolve() throws ObjectStreamException {
			return ListHead.INSTANCE;
		}
	}
}
