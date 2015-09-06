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

import java.util.ArrayList;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class DefaultTreeViewer extends TreeViewer {

	private TreePath selectionPath;

	public DefaultTreeViewer(Composite parent) {
		super(parent);
		init(parent.getDisplay());
	}

	public DefaultTreeViewer(Composite parent, int style) {
		super(parent, style);
		init(parent.getDisplay());
	}

	public DefaultTreeViewer(Tree tree, TreeModel model) {
		super(tree);
		init(tree.getDisplay());
	}

	protected abstract ITreeContentProvider getTreeContentProvider();

	protected abstract LabelProvider getTreeLabelProvider(Display display);

	private void init(Display display) {
		setContentProvider(getTreeContentProvider());
		setLabelProvider(getTreeLabelProvider(display));
	}

	@SuppressWarnings("unchecked")
	public TreePath getTreeSelectionPath() {
		TreePath path = null;
		Tree tree = getTree();

		if (tree != null && !tree.isDisposed()) {
			TreeItem[] items = tree.getSelection();

			if (items != null) {
				TreeItem item = items[0];

				@SuppressWarnings("rawtypes")
				ArrayList oa = new ArrayList();
				while (item != null && !item.isDisposed()) {
					oa.add(0, item.getData());
					item = item.getParentItem();
				}

				path = new TreePath(oa.toArray());
			}
		}

		return path;
	}

	public void setTreeSelectionPath(TreePath path) {

		int pathCount = (path != null ? path.getPathCount() : 0);

		if (pathCount > 0) {
			Tree tree = getTree();

			if (tree != null && !tree.isDisposed() && pathCount > 0) {
				TreeItem cur = null;

				int matchLength = 0;

				for (int i = 0; i < pathCount; i++) {
					boolean match = false;
					Object data = path.getPathComponent(i);

					if (cur == null) {
						cur = tree.getTopItem();
						if (cur.getData() != null && cur.getData().equals(data)) {
							matchLength++;
							match = true;
						}
					} else {
						for (TreeItem item : cur.getItems()) {
							if (item.getData() != null && item.getData().equals(data)) {
								cur = item;
								matchLength++;
								match = true;
							}
						}
					}

					if (!match) {
						break;
					}

				}

				if (matchLength == pathCount) {
					tree.setSelection(cur);
				}

			}
		}

	}

	public TreePath getSelectionPath() {
		return selectionPath;
	}

	public void setSelectionPath(TreePath path) {
		this.selectionPath = path;
	}

}
