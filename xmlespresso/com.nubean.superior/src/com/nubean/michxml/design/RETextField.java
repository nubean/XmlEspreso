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

package com.nubean.michxml.design;

import javax.swing.*;
import java.awt.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michutil.*;
import com.nubean.michxml.TypeDefinition;

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

public class RETextField extends JTextField implements InsertListener {
	private static final long serialVersionUID = 3016575661117654336L;
	protected RETextDocument document;
	protected Color fg;
	protected boolean invalid;

	public RETextField(String text, int cols, TypeDefinition typedef) {
		super(new RETextDocument(typedef), null, cols);
		document = (RETextDocument) getDocument();
		document.addInsertListener(this);
		if (text != null)
			setText(CommonUtils.unescape(text.trim()));
		fg = getForeground();
	}

	public void textInsert(InsertEvent e) {
		if (e.getType() == InsertEvent.INVALID_INSERT) {
			setForeground(Color.red);
			invalid = true;
		} else {
			setForeground(fg);
			invalid = false;
		}
	}

	public String getText() {
		if (invalid)
			return null;
		else
			return super.getText();
	}

}