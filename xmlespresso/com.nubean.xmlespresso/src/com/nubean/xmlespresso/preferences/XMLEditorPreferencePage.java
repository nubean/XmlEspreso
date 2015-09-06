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
import org.eclipse.swt.events.KeyAdapter;
import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nubean.michbase.EditorConfiguration;
import com.nubean.michutil.LocalizedResources;
import com.nubean.michutil.StyleItem;
import com.nubean.michutil.StyleListModel;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.xmlespresso.XMLEspressoActivator;

public class XMLEditorPreferencePage extends EditorPreferencePage {

	private static final String MIME_TYPE = "text/xml";

	private Button validateSchema, splitLineStyle, singleLineStyle, fgButton,
			bgButton;

	private Text proxyHostText, proxyPortText;

	private Label selected;

	private Combo elist;

	private StyleItem[] styles;

	private XMLEditorConfiguration xconfig;

	public XMLEditorPreferencePage() {
		super();
	}

	public XMLEditorPreferencePage(String title) {
		super(title);
	}

	public XMLEditorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected String getMimeType() {
		return MIME_TYPE;
	}

	protected EditorConfiguration getEditorConfiguration() {
		return new XMLEditorConfiguration();
	}

	private void initUI() {
		validateSchema.setSelection(xconfig.getValidateSchema());
		splitLineStyle
				.setSelection(xconfig.getTagStyle() == XMLAbstractEditor.TAG_INDENT);
		singleLineStyle
				.setSelection(xconfig.getTagStyle() == XMLAbstractEditor.TAG_NOT_INDENT);

		String value = xconfig.getProxyHost();
		if (value == null) {
			value = "";
		}
		proxyHostText.setText(value);

		value = xconfig.getProxyPort();
		if (value == null) {
			value = "";
		}
		proxyPortText.setText(value);

		StyleListModel listModel = new StyleListModel(xconfig.getStyleContext());
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
		xconfig = (XMLEditorConfiguration) config;
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());

		Form form = formToolkit.createForm(parent);
		form.setFont(parent.getFont());

		FormLayout formLayout = new FormLayout();
		form.getBody().setLayout(formLayout);

		// create the validate schema checkbox
		validateSchema = formToolkit.createButton(form.getBody(),
				LocalizedResources.applicationResources
						.getString("validate.schema"), SWT.CHECK);

