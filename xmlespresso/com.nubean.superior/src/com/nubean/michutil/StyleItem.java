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

package com.nubean.michutil;

import javax.swing.text.*;
import java.awt.*;

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

public class StyleItem implements Comparable {
	private Style style;

	private String title;

	public StyleItem(Style style, String title) {
		this.style = style;
		this.title = title;
	}

	public Style getStyle() {
		return style;
	}

	public String getTitle() {
		return title;
	}

	public Font getFont() {
		String family = StyleConstants.getFontFamily(style);
		int size = StyleConstants.getFontSize(style);
		int fstyle = Font.PLAIN;
		if (StyleConstants.isBold(style))
			fstyle = Font.BOLD;

		if (StyleConstants.isItalic(style))
			fstyle |= Font.ITALIC;
		return new Font(family, fstyle, size);
	}

	public Color getForeground() {
		return StyleConstants.getForeground(style);
	}

	public Color getBackground() {
		return StyleConstants.getBackground(style);
	}

	public void setFont(Font font) {
		StyleConstants.setFontFamily(style, font.getFamily());
		StyleConstants.setFontSize(style, font.getSize());
		StyleConstants.setBold(style, font.isBold());
		StyleConstants.setItalic(style, font.isItalic());
	}

	public void setForeground(Color color) {
		StyleConstants.setForeground(style, color);
	}

	public void setBackground(Color color) {
		StyleConstants.setBackground(style, color);
	}

	public String toString() {
		return title;
	}
	
	public int compareTo(Object obj) {
		return title.compareTo(obj.toString());
	}
}