package com.mtgi.analytics.xml;

import java.util.LinkedList;

public class Main {

	private static final String HELP_TEXT = "Usage: -tool xml|xslt [-help|--help|-?] [tool options]";
	
	public static enum Tool { 
		Xml  { protected BinaryXmlProcessor instantiate() { return new BinaryToXml(); }}, 
		XSLT { protected BinaryXmlProcessor instantiate() { return new BinaryToXSLT(); }};
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
