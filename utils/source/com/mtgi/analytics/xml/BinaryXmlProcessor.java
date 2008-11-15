package com.mtgi.analytics.xml;

import java.util.Arrays;
import java.util.List;

import com.sun.xml.fastinfoset.tools.TransformInputOutput;

/** 
 * base class for command line binary xml processors.
 */
public abstract class BinaryXmlProcessor extends TransformInputOutput {

	private static final List<String> HELP = Arrays.asList(new String[]{ "--help", "-?", "-h" });
	private String help;
    
	protected BinaryXmlProcessor(String helpText) {
		this.help = helpText;
	}

    protected void usage() {
		System.out.println(help);
    }

    /**
     * process any subclass-specific command line arguments.  Arguments handled by the
     * subclass should be removed from the provided list as a side-effect of this operation.
     * The default behavior is to do nothing and return 0.
     * @return an exit code.  non-zero code will cause the program to terminate with the given result without further processing
     */
	protected int processArgs(List<String> args) throws Exception {
		return 0;
	}
	
	/**
	 * Process the command with the given arguments.  If argv contains "--help", "-?", or "-h",
	 * the command help text is echoed to standard out and processing is aborted with status code 1.
	 * Otherwise, {@link #processArgs(List)} is called.  If that returns 0, then {@link #parse(String[])}
	 * is invoked to transform the Xml document.
	 */
    public final int process(List<String> argv) throws Exception {
    	if (argv.size() == 1 && HELP.contains(argv.get(0))) {
    		usage();
    		return 1;
    	}
    	int rc = processArgs(argv);
    	if (rc != 0)
    		return rc;

    	parse(argv.toArray(new String[argv.size()]));
    	return 0;
    }
}
