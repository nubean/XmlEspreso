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

package com.nubean.michbase.factory;

/**
 * <p>Title: Michigan XML Editor</p>
 * <p>Description: This edits an XML document based on an XML schema.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Nubean LLC</p>
 * @author Ajay Vohra
 * @version 1.0
 */
import com.nubean.css.CSSDocumentDescriptor;
import com.nubean.java.JavaDocumentDescriptor;
import com.nubean.javacc.JavaCCDocumentDescriptor;
import com.nubean.michbase.DefaultDocumentDescriptor;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michdtd.DTDDocumentDescriptor;
import com.nubean.michxml.XMLDocumentDescriptor;

public class DocumentDescriptorFactory {
	public static DocumentDescriptor createNewDocumentDescriptor(String mimeType) {
		if (mimeType.equals("text/xml")) {
			DocumentDescriptor dd = new XMLDocumentDescriptor();
			dd.setMimeType(mimeType);
			return dd;
		}
		if (mimeType.equals("text/dtd")) {
			DocumentDescriptor dd = new DTDDocumentDescriptor();
			dd.setMimeType(mimeType);
			return dd;
		}
		if (mimeType.equals("text/css")) {
			DocumentDescriptor dd = new CSSDocumentDescriptor();
			dd.setMimeType(mimeType);
			return dd;
		}
		if (mimeType.equals("text/javacc")) {
			DocumentDescriptor dd = new JavaCCDocumentDescriptor();
			dd.setMimeType(mimeType);
			return dd;
		}
		if (mimeType.equals("text/java")) {
			DocumentDescriptor dd = new JavaDocumentDescriptor();
			dd.setMimeType(mimeType);
			return dd;
		} else {
			DocumentDescriptor dd = new DefaultDocumentDescriptor();
			dd.setMimeType(mimeType);
			return dd;
		}

	}
}