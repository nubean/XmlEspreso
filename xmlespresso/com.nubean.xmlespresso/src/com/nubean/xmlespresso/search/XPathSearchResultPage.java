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

package com.nubean.xmlespresso.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.editors.XMLEspressoEditor;
import com.nubean.xmlespresso.editors.XMLEspressoFormEditor;

public class XPathSearchResultPage extends AbstractTextSearchViewPage {

	private TreeViewer viewer;

	public XPathSearchResultPage() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	@Override
	protected void clear() {
		if (viewer != null) {
			viewer.refresh(true);
		}
	}

	@Override
	protected void configureTableViewer(TableViewer arg0) {

	}

	@Override
	protected void configureTreeViewer(TreeViewer treeViewer) {
		treeViewer
				.setContentProvider(new XPathSearchResultTreeContentProvider());
		treeViewer.setLabelProvider(new XPathSearchResultTreeLabelProvider());
		this.viewer = treeViewer;
		treeViewer.getTree().addSelectionListener(
				new XPathResultsTreeListener());
	}

	@Override
	protected void elementsChanged(Object[] elements) {
		if (viewer != null) {
			viewer.refresh(true);
			viewer.expandAll();
		}
	}

	@Override
	protected void showMatch(Match match, int currentOffset, int currentLength,
			boolean activate) throws PartInitException {

		IFile file = ((XPathMatchElement) match.getElement()).getFile();
		IEditorPart editorPart = XMLEspressoActivator.openEditor(file);
		if (editorPart instanceof XMLEspressoFormEditor) {
			XMLEspressoFormEditor formEditor = (XMLEspressoFormEditor) editorPart;
			XMLEspressoEditor editor = formEditor.getXMLEditor();
			ITextViewer textViewer = editor.getTextViewer();
			textViewer.setSelectedRange(currentOffset, currentLength);
			textViewer.getTextWidget().showSelection();
		} else {
			super.showMatch(match, currentOffset, currentLength, activate);
		}
	}

	@Override
	protected boolean canRemoveMatchesWith(ISelection selection) {
		return false;
	}

	private class XPathResultsTreeListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			Object data = e.item.getData();
			if (data instanceof XPathMatch) {
				XPathMatch match = ((XPathMatch) data);
				int currentOffset = match.getOffset();
				int currentLength = match.getLength();
				IFile file = ((XPathMatchElement) match.getElement()).getFile();
				IEditorPart editorPart = XMLEspressoActivator.openEditor(file);
				if (editorPart instanceof XMLEspressoFormEditor) {
					XMLEspressoFormEditor formEditor = (XMLEspressoFormEditor) editorPart;
					XMLEspressoEditor editor = formEditor.getXMLEditor();
					ITextViewer textViewer = editor.getTextViewer();
					textViewer.setSelectedRange(currentOffset, currentLength);
					textViewer.getTextWidget().showSelection();
				}
			}
		}
	}
}
