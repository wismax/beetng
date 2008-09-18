package com.mtgi.jmx.export.naming;

import java.util.regex.Pattern;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;

/**
 * <p>A delegating naming strategy, which collects all JMX beans into a single
 * domain by application name.  The original domain of all beans collected into
 * the application is converted into the 'package' property, which is set at
 * the beginning of the property list.</p>
 * 
 * <p>For example, <code>com.mtgi:group=analytics,name=BehaviorTrackingLog</code>
 * would become <code>myapp:package=com.mtgi,group=analytics,name=BehaviorTrackingLog</code>
 * if the application name is "myapp".</p>
 * 
 * <p>Useful primarily for complex deployments, where many applications
 * might use the same framework classes (therefore increasing the risk of a naming
 * collision).</p>
 */
public class ApplicationNamingStrategy implements ObjectNamingStrategy {

	private static final Pattern QUOTE_NEEDED = Pattern.compile("[\\\\*?\n\",:=\\s]");
	
	private String application;
	private ObjectNamingStrategy delegate;

	@Required
	public void setApplication(String application) {
		this.application = quote(application);
	}

	@Required
	public void setDelegate(ObjectNamingStrategy delegate) {
		this.delegate = delegate;
	}

	public ObjectName getObjectName(Object managedBean, String beanKey)
			throws MalformedObjectNameException {
		ObjectName base = delegate.getObjectName(managedBean, beanKey);

		StringBuffer name = new StringBuffer(application).append(':');
		String domain = base.getDomain();
		String properties = base.getKeyPropertyListString();
		if (domain != null) {
			name.append("package=").append(quote(domain));
			if (properties != null)
				name.append(',');
		}
		if (properties != null)
			name.append(properties);
		
		return ObjectName.getInstance(name.toString());
	}

	/** reluctantly quote the given input string (quoting only applied if the string contains control characters) */
	public static final String quote(String input) {
		if (QUOTE_NEEDED.matcher(input).find())
			return ObjectName.quote(input);
		return input;
	}
}
