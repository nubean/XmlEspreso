/*
The MIT License (MIT)

Copyright (c) 2015 NuBean LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.nubean.michxml;

import java.io.CharArrayReader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.nubean.michbase.DefaultErrorHandler;

public class XMLContentHandler implements ContentHandler {

	public XMLContentHandler(XMLDocument doc) {
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			DefaultErrorHandler errorHandler = new DefaultErrorHandler();

			parser.setErrorHandler(errorHandler);
			parser.setFeature("http://xml.org/sax/features/namespaces", true);
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
			parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			parser.setContentHandler(this);
			parser.setErrorHandler(errorHandler);
			
			CharArrayReader reader = new CharArrayReader(doc.getText(0, doc.getLength()).toCharArray());
			parser.parse(new InputSource(reader));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void endDocument() throws SAXException {
		
	}

	public void startDocument() throws SAXException {
		
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		System.out.println("characters:'"+ (new String(ch, start, length) + "'"));

	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		System.out.println("ignoring ws:'"+ (new String(ch, start, length)) +"'");
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		System.out.println("End prefix mapping:"+prefix);

	}

	public void skippedEntity(String name) throws SAXException {
		System.out.println("Skipped entity:"+name);

	}

	public void setDocumentLocator(Locator locator) {
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		System.out.println("Proc. Instr.:"+target+","+data);

	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		System.out.println("Prefix Mapping:"+prefix+","+uri);
	}

	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		System.out.println("End element:"+localName+","+qName);
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		System.out.println("Start element:"+localName+","+qName);
	}

}
