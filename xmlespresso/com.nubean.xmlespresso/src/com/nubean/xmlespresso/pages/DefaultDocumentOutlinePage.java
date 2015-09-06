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

package com.nubean.xmlespresso.pages;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.ui.views.contentoutline.*;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.jface.viewers.*;

import com.nubean.xmlespresso.design.DefaultTreeViewer;

public abstract class DefaultDocumentOutlinePage implements IContentOutlinePage {

	protected TreeModel treeModel;

	protected DefaultTreeViewer treeViewer;

	protected SelectionListener selectionListener;

	public DefaultDocumentOutlinePage(TreeModel treeModel,
			SelectionListener listener) {
		super();
		this.treeModel = treeModel;
		this.selectionListener = listener;
	}

	public abstract void createControl(Composite parent);

	@Override
	public Control getControl() {
		if (treeViewer != null)
			return treeViewer.getControl();

		return null;
	}

	public TreeModel getModel() {
		return treeModel;
	}

	public void setInput(TreeModel input) {
		if (treeViewer != null) {
			treeViewer.setInput(input);
		}
	}

	public Object getRoot() {
		if (treeModel != null) {
			return treeModel.getRoot();
		}

		return null;
	}

	public void setFocus() {
		if (treeViewer != null) {
			treeViewer.getControl().setFocus();
		}
	}

	public void expandToLevel(Object obj, int level) {
		if (treeViewer != null) {
			treeViewer.expandToLevel(obj, level);
		}
	}

	public TreePath getTreeSelectionPath() {
		return (treeViewer != null ? treeViewer.getTreeSelectionPath() : null);
	}

	public void setTreeSelectionPath(TreePath path) {
		if (treeViewer != null) {
			treeViewer.setTreeSelectionPath(path);
		}
	}

	@Override
	public void setActionBars(IActionBars actionBars) {

	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
	}

	@Override
	public void setSelection(ISelection selection) {
	}

	@Override
	public void dispose() {

	}

	public void removeSelectionListener(SelectionListener listener) {
		if (treeViewer != null && treeViewer.getTree() != null)
			treeViewer.getTree().removeSelectionListener(listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		if (treeViewer != null && treeViewer.getTree() != null)
			treeViewer.getTree().addSelectionListener(listener);
	}
}
