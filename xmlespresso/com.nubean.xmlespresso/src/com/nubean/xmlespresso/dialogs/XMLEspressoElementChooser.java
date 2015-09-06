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

import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.SchemaNode;

public class XMLEspressoElementChooser extends Dialog {

	private Vector<?> elements;

	private Object element;

	private boolean hasNamespace;

	private Button cb;

	private List list;

	private static String lastSelItem;

	public XMLEspressoElementChooser(Shell parentShell, Vector<?> elements,
			boolean hasNamespace) {
		super(parentShell);
		this.elements = elements;
		this.hasNamespace = hasNamespace;
		setShellStyle(SWT.APPLICATION_MODAL | getDefaultOrientation());
	}

	@Override
	protected Control createContents(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());

		Form form = formToolkit.createForm(parent);
		form.setFont(parent.getFont());
		form.setText(LocalizedResources.applicationResources
				.getString("choose.element"));
		GridLayout gl = new GridLayout(1, false);
		form.getBody().setLayout(gl);

		list = new List(form.getBody(), SWT.SINGLE | SWT.V_SCROLL
				| SWT.H_SCROLL);
		formToolkit.adapt(list, true, true);
		int nitems = (elements != null ? elements.size() : 0);
		final String[] items = new String[nitems];
		for (int i = 0; i < nitems; i++) {
			items[i] = elements.get(i).toString();
		}
		list.setItems(items);
		list.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == '\r') {
					selectElement(items);
					close();
				}
			}

		});

		list.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				selectElement(items);
				close();
			}
		});

		list.select(getIndex(lastSelItem, items));

		cb = formToolkit.createButton(form.getBody(),
				LocalizedResources.applicationResources
						.getString("edit.namespace"), SWT.CHECK);

		cb.setSelection(hasNamespace);
		cb.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == '\r') {
					selectElement(items);
					close();
				}
			}
		});

		return form;
	}

	private void selectElement(String[] items) {
		int index = list.getSelectionIndex();
		if (index >= 0) {
			element = elements.get(index);
			lastSelItem = items[index];
			if (element instanceof SchemaNode) {
				SchemaNode sn = (SchemaNode) element;
				sn.setEditNamespace(cb.getSelection());
			}
		}
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

	public static Object showDialog(Shell shell, Vector<?> elements,
			boolean hasNamespace) {
		XMLEspressoElementChooser dialog = new XMLEspressoElementChooser(shell,
				elements, hasNamespace);
		dialog.setBlockOnOpen(true);
		dialog.open();

		return dialog.element;
	}
}
