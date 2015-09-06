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

package com.nubean.xmlespresso.design;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLTreeModel;

public class XMLTreeContentProvider implements ITreeContentProvider {

	private Viewer viewer;

	private TreeModelAdapter adapter;

	public XMLTreeContentProvider() {
		adapter = new TreeModelAdapter();
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;

		if (parentElement != null && parentElement instanceof XMLNode) {
			XMLNode parent = (XMLNode) parentElement;

			int childCount = parent.getOutlineChildCount();
			children = new Object[childCount];

			for (int i = 0; i < childCount; i++) {
				children[i] = parent.getOutlineChild(i);
			}
		}
		return children;
	}

	@Override
	public Object getParent(Object element) {

		Object parent = null;
		if (element != null && element instanceof XMLNode) {
			XMLNode xnode = (XMLNode) element;

			parent = xnode.getParent();
		}

		return parent;

	}

	@Override
	public boolean hasChildren(Object element) {
		boolean retval = false;

		if (element != null && element instanceof XMLNode) {
			XMLNode xnode = (XMLNode) element;

			retval = xnode.getOutlineChildCount() > 0;
		}

		return retval;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			if (inputElement instanceof XMLTreeModel) {
				XMLTreeModel xtm = (XMLTreeModel) inputElement;
				return new Object[]{xtm.getRoot()};
			}
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		this.viewer = viewer;

		if (oldInput != null && oldInput instanceof XMLTreeModel) {
			XMLTreeModel oldModel = (XMLTreeModel) oldInput;
			oldModel.removeTreeModelListener(adapter);
		}

		if (newInput != null && newInput instanceof XMLTreeModel) {
			XMLTreeModel newModel = (XMLTreeModel) newInput;
			newModel.addTreeModelListener(adapter);
		}

	}

	private class TreeModelAdapter implements TreeModelListener {
		private void referesh() {
			if (viewer != null) {
				viewer.refresh();
			}
		}

		public void treeStructureChanged(TreeModelEvent e) {
			referesh();
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			referesh();
		}

		public void treeNodesChanged(TreeModelEvent e) {
			referesh();
		}

		public void treeNodesInserted(TreeModelEvent e) {
			referesh();
		}
	}

}
