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
 
package com.mtgi.csv;

import java.io.IOException;

public class CSVUtil {

	/**
	 * Calls {@link #quoteCSVInner(String, Appendable)} to construct a new
	 * escaped string based on <code>str</code>.
	 * @return the escaped string
	 */
	public static String quoteCSV(Object value) {
		if (value == null)
			return "";
		String str = value.toString();
		try {
			return quoteCSVInner(str, new StringBuffer(2 + str.length())).toString();
		} catch (IOException ioe) {
			throw new RuntimeException("Unexpected error; StringBuffer should not raise I/O Exceptions", ioe);
		}
	}

	/**
	 * Add double quotes around <code>str</code>, stuttering any internal
	 * quotation marks in the manner expected by most CSV parsers.
	 * @param value the input object, to be quoted.
	 * @param escaped destination to which the escaped text is written
	 * @return a reference to <code>escaped</code>, for syntactic convenience
	 * @throws IOException if <code>escaped</code> raises errors while the data is being written.
	 */
	public static Appendable quoteCSV(Object value, Appendable escaped) throws IOException {
		if (value == null)
			return escaped;
		String str = value.toString();
		return quoteCSVInner(str, escaped);
	}
	
	//private implementation, without argument checks performed by public methods.
	private static Appendable quoteCSVInner(String str, Appendable escaped) throws IOException {
		escaped.append('"');
		for (int i = 0, len = str.length(); i < len; ++i) {
			char c = str.charAt(i);
			switch (c) {
			case '\n':
				//convert \r\n or \n to single \r.
				if (i == 0 || str.charAt(i - 1) != '\r')
					escaped.append('\r');
				break;
			case '"':
				escaped.append('"'); //stutter quotes
			default:
				escaped.append(c);
				break;
			}
		}
		escaped.append('"');
		return escaped;
	}
}
