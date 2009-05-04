package com.mtgi.analytics.jmx;

import static com.mtgi.jmx.export.naming.AppendNamingStrategy.quote;

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
 * <pre>parentDomain:key0=value0,...,keyN=valueN,nestedType=T,nestedName=N</pre>
 * 
 * If P's ObjectName already contains a key <code>nestedName</code>, then <code>nestedName[2]</code> and
 * <code>nestedType[2]</code> will be appended instead, and so on for more deeply nested events.</p>
 * @author Jason.Trump
 *
 */
public class NestedEventNamingStrategy extends BehaviorEventNamingStrategy
		implements ObjectNamingStrategy {

	private ObjectNamingStrategy parentStrategy;
	private int maxDepth = 3;

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
		if (parent != null) {
			String parentName = parentStrategy.getObjectName(parent, null).toString();
			String nestedKey = "nestedName";
			String nestedType = "nestedType";
			//attempt to construct a unique name by adding grouping keys to our parent's name.
			//fail out after max depth.
			for (int i = 0; i < maxDepth; ++i) {
				if (parentName.lastIndexOf(nestedKey) > 0) {
					nestedKey = "nestedName[" + (i + 2) + "]";
					nestedType = "nestedType[" + (i + 2) + "]";
				} else {
					return ObjectName.getInstance(parentName + 
						',' + nestedType + '=' + quote(event.getType()) + 
						',' + nestedKey + '=' + quote(event.getName()));
				}
			}
		}
		//fall-through to default naming
		return super.getObjectName(managedBean, null);
	}
}
