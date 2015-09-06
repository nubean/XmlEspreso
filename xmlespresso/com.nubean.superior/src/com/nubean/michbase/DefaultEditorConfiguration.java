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

import java.awt.Color;
import java.awt.Font;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import org.w3c.dom.NodeList;

public class DefaultEditorConfiguration extends EditorConfiguration {

	public DefaultEditorConfiguration() {
	}

	public void readElement(org.w3c.dom.Element element) {

		this.title = element.getAttribute("title");
		if (this.title.trim().length() == 0)
			title = "Text";

		this.mimeType = element.getAttribute("mimeType");

		this.proxyHost = element.getAttribute("proxyHost");
		this.proxyPort = element.getAttribute("proxyPort");
		Properties systemSettings = System.getProperties();

		if (proxyHost != null && proxyHost.trim().length() > 0) {
			systemSettings.put("http.proxyHost", proxyHost);
			systemSettings.put("proxyHost", proxyHost);
		}

		if (proxyPort != null && proxyPort.trim().length() > 0) {
			systemSettings.put("http.proxyPort", proxyPort);
			systemSettings.put("proxyPort", proxyPort);
		}

		NodeList nodeList = element.getElementsByTagName("style");
		int count = (nodeList != null ? nodeList.getLength() : 0);
		if (count > 0)
			styleContext = getStyleContext();
		else {
			return;
		}
		for (int i = 0; i < count; i++) {
			org.w3c.dom.Element se = (org.w3c.dom.Element) nodeList.item(i);
			String name = se.getAttribute("name");
			String parent = se.getAttribute("parent");
			SimpleAttributeSet attrs = new SimpleAttributeSet();

			NodeList fontList = se.getElementsByTagName("font");
			int nfonts = (fontList != null ? fontList.getLength() : 0);
			for (int j = 0; j < nfonts; j++) {
				org.w3c.dom.Element fe = (org.w3c.dom.Element) fontList.item(j);
				String fname = fe.getAttribute("name");

				String fstyle = fe.getAttribute("style");
				boolean isBold = (fstyle.indexOf("BOLD") != -1);
				boolean isItalic = (fstyle.indexOf("ITALIC") != -1);
				String size = fe.getAttribute("size");
				int fsize = EditorConfiguration.DEFAULT_FONT_SIZE;

				try {
					fsize = Integer.parseInt(size);
				} catch (NumberFormatException e) {
				}

				StyleConstants.setFontFamily(attrs, fname);
				StyleConstants.setFontSize(attrs, fsize);
				StyleConstants.setBold(attrs, isBold);
				StyleConstants.setItalic(attrs, isItalic);
			}

			NodeList colorList = se.getElementsByTagName("color");
			int ncolors = (colorList != null ? colorList.getLength() : 0);
			for (int j = 0; j < ncolors; j++) {
				org.w3c.dom.Element ce = (org.w3c.dom.Element) colorList
						.item(j);
				String bg = ce.getAttribute("bg");
				String fg = ce.getAttribute("fg");

				StyleConstants.setForeground(attrs, Color.decode(fg));
				StyleConstants.setBackground(attrs, Color.decode(bg));
			}

			Style style = styleContext.addStyle(name, styleContext
					.getStyle(parent));
			style.addAttributes(attrs);
		}
	}

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<editorConfiguration class='" + getClass().getName() + "' ");

		String indent = " ";
		sb.append("title='");
		sb.append(title);
		sb.append("'");

		sb.append(indent);
		sb.append("mimeType='");
		sb.append(mimeType);
		sb.append("'");

		if (proxyHost != null && proxyHost.trim().length() > 0) {
			sb.append(indent);
			sb.append("proxyHost='");
			sb.append(proxyHost);
			sb.append("'");
		}

		if (proxyPort != null && proxyPort.trim().length() > 0) {
			sb.append(indent);
			sb.append("proxyPort='");
			sb.append(proxyPort);
			sb.append("'");
		}

		sb.append(">");

