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
import javax.swing.event.*;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLDocument;
import com.nubean.michxml.XMLAbstractEditor;

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

public class XMLTextElement extends XMLLeafElement {
	private int length;

	public XMLTextElement(XMLAbstractElement parent, int startOffset, String str) {
		super(parent, startOffset, str);
		length = (str != null ? str.length() : 1);
	}

	public int getEndOffset() {
		return getStartOffset() + length;
	}

	public boolean isEditable() {
		return true;
	}

	public void insertString(int offset, String str)
			throws BadLocationException {

		XMLDocument document = (XMLDocument) getDocument();
		XMLAbstractEditor editor = document.getEditor();
		boolean canInsertText = str.trim().length() == 0
				|| editor.canInsertTextInNode((XMLNode) getXMLNode()
						.getParent());

		if (!canInsertText) {
			// we send the event to grand parent because
			// parent will send it right back to us, and we will be in an
			// infinite
			// loop.
			XMLAbstractElement grandParent = (XMLAbstractElement) parent
					.getParentElement();
			if (grandParent != null)
				grandParent.insertEvent(this, offset, str);
			return;
		}

		if (offset < getStartOffset())
			offset = getStartOffset();
		else if (offset > getEndOffset())
			offset = getEndOffset();

		if (offset > getStartOffset()) {
			document.insertContent(offset, str, getAttributes());
		} else if (offset == getStartOffset()) {
			document.insertContent(offset, str, getAttributes());
			startPosition = getDocument().createPosition(offset);
		}
		length += str.length();
		setLocked(true);
		org.w3c.dom.Node node = getXMLNode().getDomNode();
		String oldValue = node.getNodeValue();
		str = document.getText(getStartOffset(), getEndOffset()
				- getStartOffset());
		node.setNodeValue(str);
		getEditor().nodeChanged(getXMLNode(), oldValue);
		setLocked(false);

	}

	public void remove(int offset, int len) throws BadLocationException {
		if (offset >= getStartOffset() && (offset + len <= getEndOffset())) {
			XMLDocument doc = (XMLDocument) getDocument();
			doc.removeContent(offset, len);
			length -= len;
			setLocked(true);
			org.w3c.dom.Node node = getXMLNode().getDomNode();
			String oldValue = node.getNodeValue();
			String str = doc.getText(getStartOffset(), getEndOffset()
					- getStartOffset());
			node.setNodeValue(str);
			getEditor().nodeChanged(getXMLNode(), oldValue);
			setLocked(false);
		}
	}

	public void replace(int offset, int len, String str)
			throws BadLocationException {
		if (offset >= getStartOffset() && (offset + len <= getEndOffset())) {
			XMLDocument document = (XMLDocument) getDocument();
			document.removeContent(offset, len);
			length -= len;

			if (offset > getStartOffset()) {
				document.insertContent(offset, str, getAttributes());
			} else if (offset == getStartOffset()) {
				document.insertContent(offset, str, getAttributes());
				startPosition = getDocument().createPosition(offset);
			}
			length += str.length();
			setLocked(true);
			org.w3c.dom.Text node = (org.w3c.dom.Text) getXMLNode()
					.getDomNode();
			str = document.getText(getStartOffset(), getEndOffset()
					- getStartOffset());
			String oldValue = node.getNodeValue();
			node.setNodeValue(str);
			getEditor().nodeChanged(getXMLNode(), oldValue);
			setLocked(false);
		}
	}

	// TreeModelListener methods
	public void treeNodesChanged(TreeModelEvent e) {
	}

	public void treeNodesInserted(TreeModelEvent e) {

	}

	public void treeNodesRemoved(TreeModelEvent e) {

	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

	public String getName() {
		return "text";
	}

	public int getType() {
		return TEXT;
	}

	@Override
	public boolean isWhitespace() {
		org.w3c.dom.Text node = (org.w3c.dom.Text) getXMLNode().getDomNode();
		String str = (node != null ? node.getNodeValue(): null);
		return (str != null ? str.trim().length() == 0 : true);
	}
}