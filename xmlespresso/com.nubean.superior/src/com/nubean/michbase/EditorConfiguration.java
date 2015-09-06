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

import javax.swing.text.StyleContext;
import java.util.*;
import javax.swing.text.*;

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

public abstract class EditorConfiguration implements Cloneable {

	public static final int DEFAULT_CHARS_PER_TAB = 2;
	public static int DEFAULT_FONT_SIZE = 12;

	protected String title;

	protected String mimeType;

	protected StyleContext styleContext;

	protected transient boolean settingsChanged;
	protected String proxyHost, proxyPort;

	public EditorConfiguration() {
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyHost(String host) {
		this.proxyHost = host;
	}

	public void setProxyPort(String port) {
		this.proxyPort = port;
	}

	protected void copy(EditorConfiguration src, EditorConfiguration dst) {
		dst.title = src.title;
		dst.mimeType = src.mimeType;
		Enumeration styleNames = src.styleContext.getStyleNames();
		if (src.styleContext == dst.styleContext)
			dst.styleContext = new StyleContext();
		while (styleNames.hasMoreElements()) {
			String sname = (String) styleNames.nextElement();
			Style style = src.styleContext.getStyle(sname);

			Style newStyle = dst.styleContext.addStyle(sname, null);
			newStyle.addAttributes(style.copyAttributes());
		}

		styleNames = dst.styleContext.getStyleNames();
		while (styleNames.hasMoreElements()) {
			String sname = (String) styleNames.nextElement();
			Style style = src.styleContext.getStyle(sname);
			AttributeSet resolveParent = style.getResolveParent();
			Style parent = (Style) resolveParent;

			if (parent != null) {
				dst.styleContext.getStyle(sname).setResolveParent(
						dst.styleContext.getStyle(parent.getName()));
			}
		}
	}

	public abstract Object clone();

	public void setSettingsChanged(boolean settingsChanged) {
		this.settingsChanged = settingsChanged;
	}

	public boolean isSettingsChanged() {
		return settingsChanged;
	}

	public String getTitle() {
		return title;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public abstract void printXml(java.io.PrintWriter pw, String indent);

	public abstract String toXMLString();

	public abstract void readElement(org.w3c.dom.Element element);

	public StyleContext getStyleContext() {
		return styleContext;
	}

	public void setStyleContext(StyleContext styleContext) {
		this.styleContext = styleContext;
	}

}