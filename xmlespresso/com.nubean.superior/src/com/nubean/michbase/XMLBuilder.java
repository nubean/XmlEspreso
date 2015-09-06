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

package com.nubean.michbase;

import javax.xml.parsers.*;

/**
 * <p>
 * Title: Michigan XML Editor
 * </p>
 * <p>
 * Description: This edits an XML document based on an XML schema.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: Nubean LLC
 * </p>
 * 
 * @author Ajay Vohra
 * @version 1.0
 */

public class XMLBuilder {
	public static DocumentBuilder nonValidatingBuilder;
	public static DocumentBuilder validatingBuilder;

	static {
		try {
			System.setProperty("org.xml.sax.driver",
					"org.apache.xerces.parsers.SAXParser");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);

			factory
					.setAttribute(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							Boolean.FALSE);
			factory.setExpandEntityReferences(false);
			nonValidatingBuilder = factory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			factory.setAttribute("http://apache.org/xml/features/validation/dynamic",
					Boolean.TRUE);
			factory
					.setAttribute(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							Boolean.TRUE);
			
			validatingBuilder = factory.newDocumentBuilder();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}