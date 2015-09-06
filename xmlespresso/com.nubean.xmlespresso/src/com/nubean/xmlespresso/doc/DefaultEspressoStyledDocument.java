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

package com.nubean.xmlespresso.doc;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;

import com.nubean.xmlespresso.editors.DefaultEspressoEditor;

public class DefaultEspressoStyledDocument extends DefaultStyledDocument {
	private static final long serialVersionUID = 8037906243235416263L;
	private DefaultEspressoEditor editor;

	public DefaultEspressoStyledDocument(Content c, StyleContext styles) {
		super(c, styles);
	}

	public DefaultEspressoStyledDocument(StyleContext styles) {
		super(styles);
	}

	public DefaultEspressoStyledDocument() {
		super();
	}
	
	public void setEditor(DefaultEspressoEditor editor) {
		this.editor = editor;
	}
	
	public DefaultEspressoEditor getEditor() {
		return editor;
	}

	public void replace(int offset, int length, String text) {
		try {
			if (editor != null)
				editor.beginUndoEditSession();
			remove(offset, length);
			insertString(offset, text, null);
			if (editor != null)
				editor.endUndoEditSession();
		} catch (BadLocationException ex) {

		}
	}
}
