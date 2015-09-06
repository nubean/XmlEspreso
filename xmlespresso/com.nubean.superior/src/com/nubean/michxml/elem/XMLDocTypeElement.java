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

public class XMLDocTypeElement extends XMLBranchElement {
	public XMLDocTypeElement(XMLNode xmlNode, XMLDocument xmlDocument,
			XMLAbstractElement parent, int startOffset, int indent) {
		super(xmlNode, xmlDocument, parent, startOffset, indent);
	}

	protected void init(int offset) throws BadLocationException {
		org.w3c.dom.Node node = getXMLNode().getDomNode();
		Position insert = getDocument().createPosition(offset);
		Vector<XMLAbstractElement> elements = new Vector<XMLAbstractElement>();
		StringBuffer sb = new StringBuffer();
		switch (node.getNodeType()) {
		case org.w3c.dom.Node.DOCUMENT_NODE:
			org.w3c.dom.Document doc = (org.w3c.dom.Document) node;
			org.w3c.dom.DocumentType docType = doc.getDoctype();

			if (docType != null) {
				sb.append(' ').append(docType.getName()).append(' ');
				if (docType.getPublicId() != null)
					sb.append(" PUBLIC '").append(docType.getPublicId())
							.append("' ");

				if (docType.getPublicId() == null
						&& docType.getSystemId() != null)
					sb.append(" SYSTEM '");
				else if (docType.getPublicId() != null
						&& docType.getSystemId() != null)
					sb.append("'");

				if (docType.getSystemId() != null)
					sb.append(docType.getSystemId()).append("' ");

				if (docType.getInternalSubset() != null)
					sb.append(" [").append(docType.getInternalSubset()).append(
							" ]");
			} else {
				sb.append(' ').append(doc.getDocumentElement().getNodeName());
				if (getEditor() != null)
					sb.append(getEditor().getDocType());
			}
			break;
		}

		elements
				.add(new XMLLabelElement(this, insert.getOffset(), "<!DOCTYPE "));
		elements.add(new XMLDocTypeDataElement(this, insert.getOffset(), sb
				.toString()));
		elements.add(new XMLLabelElement(this, insert.getOffset(), "  >"));
		elements.add(new XMLWhiteSpaceElement(this, insert.getOffset(), "\n"));
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
		return "doctype";
	}

	public int getType() {
		return DOCTYPE;
	}
}