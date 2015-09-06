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

package com.nubean.xmlespresso.preferences;

import java.awt.Color;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michbase.EditorConfiguration;
import com.nubean.michdtd.DTDEditorConfiguration;
import com.nubean.michutil.LocalizedResources;
import com.nubean.michutil.StyleItem;
import com.nubean.michutil.StyleListModel;
import com.nubean.xmlespresso.XMLEspressoActivator;

public class DTDEditorPreferencePage extends EditorPreferencePage {

	private static final String MIME_TYPE = "text/dtd";

	private Label selected;

	private Combo elist;

	private StyleItem[] styles;

	private DTDEditorConfiguration dconfig;

	public DTDEditorPreferencePage() {
		super();
	}

	public DTDEditorPreferencePage(String title) {
		super(title);
	}

	public DTDEditorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected String getMimeType() {
		return MIME_TYPE;
	}

	protected EditorConfiguration getEditorConfiguration() {
		return new DTDEditorConfiguration();
	}
	
	private void initUI() {
		StyleListModel listModel = new StyleListModel(dconfig.getStyleContext());
		styles = new StyleItem[listModel.getSize()];
		listModel.copyInto(styles);

		String[] items = new String[styles.length];

		for (int i = 0; i < items.length; i++) {
			items[i] = styles[i].getTitle();
		}
		elist.setItems(items);

		elist.select(0);
		listSelectionChanged();
	}

	@Override
	protected Control createControl(Composite parent, EditorConfiguration config) {
		dconfig = (DTDEditorConfiguration) config;
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());

		Form form = formToolkit.createForm(parent);
		form.setFont(parent.getFont());

		FormLayout formLayout = new FormLayout();
		form.getBody().setLayout(formLayout);

		Group elementGroup = new Group(form.getBody(), SWT.SHADOW_NONE);
		formToolkit.adapt(elementGroup);
		elementGroup.setText(XMLEspressoActivator
				.getResourceString("element.style"));

		FormData layoutData = new FormData();
		layoutData.top = new FormAttachment(0, 5);
		layoutData.left = new FormAttachment(0, 5);
		layoutData.right = new FormAttachment(100, -5);

		elementGroup.setLayoutData(layoutData);

		elementGroup.setLayout(new GridLayout(1, true));

		Composite tgroup = new Composite(elementGroup, SWT.NONE);
		formToolkit.adapt(tgroup);

		tgroup.setLayout(new FillLayout());

		selected = new Label(tgroup, SWT.CENTER);
		
		formToolkit.adapt(selected, true, true);

		Button fgButton = formToolkit.createButton(tgroup,
				LocalizedResources.applicationResources
						.getString("change.foreground"), SWT.PUSH);

		fgButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {

				int index = elist.getSelectionIndex();
				if (index > -1 && index < styles.length) {
					StyleItem sel = styles[index];
					ColorDialog chooser = new ColorDialog(e.widget.getDisplay()
							.getActiveShell(), SWT.TITLE | SWT.BORDER);
					Color fg = sel.getForeground();

					chooser.setRGB(new RGB(fg.getRed(), fg.getGreen(), fg
							.getBlue()));

					RGB rgb = chooser.open();

					if (rgb != null) {
						selected.setForeground(XMLEspressoActivator.getDefault()
								.getColor(rgb));
						sel.setForeground(new Color(rgb.red, rgb.green,
								rgb.blue));
						dconfig.setSettingsChanged(true);
					}
				}
			}
		});

		Button bgButton = formToolkit.createButton(tgroup,
				LocalizedResources.applicationResources
						.getString("change.background"), SWT.PUSH);

		bgButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				int index = elist.getSelectionIndex();
				if (index > -1 && index < styles.length) {
					StyleItem sel = styles[index];
					ColorDialog chooser = new ColorDialog(e.widget.getDisplay()
							.getActiveShell(), SWT.TITLE | SWT.BORDER);
					Color bg = sel.getBackground();

					chooser.setRGB(new RGB(bg.getRed(), bg.getGreen(), bg
							.getBlue()));

					RGB rgb = chooser.open();

					if (rgb != null) {
						selected.setBackground(XMLEspressoActivator.getDefault()
								.getColor(rgb));
						sel.setBackground(new Color(rgb.red, rgb.green,
								rgb.blue));
						dconfig.setSettingsChanged(true);
					}
				}
			}
		});

		elist = new Combo(elementGroup, SWT.SINGLE);
		formToolkit.adapt(elist, true, true);

		elist.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				listSelectionChanged();
			}
		});

		initUI();

		return form.getBody();
	}

	private void listSelectionChanged() {
		int index = elist.getSelectionIndex();
		if (index > -1 && index < styles.length) {
			StyleItem sel = styles[index];
			selected.setText(sel.getTitle());
			Color bg = sel.getBackground();
			selected.setBackground(XMLEspressoActivator.getDefault().getColor(
					new RGB(bg.getRed(), bg.getGreen(), bg.getBlue())));
			Color fg = sel.getForeground();
			selected.setForeground(XMLEspressoActivator.getDefault().getColor(
					new RGB(fg.getRed(), fg.getGreen(), fg.getBlue())));
		}
	}

	private boolean save() {
		IPreferenceStore ps = XMLEspressoActivator.getDefault()
				.getPreferenceStore();

		StringBuffer sb = new StringBuffer(XMLEspressoActivator.CONFIG_PREF);
		sb.append(":").append(getMimeType());

		ps.setValue(sb.toString(), dconfig.toXMLString());
		return true;
	}
	
	@Override
	protected void performDefaults() {
		dconfig = (DTDEditorConfiguration)getEditorConfiguration();
		initUI();
	}
	
	@Override
	public boolean performOk() {
		return save();
	}

	@Override
	public void performApply() {
		save();
	}
}
