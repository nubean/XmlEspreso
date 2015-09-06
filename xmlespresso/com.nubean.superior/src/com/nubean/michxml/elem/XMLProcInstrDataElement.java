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

public class XMLProcInstrDataElement extends XMLLeafElement {
	private int length;

	public XMLProcInstrDataElement(XMLAbstractElement parent, int startOffset,
			String str) {
		super(parent, startOffset, str);
		length = (str != null ? str.length() : 1);
	}

	public int getEndOffset() {
		return getStartOffset() + length;
	}

	public void insertString(int offset, String str)
			throws BadLocationException {
		XMLDocument document = (XMLDocument) getDocument();
		if (offset > getStartOffset()) {
			document.insertContent(offset, str, getAttributes());
		} else if (offset == getStartOffset()) {
			document.insertContent(offset, str, getAttributes());
			startPosition = getDocument().createPosition(offset);
		}
		length += str.length();
		setLocked(true);
		try {
			org.w3c.dom.ProcessingInstruction node = (org.w3c.dom.ProcessingInstruction) getXMLNode()
					.getDomNode();
			str = document.getText(getStartOffset(), getEndOffset()
					- getStartOffset());
			String oldValue = node.getData();
			node.setData(str);
			getEditor().nodeChanged(getXMLNode(), oldValue);
		} catch (ClassCastException e) {
		}
		setLocked(false);
	}

	public void remove(int offset, int len) throws BadLocationException {
		if (offset >= getStartOffset() && (offset + len <= getEndOffset())) {
			XMLDocument doc = (XMLDocument) getDocument();
			doc.removeContent(offset, len);
			length -= len;
			setLocked(true);
			try {
				org.w3c.dom.ProcessingInstruction node = (org.w3c.dom.ProcessingInstruction) getXMLNode()
						.getDomNode();
				String str = getDocument().getText(getStartOffset(),
						getEndOffset() - getStartOffset());
				String oldValue = node.getData();
				node.setData(str);
				getEditor().nodeChanged(getXMLNode(), oldValue);
			} catch (ClassCastException e) {
			}
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
		return "proc-instr-data";
	}

	public int getType() {
		return PROC_INSTR_DATA;
	}

	@Override
	public boolean isWhitespace() {
		return false;
	}
}