package com.mtgi.analytics.jmx;

import static com.mtgi.jmx.export.naming.AppendNamingStrategy.quote;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;

import com.mtgi.analytics.BehaviorEvent;

/**
 * <p>A naming strategy for {@link BehaviorEvent} instances which creates an {@link ObjectName}
 * based on the {@link ObjectName} of the event's parent.  The parent event's name is computed
 * from a {@link #setParentStrategy(ObjectNamingStrategy) delegate ObjectNamingStrategy instance}.</p>
 * <p>BehaviorEvent <code>E</code> has application <code>A</code>, type <code>T</code>, 
 * name <code>N</code>, and parent event <code>P</code>.
 * If P has {@link ObjectName} equal to 
 * <code>parentDomain:key0=value0,...,keyN=valueN</code>, then <code>E</code>'s {@link ObjectName}
 * will be 
 * 
 * <pre>parentDomain:key0=value0,...,keyN=valueN,nested=T_N</pre>
 * 
 * If P's ObjectName already contains a key <code>nested</code>, then <code>nested[2]</code>
 * will be appended instead, and so on for more deeply nested events.</p>
 */
public class NestedEventNamingStrategy extends BehaviorEventNamingStrategy
		implements ObjectNamingStrategy {

	private ObjectNamingStrategy parentStrategy;
	private int maxDepth = 10;

	@Required
	public void setParentStrategy(ObjectNamingStrategy parentStrategy) {
		this.parentStrategy = parentStrategy;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException {
		BehaviorEvent event = (BehaviorEvent)managedBean;
		BehaviorEvent parent = event.getParent();
		if (parent == null) {
			//root event, delegate to parent strategy to determine name.
			return parentStrategy.getObjectName(managedBean, beanKey);
		} else {
			//descend on parent name
			ObjectName pName = getObjectName(parent, null);
			Hashtable<String,String> props = pName.getKeyPropertyList();

			String nestedKey = "nested";
			//attempt to construct a unique name by adding grouping keys to our parent's name.
			//fail out after max depth.
			for (int i = 0; i < maxDepth; ++i) {
				if (props.containsKey(nestedKey)) {
					nestedKey = "nested[" + (i + 2) + "]";
				} else {
					return ObjectName.getInstance(pName.toString() + 
						',' + nestedKey + '=' + quote(event.getType() + '_' + event.getName()));
				}
			}
			throw new IllegalStateException("Maximum event depth of " + maxDepth + " exceeded");
		}
	}
	
}
