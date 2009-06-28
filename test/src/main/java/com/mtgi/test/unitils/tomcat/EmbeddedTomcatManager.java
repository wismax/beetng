package com.mtgi.test.unitils.tomcat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

public class EmbeddedTomcatManager {
	
	public static File getDeployableResource(String resourcePath) throws IOException {

		URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
		if (url == null)
			throw new FileNotFoundException("Unable to locate classpath resource at " + resourcePath);
		
		String ext = url.toExternalForm();
		ext = ext.replaceFirst("^file:/+", "/");
		ext = URLDecoder.decode(ext, "UTF-8");
		
		File location = new File(ext);
		if (!location.exists())
			throw new FileNotFoundException("No resource found at path " + location.getAbsolutePath());
		
		return location;
	}
	
}
