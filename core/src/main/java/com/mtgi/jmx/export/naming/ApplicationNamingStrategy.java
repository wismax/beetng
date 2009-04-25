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
 
package com.mtgi.jmx.export.naming;

import java.util.Hashtable;
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
 * <p>For example, <code>com.mtgi:group=analytics,name=BeetLog</code>
 * would become <code>myapp:package=com.mtgi,group=analytics,name=BeetLog</code>
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
	private String suffix;

	@Required
	public void setApplication(String application) {
		this.application = quote(application);
	}

	@Required
	public void setDelegate(ObjectNamingStrategy delegate) {
		this.delegate = delegate;
	}

	/** 
	 * Optional attribute identifying a suffix used to append to the end of
	 * all ObjectNames produced by this strategy.  Defaults to none.
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public ObjectName getObjectName(Object managedBean, String beanKey)
			throws MalformedObjectNameException {
		ObjectName base = delegate.getObjectName(managedBean, beanKey);

		Hashtable<String,String> properties = new Hashtable<String,String>(base.getKeyPropertyList());
		String domain = base.getDomain();
		if (domain != null) {
			//append the prior domain name onto the package property.
			String pkg = properties.get("package");
			pkg = (pkg == null) ? domain : pkg + "." + domain;
			properties.put("package", pkg);
		}
		if (suffix != null) {
			//append the suffix to the object name.
			String name = properties.get("name");
			name = (name == null) ? suffix : name + "@" + suffix;
			properties.put("name", name);
		}
		
		return ObjectName.getInstance(application, properties);
	}

	/** reluctantly quote the given input string (quoting only applied if the string contains control characters) */
	public static final String quote(String input) {
		if (QUOTE_NEEDED.matcher(input).find())
			return ObjectName.quote(input);
		return input;
	}
}
