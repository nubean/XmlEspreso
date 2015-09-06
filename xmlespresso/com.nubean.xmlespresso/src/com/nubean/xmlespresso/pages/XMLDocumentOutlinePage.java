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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import com.nubean.michxml.XMLTreeModel;
import com.nubean.xmlespresso.design.XMLTreeViewer;

public class XMLDocumentOutlinePage extends DefaultDocumentOutlinePage {

	public XMLDocumentOutlinePage(XMLTreeModel treeModel,
			SelectionListener listener) {
		super(treeModel, listener);
	}

	@Override
	public void createControl(Composite parent) {
		treeViewer = new XMLTreeViewer(parent, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		treeViewer.setInput(treeModel);
		treeViewer.getTree().addSelectionListener(selectionListener);
	}

}
