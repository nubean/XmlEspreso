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
import org.eclipse.jface.viewers.LabelProvider;

public class XPathSearchResultTreeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		String text = element.toString();

		if (element instanceof XPathMatch) {
			XPathMatch match = (XPathMatch) element;
			text = "Line " + match.getLine() +": "+ match.getText();
		} else if (element instanceof XPathMatchElement) {
			IFile  file =  ((XPathMatchElement) element).getFile();
			text = file.getFullPath().toPortableString();
		}

		return text;
	}
}
