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

import javax.swing.event.*;
import javax.swing.text.*;
import java.util.*;
import com.nubean.michxml.XMLNode;
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

public class XMLCommentElement extends XMLBranchElement {

	public XMLCommentElement(XMLNode xmlNode, XMLDocument xmlDocument,
			XMLAbstractElement parent, int startOffset, int indent) {
		super(xmlNode, xmlDocument, parent, startOffset, indent);
	}

	protected void init(int offset) throws BadLocationException {
		org.w3c.dom.Comment comment = (org.w3c.dom.Comment) getXMLNode()
				.getDomNode();
		Position insert = getDocument().createPosition(offset);
		String data = comment.getData();
		Vector<XMLAbstractElement> elements = new Vector<XMLAbstractElement>();

		elements.add(new XMLLabelElement(this, insert.getOffset(), "<!--"));
		elements.add(new XMLCommentDataElement(this, insert.getOffset(), data));
		elements.add(new XMLLabelElement(this, insert.getOffset(), "-->"));
		elements.add(new XMLWhiteSpaceElement(this, insert.getOffset(), "\n"));
		XMLAbstractElement[] elems = new XMLAbstractElement[elements.size()];
		elements.toArray(elems);
		replace(0, 0, elems);
		this.startPosition = getDocument().createPosition(offset);
	}

	private XMLAbstractElement getCommentDataElement() {
		for (int i = 0; i < getElementCount(); i++) {
			XMLAbstractElement ele = (XMLAbstractElement) this.getElement(i);
			if (ele.getType() == COMMENT_DATA)
				return ele;
		}
		return null; // should not happend
	}

	public void insertEvent(XMLAbstractElement source, int offset, String str)
			throws BadLocationException {
		XMLAbstractElement text = getCommentDataElement();
		if (offset <= text.getEndOffset() && offset >= text.getStartOffset())
			text.insertString(offset, str);
		else
			parent.insertEvent(this, offset, str);
	}

	public void removeEvent(XMLAbstractElement source, int where, int len)
			throws BadLocationException {
		if (len == 0)
			return;
		if (where >= getStartOffset() && where <= getTrimStartOffset()
				&& (where + len >= getTrimEndOffset())
				&& (where + len <= getEndOffset())) {
			getEditor().removeNode(parent.getXMLNode(), getXMLNode());
		} else {
			parent.removeEvent(this, where, len);
		}
	}

	// TreeModelListener methods
	public void treeNodesChanged(TreeModelEvent e) {
		try {
			int soffset = getStartOffset();
			XMLDocument doc = (XMLDocument) getDocument();
			doc.removeContent(soffset, getEndOffset() - soffset);
			init(soffset);
		} catch (javax.swing.text.BadLocationException be) {
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {

	}

	public void treeNodesRemoved(TreeModelEvent e) {

	}

	public void treeStructureChanged(TreeModelEvent e) {

	}

	public String getName() {
		return "comment";
	}

	public int getType() {
		return COMMENT;
	}
}