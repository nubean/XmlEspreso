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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

public class XPathSearchResult extends AbstractTextSearchResult {

	private String label;
	private XPathSearchQuery searchQuery;
	private XMLEspressoEditorMatchAdapter editorMatchAdapter;
	private XPathFileMatchAdapter fileMatchAdapter;

	public XPathSearchResult(String label, String query, XPathSearchQuery searchQuery) {
		this.label = label;
		this.editorMatchAdapter = new XMLEspressoEditorMatchAdapter();
		this.fileMatchAdapter = new XPathFileMatchAdapter();
		this.searchQuery = searchQuery;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ISearchQuery getQuery() {
		return searchQuery;

	}

	@Override
	public String getTooltip() {
		return label;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return editorMatchAdapter;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return fileMatchAdapter;
	}

}
