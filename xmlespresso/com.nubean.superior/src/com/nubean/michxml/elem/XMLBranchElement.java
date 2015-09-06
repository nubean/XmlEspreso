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

public abstract class XMLBranchElement implements XMLAbstractElement {
	/**
	 * Creates a new XMLElement.
	 * 
	 * @param xmlNode
	 *            xml element
	 * @param document
	 *            this elements document
	 * @param parent
	 *            the parent element
	 * @param startOffset
	 *            start offset of this element
	 * @param indent
	 *            line indent for this element in the document
	 */
	public XMLBranchElement(XMLNode xmlNode, XMLDocument xmlDocument,
			XMLAbstractElement parent, int startOffset, int indent) {
		this.parent = parent;
		this.xmlDocument = xmlDocument;
		this.xmlNode = xmlNode;
		this.indent = indent;
		lastIndex = -1;

		try {
			this.startPosition = xmlDocument.createPosition(startOffset);
			if (xmlNode != null && !xmlNode.isParsed())
				init(startOffset);
		} catch (BadLocationException e) {

		}
	}

	public int getTrimStartOffset() {
		int retval = getStartOffset();
		for (int i = 0; i < nchildren; i++) {
			if (children[i].getType() != WHITE_SPACE) {
				retval = children[i].getStartOffset();
				break;
			}
		}
		return retval;
	}

	public int getTrimEndOffset() {
		int retval = getEndOffset();
		for (int i = nchildren - 1; i >= 0; i--) {
			if (children[i].getType() != WHITE_SPACE) {
				retval = children[i].getEndOffset();
				break;
			}
		}
		return retval;
	}

