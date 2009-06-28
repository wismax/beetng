package com.mtgi.test.unitils.tomcat;

import java.util.Properties;

import org.unitils.core.Module;
import org.unitils.core.TestListener;

public class TomcatModule implements Module {

	public void afterInit() {
	}

	public TestListener getTestListener() {
		return null;
	}

	public void init(Properties config) {
	}

	public static class Listener extends TestListener {
		
	}
	
}
