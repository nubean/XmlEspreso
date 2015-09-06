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

import java.util.*;
import javax.swing.text.*;
import javax.swing.event.*;
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
public class XMLElement extends XMLBranchElement {

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
	 * @param getIndent
	 *            () line getIndent() for this element in the document
	 */
	public XMLElement(XMLNode xmlNode, XMLDocument xmlDocument,
			XMLAbstractElement parent, int startOffset, int indent) {
		super(xmlNode, xmlDocument, parent, startOffset, indent);
	}

	private void insertCommentElement(int index, XMLNode node) {
		index++;
		XMLStartTagElement startTag = (XMLStartTagElement) this.getElement(0);
		int adjust = startTag.getAttributeCount();

		index -= adjust;
		XMLAbstractElement ele = (XMLAbstractElement) (index < getElementCount() ? this
				.getElement(index) : null);
		int offset = (ele != null ? ele.getStartOffset() : this.getElement(0)
				.getEndOffset());

		XMLAbstractElement insertElement = new XMLCommentElement(node,
				xmlDocument, this, offset, getIndent() + INDENT);

		XMLAbstractElement[] elems = new XMLAbstractElement[1];
		elems[0] = insertElement;

		replace(index, 0, elems);
	}

	private void insertProcInstrElement(int index, XMLNode node) {
		index++;
		XMLStartTagElement startTag = (XMLStartTagElement) this.getElement(0);
		int adjust = startTag.getAttributeCount();

		index -= adjust;
		XMLAbstractElement ele = (XMLAbstractElement) (index < getElementCount() ? this
				.getElement(index) : null);
		int offset = (ele != null ? ele.getStartOffset() : this.getElement(0)
				.getEndOffset());

		XMLAbstractElement insertElement = new XMLProcInstrElement(node,
				xmlDocument, this, offset, getIndent() + INDENT);

		XMLAbstractElement[] elems = new XMLAbstractElement[1];
		elems[0] = insertElement;

		replace(index, 0, elems);
	}

	private void insertTextElement(int index, XMLNode node) {
		index++;
		XMLStartTagElement startTag = (XMLStartTagElement) this.getElement(0);
		int adjust = startTag.getAttributeCount();

		index -= adjust;
		XMLAbstractElement ele = (XMLAbstractElement) (index < getElementCount() ? this
				.getElement(index) : null);
		int offset = (ele != null ? ele.getStartOffset() : this.getElement(0)
				.getEndOffset());

		XMLAbstractElement insertElement = new XMLContentElement(node,
				xmlDocument, this, offset, getIndent() + INDENT);

		XMLAbstractElement[] elems = new XMLAbstractElement[1];
		elems[0] = insertElement;

		replace(index, 0, elems);
	}

	private void insertCDATAElement(int index, XMLNode node) {
		index++;
		XMLStartTagElement startTag = (XMLStartTagElement) this.getElement(0);
		int adjust = startTag.getAttributeCount();

		index -= adjust;
		XMLAbstractElement ele = (XMLAbstractElement) (index < getElementCount() ? this
				.getElement(index) : null);
		int offset = (ele != null ? ele.getStartOffset() : this.getElement(0)
				.getEndOffset());

		XMLAbstractElement insertElement = new XMLCDATAElement(node,
				xmlDocument, this, offset, getIndent() + INDENT);

		XMLAbstractElement[] elems = new XMLAbstractElement[1];
		elems[0] = insertElement;

		replace(index, 0, elems);
	}

	public int getTrimStartOffset() {
		return ((XMLAbstractElement) this.getElement(0)).getTrimStartOffset();
	}

