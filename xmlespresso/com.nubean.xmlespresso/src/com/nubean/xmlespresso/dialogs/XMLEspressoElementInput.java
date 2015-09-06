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

package com.nubean.xmlespresso.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michutil.LocalizedResources;

public class XMLEspressoElementInput extends Dialog {

	private Combo ecombo;

	private String element;

	private String[] elements;

	private static String lastSel;

	public XMLEspressoElementInput(Shell parentShell, String[] elements) {
		super(parentShell);
		setShellStyle(SWT.APPLICATION_MODAL | getDefaultOrientation());
		this.elements = elements;
	}

	@Override
	protected Control createContents(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());
		Form form = formToolkit.createForm(parent);
		form.setText(LocalizedResources.applicationResources
				.getString("element.name"));
		form.setFont(parent.getFont());

		Composite panel = form.getBody();
		GridLayout gl = new GridLayout(2, false);
		panel.setLayout(gl);

		ecombo = new Combo(panel, SWT.SIMPLE);
		formToolkit.adapt(ecombo, true, true);
		GridData gdata = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		ecombo.setLayoutData(gdata);
		ecombo.setItems(elements);
		ecombo.select(getIndex(lastSel, elements));
		ecombo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == '\r') {
					int index = ecombo.getSelectionIndex();

					if (index > 0) {
						element = elements[index];
					} else {
						element = ecombo.getText();
					}
					lastSel = element;
					close();
				}
			}
		});

		return form;
	}

	private int getIndex(String key, String[] array) {
		int count = (key != null && array != null ? array.length : 0);
		for (int i = 0; i < count; i++) {
			if (array[i].equals(key)) {
				return i;
			}
		}
		return 0;
	}

	public static String showDialog(Shell shell, String[] ele) {
		XMLEspressoElementInput dialog = new XMLEspressoElementInput(shell, ele);
		dialog.setBlockOnOpen(true);
		dialog.open();

		return dialog.element;
	}

}
