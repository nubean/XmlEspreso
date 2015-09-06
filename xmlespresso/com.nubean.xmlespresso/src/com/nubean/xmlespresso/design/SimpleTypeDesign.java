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

package com.nubean.xmlespresso.design;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Node;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.TypeDefinition;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.xmlespresso.XMLEspressoActivator;

public class SimpleTypeDesign {
	private String label;

	private Button cdata;

	private Button ok, apply, cancel;

	private XMLAbstractEditor editor;

	private SchemaNode schemaNode;
	private Control control;
	private Composite controlPanel, parent;

	public SimpleTypeDesign(XMLAbstractEditor editor, Composite parent) {
		this.editor = editor;
		this.parent = parent;
	}

	public void setSchemaNode(SchemaNode schemaNode) {
		this.schemaNode = schemaNode;
		refreshControlPanel();
	}
	
	private void refreshControlPanel() {
		if(controlPanel != null) {
			controlPanel.dispose();
		}
		
		controlPanel = new Composite(parent, SWT.NONE);
		controlPanel.setLayout(new FillLayout());
		FormToolkit formToolkit = new FormToolkit(controlPanel.getDisplay());

		Form form = formToolkit.createForm(controlPanel);
		form.setFont(controlPanel.getFont());
		form.setText(XMLEspressoActivator.getResourceString("specify.simpleType"));
		GridLayout gl = new GridLayout(1, true);
		form.getBody().setLayout(gl);
		
		if (schemaNode.getTagName() != null)
			label = schemaNode.getTagName();

		cdata = formToolkit.createButton(form.getBody(), "CDATA", SWT.CHECK);

		Node textNode = editor.getSimpleTypeNode().getTextNode();
		if (textNode == null) {
			textNode = editor.getSimpleTypeNode().getCDATANode();
			if (textNode != null) {
				cdata.setSelection(true);
				cdata.setEnabled(false);
			} else {
				cdata.setEnabled(true);
			}
		} else {
			cdata.setSelection(false);
			cdata.setEnabled(false);
		}

		String curValue = (textNode != null ? textNode.getNodeValue() : null);

		TypeDefinition typeDef = schemaNode.getTypeDef();

		Composite cpanel = new Composite(form.getBody(), SWT.TITLE | SWT.BORDER);
		GridLayout gl2 = new GridLayout(1, true);
		cpanel.setLayout(gl2);

		formToolkit.adapt(cpanel);

		if (typeDef.getEnumerations() != null
				&& typeDef.getEnumerations().size() > 0) {
			Vector<String> enums = typeDef.getEnumerations();
			Combo cb = new Combo(cpanel, SWT.READ_ONLY);
			formToolkit.adapt(cb, true, true);
			String[] items = new String[enums.size()];
			enums.toArray(items);

			cb.setItems(items);
			control = cb;
		} else if (typeDef.getBase().equals("boolean")) {
			Button cb = new Button(cpanel, SWT.CHECK);
			formToolkit.adapt(cb, true, true);
			cb.setText(label);
			cb.setSelection(curValue != null && curValue.equals("true"));
			control = cb;
		} else {
			Text text = new Text(cpanel, SWT.V_SCROLL | SWT.H_SCROLL);
			formToolkit.adapt(text, true, true);
			if (curValue != null)
				text.setText(curValue);
			GridData tGridData = new GridData();

			tGridData.horizontalAlignment = GridData.FILL;
			tGridData.heightHint = 75;
			tGridData.widthHint = 200;

			text.setLayoutData(tGridData);
			control = text;
		}

		Composite bpanel = new Composite(form.getBody(), SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		bpanel.setLayoutData(gd);
		
		FillLayout fl = new FillLayout(SWT.HORIZONTAL);
		bpanel.setLayout(fl);

		formToolkit.adapt(bpanel);
		
		ok = new Button(bpanel, SWT.PUSH);
		formToolkit.adapt(ok, true, true);
		ok.setText(LocalizedResources.applicationResources.getString("ok"));
		ok.addSelectionListener(new SimpleTypeOkAction());

		apply = new Button(bpanel, SWT.PUSH);
		formToolkit.adapt(apply, true, true);
		apply.setText(LocalizedResources.applicationResources
				.getString("apply"));
		apply.addSelectionListener(new SimpleTypeApplyAction());

		cancel = new Button(bpanel, SWT.PUSH);
		formToolkit.adapt(cancel, true, true);
		cancel.setText(LocalizedResources.applicationResources
				.getString("cancel"));
		cancel.addSelectionListener(new SimpleTypeCancelAction());

		parent.layout();
	}

	private class SimpleTypeCancelAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			try {
				editor.setTreeSelectionPath(editor.getSimpleTypePath()
						.getParentPath());
			} catch (Exception ex) {

			}
		}
	}

	private class SimpleTypeApplyAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			String value = "";

			if (control instanceof Text)
				value = ((Text) control).getText();
			else if (control instanceof Combo)
				value = (String) ((Combo) control).getText();
			else if (control instanceof Button) {

				value = (((Button) control).getSelection() ? "true" : "false");
			}
			if (cdata.getSelection())
				editor.insertCDATAInSimpleType(value);
			else
				editor.insertTextInSimpleType(value);
		}
	}

	private class SimpleTypeOkAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			String value = "";

			if (control instanceof Text)
				value = ((Text) control).getText();
			else if (control instanceof Combo)
				value = (String) ((Combo) control).getText();
			else if (control instanceof Button) {

				value = (((Button) control).getSelection() ? "true" : "false");
			}
			if (cdata.getSelection())
				editor.insertCDATAInSimpleType(value);
			else
				editor.insertTextInSimpleType(value);

			try {
				editor.setTreeSelectionPath(editor.getSimpleTypePath()
						.getParentPath());
			} catch (Exception ex) {
			}
		}
	}

}
