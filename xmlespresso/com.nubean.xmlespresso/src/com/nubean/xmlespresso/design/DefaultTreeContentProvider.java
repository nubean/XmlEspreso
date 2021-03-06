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
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nubean.michbase.DefaultElementNode;

public class DefaultTreeContentProvider implements ITreeContentProvider {

	private Viewer viewer;

	private TreeModelAdapter adapter;

	public DefaultTreeContentProvider() {
		adapter = new TreeModelAdapter();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;

		if (parentElement != null
				&& parentElement instanceof DefaultElementNode) {
			DefaultElementNode parent = (DefaultElementNode) parentElement;

			int childCount = parent.getChildCount();
			children = new Object[childCount];

			for (int i = 0; i < childCount; i++) {
				children[i] = parent.getChildAt(i);
			}
		}
		return children;
	}

	@Override
	public Object getParent(Object element) {

		Object parent = null;
		if (element != null && element instanceof DefaultElementNode) {
			DefaultElementNode denode = (DefaultElementNode) element;

			parent = denode.getParent();
		}

		return parent;

	}

	@Override
	public boolean hasChildren(Object element) {
		boolean retval = false;

		if (element != null && element instanceof DefaultElementNode) {
			DefaultElementNode xnode = (DefaultElementNode) element;

			retval = xnode.getChildCount() > 0;
		}

		return retval;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			if (inputElement instanceof DefaultTreeModel) {
				DefaultTreeModel dtm = (DefaultTreeModel) inputElement;
				return new Object[] { dtm.getRoot() };
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

		if (oldInput != null && oldInput instanceof DefaultTreeModel) {
			DefaultTreeModel oldModel = (DefaultTreeModel) oldInput;
			oldModel.removeTreeModelListener(adapter);
		}

		if (newInput != null && newInput instanceof DefaultTreeModel) {
			DefaultTreeModel newModel = (DefaultTreeModel) newInput;
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
