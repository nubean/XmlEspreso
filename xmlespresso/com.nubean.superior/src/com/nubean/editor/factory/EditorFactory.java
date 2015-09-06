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

package com.nubean.editor.factory;

import com.nubean.css.editor.CSSEditor;
import com.nubean.java.editor.JavaEditor;
import com.nubean.javacc.editor.JavaCCEditor;
import com.nubean.michxml.editor.XMLEditor;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.Editor;
import com.nubean.michbase.project.Project;
import com.nubean.michdtd.editor.DTDEditor;
import com.nubean.michtext.editor.TextEditor;

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

public class EditorFactory {
	public static Editor createNewEditor(Project project, 
			DocumentDescriptor dd, boolean integrated) {
		if (dd.getMimeType().equals("text/xml"))
			return new XMLEditor(project, dd, integrated);
		else if (dd.getMimeType().equals("text/dtd"))
			return new DTDEditor(project, dd, integrated);
		else if (dd.getMimeType().equals("text/css"))
			return new CSSEditor(project, dd, integrated);
		else if (dd.getMimeType().equals("text/javacc"))
			return new JavaCCEditor(project, dd, integrated);
		else if (dd.getMimeType().equals("text/java"))
			return new JavaEditor(project, dd, integrated);
		else
			return new TextEditor(project,  dd, integrated);
	}
}