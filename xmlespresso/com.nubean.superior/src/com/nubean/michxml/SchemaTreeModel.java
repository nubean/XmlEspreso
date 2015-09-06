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
import javax.swing.event.*;

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

public class SchemaTreeModel implements TreeModel {
	private SchemaNode root;

	public SchemaTreeModel(SchemaNode root) {
		this.root = root;
	}

	public String getPattern() {
		return root.getPattern();
	}

	public String getPrintPattern() {
		return root.getPrintPattern();
	}

	public Vector<SchemaNode> getTerminals() {
		return root.getTerminals();

	}

	// Basic TreeModel operations
	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object aNode) {
		// Determines whether the icon shows up to the left.
		// Return true for any node with no children
		SchemaNode node = (SchemaNode) aNode;
		return node.isLeaf();
	}

	public int getChildCount(Object parent) {
		SchemaNode node = (SchemaNode) parent;
		return node.childCount();
	}

	public Object getChild(Object parent, int index) {
		SchemaNode node = (SchemaNode) parent;
		return node.child(index);
	}

	public int getIndexOfChild(Object parent, Object child) {
		SchemaNode node = (SchemaNode) parent;
		return node.index((SchemaNode) child);
	}

	public TreePath getPathToFirstLeaf() {
		Vector<SchemaNode> path = new Vector<SchemaNode>(3);
		SchemaNode n = root;
		while (n != null && !isLeaf(n)) {
			path.add(n);
			n = (SchemaNode) getChild(n, 0);
		}
		if (n != null)
			path.add(n);

		return new TreePath(path.toArray());
	}

	public TreePath getPathToRoot(SchemaNode node) {
		Vector<SchemaNode> path = new Vector<SchemaNode>(10);
		// path.insertElementAt(node, 0);
		SchemaNode p = node;
		while (p != null && p != root) {
			path.insertElementAt(p, 0);
			p = (SchemaNode) p.getParent();
		}
		path.insertElementAt(root, 0);
		return new TreePath(path.toArray());
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
			TreeModelListener listener = listeners.nextElement();
			listener.treeNodesChanged(e);
		}
	}

	public void fireTreeNodesInserted(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = listeners.nextElement();
			listener.treeNodesInserted(e);
		}
	}

	public void fireTreeNodesRemoved(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = listeners.nextElement();
			listener.treeNodesRemoved(e);
		}
	}

	public void fireTreeStructureChanged(TreeModelEvent e) {
		Enumeration<TreeModelListener> listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = listeners.nextElement();
			listener.treeStructureChanged(e);
		}
	}

}