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

package com.nubean.michxml;

import javax.swing.tree.*;
import java.util.*;
import org.w3c.dom.*;
import javax.swing.ImageIcon;

import com.nubean.michbase.CommonUtils;
import com.nubean.michutil.*;

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

public class XMLNode implements TreeNode, Iconable {
	private XMLNode parent;

	private Node domNode;

	private Vector<XMLNode> children;

	private Character cc;

	private boolean parsed;

	private SchemaNode schemaNode;

	public String getNamespace() {
		return (domNode != null ? domNode.getNamespaceURI() : null);
	}

	public SchemaNode getSchemaNode() {
		return schemaNode;
	}

	public void setSchemaNode(SchemaNode schemaNode) {
		this.schemaNode = schemaNode;
	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
		for (int i = 0; i < this.childCount(); i++) {
			child(i).setParsed(parsed);
		}

	}

	public XMLNode findXMLNodeWithNode(Node node) {
		XMLNode ret = null;

		if (node == domNode) {
			ret = this;
		} else {
			for (int i = 0; i < this.childCount(); i++) {
				XMLNode childXmlNode = child(i);
				ret = childXmlNode.findXMLNodeWithNode(node);
				if (ret != null) {
					break;
				}
			}
		}

		return ret;

	}

	public ImageIcon getIcon() {
		ImageIcon icon = null;
		switch (domNode.getNodeType()) {
		case Node.DOCUMENT_NODE:
			icon = IconLoader.documentIcon;
			break;
		case Node.ELEMENT_NODE:
			icon = IconLoader.elementIcon;
			break;
		case Node.ATTRIBUTE_NODE:
			icon = IconLoader.attrIcon;
			break;
		case Node.COMMENT_NODE:
			icon = IconLoader.commentIcon;
			break;
		case Node.TEXT_NODE:
			icon = IconLoader.contentIcon;
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			icon = IconLoader.procIcon;
		default:
			icon = IconLoader.elementIcon;
			break;
		}
		return icon;
	}

	public Node getDomNode() {
		return domNode;
	}

	public void setCharCode(Character cc) {
		this.cc = cc;
	}

	public Character getCharCode() {
		return cc;
	}

	public Node getTextNode() {
		return CommonUtils.getNodeByType(domNode, Node.TEXT_NODE);
	}

	public Node getCDATANode() {
		return CommonUtils.getNodeByType(domNode, Node.CDATA_SECTION_NODE);
	}

	public boolean isDescendant(XMLNode ancestor) {
		boolean retval = false;
		XMLNode p = parent;
		while (p != null) {
			if (p == ancestor) {
				retval = true;
				break;
			}
			p = p.parent;
		}
		return retval;
	}

	private int getAttrPos() {
		NamedNodeMap nmap = domNode.getAttributes();
		int count = (nmap != null ? nmap.getLength() : 0);
		if (count > 0)
			count--;
		return count;
	}

	private void append(XMLNode node) {
		children.add(node);
		node.setParent(this);
	}

	private void setAttrNode(XMLNode node) {
		Attr attr = (Attr) node.getDomNode();
		Element ele = (Element) domNode;
		ele.setAttributeNode(attr);
	}

	public void appendChild(XMLNode node) {
		Node dn = node.getDomNode();
		switch (node.getDomNode().getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			setAttrNode(node);
			this.insertElementAt(node, this.getAttrPos());
			break;
		default:
			domNode.appendChild(dn);
			append(node);
			break;
		}
	}

