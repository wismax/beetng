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
 
package com.mtgi.analytics.xml;

import java.util.LinkedList;

public class Main {

	private static final String HELP_TEXT = "Usage: -tool xml|xslt|csv [-help|--help|-?] [tool options]";
	
	public static enum Tool { 
		Xml  { protected BinaryXmlProcessor instantiate() { return new BinaryToXml(); }}, 
		XSLT { protected BinaryXmlProcessor instantiate() { return new BinaryToXSLT(); }},
		Csv  { protected BinaryXmlProcessor instantiate() { return new BinaryToCSV(); }};
		protected abstract BinaryXmlProcessor instantiate();
	};

	public static int process(String[] argv) throws Exception {
		BinaryXmlProcessor proc = null;
		LinkedList<String> args = new LinkedList<String>();
		//scan to see which tool has been requested.
		for (int i = 0; i < argv.length; ++i) {
			if ("-tool".equals(argv[i])) {
				String name = argv[++i];
				for (Tool t : Tool.values())
					if (t.name().equalsIgnoreCase(name)) {
						proc = t.instantiate();
						break;
					}
			} else {
				args.add(argv[i]);
			}
		}
		if (proc == null) {
			System.err.println(HELP_TEXT);
			return 1;
		}
		return proc.process(args);
	}
	
	public static void main(String[] argv) {
		try {
			int rc = process(argv);
			System.exit(rc);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
	
}
