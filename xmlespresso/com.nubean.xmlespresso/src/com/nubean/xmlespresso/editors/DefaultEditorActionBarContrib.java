/*
 * Coppyright NuBean LLC 2002-2004
 * Created on Sep 9, 2005
 *
 * 
 */
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

package com.nubean.xmlespresso.editors;


import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * 
 * 
 * @author Ajay Vohra
 * 
 */
public class DefaultEditorActionBarContrib extends
		TextEditorActionContributor {

	public DefaultEditorActionBarContrib() {
		super();
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		if(part instanceof FormEditor) {
			FormEditor formEditor = (FormEditor)part;
			super.setActiveEditor(formEditor.getActiveEditor());
		} else {
			super.setActiveEditor(part);
		}
	}
	
}