	public int getTrimEndOffset() {
		return ((XMLAbstractElement) this.getElement(getElementCount() - 1))
				.getTrimEndOffset();
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

	public void insertEvent(XMLAbstractElement source, int where, String str)
			throws BadLocationException {
		if (str == null)
			return;
		XMLNode node = getXMLNode();
		int bias = 0;
		switch (source.getType()) {
		case START_TAG:
			bias = source.getBias(where);
			if (bias == BACKWARD_BIAS) {
				if (str.equals("<")) {
					getEditor().insertSubElement(parent.getXMLNode(),
							source.getXMLNode());
				} else if (str.startsWith("<")) {
					getEditor().insertFragment(str, parent.getXMLNode(),
							source.getXMLNode());
				} else
					parent.insertEvent(this, where, str);
			} else {
				XMLAbstractElement forward = source.getNextSibling();
				if (forward != null
						&& (forward.getType() == CONTENT || forward.getType() == CDATA))
					forward.insertString(where, str);
				else {
					XMLNode fnode = (forward != null ? forward.getXMLNode()
							: null);
					if (fnode != node)
						getEditor().insertTextNode(str, node, fnode);
					else
						getEditor().insertTextNode(str, node, null);
				}
			}
			break;
		case END_TAG:
			if (str.equals("<"))
				getEditor().insertSubElement(node, null);
			else if (str.startsWith("<")) {
				getEditor().insertFragment(str, node, null);
			} else {
				bias = source.getBias(where);
				switch (bias) {
				case BACKWARD_BIAS:
					XMLAbstractElement backward = source.getPrevSibling();
					if (backward != null
							&& (backward.getType() == CONTENT || backward
									.getType() == CDATA))
						backward.insertString(where, str);
					else
						getEditor().insertTextNode(str, node, null);
					break;
				case FORWARD_BIAS:
					XMLAbstractElement forward = source.getNextSibling();
					if (forward != null
							&& (forward.getType() == CONTENT || forward
									.getType() == CDATA))
						forward.insertString(where, str);
					else
						parent.insertEvent(this, where, str);
					break;
				}
			}
			break;
		case ELEMENT:
			if (str.equals("<"))
				getEditor().insertSubElement(node, source.getXMLNode());
			else if (str.startsWith("<")) {
				getEditor().insertFragment(str, node, source.getXMLNode());
			} else {
				bias = source.getBias(where);
				switch (bias) {
				case BACKWARD_BIAS:
					XMLAbstractElement backward = source.getPrevSibling();
					if (backward != null
							&& (backward.getType() == CONTENT || backward
									.getType() == CDATA))
						backward.insertString(where, str);
					else
						getEditor().insertTextNode(str, node,
								source.getXMLNode());
					break;
				case FORWARD_BIAS:
					XMLAbstractElement forward = source.getNextSibling();
					XMLNode fnode = (forward != null ? forward.getXMLNode()
							: null);
					if (forward != null
							&& (forward.getType() == CONTENT || forward
									.getType() == CDATA))
						forward.insertString(where, str);
					else {
						if (fnode.getParent() == node)
							getEditor().insertTextNode(str, node, fnode);
						else
							getEditor().insertTextNode(str, node, null);
					}
					break;
				}
			}
			break;

		default:
			break;
		}
	}

	private void insertDocumentElement(int index, XMLNode node) {
		index++;
		XMLStartTagElement startTag = (XMLStartTagElement) this.getElement(0);
		int adjust = startTag.getAttributeCount();

		index -= adjust;
		XMLAbstractElement ele = (XMLAbstractElement) (index < getElementCount() ? this
				.getElement(index) : null);
		int offset = (ele != null ? ele.getStartOffset() : this.getElement(0)
				.getEndOffset());
		XMLElement insertElement = new XMLElement(node, xmlDocument, this,
				offset, INDENT + getIndent());

		XMLAbstractElement[] elems = new XMLAbstractElement[1];
		elems[0] = insertElement;
		replace(index, 0, elems);
	}

	private void removeElement(int index) {
		index++;
		XMLStartTagElement startTag = (XMLStartTagElement) this.getElement(0);
		int adjust = startTag.getAttributeCount();

		index -= adjust;
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

	protected void init(int offset) throws BadLocationException {

		int count = getXMLNode().getChildCount();
		Position insert = getDocument().createPosition(offset);

		Vector<XMLAbstractElement> elements = new Vector<XMLAbstractElement>(
				count + 5);

		elements.add(new XMLStartTagElement(getXMLNode(), xmlDocument, this,
				insert.getOffset(), getIndent()));

		for (int i = 0; i < count; i++) {
			XMLNode node = getXMLNode().child(i);

			if (node.getDomNode().getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				if (getEditor() == null || !getEditor().isEmpty(getXMLNode())) {
					elements.add(new XMLElement(node, xmlDocument, this, insert
							.getOffset(), getIndent() + INDENT));
				} else {
					elements.add(new XMLEmptyElement(node, xmlDocument, this,
							insert.getOffset(), getIndent() + INDENT));
				}
			} else if (node.getDomNode().getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
				elements.add(new XMLContentElement(node, xmlDocument, this,
						insert.getOffset(), getIndent() + INDENT));
			} else if (node.getDomNode().getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE) {
				elements.add(new XMLCDATAElement(node, xmlDocument, this,
						insert.getOffset(), getIndent() + INDENT));
			} else if (node.getDomNode().getNodeType() == org.w3c.dom.Node.COMMENT_NODE) {
				elements.add(new XMLCommentElement(node, xmlDocument, this,
						insert.getOffset(), getIndent() + INDENT));
			}
		}

		elements.add(new XMLEndTagElement(getXMLNode(), xmlDocument, this,
				insert.getOffset(), getIndent()));
		XMLAbstractElement[] elems = new XMLAbstractElement[elements.size()];
		elements.toArray(elems);
		replace(0, 0, elems);
		this.startPosition = getDocument().createPosition(offset);
	}

	public String getName() {
		return "element";
	}

	public int getType() {
		return ELEMENT;
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
			int index = getCanonicalElementIndex(childIndicies[i]);
			switch (node.getDomNode().getNodeType()) {
			case org.w3c.dom.Node.ELEMENT_NODE:
				insertDocumentElement(index, node);
				break;
			case org.w3c.dom.Node.ATTRIBUTE_NODE:
				((XMLAbstractElement) this.getElement(0)).treeNodesInserted(e);
				break;
			case org.w3c.dom.Node.TEXT_NODE:
				insertTextElement(index, node);
				break;
			case org.w3c.dom.Node.CDATA_SECTION_NODE:
				insertCDATAElement(index, node);
				break;
			case org.w3c.dom.Node.COMMENT_NODE:
				insertCommentElement(index, node);
				break;
			case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
				insertProcInstrElement(index, node);
			}
		}
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		int[] childIndicies = e.getChildIndices();
		Object[] children = e.getChildren();
		int count = (childIndicies != null ? childIndicies.length : 0);
		for (int i = 0; i < count; i++) {
			XMLNode node = (XMLNode) children[i];
			int index = getCanonicalElementIndex(childIndicies[i]);
			switch (node.getDomNode().getNodeType()) {
			case org.w3c.dom.Node.ATTRIBUTE_NODE:
				((XMLAbstractElement) this.getElement(0)).treeNodesRemoved(e);
				break;
			default:
				removeElement(index);
				break;
			}
		}
	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

}
