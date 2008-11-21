package com.mtgi.analytics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
	private ArrayList<EventDataElement> children;
	
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
		addElement(ret);
		return ret;
	}
	
	public void addElement(EventDataElement child) {
		if (children == null)
			children = new ArrayList<EventDataElement>();
		children.add(child);
	}
	
	public boolean isEmpty() {
		return text == null && properties == null && children == null;
	}
	
	public Iterator<Map.Entry<String,Object>> iterateProperties() {
		return properties == null ? Collections.<Map.Entry<String,Object>>emptySet().iterator()
								  : properties.entrySet().iterator();
	}
	
	public Iterator<EventDataElement> iterateChildren() {
		return children == null ? Collections.<EventDataElement>emptyList().iterator() 
								: children.iterator();
	}
}
