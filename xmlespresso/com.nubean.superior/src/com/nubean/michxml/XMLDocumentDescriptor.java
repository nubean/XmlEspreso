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

import com.nubean.michutil.*;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.CommonUtils;

import javax.swing.ImageIcon;
import org.w3c.dom.*;

import java.io.*;
import java.net.*;
import java.util.*;

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

public class XMLDocumentDescriptor extends DocumentDescriptor {
	private String nsPrefix;

	private String nsURI;

	private String schemaLocation;

	private String dtdLocation;

	private String dtdPublicId;

	private String rootElement;

	private String cssResource = "styles/default.css";

	private String encoding;

	private Vector<TaglibDef> taglibs;

	private long timestamp;

	private boolean useSchemaTargetNamespace;

	private transient String css;

	public String getNSPrefix() {
		return nsPrefix;
	}

	public void setNSPrefix(String prefix) {
		this.nsPrefix = prefix;
	}

	public String getNSUri() {
		return nsURI;
	}

	public void setTimestamp(long ts) {
		this.timestamp = ts;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setNSUri(String uri) {
		this.nsURI = uri;
	}

	public String getRootElement() {
		String root = rootElement;
		try {
			int index = root.indexOf(":");
			if (index > 0) {
				root = root.substring(index + 1).trim();
			}
		} catch (Exception e) {
		}
		return root;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public String getCssResource(String cssResource) {
		return cssResource;
	}

	public void setCssResource(String css) {
		this.cssResource = css;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}

	public void setSchemaLocation(String loc) {
		this.schemaLocation = loc;
	}

	public ImageIcon getIcon() {
		return IconLoader.xmlIcon;
	}

	public void setDtdLocation(String loc) {
		this.dtdLocation = loc;
	}

	public String getDtdLocation() {
		return this.dtdLocation;
	}

	public void setDtdPublicId(String publicId) {
		this.dtdPublicId = publicId;
	}

	public String getDtdPublicId() {
		return this.dtdPublicId;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setUseSchemaTargetNamespace(boolean flag) {
		this.useSchemaTargetNamespace = flag;
	}

	public boolean getUseSchemaTargetNamespace() {
		return this.useSchemaTargetNamespace;
	}

	public Vector<TaglibDef> getTaglibs() {
		return taglibs;
	}

	public void addTaglib(TaglibDef td) {
		if (td != null && td.nsPrefix != null && td.nsURI != null
				&& td.schemaLocation != null) {
			if (taglibs == null)
				taglibs = new Vector<TaglibDef>(5, 5);
			taglibs.add(td);
		}
	}

	public String getStyleSheet() {
		if (css != null)
			return css;

		StringBuffer sb = new StringBuffer();
		try {
			URL url = getClass().getClassLoader().getResource(cssResource);
			URLConnection connection = url.openConnection();
			InputStreamReader reader = new InputStreamReader(connection
					.getInputStream());
			BufferedReader br = new BufferedReader(reader);
			String line = null;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			css = sb.toString();
		} catch (Exception e) {
		}
		return css;

	}

	public XMLDocumentDescriptor() {
		super();
	}

	public void readElement(Element element) {
		if (element.getAttributeNode("nsPrefix") != null)
			nsPrefix = element.getAttribute("nsPrefix");

		if (element.getAttributeNode("nsURI") != null)
			nsURI = element.getAttribute("nsURI");

		if (element.getAttributeNode("path") != null)
			path = element.getAttribute("path");

		if (element.getAttributeNode("name") != null)
			name = element.getAttribute("name");

		try {
			if (element.getAttributeNode("timestamp") != null)
				timestamp = Long.parseLong(element.getAttribute("timestamp"));
		} catch (NumberFormatException e) {
		}

		if (element.getAttributeNode("ext") != null)
			ext = element.getAttribute("ext");

		if (element.getAttributeNode("mimeType") != null)
			mimeType = element.getAttribute("mimeType");

		if (element.getAttributeNode("schemaLocation") != null)
			schemaLocation = element.getAttribute("schemaLocation");

		if (element.getAttributeNode("cssResource") != null)
			cssResource = element.getAttribute("cssResource");
		if (cssResource == null || cssResource.trim().length() == 0)
			cssResource = "styles/default.css";

		if (element.getAttributeNode("dtdLocation") != null)
			dtdLocation = element.getAttribute("dtdLocation");

		if (element.getAttributeNode("dtdPublicId") != null)
			dtdPublicId = element.getAttribute("dtdPublicId");

		if (element.getAttributeNode("rootElement") != null)
			rootElement = element.getAttribute("rootElement");

		if (element.getAttributeNode("encoding") != null)
			encoding = element.getAttribute("encoding");

		if (element.getAttributeNode("usetns") != null)
			useSchemaTargetNamespace = Boolean.valueOf(
					element.getAttribute("usetns")).booleanValue();

		Node descNode = CommonUtils.getChildByName(element, "description");
		if (descNode != null)
			description = CommonUtils.getContent(descNode);

		Vector<Node> tl = CommonUtils.getChildrenByName(element, "taglib");
		int count = (tl != null ? tl.size() : 0);
		if (count > 0)
			taglibs = new Vector<TaglibDef>(count);
		for (int i = 0; i < count; i++) {
			Element etaglib = (Element) tl.elementAt(i);
			TaglibDef td = new TaglibDef();
			if (etaglib.getAttributeNode("nsPrefix") != null)
				td.nsPrefix = etaglib.getAttribute("nsPrefix");
			if (etaglib.getAttributeNode("nsURI") != null)
				td.nsURI = etaglib.getAttribute("nsURI");
			if (etaglib.getAttributeNode("schemaLocation") != null)
				td.schemaLocation = etaglib.getAttribute("schemaLocation");
			if (td.nsPrefix != null && td.nsURI != null
					&& td.schemaLocation != null)
				taglibs.add(td);
		}

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

		if (timestamp > 0) {
			pw.print(indent);
			pw.print("timestamp='");
			pw.print(Long.toString(timestamp));
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

		if (rootElement != null) {
			pw.print(indent);
			pw.print("rootElement='");
			pw.print(rootElement);
			pw.println("'");
		}

		if (nsPrefix != null) {
			pw.print(indent);
			pw.print("nsPrefix='");
			pw.print(nsPrefix);
			pw.println("'");
		}

		if (nsURI != null) {
			pw.print(indent);
			pw.print("nsURI='");
			pw.print(nsURI);
			pw.println("'");
		}

		if (schemaLocation != null) {
			pw.print(indent);
			pw.print("schemaLocation='");
			pw.print(schemaLocation);
			pw.println("'");
		}

		if (cssResource != null) {
			pw.print(indent);
			pw.print("cssResource='");
			pw.print(cssResource);
			pw.println("'");
		}

		if (dtdLocation != null) {
			pw.print(indent);
			pw.print("dtdLocation='");
			pw.print(dtdLocation);
			pw.println("'");
		}

		if (dtdPublicId != null) {
			pw.print(indent);
			pw.print("dtdPublicId='");
			pw.print(dtdPublicId);
			pw.println("'");
		}

		if (encoding != null) {
			pw.print(indent);
			pw.print("encoding='");
			pw.print(encoding);
			pw.println("'");
		}

		pw.print(indent);
		pw.print("usetns='");
		pw.print(Boolean.toString(useSchemaTargetNamespace));
		pw.println("'");

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

		int count = (taglibs != null ? taglibs.size() : 0);
		for (int i = 0; i < count; i++) {

			TaglibDef td = (TaglibDef) taglibs.elementAt(i);
			pw.print("<taglib");
			pw.print(" nsPrefix=\"");
			pw.print(td.nsPrefix);
			pw.print("\" ");
			pw.print(" nsURI=\"");
			pw.print(td.nsURI);
			pw.print("\" ");
			pw.print(" schemaLocation=\"");
			pw.print(td.schemaLocation);
			pw.print("\" ");
			pw.println("/>");
		}

		pw.print(save);
		pw.println("</documentDescriptor>");

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

		if (timestamp > 0) {
			sb.append(indent);
			sb.append("timestamp='");
			sb.append(Long.toString(timestamp));
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

		if (rootElement != null) {
			sb.append(indent);
			sb.append("rootElement='");
			sb.append(rootElement);
			sb.append("'");
		}

		if (nsPrefix != null) {
			sb.append(indent);
			sb.append("nsPrefix='");
			sb.append(nsPrefix);
			sb.append("'");
		}

		if (nsURI != null) {
			sb.append(indent);
			sb.append("nsURI='");
			sb.append(nsURI);
			sb.append("'");
		}

		if (schemaLocation != null) {
			sb.append(indent);
			sb.append("schemaLocation='");
			sb.append(schemaLocation);
			sb.append("'");
		}

		if (cssResource != null) {
			sb.append(indent);
			sb.append("cssResource='");
			sb.append(cssResource);
			sb.append("'");
		}

		if (dtdLocation != null) {
			sb.append(indent);
			sb.append("dtdLocation='");
			sb.append(dtdLocation);
			sb.append("'");
		}

		if (dtdPublicId != null) {
			sb.append(indent);
			sb.append("dtdPublicId='");
			sb.append(dtdPublicId);
			sb.append("'");
		}

		if (encoding != null) {
			sb.append(indent);
			sb.append("encoding='");
			sb.append(encoding);
			sb.append("'");
		}

		sb.append(indent);
		sb.append("usetns='");
		sb.append(Boolean.toString(useSchemaTargetNamespace));
		sb.append("'");

		sb.append(">");

		if (description != null) {
			sb.append(indent);
			sb.append("<description>");
			sb.append(description);
			sb.append("</description>");
		}

		int count = (taglibs != null ? taglibs.size() : 0);
		for (int i = 0; i < count; i++) {

			TaglibDef td = (TaglibDef) taglibs.elementAt(i);
			sb.append(indent);
			sb.append("<taglib");
			sb.append(" nsPrefix=\"");
			sb.append(td.nsPrefix);
			sb.append("\" ");
			sb.append(" nsURI=\"");
			sb.append(td.nsURI);
			sb.append("\" ");
			sb.append(" schemaLocation=\"");
			sb.append(td.schemaLocation);
			sb.append("\" ");
			sb.append("/>");
		}

		sb.append("</documentDescriptor>");
		return sb.toString();
	}
}