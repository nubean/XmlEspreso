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

package com.nubean.michide;

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

public class ProjectTreeModel implements TreeModel {
	private TreeNode root;

	public ProjectTreeModel(TreeNode root) {
		this.root = root;
	}

	public Object getRoot() {
		return root;
	}

	public Object getChild(Object parent, int index) {
		TreeNode node = (TreeNode) parent;
		return node.getChildAt(index);
	}

	public int getChildCount(Object parent) {
		TreeNode node = (TreeNode) parent;
		return node.getChildCount();
	}

	public boolean isLeaf(Object node) {
		if (node == null)
			return false;

		TreeNode tnode = (TreeNode) node;
		return tnode.isLeaf();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

	public int getIndexOfChild(Object parent, Object child) {
		TreeNode node = (TreeNode) parent;
		return node.getIndex((TreeNode) child);
	}

	/*
	 * Use these methods to add and remove event listeners. (Needed to satisfy
	 * TreeModel interface, but not used.)
	 */
	private Vector listenerList = new Vector();

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
		Enumeration listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeNodesChanged(e);
		}
	}

	public void fireTreeNodesInserted(TreeModelEvent e) {
		Enumeration listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeNodesInserted(e);
		}
	}

	public void fireTreeNodesRemoved(TreeModelEvent e) {
		Enumeration listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeNodesRemoved(e);
		}
	}

	public void fireTreeStructureChanged(TreeModelEvent e) {
		Enumeration listeners = listenerList.elements();
		while (listeners.hasMoreElements()) {
			TreeModelListener listener = (TreeModelListener) listeners
					.nextElement();
			listener.treeStructureChanged(e);
		}
	}
}