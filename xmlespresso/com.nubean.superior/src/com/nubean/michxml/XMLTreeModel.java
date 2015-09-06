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

import org.w3c.dom.*;


import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;

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
// This adapter converts the current Document (a DOM) into
// a JTree model.
public class XMLTreeModel implements javax.swing.tree.TreeModel {
	private XMLNode root;

	public XMLTreeModel(Node root, boolean parsed) {
		this.root = new XMLNode(root, parsed);
	}

	public XMLTreeModel(XMLNode root) {
		this.root = root;
	}

	public XMLNode getRootElement() {
		XMLNode retval = null;
		int count = (root != null ? root.childCount() : 0);
		for (int i = 0; i < count; i++) {
			XMLNode child = root.child(i);
			if (child.getDomNode().getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				retval = child;
				break;
			}
		}
		return retval;
	}

	public String getPattern() {
		return root.getPattern();
	}

	public String getPrintPattern() {
		return root.getPrintPattern();
	}
	
	public Vector<XMLNode> getElements() {
		Vector<XMLNode> terminals = new Vector<XMLNode>(8, 4);
		int count = root.childCount();
		for (int i = 0; i < count; i++) {
			XMLNode child = root.child(i);
			if (child.getDomNode().getNodeType() == Node.ELEMENT_NODE)
				terminals.add(child);
		}
		return terminals;
	}

	public int getCanonicalPosition(int pos) {
		int count = root.childCount();
		int nadjust = 0;
		for (int i = 0, ne = 0; (i < count) && (ne < pos); i++) {
			XMLNode child = root.child(i);
			if (child.getDomNode().getNodeType() == Node.ELEMENT_NODE) {
				ne++;
			} else {
				nadjust++;
			}
		}
		// System.out.println("mapping:" + pos + " --> " + (pos + nadjust));
		return pos + nadjust;
	}

	public TreePath getPathToNode(XMLNode xnode) {
		if (root == null)
			return null;
		Stack<XMLNode> nodeStack = new Stack<XMLNode>();
		HashMap<XMLNode, Stack<Integer>> stackMap = new HashMap<XMLNode, Stack<Integer>>(17, 0.85f);

		nodeStack.push(root);
		while (!nodeStack.empty()) {
			XMLNode node = (XMLNode) nodeStack.peek();
			if (node.getDomNode() == xnode.getDomNode()) {
				return getPathToRoot(node);
			}
			int count = getChildCount(node);
			if (count == 0) {
				nodeStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(node);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(node, indexStack);
					nodeStack.push((XMLNode)getChild(node, 0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						nodeStack.push((XMLNode)getChild(node, top));
						indexStack.push(new Integer(top + 1));
						continue;
					} else {
						Object key = nodeStack.pop();
						stackMap.remove(key);
						continue;
					}
				}
			}
		}
		return null;
	}

	public void setParsed(boolean parsed) {
		root.setParsed(parsed);
	}

	public TreePath getPathToFirstLeaf() {
		Vector<XMLNode> path = new Vector<XMLNode>(3);
		XMLNode n = root;
		while (n != null && !isLeaf(n)) {
			path.add(n);
			n = (XMLNode) getChild(n, 0);
		}
		if (n != null)
			path.add(n);

		return new TreePath(path.toArray());
	}

	public TreePath getPathToRoot(XMLNode node) {
		Vector<XMLNode> path = new Vector<XMLNode>(10);

		XMLNode p = node;
		while (p != null && p != root) {
			path.insertElementAt(p, 0);
			p = (XMLNode) p.getParent();
		}
		path.insertElementAt(root, 0);
		return new TreePath(path.toArray());
	}

	// Basic TreeModel operations
	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object aNode) {
		// Determines whether the icon shows up to the left.
		// Return true for any node with no children
		XMLNode node = (XMLNode) aNode;
		return node.isOutlineLeaf();
	}

	public int getChildCount(Object parent) {
		XMLNode node = (XMLNode) parent;
		return node.getOutlineChildCount();
	}

	public Object getChild(Object parent, int index) {
		XMLNode node = (XMLNode) parent;
		return node.getOutlineChild(index);
	}

	public int getIndexOfChild(Object parent, Object child) {
		XMLNode node = (XMLNode) parent;
		return node.getOutlineChildIndex((XMLNode) child);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	/*
	 * Use these methods to add and remove event listeners. (Needed to satisfy
	 * TreeModel interface, but not used.)
	 */
	private Vector<TreeModelListener> listenerList = new Vector<TreeModelListener>();

	public void addTreeModelListener(TreeModelListener listener) {
		if (listener != null && !listenerList.contains(listener)) {
			listenerList.addElement(listener);
		}
	}

	public void removeTreeModelListener(TreeModelListener listener) {
		if (listener != null) {
			listenerList.removeElement(listener);
		}
	}

	public void fireTreeNodesChanged(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeNodesChanged(e);
		}
	}

	public void fireTreeNodesInserted(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeNodesInserted(e);
		}
	}

	public void fireTreeNodesRemoved(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeNodesRemoved(e);
		}
	}

	public void fireTreeStructureChanged(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeStructureChanged(e);
		}
	}
}
