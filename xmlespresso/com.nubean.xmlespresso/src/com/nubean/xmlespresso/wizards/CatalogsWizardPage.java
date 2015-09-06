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
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;

import com.nubean.michbase.CatalogEntry;
import com.nubean.michbase.CommonUtils;
import com.nubean.xmlespresso.*;

public class CatalogsWizardPage extends WizardPage {

	private TreeViewer ttv;

	private static final String SCHEMA_LABEL = XMLEspressoActivator
			.getResourceString("schema.label");

	private static final String DTD_LABEL = XMLEspressoActivator
			.getResourceString("dtd.label");

	private static final String ROOT_LABEL = XMLEspressoActivator
			.getResourceString("root.label");

	private static final String PUBLIC_LABEL = XMLEspressoActivator
			.getResourceString("public.label");

	private static final String ENCODING_LABEL = XMLEspressoActivator
			.getResourceString("encoding.label");

	private static final String URI_LABEL = XMLEspressoActivator
			.getResourceString("uri.label");

	private static final String PREFIX_LABEL = XMLEspressoActivator
			.getResourceString("prefix.label");

	private static final String DEFAULT_NAMESPACE_LABEL = XMLEspressoActivator
			.getResourceString("default.namespace.label");

	private CatalogEntry entry;

	private Text schemaText, dtdText, rootText, publicText, uriText,
			prefixText;

	private String encoding, uri, prefix;

	private Combo encodingCombo;

	private Button check;

	private boolean useSchemaNamespace;

	public CatalogsWizardPage(String pageName) {
		super(pageName);
	}

	public CatalogsWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);

	}

	@Override
	public void createControl(Composite parent) {

		setPageComplete(false);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		ttv = new TreeViewer(composite);
		ttv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		// Set the content and label providers
		ttv.setContentProvider(new CatalogsTreeContentProvider());
		ttv.setLabelProvider(new CatalogsTableLabelProvider());
		ttv.setInput(XMLEspressoActivator.getCatalogs());

		ttv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(false);
				if (event.getSelection().isEmpty()) {
					return;
				}

				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event
							.getSelection();
					if (selection.size() > 1) {
						return;
					}
					boolean found = false;
					for (java.util.Iterator iterator = selection.iterator(); iterator
							.hasNext();) {
						Object object = iterator.next();
						if (object instanceof CatalogEntry) {
							entry = (CatalogEntry) object;
							if (entry.getProperty("schema") != null)
								schemaText.setText(entry.getProperty("schema"));

							if (entry.getProperty("dtd") != null)
								dtdText.setText(entry.getProperty("dtd"));

							if (entry.getProperty("root") != null)
								rootText.setText(entry.getProperty("root"));

							if (entry.getProperty("targetPrefix") != null)
								prefixText.setText(entry
										.getProperty("targetPrefix"));

							if (entry.getProperty("targetNamespace") != null)
								uriText.setText(entry
										.getProperty("targetNamespace"));

							if (entry.getProperty("public") != null)
								publicText.setText(entry.getProperty("public"));
							found = true;
							break;
						}
					}

					setPageComplete(found);

				}
			}
		});

		createPreview(composite).setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));

		setControl(composite);
	}

	public Composite createPreview(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = false;
		composite.setLayout(gridLayout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(SCHEMA_LABEL);

		schemaText = new Text(composite, SWT.BORDER);
		schemaText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		schemaText.setEditable(false);
		schemaText.setEnabled(false);

		label = new Label(composite, SWT.NONE);
		label.setText(DTD_LABEL);

		dtdText = new Text(composite, SWT.BORDER);
		dtdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dtdText.setEditable(false);
		dtdText.setEnabled(false);

		label = new Label(composite, SWT.NONE);
		label.setText(PUBLIC_LABEL);

		publicText = new Text(composite, SWT.BORDER);
		publicText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		publicText.setEditable(false);
		publicText.setEnabled(false);

		label = new Label(composite, SWT.NONE);
		label.setText(ROOT_LABEL);

		rootText = new Text(composite, SWT.BORDER);
		rootText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rootText.setEditable(false);
		rootText.setEnabled(false);

		label = new Label(composite, SWT.NONE);
		label.setText(ENCODING_LABEL);

		encodingCombo = new Combo(composite, SWT.NONE);

		encodingCombo.setItems(CommonUtils.encodings);
		encodingCombo.setText("UTF-8");
		encoding = encodingCombo.getText();

		encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = encodingCombo.getText();
				if (st != null && st.trim().length() > 0
						&& CommonUtils.contains(CommonUtils.encodings, st)) {
					encoding = st.trim();
				} else {
					encodingCombo.setText("UTF-8");
					encoding = encodingCombo.getText();
				}
			}
		});

		encodingCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String st = encodingCombo.getText();
				if (st != null && st.trim().length() > 0
						&& CommonUtils.contains(CommonUtils.encodings, st)) {
					encoding = st.trim();
				} else {
					encodingCombo.setText("UTF-8");
					encoding = encodingCombo.getText();
				}
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText(PREFIX_LABEL);

		prefixText = new Text(composite, SWT.BORDER);
		prefixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		prefixText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				prefix = prefixText.getText();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText(URI_LABEL);

		uriText = new Text(composite, SWT.BORDER);
		uriText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		uriText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				uri = uriText.getText();
			}
		});

		check = new Button(composite, SWT.CHECK);
		check.setText(DEFAULT_NAMESPACE_LABEL);

		check.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useSchemaNamespace = check.getSelection();
				prefixText.setEnabled(!useSchemaNamespace);
				uriText.setEnabled(!useSchemaNamespace);

				prefixText.setEditable(!useSchemaNamespace);
				uriText.setEditable(!useSchemaNamespace);
			}
		});
		return composite;

	}

	public CatalogEntry getEntry() {
		return entry;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getURI() {
		return uri;
	}

	public String getPrefix() {
		return prefix;
	}

	public boolean getUseDefulatNamespace() {
		return useSchemaNamespace;
	}
}