	public void insertEvent(XMLAbstractElement source, int where, String str)
			throws BadLocationException {
		if (parent != null)
			parent.insertEvent(this, where, str);
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

	public void removeEvent(XMLAbstractElement source, int where, int len)
			throws BadLocationException {
		if (parent != null)
			parent.removeEvent(this, where, len);
	}

	public void replace(int offset, int len, String text)
			throws BadLocationException {
	}

	public void insertString(int where, String str) throws BadLocationException {
	}

	public void remove(int offset, int len) throws BadLocationException {

	}

	protected abstract void init(int offset) throws BadLocationException;

	public XMLAbstractEditor getEditor() {
		return xmlDocument.getEditor();
	}

	// --- Element methods -------------------------------------

	/**
	 * Retrieves the underlying model.
	 * 
	 * @return the model
	 */
	public Document getDocument() {
		return xmlDocument;
	}

	/**
	 * Gets the parent of the element.
	 * 
	 * @return the parent
	 */
	public Element getParentElement() {
		return parent;
	}

	/**
	 * Gets the attributes for the element.
	 * 
	 * @return the attribute set
	 */
	public AttributeSet getAttributes() {
		StyledDocument sd = (StyledDocument) getDocument();
		return sd.getStyle(getName());
	}

	public int getElementCount() {
		return nchildren;
	}

	public Element getElement(int index) {
		return children[index];
	}

	public int getStartOffset() {
		return startPosition.getOffset();
	}

	public void dump() {
		System.out.println(CommonUtils.expandTabs(indent) + "{" + getName()
				+ ":" + getStartOffset());
		for (int i = 0; i < nchildren; i++) {
			XMLAbstractElement child = children[i];
			child.dump();
		}
		System.out.println(CommonUtils.expandTabs(indent) + getEndOffset()
				+ "}");
	}

	public int getElementIndex(int offset) {
		int index;
		int lower = 0;
		int upper = nchildren - 1;
		int mid = 0;
		int p0 = getStartOffset();
		int p1;

		if (nchildren == 0) {
			return 0;
		}
		if (offset >= getEndOffset()) {
			return nchildren - 1;
		}

		// see if the last index can be used.
		if ((lastIndex >= lower) && (lastIndex <= upper)) {
			Element lastHit = children[lastIndex];
			p0 = lastHit.getStartOffset();
			p1 = lastHit.getEndOffset();
			if ((offset >= p0) && (offset < p1)) {
				return lastIndex;
			}

			// last index wasn't a hit, but it does give useful info about
			// where a hit (if any) would be.
			if (offset < p0) {
				upper = lastIndex;
			} else {
				lower = lastIndex;
			}
		}

		while (lower <= upper) {
			mid = lower + ((upper - lower) / 2);
			Element elem = children[mid];
			p0 = elem.getStartOffset();
			p1 = elem.getEndOffset();
			if ((offset >= p0) && (offset < p1)) {
				// found the location
				index = mid;
				lastIndex = index;
				return index;
			} else if (offset < p0) {
				upper = mid - 1;
			} else {
				lower = mid + 1;
			}
		}

		// didn't find it, but we indicate the index of where it would belong
		if (offset < p0) {
			index = mid;
		} else {
			index = mid + 1;
		}
		lastIndex = index;
		return index;
	}

	public int getEndOffset() {
		return (nchildren > 0 ? children[nchildren - 1].getEndOffset()
				: getStartOffset());
	}

	public abstract String getName();

	public XMLAbstractElement getPrevSibling() {
		if (parent == null)
			return null;
		XMLAbstractElement prev = null;

		for (int i = parent.getElementCount() - 1; i > 0; i--) {
			if (parent.getElement(i) == this) {
				prev = (XMLAbstractElement) parent.getElement(i - 1);
				break;
			}
		}

		return prev;
	}

	public XMLAbstractElement getNextSibling() {
		if (parent == null)
			return null;
		XMLAbstractElement next = null;
		for (int i = 0; i < parent.getElementCount() - 1; i++) {
			if (parent.getElement(i) == this) {
				next = (XMLAbstractElement) parent.getElement(i + 1);
				break;
			}
		}
		return next;
	}

	/**
	 * Replaces content with a new set of elements.
	 * 
	 * @param offset
	 *            the starting offset >= 0
	 * @param length
	 *            the length to replace >= 0
	 * @param elems
	 *            the new elements
	 */
	public void replace(int offset, int length, XMLAbstractElement[] elems) {
		int elen = (elems != null ? elems.length : 0);
		int clen = (children != null ? children.length : 0);
		int delta = elen - length;
		int src = offset + length;
		int nmove = nchildren - src;
		int dest = src + delta;
		if ((nchildren + delta) >= clen) {
			// need to grow the array
			int newLength = Math.max(2 * clen, nchildren + delta);
			XMLAbstractElement[] newChildren = new XMLAbstractElement[newLength];
			if (children != null)
				System.arraycopy(children, 0, newChildren, 0, offset);
			if (elems != null)
				System.arraycopy(elems, 0, newChildren, offset, elen);
			if (children != null)
				System.arraycopy(children, src, newChildren, dest, nmove);
			children = newChildren;
		} else {
			// patch the existing array
			System.arraycopy(children, src, children, dest, nmove);
			if (elems != null)
				System.arraycopy(elems, 0, children, offset, elen);
		}
		nchildren = nchildren + delta;
	}

	public XMLNode getXMLNode() {
		return xmlNode;
	}

	public int getCanonicalElementIndex(int index) {
		
		int whiteSpaceElementCount = 0;
		int nonWhiteSpaceElementCount = 0;
		
		for (int i = 1; i < (nchildren - 1) && (nonWhiteSpaceElementCount < index); i++) {
			if(children[i].isWhitespace())
				whiteSpaceElementCount++;
			else
				nonWhiteSpaceElementCount++;
		}

		index += whiteSpaceElementCount;
		return index;
	}

	public XMLAbstractElement positionToElement(int pos) {
		int index = getElementIndex(pos);
		XMLAbstractElement child = children[index];
		int p0 = child.getStartOffset();
		int p1 = child.getEndOffset();
		if ((pos >= p0) && (pos < p1)) {
			if (child.isLeaf())
				return child;
			else
				return child.positionToElement(pos);
		}
		return null;
	}

	public XMLAbstractElement spanToElement(int pos, int len) {
		int index = getElementIndex(pos);
		XMLAbstractElement child = children[index];
		int p0 = child.getStartOffset();
		int p1 = child.getEndOffset();
		if ((pos == p0) && (pos + len == p1))
			return child;
		else {
			if (!child.isLeaf())
				return child.spanToElement(pos, len);
			else
				return null;
		}
	}

	public void applyAttributes() {
		for (int i = 0; i < nchildren; i++) {
			children[i].applyAttributes();
		}
	}

	public boolean isLeaf() {
		return false;
	}

	public int getIndent() {
		return indent;
	}

	public boolean getLocked() {
		if (parent != null)
			return parent.getLocked();
		else
			return locked;
	}

	public void setLocked(boolean lock) {
		if (parent != null)
			parent.setLocked(lock);
		else
			locked = lock;
	}

	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isWhitespace() {
		boolean ws = true;
		
		for(int i=0; i < nchildren; i++) {
			if(!children[i].isWhitespace()) {
				ws = false;
				break;
			}
		}
		
		return ws;
	}
	// ---- variables -----------------------------------------------------

	protected XMLAbstractElement parent;

	protected XMLDocument xmlDocument;

	private int nchildren;

	protected Position startPosition;

	private XMLNode xmlNode;

	private XMLAbstractElement[] children;

	private int lastIndex;

	private int indent;

	protected boolean locked;
}