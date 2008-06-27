package com.mtgi.analytics;

import static java.lang.Character.isLetter;
import static java.lang.Character.isLetterOrDigit;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class EventDataElementSerializer {

	private DocumentBuilder builder;
	private XMLSerializer serializer;
	private StringWriter buffer;
	
	public EventDataElementSerializer(DocumentBuilderFactory domFactory) 
	{
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new RuntimeException("Unable to access DOM API", pce);
		}
		serializer = new XMLSerializer();
		buffer = new StringWriter();
		serializer.setOutputCharStream(buffer);
	}
	
	/**
	 * Append a DOM representation of the given data element to <code>document</code>.
	 * Does nothing if <code>data</code> is null.
	 */
	public void serializeToDOM(Document document, EventDataElement data) {
		if (data != null)
			serializeElement(document, document, data);
	}
	
	/**
	 * Serialize the given event data as a standalone XML document
	 * @return the XML serialization, or null if <code>data</code> is null.
	 */
	public String serialize(EventDataElement data) {
		if (data == null)
			return null;
		
		//convert the EventDataElement object to DOM
		Document document = builder.newDocument();
		serializeElement(document, document, data);
		
		try {
			//serialize the DOM to our string buffer.
			serializer.serialize(document);
			//return buffer contents.
			return buffer.toString();
			
		} catch (IOException ioe) {
			//this shouldn't happen, since the target output stream is a StringWriter, but
			//the compiler demands that we handle it.
			throw new RuntimeException("Error serializing XML data", ioe);
		} finally {
			
			//reset the internal buffer for next run.
			buffer.getBuffer().setLength(0);
		}
	}
	
	/**
	 * Recursively serialize a single element, appending it to DOM element <code>parent</code>.
	 */
	protected void serializeElement(Document document, Node parent, EventDataElement element) {

		//create a new node for the element and append it to the parent.
		String name = getXMLElementName(element.getName());
		Element elt = document.createElement(name);
		parent.appendChild(elt);
		
		//add text to the element if applicable.
		String text = element.getText();
		if (text != null)
			elt.appendChild(document.createTextNode(text));
		
		//add child elements for properties.
		for (Iterator<Entry<String,Object>> it = element.iterateProperties(); it.hasNext(); ) {
			Entry<String,Object> prop = it.next();
			Element propElt = document.createElement(getXMLElementName(prop.getKey()));
			elt.appendChild(propElt);
			
			Object value = prop.getValue();
			if (value != null)
				propElt.appendChild(document.createTextNode(value.toString()));
		}
		
		//add child elements for children.
		for (Iterator<EventDataElement> it = element.iterateChildren(); it.hasNext(); ) {
			EventDataElement child = it.next();
			serializeElement(document, elt, child);
		}
	}
	
	/**
	 * Convert the given input text into a valid XML entity name:
	 * <ul>
	 * <li>all letter characters are preserved</li>
	 * <li>digit characters after the first letter character are preserved</li>
	 * <li>leading non-letter characters are discarded</li>
	 * <li>trailing non-letter/digit characters are discarded</li>
	 * <li>all other sequences of non-letter/digit characters are converted to hyphens</li>
	 * </ul>
	 * 
	 * If the above conversion rules yield an empty string, the static string "data" is
	 * returned instead.
	 */
	private static String getXMLElementName(String name) {

		//accumulates return value.
		StringBuffer buf = new StringBuffer();

		//recognizer state machine that chews up an arbitrary string and
		//spits out a valid XML element name.  recognizer is always in one of three
		//states:  
		//  'e'psilon while no characters are yet in the output, 
		//  'i'nterior while there are some valid name characters,
		//  'h'yphenated while encountering invalid name characters
		
		char state = 'e';
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			
			switch (state) {
			case 'e':
				//beginning of string.  ignore everything up to the first character for the name.
				if (isLetter(c)) {
					//found a character, transition to 'i'nterior state.
					buf.append(c);
					state = 'i';
				}
				break;
				
			case 'i':
				//letters or digits ok after the first character.
				if (isLetterOrDigit(c)) {
					buf.append(c);
				} else {
					//invalid name character.  convert to hyphen and absorb all invalid characters
					//that follow by falling into the 'h'yphenated state.
					buf.append('-');
					state = 'h';
				}
				break;
				
			case 'h':
				//hyphenated state, absorb invalid characters.
				if (isLetterOrDigit(c)) {
					//we have a valid character, back to 'i'interior state.
					buf.append(c);
					state = 'i';
				}
				break;
				
			}
		}

		//strip trailing '-' from the generated name.
		int length = buf.length();
		if (length > 0 && buf.charAt(length -1) == '-')
			buf.setLength(--length);
		
		//input was just numbers or other gobbledigook.  return default value for element name.
		if (length == 0)
			return "data";
		
		//we have a usable name, return it.
		return buf.toString();
	}
	
}