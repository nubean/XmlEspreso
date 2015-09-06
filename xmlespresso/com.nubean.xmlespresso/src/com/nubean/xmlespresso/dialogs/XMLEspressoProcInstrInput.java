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
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.ProcInstr;

public class XMLEspressoProcInstrInput extends Dialog {
	private Text targettf, datatf;

	private ProcInstr pi;

	public XMLEspressoProcInstrInput(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.APPLICATION_MODAL | getDefaultOrientation());
	}

	@Override
	protected Control createContents(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());
		Form form = formToolkit.createForm(parent);
		form.setText(LocalizedResources.applicationResources
				.getString("processing.instruction"));
		form.setFont(parent.getFont());

		Composite panel = form.getBody();
		GridLayout gl = new GridLayout(2, false);
		panel.setLayout(gl);

		formToolkit.createLabel(panel, LocalizedResources.applicationResources
				.getString("target"), SWT.NONE);
		GridData gdata = new GridData(SWT.RIGHT, SWT.CENTER, false, false);

		targettf = formToolkit.createText(panel, "", SWT.BORDER);
		gdata = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		targettf.setLayoutData(gdata);

		Label datal = formToolkit.createLabel(panel,
				LocalizedResources.applicationResources.getString("data"),
				SWT.NONE);
		datal
				.setText(LocalizedResources.applicationResources
						.getString("data"));
		gdata = new GridData(SWT.RIGHT, SWT.CENTER, false, false);

		datatf = formToolkit.createText(panel, "", SWT.BORDER);
		gdata = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		datatf.setLayoutData(gdata);

		datatf.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == '\r') {
					doAction();
				}
			}
		});

		return form;
	}

	protected void doAction() {
		pi = new ProcInstr(targettf.getText(), datatf.getText());
		close();
	}

	public static ProcInstr showDialog(Shell shell) {
		XMLEspressoProcInstrInput dialog = new XMLEspressoProcInstrInput(shell);
		dialog.setBlockOnOpen(true);
		dialog.open();

		return dialog.pi;
	}

}
