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

/**
 * <p>Title: Michigan XML Editor</p>
 * <p>Description: This edits an XML document based on an XML schema.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Nubean LLC</p>
 * @author Ajay Vohra
 * @version 1.0
 */

import java.io.*;
import org.w3c.dom.*;

public class XMLWriter {
	protected PrintWriter pw;
	protected String encoding;
	protected boolean canonical;

	public XMLWriter(PrintWriter pw, boolean canonical, String encoding) {
		this.pw = pw;
		this.encoding = encoding;
		this.canonical = canonical;
	}

	public void print(Node node) {
		if (node == null) {
			return;
		}

		short type = node.getNodeType();
		Node child = null;
		switch (type) {
		case Node.DOCUMENT_NODE:
			Document document = (Document) node;
			if (!canonical) {
				pw.println("<?xml version=\"1.0\" encoding=\"" + encoding
						+ "\" ?>");
				pw.flush();
				print(document.getDoctype());
			}
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++)
				print(nodeList.item(i));
			break;

		case Node.DOCUMENT_TYPE_NODE:
			DocumentType doctype = (DocumentType) node;
			pw.print("<!DOCTYPE ");
			pw.print(doctype.getName());
			String publicId = doctype.getPublicId();
			String systemId = doctype.getSystemId();
			if (publicId != null) {
				pw.print(" PUBLIC '");
				pw.print(publicId);
				pw.print("' '");
				pw.print(systemId);
				pw.print('\'');
			} else {
				pw.print(" SYSTEM '");
				pw.print(systemId);
				pw.print('\'');
			}
			String internalSubset = doctype.getInternalSubset();
			if (internalSubset != null) {
				pw.println(" [");
				pw.print(internalSubset);
				pw.print(']');
			}
			pw.println('>');
			break;

		case Node.ELEMENT_NODE:
			pw.print('<');
			pw.print(node.getNodeName());
			Attr attrs[] = getAttributeNodes(node.getAttributes());
			for (int i = 0; i < attrs.length; i++) {
				Attr attr = attrs[i];

				pw.print(' ');
				pw.print(attr.getNodeName());
				pw.print("=\"");
				unescape(attr.getNodeValue());
				pw.print('"');
			}
			pw.println('>');
			pw.flush();

			child = node.getFirstChild();
			while (child != null) {
				print(child);
				child = child.getNextSibling();
			}
			break;

		case Node.ENTITY_REFERENCE_NODE:
			if (canonical) {
				child = node.getFirstChild();
				while (child != null) {
					print(child);
					child = child.getNextSibling();
				}
			} else {
				pw.print('&');
				pw.print(node.getNodeName());
				pw.print(';');
				pw.flush();
			}
			break;

		case Node.CDATA_SECTION_NODE:
			if (canonical) {
				unescape(node.getNodeValue());
			} else {
				pw.print("<![CDATA[");
				pw.print(node.getNodeValue());
				pw.println("]]>");
			}
			pw.flush();
			break;

		case Node.TEXT_NODE:
			unescape(node.getNodeValue());
			pw.flush();
			break;

		case Node.PROCESSING_INSTRUCTION_NODE: {
			pw.print("<?");
			ProcessingInstruction pi = (ProcessingInstruction) node;
			pw.print(pi.getTarget());
			String data = pi.getData();
			if (data != null && data.length() > 0) {
				pw.print(' ');
				pw.print(data);
			}
			pw.println("?>");
			pw.flush();
			break;
		}
		case Node.COMMENT_NODE: {
			Comment comment = (Comment) node;
			pw.print("<!-- ");
			String data = comment.getData();
			if (data != null && data.length() > 0) {
				pw.print(data);
			}
			pw.println("-->");
			break;
		}
		}

		if (type == Node.ELEMENT_NODE) {
			pw.print("</");
			pw.print(node.getNodeName());
			pw.println('>');
			pw.flush();
		}

	}

	private org.w3c.dom.Attr[] getAttributeNodes(org.w3c.dom.NamedNodeMap attrs) {

		int len = (attrs != null) ? attrs.getLength() : 0;
		org.w3c.dom.Attr array[] = new org.w3c.dom.Attr[len];
		for (int i = 0; i < len; i++) {
			array[i] = (org.w3c.dom.Attr) attrs.item(i);
		}

		return array;
	}

	protected void unescape(String s) {

		int len = (s != null) ? s.length() : 0;
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			unescape(c);
		}
	}

	protected void unescape(char c) {

		switch (c) {
		case '<':
			pw.print("&lt;");
			break;

		case '>':
			pw.print("&gt;");
			break;

		case '&':
			pw.print("&amp;");
			break;

		case '"':
			pw.print("&quot;");
			break;

		default:
			pw.print(c);
			break;

		}

	}

}