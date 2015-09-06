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

import java.io.PrintWriter;

import javax.swing.ImageIcon;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DefaultDocumentDescriptor extends DocumentDescriptor {

	public DefaultDocumentDescriptor() {
		super();
	}

	public DefaultDocumentDescriptor(String name, String path, String mimeType) {
		super(name, path, mimeType);
	}

	public void printXml(PrintWriter pw, String indent) {
		String save = indent;
		pw.print(indent);
		pw.println("<documentDescriptor class='" + getClass().getName() + "' ");

		indent += "\t";
		if (name != null) {
			pw.print(indent);
			pw.print("name='");
			pw.print(name);
			pw.println("'");
		}

		if (path != null) {
			pw.print(indent);
			pw.print("path='");
			pw.print(path);
			pw.println("'");
		}

		if (name != null) {
			pw.print(indent);
			pw.print("mimeType='");
			pw.print(mimeType);
			pw.println("'");
		}

		if (ext != null) {
			pw.print(indent);
			pw.print("ext='");
			pw.print(ext);
			pw.println("'");
		}

		pw.print(save);
		pw.println(">");

		indent += "\t";
		if (description != null) {
			pw.print(indent);
			pw.print("<description>");
			pw.print(description);
			pw.println("</description>");
		}

		pw.print(save);
		pw.println("</documentDescriptor>");

	}

	public ImageIcon getIcon() {
		return com.nubean.michutil.IconLoader.textIcon;
	}

	public String toXMLString() {
		String indent = " ";
		StringBuffer sb = new StringBuffer();

		sb.append("<documentDescriptor class='" + getClass().getName() + "' ");

		if (name != null) {
			sb.append(indent);
			sb.append("name='");
			sb.append(name);
			sb.append("'");
		}

		if (path != null) {
			sb.append(indent);
			sb.append("path='");
			sb.append(path);
			sb.append("'");
		}

		if (name != null) {
			sb.append(indent);
			sb.append("mimeType='");
			sb.append(mimeType);
			sb.append("'");
		}

		if (ext != null) {
			sb.append(indent);
			sb.append("ext='");
			sb.append(ext);
			sb.append("'");
		}

		sb.append(">");

		if (description != null) {
			sb.append(indent);
			sb.append("<description>");
			sb.append(description);
			sb.append("</description>");
		}

		sb.append("</documentDescriptor>");
		return sb.toString();
	}

	public void readElement(Element element) {

		if (element.getAttributeNode("path") != null)
			path = element.getAttribute("path");

		if (element.getAttributeNode("name") != null)
			name = element.getAttribute("name");

		if (element.getAttributeNode("ext") != null)
			ext = element.getAttribute("ext");

		if (element.getAttributeNode("mimeType") != null)
			mimeType = element.getAttribute("mimeType");

		Node descNode = CommonUtils.getChildByName(element, "description");
		if (descNode != null)
			description = CommonUtils.getContent(descNode);
	}

}
