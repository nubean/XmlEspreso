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

package com.nubean.michxml;

import javax.swing.text.*;
import java.io.*;

/**
 * <p>
 * Title: Michigan XML Editor
 * </p>
 * <p>
 * Description: This edits an XML document based on an XML schema.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: Nubean LLC
 * </p>
 * 
 * @author Ajay Vohra
 * @version 1.0
 */

public class XMLEditorKit extends StyledEditorKit {
	private static final long serialVersionUID = 2334218225266971554L;

	private XMLAbstractEditor editor;

	private StyleContext styleContext;

	public XMLEditorKit(XMLAbstractEditor editor, StyleContext styleContext) {
		super();
		this.styleContext = styleContext;
		this.editor = editor;
	}

	public XMLEditorKit(StyleContext styleContext) {
		super();
		this.styleContext = styleContext;
	}

	public Document createDefaultDocument() {
		if (editor != null)
			return new XMLDocument(editor, styleContext);
		else
			return new XMLDocument(styleContext);
	}

	public String getContentType() {
		return "text/xml";
	}

	/**
	 * Inserts content from the given stream, which will be treated as plain
	 * text.
	 * 
	 * @param in
	 *            The stream to read from
	 * @param doc
	 *            The destination for the insertion.
	 * @param pos
	 *            The location in the document to place the content >= 0.
	 * @exception IOException
	 *                on any I/O error
	 * @exception BadLocationException
	 *                if pos represents an invalid location within the document.
	 */
	public void read(Reader in, Document doc, int pos) throws IOException,
			BadLocationException {

		char[] buff = new char[512];
		StringBuffer sb = new StringBuffer(4096);
		int nch = -1;
		while ((nch = in.read(buff, 0, buff.length)) != -1) {
			sb.append(buff, 0, nch);
		}
		doc.insertString(pos, sb.toString(), null);
	}
}