		validateSchema.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (validateSchema.getSelection()
						&& !xconfig.getValidateSchema()) {
					xconfig.setValidateSchema(true);
					xconfig.setSettingsChanged(true);
				} else if (!validateSchema.getSelection()
						&& xconfig.getValidateSchema()) {
					xconfig.setValidateSchema(false);
					xconfig.setSettingsChanged(true);
				}
			}
		});

		FormData layoutData = new FormData();
		layoutData.left = new FormAttachment(0, 5);
		layoutData.top = new FormAttachment(0, 5);
		validateSchema.setLayoutData(layoutData);

		Group tagStyleGroup = new Group(form.getBody(), SWT.SHADOW_NONE);
		formToolkit.adapt(tagStyleGroup);
		tagStyleGroup.setText(XMLEspressoActivator.getResourceString("tag.style"));

		layoutData = new FormData();
		layoutData.top = new FormAttachment(validateSchema, 20);
		layoutData.right = new FormAttachment(100, -5);
		layoutData.left = new FormAttachment(0, 5);
		tagStyleGroup.setLayoutData(layoutData);

		tagStyleGroup.setLayout(new GridLayout(1, false));

		Label help = formToolkit.createLabel(tagStyleGroup, XMLEspressoActivator
				.getResourceString("tag.style.help"));

		splitLineStyle = formToolkit.createButton(tagStyleGroup,
				LocalizedResources.applicationResources
						.getString("tag.style.split.line"), SWT.RADIO);

		singleLineStyle = formToolkit.createButton(tagStyleGroup,
				LocalizedResources.applicationResources
						.getString("tag.style.single.line"), SWT.RADIO);

		splitLineStyle.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (singleLineStyle.getSelection()
						&& xconfig.getTagStyle() != XMLAbstractEditor.TAG_INDENT) {
					xconfig.setTagStyle(XMLAbstractEditor.TAG_INDENT);
					xconfig.setSettingsChanged(true);
				}
			}
		});

		singleLineStyle.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (singleLineStyle.getSelection()
						&& xconfig.getTagStyle() != XMLAbstractEditor.TAG_NOT_INDENT) {
					xconfig.setTagStyle(XMLAbstractEditor.TAG_NOT_INDENT);
					xconfig.setSettingsChanged(true);
				}
			}
		});

		Group proxyGroup = new Group(form.getBody(), SWT.SHADOW_NONE);
		formToolkit.adapt(proxyGroup);
		proxyGroup.setText(XMLEspressoActivator
				.getResourceString("internet.proxy.settings"));

		layoutData = new FormData();
		layoutData.top = new FormAttachment(tagStyleGroup, 20);
		layoutData.left = new FormAttachment(0, 5);
		layoutData.right = new FormAttachment(100, -5);
		proxyGroup.setLayoutData(layoutData);

		proxyGroup.setLayout(new GridLayout(2, false));

		help = formToolkit.createLabel(proxyGroup, XMLEspressoActivator
				.getResourceString("proxy.help"));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		help.setLayoutData(gd);

		formToolkit.createLabel(proxyGroup,
				LocalizedResources.applicationResources
						.getString("http.proxy.host"));

		proxyHostText = new Text(proxyGroup, SWT.BORDER);
		formToolkit.adapt(proxyHostText, true, true);

		proxyHostText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				xconfig.setProxyHost(proxyHostText.getText());
				xconfig.setSettingsChanged(true);
			}
		});

		formToolkit.createLabel(proxyGroup,
				LocalizedResources.applicationResources
						.getString("http.proxy.port"));
		proxyPortText = new Text(proxyGroup, SWT.BORDER);
		formToolkit.adapt(proxyPortText, true, true);

		proxyPortText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				xconfig.setProxyPort(proxyPortText.getText());
				xconfig.setSettingsChanged(true);
			}
		});

		Group elementGroup = new Group(form.getBody(), SWT.SHADOW_NONE);
		formToolkit.adapt(elementGroup);
		elementGroup.setText(XMLEspressoActivator
				.getResourceString("element.style"));

		layoutData = new FormData();
		layoutData.top = new FormAttachment(proxyGroup, 20);
		layoutData.left = new FormAttachment(0, 5);
		layoutData.right = new FormAttachment(100, -5);

		elementGroup.setLayoutData(layoutData);

		elementGroup.setLayout(new GridLayout(1, true));

		Composite tgroup = new Composite(elementGroup, SWT.NONE);
		formToolkit.adapt(tgroup);

		tgroup.setLayout(new FillLayout());

		selected = new Label(tgroup, SWT.CENTER);

		formToolkit.adapt(selected, true, true);
		
		fgButton = formToolkit.createButton(tgroup,
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
						xconfig.setSettingsChanged(true);
					}
				}
			}
		});

		bgButton = formToolkit.createButton(tgroup,
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
						xconfig.setSettingsChanged(true);
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

		ps.setValue(sb.toString(), xconfig.toXMLString());
		Properties systemSettings = System.getProperties();

		String proxyHost = xconfig.getProxyHost();
		if (proxyHost != null && proxyHost.trim().length() > 0) {
			systemSettings.put("http.proxyHost", proxyHost);
			systemSettings.put("proxyHost", proxyHost);
		} else {
			systemSettings.remove("http.proxyHost");
			systemSettings.remove("proxyHost");
		}

		String proxyPort = xconfig.getProxyPort();
		if (proxyPort != null && proxyPort.trim().length() > 0) {
			systemSettings.put("http.proxyPort", proxyPort);
			systemSettings.put("proxyPort", proxyPort);
		} else {
			systemSettings.remove("http.proxyPort");
			systemSettings.remove("proxyPort");
		}
		return true;
	}

	@Override
	protected void performDefaults() {
		xconfig = (XMLEditorConfiguration) getEditorConfiguration();
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
