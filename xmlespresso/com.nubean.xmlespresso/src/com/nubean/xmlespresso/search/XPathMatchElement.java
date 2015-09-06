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

public class XPathMatchElement implements Comparable<XPathMatchElement> {
	private IFile file;

	public XPathMatchElement(IFile file) {
		this.file = file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	@Override
	public int compareTo(XPathMatchElement o) {
		return this.file.getFullPath().toPortableString()
				.compareTo(o.getFile().getFullPath().toPortableString());
	}
	
	@Override
	public boolean equals(Object o) {
		boolean retval = false;
		if(o instanceof XPathMatchElement) {
			retval = file.equals(((XPathMatchElement) o).getFile());
		}
		
		return retval;
	}
	
	@Override
	public int hashCode() {
		return file.hashCode();
	}

}