	public Attr getAttributeNode(String name) {
		if (domNode.getNodeType() == Node.ELEMENT_NODE) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) domNode;
			return element.getAttributeNode(name);
		} else
			return null;
	}

	public int childCount(short type) {
		int typeCount = 0;
		int count = childCount();
		for (int i = 0; i < count; i++) {
			XMLNode child = this.child(i);
			if (child.getDomNode().getNodeType() == type)
				typeCount++;
		}
		return typeCount;
	}

	public XMLNode findTextNode() {
		if (domNode.getNodeType() == Node.ELEMENT_NODE) {
			int count = childCount();
			for (int i = 0; i < count; i++) {
				XMLNode child = child(i);
				if (child.domNode.getNodeType() == Node.TEXT_NODE) {
					return child;
				}
			}
		}
		return null;
	}

	public XMLNode findCDATANode() {
		if (domNode.getNodeType() == Node.ELEMENT_NODE) {
			int count = childCount();
			for (int i = 0; i < count; i++) {
				XMLNode child = child(i);
				if (child.domNode.getNodeType() == Node.CDATA_SECTION_NODE) {
					return child;
				}
			}
		}
		return null;
	}

	public XMLNode findAttribute(String name) {
		if (domNode.getNodeType() == Node.ELEMENT_NODE) {
			int count = childCount();
			for (int i = 0; i < count; i++) {
				XMLNode child = child(i);
				if (child.domNode.getNodeType() == Node.ATTRIBUTE_NODE) {
					Attr attr = (Attr) child.getDomNode();
					if (attr.getName().equals(name)) {
						return child;
					}
				}
			}
		}
		return null;
	}

	public boolean equals(Object obj) {
		boolean retval = false;
		try {
			XMLNode node = (XMLNode) obj;
			retval = node.getDomNode().equals(getDomNode());
		} catch (Throwable e) {
			super.equals(obj);
		}

		return retval;
	}

	public void removeAttribute(String name) {
		XMLNode node = findAttribute(name);
		if (node != null)
			removeChild(node);
	}

	public XMLNode setAttribute(Attr attr) {
		if (domNode.getNodeType() == Node.ELEMENT_NODE) {
			XMLNode anode = new XMLNode(attr, false);
			org.w3c.dom.Element element = (org.w3c.dom.Element) domNode;
			element.setAttributeNode(attr);
			insertElementAt(anode, this.getAttrPos());
			return anode;
		} else
			return null;
	}

	public void insertBefore(XMLNode node, int pos) {
		XMLNode ref = child(pos);
		while (ref != null
				&& (ref.getDomNode().getNodeType() == Node.ATTRIBUTE_NODE)
				&& pos < childCount())
			ref = child(++pos);

		if (ref != null)
			insertBefore(node, ref);
		else
			appendChild(node);
	}

	public void insertBefore(XMLNode node, XMLNode ref) {
		if (ref == null) {
			appendChild(node);
		} else {
			if (node.getDomNode().getNodeType() == Node.ATTRIBUTE_NODE) {
				setAttrNode(node);
			} else {
				domNode.insertBefore(node.getDomNode(), ref.getDomNode());
			}
			int index = index(ref);
			insertElementAt(node, index);
		}
	}

	public String getPattern() {
		StringBuffer sb = new StringBuffer();
		int count = childCount();
		for (int i = 0; i < count; i++) {
			XMLNode child = child(i);
			if (child == null
					|| child.getDomNode().getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
				continue;

			Character cc = child.getCharCode();
			if (cc != null)
				sb.append(cc.charValue());
		}
		return sb.toString();
	}

	public String getPrintPattern() {
		StringBuffer sb = new StringBuffer();
		int count = childCount();
		for (int i = 0; i < count; i++) {
			XMLNode child = child(i);
			if (child == null
					|| child.getDomNode().getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
				continue;

			Character cc = child.getCharCode();
			if (cc != null)
				sb.append(child.toString());
		}
		return sb.toString();
	}

	private void insertElementAt(XMLNode node, int pos) {
		node.setParent(this);
		if (pos < children.size())
			children.insertElementAt(node, pos);
		else
			children.add(node);
	}

	public int removeChild(XMLNode child) {
		child.setParent(null);
		org.w3c.dom.Node childDomNode = child.getDomNode();
		if (childDomNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) domNode;
			element.removeAttributeNode(((Attr) childDomNode));
		} else {
			Node parent = child.getDomNode().getParentNode();
			if (parent != null)
				parent.removeChild(childDomNode);
		}
		int pos = index(child);
		children.remove(child);
		child.setParent(null);
		return pos;
	}

	public void setParent(XMLNode parent) {
		this.parent = parent;
	}

	public TreeNode getParent() {
		return parent;
	}

	// Construct an Adapter node from a DOM node
	public XMLNode(org.w3c.dom.Node node, boolean parsed) {
		this.parsed = parsed;
		domNode = node;
		children = new Vector<XMLNode>(2, 2);
		if (node.getNodeType() == Node.ATTRIBUTE_NODE)
			return;

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			if (element.getAttributes() != null) {
				NamedNodeMap attrs = element.getAttributes();
				for (int k = 0; k < attrs.getLength(); k++) {
					append(new XMLNode(attrs.item(k), parsed));
				}
			}
		}
		NodeList nodeList = domNode.getChildNodes();
		int nnodes = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < nnodes; i++) {
			org.w3c.dom.Node child = domNode.getChildNodes().item(i);
			if (child == null)
				continue;

			switch (child.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				break;
			default:
				XMLNode enode = new XMLNode(child, parsed);
				append(enode);
				break;
			}
		}
	}

	// Return a string that identifies this node in the tree
	// *** Refer to table at top of org.w3c.dom.Node ***
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int type = domNode.getNodeType();
		switch (type) {
		case Node.ELEMENT_NODE:
			sb.append(domNode.getNodeName());
			break;
		case Node.TEXT_NODE:
			sb.append(domNode.getNodeValue());
			break;
		case Node.ATTRIBUTE_NODE:
			Attr attr = (Attr) domNode;
			sb.append(attr.getName()).append('=').append(attr.getValue());
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			org.w3c.dom.ProcessingInstruction pi = (org.w3c.dom.ProcessingInstruction) domNode;
			sb.append("'").append(pi.getTarget());
			sb.append("' '").append(pi.getData()).append("'");
			break;
		case Node.DOCUMENT_NODE:
			org.w3c.dom.Document document = (org.w3c.dom.Document) domNode;
			sb.append(document.getNodeName());
			break;
		case Node.DOCUMENT_TYPE_NODE:
			org.w3c.dom.DocumentType documentType = (org.w3c.dom.DocumentType) domNode;
			sb.append("DOCTYPE ").append(documentType.getName());
		default:
			sb.append(domNode);
			break;
		}
		return sb.toString();
	}

	/*
	 * Return children, index, and count values
	 */
	public int index(XMLNode child) {
		for (int i = 0; i < children.size(); i++) {
			if (children.elementAt(i) == child)
				return i;
		}
		return -1; // Should never get here.
	}

	public XMLNode child(int searchIndex) {
		XMLNode retval = null;

		try {
			retval = (XMLNode) children.elementAt(searchIndex);
		} catch (Exception e) {
		}

		return retval;
	}

	public int childCount() {
		return children.size();
	}

	public int elementCount(String tag) {
		int count = 0;
		for (int i = 0; i < children.size(); i++) {
			XMLNode child = (XMLNode) children.elementAt(i);
			if (child.getDomNode().getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (child.toString().equals(tag))
				count++;
		}
		return count;
	}

	public TreeNode getChildAt(int childIndex) {
		return child(childIndex);
	}

	public int getChildCount() {
		return childCount();
	}

	public int getIndex(TreeNode node) {
		return index((XMLNode) node);
	}

	public boolean getAllowsChildren() {
		return domNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE;
	}

	public boolean isLeaf() {
		return childCount() == 0;
	}

	public Enumeration<XMLNode> children() {
		return children.elements();
	}
	
	public int getOutlineChildCount() {
		int count = 0;
		for (int i = 0; i < children.size(); i++) {
			XMLNode child = (XMLNode) children.elementAt(i);
			short nodeType =child.getDomNode().getNodeType();
			if (nodeType == Node.TEXT_NODE && child.toString().trim().length() == 0)
				continue;
			count++;
		}
		return count;
	}

	public XMLNode getOutlineChild(int index) {
		XMLNode child = null;
		int pos = 0;
		for (int i = 0; i < children.size(); i++) {
			child = (XMLNode) children.elementAt(i);
			short nodeType =child.getDomNode().getNodeType();
			if (nodeType == Node.TEXT_NODE && child.toString().trim().length() == 0)
				continue;
			if(pos == index)
				break;
			pos++;
		}
		
		return child;
	}
	
	public int getOutlineChildIndex(XMLNode select) {
		int index = -1;
		int pos = 0;
		for (int i = 0; i < children.size(); i++) {
			XMLNode child = (XMLNode) children.elementAt(i);
			short nodeType =child.getDomNode().getNodeType();
			if (nodeType == Node.TEXT_NODE && child.toString().trim().length() == 0)
				continue;
			if(child == select) {
				index = pos;
				break;
			}
			
			pos++;
		}
		
		return index;
	}
	
	public boolean isOutlineLeaf() {
		return getOutlineChildCount() == 0;
	}
}