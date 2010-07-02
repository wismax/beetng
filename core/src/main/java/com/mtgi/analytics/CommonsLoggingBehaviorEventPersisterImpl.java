/* 
 * Copyright 2008-2010 the original author or authors.
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An event persister which logs all incoming events using commons-logging.
 * Mainly intended for development and diagnostic purposes only.
 */
public class CommonsLoggingBehaviorEventPersisterImpl implements BehaviorEventPersister {

	private static final Log log = LogFactory.getLog(CommonsLoggingBehaviorEventPersisterImpl.class);

	public void persist(Queue<BehaviorEvent> events) {
		if (!events.isEmpty()) {
			// sort the events so as to display them in a tree fashion
			BehaviorEventRoot root = new BehaviorEventRoot();
			for (BehaviorEvent currEvent : events) {
				root.add(currEvent);
			}

			log.info(this.prettyPrintEvents(root));
		}
	}

	private String prettyPrintEvents(BehaviorEventRoot root) {
		StringBuilder logBuilder = new StringBuilder();

		// emit a first line so that even the first event is correctly aligned
		// visually
		logBuilder.append("Events seen:\n");

		for (BehaviorEventHierarchy currRootHierarchy : root.getRootEvents()) {
			this.prettyPrintEvent(logBuilder, currRootHierarchy, 0);
		}

		return logBuilder.toString();
	}

	private void prettyPrintEvent(StringBuilder builder, BehaviorEventHierarchy hierarchy, int indent) {
		this.indentln(builder, BehaviorEventLabelProvider.toPlainString(hierarchy.father), indent);

		for (BehaviorEventHierarchy currChildHierarchy : hierarchy.getChildren()) {
			this.prettyPrintEvent(builder, currChildHierarchy, indent + 1);
		}
	}

	private void indentln(StringBuilder builder, String str, int indent) {
		for (int i = 0; i < indent; i++) {
			builder.append('\t');
		}

		builder.append(str);
		builder.append('\n');
	}

	public void setLevel(String level) {
		// do nothing
	}

	class BehaviorEventRoot {
		private List<BehaviorEventHierarchy> rootEvents;
		private Map<BehaviorEvent, BehaviorEventHierarchy> allEvents;

		public BehaviorEventRoot() {
			this.rootEvents = new ArrayList<BehaviorEventHierarchy>();
			this.allEvents = new HashMap<BehaviorEvent, BehaviorEventHierarchy>();
		}

		public void add(BehaviorEvent newEvent) {

			Stack<BehaviorEvent> parents = new Stack<BehaviorEvent>();
			BehaviorEvent currEvent = newEvent;
			while (currEvent != null) {
				parents.push(currEvent);
				currEvent = currEvent.getParent();
			}

			while (!parents.isEmpty()) {
				BehaviorEvent currEvent2 = parents.pop();

				if (!this.allEvents.containsKey(currEvent2)) {
					// this particular event has not already been seen

					BehaviorEventHierarchy newHierarchy = null;
					if (currEvent2.isRoot()) {
						newHierarchy = new BehaviorEventHierarchy();
						newHierarchy.father = currEvent2;
						this.allEvents.put(currEvent2, newHierarchy);
						this.rootEvents.add(newHierarchy);

					} else {
						BehaviorEventHierarchy fatherHierarchy = this.allEvents.get(currEvent2.getParent());
						if (fatherHierarchy != null) {
							newHierarchy = fatherHierarchy.addChildren(currEvent2);
							this.allEvents.put(currEvent2, newHierarchy);

						} else {
							log.fatal("Missing parent: " + currEvent2.getParent().getId());
						}
					}
				}
			}
		}

		public List<BehaviorEventHierarchy> getRootEvents() {
			return rootEvents;
		}
	}

	class BehaviorEventHierarchy {
		protected BehaviorEvent father;
		private Map<BehaviorEvent, BehaviorEventHierarchy> children;

		public BehaviorEventHierarchy() {
			// do nothing
		}

		public BehaviorEventHierarchy addChildren(BehaviorEvent newEvent) {
			BehaviorEventHierarchy hierarchy = new BehaviorEventHierarchy();
			hierarchy.father = newEvent;

			if (this.children == null) {
				this.children = new HashMap<BehaviorEvent, BehaviorEventHierarchy>();
			}

			this.children.put(newEvent, hierarchy);
			return hierarchy;
		}

		@SuppressWarnings("unchecked")
		public Collection<BehaviorEventHierarchy> getChildren() {
			if (this.children != null) {
				return this.children.values();
			} else {
				return Collections.EMPTY_SET;
			}
		}

		@Override
		public String toString() {
			if (this.father != null) {
				return this.father.toString();
			} else {
				return "";
			}
		}
	}

	private static class BehaviorEventLabelProvider {
		public static String toPlainString(BehaviorEvent event) {
			StringBuffer buf = new StringBuffer();

			buf.append(NumberFormat.getNumberInstance().format(event.getDurationNs()));
			buf.append(" ns\t");

			buf.append(" type=\"").append(event.getType()).append('"').append(" name=\"").append(event.getName())
					.append('"').append(" application=\"").append(event.getApplication()).append('"').append(
							" start=\"").append(event.getStart()).append('"').append(" user-id=\"").append(
							event.getUserId()).append('"').append(" session-id=\"").append(event.getSessionId())
					.append('"').append(" error=\"").append(event.getError()).append('"');

			return buf.toString();
		}
	}

}
