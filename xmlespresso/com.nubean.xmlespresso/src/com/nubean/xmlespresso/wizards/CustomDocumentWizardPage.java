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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.ui.PlatformUI;

import com.nubean.xmlespresso.*;

import com.nubean.michutil.*;
import com.nubean.michbase.CommonUtils;

public class CustomDocumentWizardPage extends WizardPage {

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

	private static final String CUSTOM_PAGE_DESC = XMLEspressoActivator
			.getResourceString("custom.page.description");

	private static final String BROWSE = XMLEspressoActivator
			.getResourceString("browse");

	private static final String DEFAULT_NAMESPACE_LABEL = XMLEspressoActivator
			.getResourceString("default.namespace.label");

	private static String filterPath;

	private Text schemaText, dtdText, rootText, publicText, uriText,
			prefixText;

	private Combo encodingCombo;

	private Button check;

	private String schema, dtd, root, publicId, encoding, uri, prefix;

	private boolean useSchemaNamespace;

	public CustomDocumentWizardPage(String pageName) {
		super(pageName);
	}

	public CustomDocumentWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		this.setDescription(CUSTOM_PAGE_DESC);
		setPageComplete(false);
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.makeColumnsEqualWidth = false;
		composite.setLayout(gridLayout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(SCHEMA_LABEL);

		schemaText = new Text(composite, SWT.BORDER);
		schemaText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		schemaText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = schemaText.getText();
				if (st != null
						&& st.trim().length() > 0
						&& (st.trim().endsWith(".xsd") || st.trim().endsWith(
								".xs"))) {
					schema = st.trim();
					if (root != null && root.trim().length() > 0)
						setPageComplete(true);
					else
						setPageComplete(false);
				} else {
					setPageComplete(false);
				}
			}
		});
		Button browse = new Button(composite, SWT.NONE);
		browse.setText(BROWSE);

		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterPath(filterPath);
				String path = dialog.open();
				if (path != null) {
					filterPath = dialog.getFilterPath();
					schemaText.setText(path);
					schema = path;
					if (root != null && root.trim().length() > 0)
						setPageComplete(true);
					else
						setPageComplete(false);
				} else {
					setPageComplete(false);
				}
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText(DTD_LABEL);

		dtdText = new Text(composite, SWT.BORDER);
		dtdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		dtdText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = dtdText.getText();
				if (st != null && st.trim().length() > 0
						&& st.trim().endsWith(".dtd")) {
					dtd = st.trim();
					if (root != null && root.trim().length() > 0)
						setPageComplete(true);
					else
						setPageComplete(false);
				} else {
					setPageComplete(false);
				}
			}
		});

		browse = new Button(composite, SWT.NONE);
		browse.setText(BROWSE);
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterPath(filterPath);
				String path = dialog.open();
				if (path != null) {
					filterPath = dialog.getFilterPath();
					dtdText.setText(path);
					dtd = path;
					if (root != null && root.trim().length() > 0)
						setPageComplete(true);
					else
						setPageComplete(false);
				} else {
					setPageComplete(false);
				}
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText(PUBLIC_LABEL);

		publicText = new Text(composite, SWT.BORDER);
		publicText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		publicText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = publicText.getText();
				if (st != null && st.trim().length() > 0) {
					publicId = st.trim();
				}
			}
		});

		label = new Label(composite, SWT.NONE);

		label = new Label(composite, SWT.NONE);
		label.setText(ROOT_LABEL);

		rootText = new Text(composite, SWT.BORDER);
		rootText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		rootText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = rootText.getText();
				if (st != null && st.trim().length() > 0) {
					root = st.trim();
					if ((schema != null && schema.trim().length() > 0)
							|| (dtd != null && dtd.trim().length() > 0))
						setPageComplete(true);
					else
						setPageComplete(false);
				} else {
					setPageComplete(false);
				}
			}
		});

		label = new Label(composite, SWT.NONE);

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

		label = new Label(composite, SWT.NONE);
		label.setText(PREFIX_LABEL);

		prefixText = new Text(composite, SWT.BORDER);
		prefixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		prefixText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = prefixText.getText();
				if (st != null && st.trim().length() > 0) {
					prefix = st.trim();
				}
			}
		});

		label = new Label(composite, SWT.NONE);

		label = new Label(composite, SWT.NONE);
		label.setText(URI_LABEL);

		uriText = new Text(composite, SWT.BORDER);
		uriText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		uriText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String st = uriText.getText();
				if (st != null && st.trim().length() > 0) {
					uri = st.trim();
				}
			}
		});

		label = new Label(composite, SWT.NONE);

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

		setControl(composite);
	}

	public String getSchema() {
		return schema;
	}

	public String getDTD() {
		return dtd;
	}

	public String getRoot() {
		return root;
	}

	public String getPublicID() {
		return publicId;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getURI() {
		return uri;
	}

	public boolean getUseDefulatNamespace() {
		return useSchemaNamespace;
	}

}
