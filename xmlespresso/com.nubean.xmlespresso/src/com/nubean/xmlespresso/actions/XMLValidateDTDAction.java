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

package com.nubean.xmlespresso.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.editors.XMLEspressoEditor;

public class XMLValidateDTDAction extends Action {

	private XMLEspressoEditor editor;

	public XMLValidateDTDAction(XMLEspressoEditor editor) {
		super();
		this.editor = editor;
	}

	public XMLValidateDTDAction(String text) {
		super(text);
	}

	public XMLValidateDTDAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public XMLValidateDTDAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void run() {
		if (editor != null) {
			try {
				IProgressService progressService = PlatformUI.getWorkbench()
						.getProgressService();

				progressService.runInUI(progressService,
						new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) {
								editor.validateUsingDtd();
							}
						}, null);
			} catch (Exception e) {
				XMLEspressoActivator.getDefault().error(getText(), e);
			}
		}
	}

	@Override
	public String getText() {
		return XMLEspressoActivator.getResourceString("validate.using.dtd");
	}

	@Override
	public int getStyle() {
		return IAction.AS_PUSH_BUTTON;
	}

}
