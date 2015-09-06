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

import javax.swing.tree.TreePath;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import com.nubean.michxml.SchemaTreeModel;

public class SchemaTreeViewer extends TreeViewer {

	private TreePath selectionPath;
	
	public SchemaTreeViewer(Composite parent) {
		super(parent);
		init(parent.getDisplay());
	}

	public SchemaTreeViewer(Composite parent, int style) {
		super(parent, style);
		init(parent.getDisplay());
	}

	public SchemaTreeViewer(Tree tree, SchemaTreeModel model) {
		super(tree);
		init(tree.getDisplay());
	}

	private void init(Display display) {
		setContentProvider(new SchemaTreeContentProvider());
		setLabelProvider(new SchemaTreeLabelProvider(display));
	}

	public TreePath getSelectionPath() {
		return selectionPath;
	}
	
	public void setSelectionPath(TreePath path) {
		this.selectionPath = path;
	}


}
