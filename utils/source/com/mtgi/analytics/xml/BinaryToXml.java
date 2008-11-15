package com.mtgi.analytics.xml;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.fastinfoset.sax.SAXDocumentParser;

/**
 * A alternative to the FastInfoset FI_TO_XML utility, which continues processing if
 * the source document is malformed.  This allows us to handle incomplete XML documents,
 * e.g. performance logs left from a crashed server.
 */
public class BinaryToXml extends BinaryXmlProcessor {

	private static final String HELP_TEXT = "Usage: -tool xml [input] [output]";

	public BinaryToXml() {
		super(HELP_TEXT);
    }
    
    public void parse(InputStream finf, OutputStream xml) throws Exception {
    	
    	final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(xml);
    	try {
    		
    		SAXDocumentParser parser = new SAXDocumentParser();
    		parser.setParseFragments(true);
    		parser.setInputStream(finf);
    		parser.setContentHandler(new ContentHandler() {
    			
				public void characters(char[] ch, int start, int length) throws SAXException {
					try {
						writer.writeCharacters(ch, start, length);
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}

				public void endDocument() {
					//handled in finally block below.
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {
					try {
						writer.writeEndElement();
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}

				public void endPrefixMapping(String prefix) throws SAXException {
					//TODO: something? FI api doesn't support unbinding prefixes.
				}

				public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
					try {
						writer.writeCharacters(ch, start, length);
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}

				public void processingInstruction(String target, String data) throws SAXException {
					try {
						writer.writeProcessingInstruction(target, data);
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}

				public void setDocumentLocator(Locator locator) {}
				public void skippedEntity(String name)  {}

				public void startDocument() throws SAXException {
					try {
						writer.writeStartDocument();
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}

				public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
					try {
						if (uri != null)
							writer.writeStartElement(uri, localName);
						else
							writer.writeStartElement(localName);
						
						if (atts != null)
							for (int a = 0; a < atts.getLength(); ++a) {
								String alocal = atts.getLocalName(a);
								String auri = atts.getURI(a);
								String avalue = atts.getValue(a);
								
								if (auri != null) {
									writer.writeAttribute(auri, alocal, avalue);
								} else {
									writer.writeAttribute(alocal, avalue);
								}
							}
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}

				public void startPrefixMapping(String prefix, String uri) throws SAXException {
					try {
						writer.setPrefix(prefix, uri);
					} catch (XMLStreamException e) {
						throw new SAXException(e);
					}
				}
				
    		});
    		
    		parser.parse();
    		
    	} finally {
    		writer.writeEndDocument();
    		writer.flush();
    		writer.close();
    	}
    }
}
