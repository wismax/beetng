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
