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
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import com.nubean.xmlespresso.*;
import com.nubean.michbase.CommonUtils;

public class NewXMLFileCreationWizardPage extends WizardNewFileCreationPage {

	private Button standalone, catalog, custom;

	private static final String CATALOG_CREATE = XMLEspressoActivator
			.getResourceString("catalog.create");

	private static final String CUSTOM_CREATE = XMLEspressoActivator
			.getResourceString("custom.create");

	public NewXMLFileCreationWizardPage(String pageName,
			IStructuredSelection selection) {
		super(pageName, selection);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Composite control = (Composite) getControl();
		catalog = new Button(control, SWT.RADIO);
		catalog.setText(CATALOG_CREATE);

		catalog.setSelection(true);

		custom = new Button(control, SWT.RADIO);
		custom.setText(CUSTOM_CREATE);
	}

	public boolean isStandalone() {
		return standalone.getSelection();
	}

	public boolean isCatalog() {
		return catalog.getSelection();
	}

	public boolean isCustom() {
		return custom.getSelection();
	}

	@Override
	public boolean isPageComplete() {
		String fileName = getFileName();
		boolean xml = false;
		int index = fileName.lastIndexOf(".");
		if (index > 0) {
			String ext = fileName.substring(index);
			xml = CommonUtils.contains(CommonUtils.extensions, ext);
		}
		return xml && super.isPageComplete();
	}
}
