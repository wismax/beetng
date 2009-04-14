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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.TransformerFactoryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.fastinfoset.sax.SAXDocumentParser;

/**
 * An XSLT processing tool that is fit for use on extremely large binary xml input documents.
 * The user provides an XPath query which is used to split the source document into sub-documents,
 * each of which is run through the XSLT processor in a stream to produce the output.  This streaming
 * prevents the entire source document from being loaded into memory at once, at the expense of transformations
 * that require access to the entire source tree (for example, element sorting is not possible).
 */
public class BinaryToXSLT extends BinaryXmlProcessor {

	private static final String HELP_TEXT = "Usage: -tool xslt -split [xpath] -xsl [xsl source] [-format text|xml|html] [input] [output]";
	private static enum Format { text, html, xml };
	
	private XPathExpression pathExp;
	private Format format = Format.text;
	private Transformer xform;
	
    public BinaryToXSLT() {
    	super(HELP_TEXT);
    }
    
    @Override
    public int processArgs(List<String> args) throws Exception {

    	for (Iterator<String> it = args.iterator(); it.hasNext(); ) {

    		String arg = it.next();
    		if ("-split".equals(arg)) {
    			it.remove();
    			pathExp = XPathFactory.newInstance().newXPath().compile(it.next());
    			it.remove();
    		} else if ("-xsl".equals(arg)) {
    			it.remove();
    			String path = it.next();
    			it.remove();
    			FileInputStream fis = new FileInputStream(path);
    			try {
    				//we explicitly use Saxon as a 2.0-compliant parser.
    				xform = new TransformerFactoryImpl().newTransformer(new StreamSource(fis));
    				assert xform != null; //API doc promises never null, but the SDK 5 parser doesn't fulfill this obligation.
    			} finally {
    				fis.close();
    			}
    		} else if ("-format".equals(arg)) { 
    			it.remove();
    			format = Format.valueOf(it.next());
    			it.remove();
    		}
    	}
    	
    	if (xform == null) {
    		usage();
    		return 1;
    	}
    	
    	if (pathExp == null) {
    		usage();
    		return 1;
    	}
    	
    	return 0;
    }
    
    public void parse(InputStream finf, OutputStream doc) throws Exception {

    	final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	final StreamResult result = new StreamResult(doc);

		SAXDocumentParser parser = new SAXDocumentParser();
		parser.setParseFragments(true);
		parser.setInputStream(finf);
		parser.setContentHandler(new ContentHandler() {
			
	    	Document dom = builder.newDocument();
	    	Element elt;
	    	int position = 0;
	    	
			public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
				Element elt = null;
				if (!isBlank(uri)) {
					elt = dom.createElementNS(uri, qName);
				} else {
					elt = dom.createElement(localName);
				}
				if (this.elt != null)
					this.elt.appendChild(elt);
				this.elt = elt;
				
				if (atts != null)
					for (int a = 0; a < atts.getLength(); ++a) {
						String alocal = atts.getLocalName(a);
						String auri = atts.getURI(a);
						String avalue = atts.getValue(a);
						
						if (!isBlank(auri)) {
							elt.setAttributeNS(auri, atts.getQName(a), avalue);
						} else {
							elt.setAttribute(alocal, avalue);
						}
					}
			}

			public void characters(char[] ch, int start, int length) throws SAXException {
				if (elt != null) {
					Text text = dom.createTextNode(new String(ch, start, length));
					elt.appendChild(text);
				}
			}

			public void endElement(String uri, String localName, String qName) throws SAXException {
				try {
					//pop the stack.
					Element elt = this.elt;
					this.elt = (Element)elt.getParentNode();
					if (elt.getParentNode() != null && Boolean.TRUE.equals(pathExp.evaluate(elt.getParentNode(), XPathConstants.BOOLEAN))) {
						//element matches the split pattern.  set it at the document root and apply the
						//transform.
						dom.appendChild(elt);
						//pass position parameter so that transform can know how many prior elements have been processed,
						//for basic iteration logic.
						xform.reset();
						xform.clearParameters();
	    				xform.setOutputProperty(OutputKeys.METHOD, format.name());
						xform.setParameter("position", ++position);
						xform.transform(new DOMSource(dom), result);
						
						//reset internal state for the next match.
						dom.removeChild(elt);
					}
				} catch (XPathExpressionException xpe) {
					throw new SAXException("Error testing node with " + pathExp, xpe);
				} catch (TransformerException te) {
					throw new SAXException("Error transforming node", te);
				}
			}

			public void startPrefixMapping(String prefix, String uri) {}
			public void endPrefixMapping(String prefix) {}
			public void ignorableWhitespace(char[] ch, int start, int length) {}
			public void processingInstruction(String target, String data) {}
			public void setDocumentLocator(Locator locator) {}
			public void skippedEntity(String name)  {}
			public void startDocument() {}
			public void endDocument() {}
			
		});
		
		parser.parse();
    }
}
