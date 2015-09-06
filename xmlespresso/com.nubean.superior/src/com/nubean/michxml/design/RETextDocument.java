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

import javax.swing.text.*;
import java.util.*;

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

public class RETextDocument extends PlainDocument {
	private static final long serialVersionUID = 2972984137634903414L;
	protected Vector<InsertListener> insertListeners;
	protected TypeDefinition typedef;

	public RETextDocument(TypeDefinition typedef) {
		super();
		this.typedef = typedef;
		insertListeners = new Vector<InsertListener>(2);
	}

	public void addInsertListener(InsertListener l) {
		insertListeners.addElement(l);
	}

	public void removeInsertListener(InsertListener l) {
		insertListeners.removeElement(l);
	}

	@SuppressWarnings("unchecked")
	protected void fireInsertListeners(InsertEvent ev) {
		Vector<InsertListener> c = (Vector<InsertListener>) insertListeners.clone();

		for (Enumeration<InsertListener> e = c.elements(); e.hasMoreElements();) {
			InsertListener l = e.nextElement();

			l.textInsert(ev);
		}
	}

	public void insertString(int offs, String val, AttributeSet a)
			throws BadLocationException {
		if (val == null)
			return;

		super.insertString(offs, val, a);
		String text = getText(0, getLength());

		boolean match = typedef.isValid(text);

		if (!match) {
			fireInsertListeners(new InsertEvent(this,
					InsertEvent.INVALID_INSERT));
			return;
		} else {
			fireInsertListeners(new InsertEvent(this, InsertEvent.VALID_INSERT));
		}
	}

	public void remove(int offs, int len) throws BadLocationException {

		try {
			super.remove(offs, len);
			String curValue = getText(0, getLength());
			if (curValue == null || curValue.length() == 0)
				return;

			boolean match = typedef.isValid(curValue);
			if (!match) {
				fireInsertListeners(new InsertEvent(this,
						InsertEvent.INVALID_INSERT));
				return;
			} else {
				fireInsertListeners(new InsertEvent(this,
						InsertEvent.VALID_INSERT));
			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
