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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.*;

import com.nubean.xmlespresso.XMLEspressoActivator;

public class OpenXMLDocumentWizardPage extends WizardPage {
	private Button standalone, catalog, custom;

	private static final String CATALOG_OPEN = XMLEspressoActivator
			.getResourceString("catalog.open");

	private static final String CUSTOM_OPEN = XMLEspressoActivator
			.getResourceString("custom.open");

	public OpenXMLDocumentWizardPage(String pageName) {
		super(pageName);
	}

	public OpenXMLDocumentWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);

		control.setLayout(new GridLayout());

		catalog = new Button(control, SWT.RADIO);
		catalog.setText(CATALOG_OPEN);

		custom = new Button(control, SWT.RADIO);
		custom.setText(CUSTOM_OPEN);

		catalog.setSelection(true);

		setControl(control);
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

}
