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

import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class XPathSearchResultTreeContentProvider implements
		ITreeContentProvider {

	private XPathSearchResult searchResult;

	public XPathSearchResultTreeContentProvider() {
		this.searchResult = null;
	}

	@Override
	public void dispose() {
		this.searchResult = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.searchResult = (XPathSearchResult) newInput;
	}

	@Override
	public Object[] getChildren(Object object) {
		Object[] children = null;

		if (searchResult != null) {
			Object[] elements = searchResult.getElements();
			if (elements != null) {
				for (Object element : elements) {
					if (element.equals(object)) {
						children = searchResult.getMatches(element);
					}
				}
			}
		}

		return children;
	}

	@Override
	public Object[] getElements(Object object) {

		if (object != null && object instanceof XPathSearchResult) {
			this.searchResult = (XPathSearchResult) object;
			Object[] elements = searchResult.getElements();
			Arrays.sort(elements);
			return elements;
		}
		return null;
	}

	@Override
	public Object getParent(Object object) {
		Object parent = null;

		if (searchResult != null) {
			Object[] elements = searchResult.getElements();
			outer: for (Object element : elements) {
				Object[] matches = searchResult.getMatches(element);
				for (Object m : matches) {
					if (m.equals(object)) {
						parent = element;
						break outer;
					}
				}
			}
		}
		return parent;
	}

	@Override
	public boolean hasChildren(Object object) {
		Object[] children = getChildren(object);
		return children != null && children.length > 0;
	}

}
