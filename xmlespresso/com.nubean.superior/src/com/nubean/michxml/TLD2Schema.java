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

import java.util.*;
import java.io.*;
import org.w3c.dom.*;

import com.nubean.michbase.XMLBuilder;
import com.nubean.michbase.CommonUtils;

public class TLD2Schema {

	public static File getSchemaFromTLD(File file, String nsURI)
			throws Exception {
		PrintWriter pw = null;

		File schema = File.createTempFile(file.getName(), ".xsd");
		schema.deleteOnExit();
		try {

			pw = new PrintWriter(new FileWriter(schema));

			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw
					.println("<!DOCTYPE xs:schema PUBLIC '-//W3C//DTD XMLSCHEMA 200102//EN' 'http://www.w3.org/2001/XMLSchema.dtd'>");
			pw
					.println("<xs:schema  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "
							+ " xmlns:jsp=\"http://java.sun.com/JSP/Page\" xmlns=\""
							+ nsURI + "\" targetNamespace=\"" + nsURI + "\">");
			pw
					.println("<import namespace=\"http://java.sun.com/JSP/Page\" schemaLocation=\"http://java.sun.com/dtd/jspxml.xsd\" />");
			Document xml = null;
			synchronized (XMLBuilder.class) {
				XMLBuilder.nonValidatingBuilder.setErrorHandler(null);
				xml = XMLBuilder.nonValidatingBuilder.parse(file);
			}
			NodeList nodeList = xml.getElementsByTagName("tag");
			int count = (nodeList != null ? nodeList.getLength() : 0);
			for (int i = 0; i < count; i++) {
				Node tag = nodeList.item(i);

				Node tagName = CommonUtils.getChildByName(tag, "name");
				pw.println("<xs:element name=\""
						+ CommonUtils.getChildByType(tagName, Node.TEXT_NODE)
								.getNodeValue() + "\" >");
				pw.println("<xs:complexType>");
				Node bodyContent = CommonUtils.getChildByName(tag,
						"body-content");
				String body = CommonUtils.getChildByType(bodyContent,
						Node.TEXT_NODE).getNodeValue();
				if (body != null && !body.equals("empty")) {
					pw.println("<xs:sequence>");
					pw
							.println("<xs:group ref=\"jsp:Bodygroup\" minOccurs=\"0\" maxOccurs=\"unbounded\" />");
					pw.println("</xs:sequence>");
				}
				Vector attributes = CommonUtils.getChildrenByName(tag,
						"attribute");
				for (int j = 0; j < attributes.size(); j++) {
					Node attribute = (Node) attributes.elementAt(j);
					Node attributeName = CommonUtils.getChildByName(attribute,
							"name");
					Node attributeRequired = CommonUtils.getChildByName(
							attribute, "required");
					boolean required = false;

					if (attributeRequired != null) {
						String requiredValue = CommonUtils.getChildByType(
								attributeRequired, Node.TEXT_NODE)
								.getNodeValue();
						if (requiredValue != null) {
							required = Boolean.valueOf(requiredValue)
									.booleanValue();
						}
					}

					pw.println("<xs:attribute name=\""
							+ CommonUtils.getChildByType(attributeName,
									Node.TEXT_NODE).getNodeValue()
							+ "\" type=\"xs:string\""
							+ (required ? " use=\"required\" " : " ") + " />");

				}
				pw
						.println("<xs:anyAttribute namespace=\"##any\" processContents=\"skip\" />");
				pw.println("</xs:complexType>");
				pw.println("</xs:element>");

			}
			pw.println("</xs:schema>");

		} finally {
			if (pw != null)
				pw.close();
		}
		return schema;
	}
}
