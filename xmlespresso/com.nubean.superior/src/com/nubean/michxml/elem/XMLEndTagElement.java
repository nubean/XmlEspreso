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
import java.util.*;
import javax.swing.event.*;

import com.nubean.michbase.CommonUtils;
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

public class XMLEndTagElement extends XMLBranchElement {

	public XMLEndTagElement(XMLNode xmlNode, XMLDocument xmlDocument,
			XMLAbstractElement parent, int startOffset, int indent) {
		super(xmlNode, xmlDocument, parent, startOffset, indent);
	}

	protected void init(int offset) throws BadLocationException {
		Position insert = getDocument().createPosition(offset);
		int tagStyle = (getEditor() != null ? getEditor().getTagStyle()
				: XMLAbstractEditor.TAG_NOT_INDENT);
		boolean parsed = getXMLNode().isParsed();

		Vector<XMLAbstractElement> elements = new Vector<XMLAbstractElement>();

		StringBuffer sb = new StringBuffer();
		sb.append("</").append(getXMLNode().getDomNode().getNodeName()).append(
				'>');

		elements.add(new XMLLabelElement(this, insert.getOffset(), sb
				.toString()));

		if (!parsed && tagStyle == XMLAbstractEditor.TAG_INDENT)
			elements.add(new XMLWhiteSpaceElement(this, insert.getOffset(),
					"\n"
							+ CommonUtils.expandTabs(getIndent()
									- XMLAbstractElement.INDENT)));
		XMLAbstractElement[] elems = new XMLAbstractElement[elements.size()];
		elements.toArray(elems);
		replace(0, 0, elems);
		this.startPosition = getDocument().createPosition(offset);
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
		return "end-tag";
	}

	public int getType() {
		return END_TAG;
	}
}