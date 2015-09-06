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

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import com.nubean.michbase.CommonUtils;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLDocument;
import com.nubean.michxml.XMLNode;

public class XMLEmptyElement extends XMLBranchElement {

	public XMLEmptyElement(XMLNode xmlNode, XMLDocument xmlDocument,
			XMLAbstractElement parent, int startOffset, int indent) {
		super(xmlNode, xmlDocument, parent, startOffset, indent);
	}

	public int toChildrenOffset(int aindex) {
		Vector<Integer> v = new Vector<Integer>();
		for (int i = 0; i < getElementCount(); i++) {
			if (((XMLAbstractElement) this.getElement(i)).getType() == XMLAbstractElement.ATTRIBUTE)
				v.add(new Integer(i));
		}

		if (v.size() > aindex) {
			Integer index = (Integer) v.elementAt(aindex);
			return index.intValue();
		} else {
			for (int i = getElementCount() - 1; i > 0; i--) {
				if (((XMLAbstractElement) this.getElement(i)).getType() == XMLAbstractElement.LABEL)
					return i;
			}
		}

		// should never make it here
		return ++aindex;
	}

	public int getAttributeCount() {

		int acount = 0;
		for (int i = 0; i < getElementCount(); i++) {
			if (((XMLAbstractElement) this.getElement(i)).getType() == XMLAbstractElement.ATTRIBUTE)
				acount++;
		}

		return acount;
	}

	protected void init(int offset) throws BadLocationException {
		Position insert = getDocument().createPosition(offset);
		int tagStyle = (getEditor() != null ? getEditor().getTagStyle()
				: XMLAbstractEditor.TAG_NOT_INDENT);

		boolean parsed = getXMLNode().isParsed();
		Vector<XMLAbstractElement> elements = new Vector<XMLAbstractElement>();
		if (!parsed && tagStyle == XMLAbstractEditor.TAG_INDENT
				&& getIndent() > 0)
			elements.add(new XMLWhiteSpaceElement(this, insert.getOffset(),
					"\n" + CommonUtils.expandTabs(getIndent())));
		StringBuffer sb = new StringBuffer();
		sb.append('<').append(getXMLNode().getDomNode().getNodeName());

		XMLNode xmlNode = getXMLNode();
		int count = xmlNode.childCount();
		elements.add(new XMLLabelElement(this, insert.getOffset(), sb
				.toString()));

		for (int i = 0; i < count; i++) {
			XMLNode node = xmlNode.child(i);
			if (node.getDomNode().getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
				elements.add(new XMLAttributeElement(node, xmlDocument, this,
						insert.getOffset(), getIndent()));
			}
		}

		elements.add(new XMLLabelElement(this, insert.getOffset(), "/>"));

		XMLAbstractElement[] elems = new XMLAbstractElement[elements.size()];
		elements.toArray(elems);
		replace(0, 0, elems);
		this.startPosition = getDocument().createPosition(offset);
	}

	private void insertAttributeElement(int index, XMLNode node) {
		index = toChildrenOffset(index);
		XMLAbstractElement ele = (index < getElementCount() ? (XMLAbstractElement) this
				.getElement(index) : null);
		int offset = (ele != null ? ele.getStartOffset() : this.getElement(1)
				.getStartOffset());
		XMLAbstractElement[] elems = new XMLAbstractElement[1];
		elems[0] = new XMLAttributeElement(node, xmlDocument, this, offset,
				getIndent());
		replace(index, 0, elems);
	}

	private void removeAttributeElement(int index, XMLNode node) {
		index = toChildrenOffset(index);
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

	public void insertEvent(XMLAbstractElement source, int where, String str)
			throws BadLocationException {

		int tagStyle = (getEditor() != null ? getEditor().getTagStyle()
				: XMLAbstractEditor.TAG_NOT_INDENT);

		boolean parsed = getXMLNode().isParsed();
		boolean adjust = (!parsed && tagStyle == XMLAbstractEditor.TAG_INDENT && getIndent() > 0);
		XMLAbstractElement startElement = (XMLAbstractElement) (adjust ? this
				.getElement(1) : this.getElement(0));
		XMLAbstractElement endElement = (XMLAbstractElement) (getElement(getElementCount() - 1));

		if ((where >= getStartOffset() && where <= startElement
				.getStartOffset())
				|| (where <= getEndOffset() && where >= endElement
						.getEndOffset()))
			parent.insertEvent(this, where, str);
		else {
			if (str.equals(" "))
				getEditor().showAttributes(getXMLNode());
		}
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
			case org.w3c.dom.Node.ATTRIBUTE_NODE:
				insertAttributeElement(index, node);
				break;
			}
		}

	}

	public void treeNodesRemoved(TreeModelEvent e) {
		int[] childIndicies = e.getChildIndices();
		Object[] children = e.getChildren();
		int count = (childIndicies != null ? childIndicies.length : 0);
		for (int i = 0; i < count; i++) {
			XMLNode node = (XMLNode) children[i];
			int index = childIndicies[i];
			switch (node.getDomNode().getNodeType()) {
			case org.w3c.dom.Node.ATTRIBUTE_NODE:
				removeAttributeElement(index, node);
				break;
			}
		}
	}

	public void treeStructureChanged(TreeModelEvent e) {

	}

	public String getName() {
		return "empty-tag";
	}

	public int getType() {
		return EMPTY_TAG;
	}

}
