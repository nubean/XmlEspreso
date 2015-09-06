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

public class XMLDocumentElement extends XMLBranchElement {
	private int adjust;

	/**
	 * Creates a new XMLDocumentElement.
	 * 
	 * @param xmlNode
	 *            xml element
	 * @param document
	 *            this elements document
	 */
	public XMLDocumentElement(XMLNode xmlNode, XMLDocument xmlDocument) {
		super(xmlNode, xmlDocument, null, 0, 0);
	}

	private int skipDecl(int index) {
		if (index == 0) {
			XMLAbstractElement ele = (getElementCount() > 0 ? (XMLAbstractElement) getElement(0)
					: null);
			if (ele != null && (ele instanceof XMLDeclElement)) {
				return (index + 1);
			}
		}

		return index;
	}

	private void insertCommentElement(int index, XMLNode node) {
		index += adjust;
		index = skipDecl(index);

		XMLAbstractElement ele = (index < this.getElementCount() ? (XMLAbstractElement) this
				.getElement(index)
				: null);
		int offset = (ele != null ? ele.getStartOffset() : getEndOffset());

		XMLWhiteSpaceElement ws = new XMLWhiteSpaceElement(this, offset, "\n");
		XMLAbstractElement insertElement = new XMLCommentElement(node,
				(XMLDocument) this.getDocument(), this, ws.getEndOffset(), getIndent()
						+ INDENT);

		XMLAbstractElement[] elems = new XMLAbstractElement[2];
		elems[0] = ws;
		elems[1] = insertElement;

		replace(index, 0, elems);
	}

	private void insertProcInstrElement(int index, XMLNode node) {
		index += adjust;
		index = skipDecl(index);

		XMLAbstractElement ele = (index < this.getElementCount() ? (XMLAbstractElement) this
				.getElement(index)
				: null);
		int offset = (ele != null ? ele.getStartOffset() : getEndOffset());

		XMLWhiteSpaceElement ws = new XMLWhiteSpaceElement(this, offset, "\n");
		XMLAbstractElement insertElement = new XMLProcInstrElement(node,
				xmlDocument, this, ws.getEndOffset(), this.getIndent() + INDENT);

		XMLAbstractElement[] elems = new XMLAbstractElement[2];
		elems[0] = ws;
		elems[1] = insertElement;

		replace(index, 0, elems);
	}

	protected void init(int startOffset) throws BadLocationException {
		int count = this.getXMLNode().getChildCount();
		Vector<XMLAbstractElement> elements = new Vector<XMLAbstractElement>(2 * (count + 2));

		XMLAbstractElement xmlDecl = new XMLDeclElement(this.getXMLNode(),
				(XMLDocument) this.getDocument(), this, 0, getIndent());
		elements.add(xmlDecl);
		adjust++;
		Position insert = getDocument().createPosition(xmlDecl.getEndOffset());
		if (((org.w3c.dom.Document) getXMLNode().getDomNode()).getDoctype() != null) {
			elements.add(new XMLDocTypeElement(this.getXMLNode(),
					(XMLDocument) this.getDocument(), this, insert.getOffset(),
					this.getIndent()));
			adjust++;
		}
		int index = 0;
		for (index = 0; index < count; index++) {
			XMLNode node = this.getXMLNode().child(index);

			if (node.getDomNode().getNodeType() == org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE) {
				elements.add(new XMLProcInstrElement(node, (XMLDocument) this
						.getDocument(), this, insert.getOffset(), this
						.getIndent()));
			} else if (node.getDomNode().getNodeType() == org.w3c.dom.Node.COMMENT_NODE) {
				elements.add(new XMLCommentElement(node, (XMLDocument) this
						.getDocument(), this, insert.getOffset(), getIndent()));
			} else if (node.getDomNode().getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				if (getEditor() == null || !getEditor().isEmpty(getXMLNode())) {
					elements.add(new XMLElement(node, xmlDocument, this, insert
							.getOffset(), getIndent()));
				} else {
					elements.add(new XMLEmptyElement(node, xmlDocument, this,
							insert.getOffset(), getIndent()));
				}
				index++;
				break;
			}

		}

		for (; index < count; index++) {
			XMLNode node = this.getXMLNode().child(index);
			if (node.getDomNode().getNodeType() == org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE) {
				elements.add(new XMLProcInstrElement(node, (XMLDocument) this
						.getDocument(), this, insert.getOffset(), this
						.getIndent()));
			} else if (node.getDomNode().getNodeType() == org.w3c.dom.Node.COMMENT_NODE) {
				elements.add(new XMLCommentElement(node, (XMLDocument) this
						.getDocument(), this, insert.getOffset(), getIndent()));
			}
		}

		XMLAbstractElement[] elems = new XMLAbstractElement[elements.size()];
		elements.toArray(elems);
		replace(0, 0, elems);
		this.startPosition = getDocument().createPosition(startOffset);
	}

	public String getName() {
		return "document";
	}

	public int getType() {
		return DOCUMENT_ELEMENT;
	}

	public void insertEvent(XMLAbstractElement source, int where, String str)
			throws BadLocationException {
		if (str == null)
			return;
		if (str.trim().length() == 0) {
			XMLDocument doc = (XMLDocument) getDocument();
			doc.insertContent(where, str, null);
			return;
		}
		XMLNode node = getXMLNode();
		if (str.equals("<"))
			getEditor().insertSubElement(node, source.getXMLNode());
	}

	private void removeElement(int index) {
		index += adjust;
		XMLAbstractElement ele = (XMLAbstractElement) this.getElement(index);
		XMLDocument document = (XMLDocument) getDocument();
		try {
			document.removeContent(ele.getStartOffset(),
					(ele.getEndOffset() - ele.getStartOffset()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		replace(index, 1, (XMLAbstractElement[]) null);
	}

	// TreeModelListener methods
	public void treeNodesChanged(TreeModelEvent e) {
	}

	public void treeNodesInserted(TreeModelEvent e) {
		int[] childIndicies = e.getChildIndices();
		Object[] children = e.getChildren();
		int count = (childIndicies != null ? childIndicies.length : 0);
		for (int i = 0; i < count; i++) {
			XMLNode node = (XMLNode) children[i];
			int index = childIndicies[i];
			switch (node.getDomNode().getNodeType()) {
			case org.w3c.dom.Node.COMMENT_NODE:
				insertCommentElement(index, node);
				break;
			case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
				insertProcInstrElement(index, node);
				break;
			}
		}
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		int[] childIndicies = e.getChildIndices();
		int count = (childIndicies != null ? childIndicies.length : 0);
		for (int i = 0; i < count; i++) {
			int index = childIndicies[i];
			removeElement(index);
		}
	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

}