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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.Namespace;

public class XMLEspressoNamespaceInput extends Dialog {

	private Combo pcombo, ucombo;

	private Namespace ns;

	private String[] prefix, uri;

	private static String lastSelUri;

	public XMLEspressoNamespaceInput(Shell parentShell, String[] prefix,
			String[] uri) {
		super(parentShell);
		this.prefix = prefix;
		this.uri = uri;
		setShellStyle(SWT.APPLICATION_MODAL | getDefaultOrientation());
	}

	@Override
	protected Control createContents(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());
		Form form = formToolkit.createForm(parent);

		form.setText(LocalizedResources.applicationResources
				.getString("namespace.input"));
		form.setFont(parent.getFont());

		Composite panel = form.getBody();
		GridLayout gl = new GridLayout(2, false);
		panel.setLayout(gl);

		Group pgroup = new Group(panel, SWT.TITLE);
		pgroup.setLayout(new GridLayout(1, true));
		formToolkit.adapt(pgroup);
		
		pgroup.setText(LocalizedResources.applicationResources
				.getString("namespace.prefix"));

		pcombo = new Combo(pgroup, SWT.SIMPLE);
		pcombo.setItems(prefix);
		pcombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = pcombo.getSelectionIndex();
				if (index >= 0) {
					ucombo.select(index);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				int index = pcombo.getSelectionIndex();
				if (index >= 0) {
					ucombo.select(index);
				}
			}
		});
		formToolkit.adapt(pcombo, true, true);

		Group ugroup = new Group(panel, SWT.TITLE);
		ugroup.setLayout(new GridLayout(1, true));
		formToolkit.adapt(ugroup);
		ugroup.setText( LocalizedResources.applicationResources
				.getString("namespace.uri"));

		ucombo = new Combo(ugroup, SWT.SIMPLE);
		formToolkit.adapt(ucombo, true, true);

		ucombo.setItems(uri);
		int selIndex = getIndex(lastSelUri, uri);
		ucombo.select(selIndex);
		pcombo.select(selIndex);

		ucombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = ucombo.getSelectionIndex();
				if (index >= 0) {
					pcombo.select(index);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				int index = ucombo.getSelectionIndex();
				if (index >= 0) {
					pcombo.select(index);
				}
			}
		});

		ucombo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == '\r') {
					doAction();
				}
			}
		});

		pcombo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == '\r') {
					doAction();
				}
			}
		});

		return panel;
	}

	private void doAction() {
		String p = null;
		String u = null;

		int pindex = pcombo.getSelectionIndex();
		int uindex = ucombo.getSelectionIndex();

		if (pindex > 0) {
			p = prefix[pindex];
		} else {
			p = pcombo.getText();
		}

		if (uindex > 0) {
			u = uri[uindex];
		} else {
			u = ucombo.getText();
		}
		lastSelUri = u;
		ns = new Namespace(p, u);
		close();
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

	public static Namespace showDialog(Shell shell, String[] prefix,
			String[] url) {
		XMLEspressoNamespaceInput dialog = new XMLEspressoNamespaceInput(shell,
				prefix, url);
		dialog.setBlockOnOpen(true);
		dialog.open();

		return dialog.ns;
	}
}
