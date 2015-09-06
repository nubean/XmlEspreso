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

package com.nubean.michxml.elem;

import javax.swing.text.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLDocument;

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

public abstract class XMLLeafElement implements XMLAbstractElement {
	public XMLLeafElement(XMLAbstractElement parent, int startOffset,
			String initialString) {
		this.parent = parent;
		if (initialString == null || initialString.length() == 0)
			initialString = " ";

		try {
			XMLDocument document = (XMLDocument) getDocument();
			if (getXMLNode() != null && !getXMLNode().isParsed()) {
				document.insertContent(startOffset, initialString,
						getAttributes());
			} 
			this.startPosition = getDocument().createPosition(startOffset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void dump() {
		int startOffset = getStartOffset();
		int endOffset = getEndOffset();
		int length = endOffset - startOffset;
		
		try {
		System.out.println(CommonUtils.expandTabs(getIndent() + INDENT) + "["
				+ getName() + ":" + startOffset + "," + endOffset +": '"
				+ getDocument().getText(startOffset, length) + "'"
				+ "] ");
		} catch(BadLocationException e) {
			System.out.println(e.getMessage()+":"+startOffset+","+endOffset);
		}
	}

	public XMLAbstractEditor getEditor() {
		return (XMLAbstractEditor) ((XMLDocument) getDocument()).getEditor();
	}

	public AttributeSet getAttributes() {
		StyledDocument sd = (StyledDocument) getDocument();
		return sd.getStyle(getName());
	}

	public void applyAttributes() {
		StyledDocument sd = (StyledDocument) getDocument();
		int startOffset = getStartOffset();
		int length = getEndOffset() - startOffset;
		sd.setCharacterAttributes(startOffset, length, getAttributes(), true);
	}
	
	public boolean isEditable() {
		return false;
	}

	public XMLAbstractElement positionToElement(int pos) {
		return null;
	}

	public Document getDocument() {
		return parent.getDocument();
	}

	public Element getParentElement() {
		return parent;
	}

	public int getElementCount() {
		return 0;
	}

	public Element getElement(int index) {
		return this;
	}

	public int getStartOffset() {
		return startPosition.getOffset();
	}

	public int getElementIndex(int offset) {
		return -1;
	}

	public XMLNode getXMLNode() {
		return parent.getXMLNode();
	}

	public XMLAbstractElement spanToElement(int pos, int len) {
		int p0 = getStartOffset();
		int p1 = getEndOffset();
		if ((pos == p0) && (pos + len == p1))
			return this;
		else
			return null;
	}

	public boolean isLeaf() {
		return true;
	}

	public int getIndent() {
		return parent.getIndent();
	}

	public boolean getLocked() {
		return parent.getLocked();
	}

	public void setLocked(boolean lock) {
		parent.setLocked(lock);
	}

	public void insertString(int offset, String str)
			throws BadLocationException {
		parent.insertEvent(this, offset, str);
	}

	public int getBias(int offset) {
		int start = getStartOffset();
		int end = getEndOffset();
		int mid = (start + end) / 2;
		if (offset <= mid)
			return BACKWARD_BIAS;
		else
			return FORWARD_BIAS;
	}

	public void replace(int offset, int len, String text)
			throws BadLocationException {
	}

	public void remove(int offset, int len) throws BadLocationException {
		parent.removeEvent(this, offset, len);
	}

	public void insertEvent(XMLAbstractElement source, int where, String str)
			throws BadLocationException {

	}

	public void removeEvent(XMLAbstractElement source, int where, int len)
			throws BadLocationException {
	}

	public XMLAbstractElement getPrevSibling() {
		XMLAbstractElement prev = null;

		for (int i = parent.getElementCount() - 1; i > 0; i--) {
			if (parent.getElement(i) == this) {
				prev = (XMLAbstractElement) parent.getElement(i - 1);
				break;
			}
		}

		return prev;
	}

	public int getTrimEndOffset() {
		return getEndOffset();
	}

	public int getTrimStartOffset() {
		return getStartOffset();
	}

	public XMLAbstractElement getNextSibling() {
		XMLAbstractElement next = null;

		for (int i = 0; i < parent.getElementCount() - 1; i++) {
			if (parent.getElement(i) == this) {
				next = (XMLAbstractElement) parent.getElement(i + 1);
				break;
			}
		}
		return next;
	}

	protected XMLAbstractElement parent;

	protected Position startPosition;
}