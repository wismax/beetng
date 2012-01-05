package com.mtgi.test.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Random;

public abstract class IOUtils {

	/**
	 * Create an empty temporary directory with the given basename.
	 */
	public static File createTempDir(String name) throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		for (int counter = new Random().nextInt() & 0xffff; true; ++counter) {
			File dir = new File(tmp, name + counter + ".dir");
			if (!dir.exists()) {
				if (!dir.mkdirs())
					throw new IOException("Unable to create temporary directory " + dir.getAbsolutePath());
				return dir;
			}
		}
	}

	/**
	 * Recursively delete the given filesystem path and its children.
	 */
	public static void delete(File dir) throws IOException {
		if (dir.isDirectory())
			for (File child : dir.listFiles())
				delete(child);
		
		if (dir.exists() && !dir.delete())
			throw new IOException("Unable to delete " + dir.getAbsolutePath());
	}
	
	/**
	 * Delete all files in dir whose name start with with the provided namePrefix.
	 */
	public static void deleteAllFilesStartingWith(final File dir, final String namePrefix) {
	    File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equals(namePrefix) || name.startsWith(namePrefix);
            }
        });
	    
	    for (File file : files) {
            file.delete();
        }
	}

	/**
	 * Convert a file-scheme URL to its equivalent File representation.
	 * @throws IOException if the URL is invalid, or if the file does not exist.
	 */
	public static File urlToFile(URL url) throws IOException {
		String ext = url.toExternalForm();
		ext = ext.replaceFirst("^file:/+", "/");
		ext = URLDecoder.decode(ext, "UTF-8");
		
		File location = new File(ext);
		if (!location.exists())
			throw new FileNotFoundException("No resource found at path " + location.getAbsolutePath());
		
		return location;
	}

}
