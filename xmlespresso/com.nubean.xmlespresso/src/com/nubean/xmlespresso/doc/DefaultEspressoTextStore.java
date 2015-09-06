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

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.ITextStore;

public class DefaultEspressoTextStore implements ITextStore {

	private DefaultEspressoStyledDocument doc;
	private DocumentEvent pending;

	public DefaultEspressoTextStore(DefaultEspressoStyledDocument doc) {
		this.doc = doc;
	}

	public void setDocument(DefaultEspressoStyledDocument doc) {
		this.doc = doc;
	}

	public char get(int offset) {
		try {
			doc.getText(offset, 1).charAt(0);
		} catch (BadLocationException e) {

		}
		return 0;
	}

	public String get(int offset, int length) {
		try {
			if (pending != null) {
				int where = pending.getOffset();
				int len = pending.getLength();

				if (offset >= where) {

					String text = pending.getText();

					if (len == 0) {
						if (text != null) {
							return doc.getText(offset + text.length(), length);
						}
					} else if (len > 0) {
						if (text != null) {
							if ((offset - where + length) <= text.length())
								return text.substring(offset - where, length);
						}
					}

				}

			}
			return doc.getText(offset, length);
		} catch (BadLocationException e) {

		}
		return null;
	}

	public int getLength() {
		return doc.getLength();
	}

	public void replace(int offset, int length, String text) {
		doc.replace(offset, length, text);

	}

	public void set(String text) {
		replace(0, doc.getLength(), text);
	}
	
	public void documentAboutToBeChanged(DocumentEvent e) {
		this.pending = e;
	}

}
