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

package com.nubean.xmlespresso.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.resources.*;
import org.eclipse.ui.part.*;

import com.nubean.xmlespresso.*;

public class NewDTDDocumentWizard extends Wizard implements INewWizard {

	private static final String NEW_DTD_DOCUMENT = XMLEspressoActivator
			.getResourceString("newDTDDocument");

	private static final String NEW_FILE_PAGE = XMLEspressoActivator
			.getResourceString("newFilePage");

	private IStructuredSelection selection;

	private NewDTDFileCreationWizardPage newFilePage;

	public NewDTDDocumentWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		IFile newFile = newFilePage.createNewFile();

		try {

			FileEditorInput input = new FileEditorInput(newFile);
			String id = "com.nubean.xmlespresso.editors.dtdeditor";
			XMLEspressoActivator.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().openEditor(
							input, id);
		} catch (Exception e) {

		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(NEW_DTD_DOCUMENT);
	}

	@Override
	public void addPages() {
		newFilePage = new NewDTDFileCreationWizardPage(NEW_FILE_PAGE, selection);
		addPage(newFilePage);
	}

}
