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

import javax.swing.table.TableModel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.design.AttributeCellEditor;
import com.nubean.xmlespresso.design.AttributeCellModifier;
import com.nubean.xmlespresso.design.AttributeTableContentProvider;
import com.nubean.xmlespresso.design.AttributeTableLabelProvider;
import com.nubean.xmlespresso.design.CellEditorListener;

public class AttributeDialog extends Dialog {

	private XMLAbstractEditor editor;

	private TableModel model;

	private static String[] columnProperties = {
			XMLEspressoActivator.getResourceString("name"),
			XMLEspressoActivator.getResourceString("value") };

	public AttributeDialog(Shell parentShell, XMLAbstractEditor editor,
			TableModel model) {
		super(parentShell);
		this.editor = editor;
		this.model = model;
		setShellStyle(SWT.APPLICATION_MODAL | getDefaultOrientation());
	}

	@Override
	protected Control createContents(Composite parent) {
		Viewer viewer = createAttributePanel(parent);
		return viewer.getControl();
	}

	private Table createTable(FormToolkit formToolkit, Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		Table table = formToolkit.createTable(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn name = new TableColumn(table, SWT.CENTER, 0);
		name.setWidth(100);
		name.setText(columnProperties[0]);

		TableColumn value = new TableColumn(table, SWT.LEFT, 1);
		value.setWidth(100);
		value.setText(columnProperties[1]);

		return table;
	}

	private Viewer createAttributePanel(Composite parent) {

		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());

		Form form = formToolkit.createForm(parent);
		form.getBody().setLayout(new GridLayout(1, false));
		form.setText(LocalizedResources.applicationResources
				.getString("edit.attributes"));
		form.setFont(parent.getFont());

		Table table = createTable(formToolkit, form.getBody());

		TableViewer viewer = new TableViewer(table);

		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new AttributeTableContentProvider());
		viewer.setLabelProvider(new AttributeTableLabelProvider());

		viewer.setColumnProperties(columnProperties);
		viewer.setCellModifier(new AttributeCellModifier(columnProperties));
		CellEditor[] editors = new CellEditor[columnProperties.length];
		for (int i = 0; i < editors.length; i++) {
			editors[i] = new AttributeCellEditor(table);
			editors[i].addListener(new CellEditorListener(editors[i], editor));
		}
		viewer.setCellEditors(editors);

		viewer.setInput(model);
		return viewer;
	}
}
