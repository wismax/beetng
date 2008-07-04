package com.mtgi.analytics;

import static java.lang.Character.isLetter;
import static java.lang.Character.isLetterOrDigit;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class EventDataElementSerializer {

	private static final char[] DUMMY_TEXT = {};
	
	private XMLOutputFactory factory;
	private StringWriter buffer;
	
	public EventDataElementSerializer(XMLOutputFactory factory) {
		this.buffer = new StringWriter();
		this.factory = factory;
	}
	
	/**
	 * Serialize the given event data as a standalone XML document
	 * @param includeProlog if true, include an XML prolog; if not, just render a document fragment
	 * @return the XML serialization, or null if <code>data</code> is null.
	 */
	public String serialize(EventDataElement data, boolean includeProlog) {
		if (data == null)
			return null;
		
		try {
			//serialize the DOM to our string buffer.
			XMLStreamWriter writer = factory.createXMLStreamWriter(buffer);
			try {
				if (includeProlog)
					writer.writeStartDocument();
				serializeElement(writer, data);
				writer.writeEndDocument();
				writer.flush();
			} finally {
				writer.close();
			}
			
			//return buffer contents.
			return buffer.toString();
			
		} catch (XMLStreamException ioe) {
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
	protected void serializeElement(XMLStreamWriter writer, EventDataElement element) throws XMLStreamException {

		//create a new node for the element and append it to the parent.
		String name = getXMLElementName(element.getName());
		
		if (element.isEmpty()) {
			writer.writeEmptyElement(name);
			//TODO: remove when stax bug is fixed.
			//this is a workaround for a bug in the 1.2 StAX implementation, where
			//if the only element in your document is empty, the closing "/>" never gets written.
			//any other API call fixes the problem, so here we do a no-op string append to force
			//the element closed.
			writer.writeCharacters(DUMMY_TEXT, 0, 0);
		} else {
			writer.writeStartElement(name);
			
			//add text to the element if applicable.
			String text = element.getText();
			if (text != null)
				writer.writeCharacters(text);
			
			//add child elements for properties.
			Iterator<Entry<String,Object>> props = element.iterateProperties();
			while (props.hasNext()) {
				Entry<String,Object> prop = props.next();
				
				String propName = getXMLElementName(prop.getKey());
				Object value = prop.getValue();
				
				if (value != null) {
					writer.writeStartElement(propName);
					writer.writeCharacters(value.toString());
					writer.writeEndElement();
				} else {
					writer.writeEmptyElement(propName);
				}
			}
			
			//add child elements for children.
			Iterator<EventDataElement> children = element.iterateChildren();
			while (children.hasNext())
				serializeElement(writer, children.next());
			writer.writeEndElement();
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

		synchronized (nameCache) {
			String cached = nameCache.get(name);
			if (cached != null)
				return cached;
		}
		
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
			buf.append("data");
		
		//we have a usable name, return it.
		String ret = buf.toString();
		synchronized (nameCache) {
			nameCache.put(name, ret);
		}
		return ret;
	}

	/** cache computed values to speed up processing */
	private static HashMap<String,String> nameCache = new LinkedHashMap<String,String>() {
		private static final long serialVersionUID = 8470335497980720176L;
		@Override
		protected boolean removeEldestEntry(Entry<String, String> eldest) {
			return size() > 10000;
		}
	};
	
}