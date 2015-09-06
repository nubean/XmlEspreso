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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.jface.text.Document;

import com.nubean.xmlespresso.XMLEspressoActivator;

public class DefaultEspressoDocument extends Document {

	private DefaultEspressoStyledDocument doc;

	private DocumentListener listener;

	private org.eclipse.jface.text.DocumentEvent pending;

	public DefaultEspressoDocument(DefaultEspressoStyledDocument doc) {
		super();
		this.doc = doc;
		setTextStore(new DefaultEspressoTextStore(doc));
		try {
			int len = doc.getLength();
			if (len > 0)
				getTracker().set(doc.getText(0, len));
		} catch (javax.swing.text.BadLocationException e) {

		}

		doc.addDocumentListener(listener = new DocumentAdapter());
	}

	public DefaultEspressoStyledDocument getDocument() {
		return doc;
	}

	public void set(String text, long modificationStamp) {
		replaceText(0, doc.getLength(), text);
	}

	public void set(String text) {
		replaceText(0, doc.getLength(), text);
	}

	public void replace(int pos, int length, String text, long modificationStamp) {
		replaceText(pos, length, text);
	}

	public void replace(int pos, int length, String text) {
		replaceText(pos, length, text);
	}

	private void replaceText(int offset, int length, String text) {
		pending = new org.eclipse.jface.text.DocumentEvent(
				DefaultEspressoDocument.this, offset, length, text);
		fireDocumentAboutToBeChanged(pending);
		doc.replace(offset, length, text);
		pending = null;
	}

	public void setDocument(DefaultEspressoStyledDocument doc) {
		if (listener != null) {
			doc.removeDocumentListener(listener);
			listener = null;
		}
		this.doc = doc;
		DefaultEspressoTextStore store = (DefaultEspressoTextStore) getStore();
		store.setDocument(doc);

		try {
			getTracker().set(doc.getText(0, doc.getLength()));
		} catch (javax.swing.text.BadLocationException e) {

		}

		doc.addDocumentListener(listener = new DocumentAdapter());
	}

	private class DocumentAdapter implements DocumentListener {

		private void processUpdate(int offset, int length, String text) {
			try {
				org.eclipse.jface.text.DocumentEvent e = new org.eclipse.jface.text.DocumentEvent(
						DefaultEspressoDocument.this, offset, length, text);
				if (pending == null) {
					DefaultEspressoTextStore store = (DefaultEspressoTextStore) getStore();
					store.documentAboutToBeChanged(e);
					fireDocumentAboutToBeChanged(e);
					store.documentAboutToBeChanged(null);
				}
				getTracker().replace(offset, length, text);

				fireDocumentChanged(e);
			} catch (Exception e) {
				XMLEspressoActivator.getDefault().error("Document listener error:",
						e);
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			try {
				processUpdate(e.getOffset(), 0, doc.getText(e.getOffset(), e
						.getLength()));
			} catch (Exception ex) {
				XMLEspressoActivator.getDefault().error("insert update:", ex);
			}
		}

		public void removeUpdate(DocumentEvent e) {
			try {
				processUpdate(e.getOffset(), e.getLength(), "");
			} catch (Exception ex) {
				XMLEspressoActivator.getDefault().error("remove update:", ex);
			}
		}

	}

}