		Enumeration styleNames = styleContext.getStyleNames();
		while (styleNames.hasMoreElements()) {
			String sname = (String) styleNames.nextElement();
			Style style = styleContext.getStyle(sname);
			String iindent = " ";
			if (style != null) {
				sb.append(indent);
				sb.append("<style");

				sb.append(iindent);
				sb.append("name='");
				sb.append(sname);
				sb.append("'");

				AttributeSet resolveParent = style.getResolveParent();
				if (resolveParent != null) {
					sb.append(iindent);
					sb.append("parent='");
					sb.append(((Style) resolveParent).getName());
					sb.append("'");
				}

				sb.append(indent);
				sb.append(">");

				Font font = styleContext.getFont(style);
				String iiindent = " ";
				if (font != null) {
					sb.append(iindent);
					sb.append("<font");

					sb.append(iiindent);
					sb.append("name='");
					sb.append(font.getFamily());
					sb.append("'");

					sb.append(iiindent);
					sb.append("style='");
					int styleType = font.getStyle();
					String fontStyle = "PLAIN";
					switch (styleType) {
					case Font.BOLD:
						fontStyle = "BOLD";
						break;
					case Font.PLAIN:
						fontStyle = "PLAIN";
						break;
					case Font.ITALIC:
						fontStyle = "ITALIC";
						break;
					default:
						fontStyle = "BOLDITALIC";
						break;
					}
					sb.append(fontStyle);
					sb.append("'");

					sb.append(iiindent);
					sb.append("size='");
					sb.append(Integer.toString(font.getSize()));
					sb.append("'");

					sb.append(iindent);
					sb.append(" />");
				}

				Color fg = styleContext.getForeground(style);
				Color bg = styleContext.getBackground(style);
				if (fg != null || bg != null) {
					sb.append(iindent);

					sb.append("<color");

					if (fg != null) {
						sb.append(iiindent);
						sb.append("fg='");
						sb.append(CommonUtils.encode(fg));
						sb.append("'");
					}

					if (bg != null) {
						sb.append(iiindent);
						sb.append("bg='");
						sb.append(CommonUtils.encode(bg));
						sb.append("'");
					}
					sb.append(iindent);
					sb.append(" />");
				}

				sb.append(indent);
				sb.append("</style>");
			}
		}

		sb.append("</editorConfiguration>");
		return sb.toString();
	}

	public void printXml(PrintWriter pw, String indent) {
		String save = indent;
		pw.print(indent);
		pw
				.println("<editorConfiguration class='" + getClass().getName()
						+ "' ");

		indent += "\t";

		pw.print(indent);
		pw.print("title='");
		pw.print(title);
		pw.println("'");

		pw.print(indent);
		pw.print("mimeType='");
		pw.print(mimeType);
		pw.println("'");

		if (proxyHost != null && proxyHost.trim().length() > 0) {
			pw.print(indent);
			pw.print("proxyHost='");
			pw.print(proxyHost);
			pw.print("'");
		}

		if (proxyPort != null && proxyPort.trim().length() > 0) {
			pw.print(indent);
			pw.print("proxyPort='");
			pw.print(proxyPort);
			pw.print("'");
		}
		pw.print(save);
		pw.println(">");

		Enumeration styleNames = styleContext.getStyleNames();
		while (styleNames.hasMoreElements()) {
			String sname = (String) styleNames.nextElement();
			Style style = styleContext.getStyle(sname);
			String iindent = indent + "\t";
			if (style != null) {
				pw.print(indent);
				pw.println("<style");

				pw.print(iindent);
				pw.print("name='");
				pw.print(sname);
				pw.println("'");

				AttributeSet resolveParent = style.getResolveParent();
				if (resolveParent != null) {
					pw.print(iindent);
					pw.print("parent='");
					pw.print(((Style) resolveParent).getName());
					pw.println("'");
				}

				pw.print(indent);
				pw.println(">");

				Font font = styleContext.getFont(style);
				String iiindent = iindent + "\t";
				if (font != null) {
					pw.print(iindent);
					pw.println("<font");

					pw.print(iiindent);
					pw.print("name='");
					pw.print(font.getFamily());
					pw.println("'");

					pw.print(iiindent);
					pw.print("style='");
					int styleType = font.getStyle();
					String fontStyle = "PLAIN";
					switch (styleType) {
					case Font.BOLD:
						fontStyle = "BOLD";
						break;
					case Font.PLAIN:
						fontStyle = "PLAIN";
						break;
					case Font.ITALIC:
						fontStyle = "ITALIC";
						break;
					default:
						fontStyle = "BOLDITALIC";
						break;
					}
					pw.print(fontStyle);
					pw.println("'");

					pw.print(iiindent);
					pw.print("size='");
					pw.print(Integer.toString(font.getSize()));
					pw.println("'");

					pw.print(iindent);
					pw.println(" />");
				}

				Color fg = styleContext.getForeground(style);
				Color bg = styleContext.getBackground(style);
				if (fg != null || bg != null) {
					pw.print(iindent);

					pw.println("<color");

					if (fg != null) {
						pw.print(iiindent);
						pw.print("fg='");
						pw.print(CommonUtils.encode(fg));
						pw.println("'");
					}

					if (bg != null) {
						pw.print(iiindent);
						pw.print("bg='");
						pw.print(CommonUtils.encode(bg));
						pw.println("'");
					}
					pw.print(iindent);
					pw.println(" />");
				}

				pw.print(indent);
				pw.println("</style>");
			}
		}

		pw.print(save);
		pw.println("</editorConfiguration>");

	}

	public Object clone() {
		DefaultEditorConfiguration dst = new DefaultEditorConfiguration();
		super.copy(this, dst);
		dst.proxyHost = this.proxyHost;
		dst.proxyPort = this.proxyPort;

		return dst;
	}

}
