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

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class DefaultStyleContext extends StyleContext {

	private static final long serialVersionUID = -2724693553365559958L;

	public DefaultStyleContext(String[] styles) {
		super();

		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.BLACK);
		StyleConstants.setFontFamily(attr, "Tahoma");
		StyleConstants.setFontSize(attr, EditorConfiguration.DEFAULT_FONT_SIZE);

		Style text = this.addStyle("Text", null);
		text.addAttributes(attr);

		int nstyles = (styles != null ? styles.length : 0);
		for (int i = 0; i < nstyles; i++) {
			StyleConstants.setBackground(attr, Color.white);
			switch (i % 3) {
			case 0:
				StyleConstants.setForeground(attr, new Color((i * 8) % 127,
						127, 127));
				break;
			case 1:
				StyleConstants.setForeground(attr, new Color(127,
						(i * 8) % 127, 127));
				break;
			case 2:
				StyleConstants.setForeground(attr, new Color(127, 127,
						(i * 8) % 127));
				break;

			}
			addStyle(styles[i], text).addAttributes(attr);
		}
	}

}
