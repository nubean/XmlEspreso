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

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.EditorConfiguration;
import com.nubean.xmlespresso.XMLEspressoActivator;

public abstract class EditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public EditorPreferencePage() {
		super();
	}

	public EditorPreferencePage(String title) {
		super(title);
	}

	public EditorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected abstract String getMimeType();

	protected abstract EditorConfiguration getEditorConfiguration();

	protected abstract Control createControl(Composite parent,
			EditorConfiguration config);

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		initUIDefaults(parent);

		IPreferenceStore ps = XMLEspressoActivator.getDefault()
				.getPreferenceStore();

		StringBuffer sb = new StringBuffer(XMLEspressoActivator.CONFIG_PREF);
		sb.append(":").append(getMimeType());

		String xml = ps.getString(sb.toString());

		EditorConfiguration config = null;
		if (xml != null && xml.trim().length() > 0) {
			config = (EditorConfiguration) CommonUtils.deserialize(xml);
		} else {
			config = getEditorConfiguration();
		}

		return createControl(parent, config);

	}

	protected abstract void performDefaults();
	
	public abstract boolean performOk();

	public abstract void performApply();

	private void initUIDefaults(Composite parent) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Font defaultFont = JFaceResources.getDefaultFont();
		FontData defaultFontData = defaultFont.getFontData()[0];

		java.awt.Font font = new java.awt.Font(defaultFontData.getName(),
				java.awt.Font.PLAIN, 11);

		FontUIResource PLAIN_FONT = new FontUIResource(font);

		Font dialogFont = JFaceResources.getDialogFont();
		FontData dialogFontData = dialogFont.getFontData()[0];

		font = new java.awt.Font(dialogFontData.getName(), java.awt.Font.PLAIN,
				11);

		FontUIResource DIALOG_FONT = new FontUIResource(font);

		Font textFont = JFaceResources.getTextFont();
		FontData textFontData = textFont.getFontData()[0];

		font = new java.awt.Font(textFontData.getName(), java.awt.Font.PLAIN,
				11);

		FontUIResource TEXT_FONT = new FontUIResource(font);

		if (PLAIN_FONT != null) {
			UIManager.put("Table.font", PLAIN_FONT);
			UIManager.put("Tree.font", PLAIN_FONT);
			UIManager.put("Label.font", PLAIN_FONT);
			UIManager.put("Button.font", PLAIN_FONT);
			UIManager.put("ToolBar.font", PLAIN_FONT);
			UIManager.put("TabbedPane.font", PLAIN_FONT);

			UIManager.put("Panel.font", PLAIN_FONT);
			UIManager.put("Menu.font", PLAIN_FONT);
			UIManager.put("CheckBoxMenuItem.font", PLAIN_FONT);
			UIManager.put("RadioButtonMenuItem.font", PLAIN_FONT);
			UIManager.put("MenuItem.font", PLAIN_FONT);
			UIManager.put("ComboBox.font", PLAIN_FONT);
			UIManager.put("List.font", PLAIN_FONT);
			UIManager.put("Dialog.font", DIALOG_FONT);
			UIManager.put("CheckBox.font", PLAIN_FONT);
			UIManager.put("TitledBorder.font", PLAIN_FONT);

			UIManager.put("TextField.font", TEXT_FONT);

			UIManager.put("Table.font", PLAIN_FONT);
			UIManager.put("Tree.font", PLAIN_FONT);
			UIManager.put("Label.font", PLAIN_FONT);
			UIManager.put("Button.font", PLAIN_FONT);
			UIManager.put("ToolBar.font", PLAIN_FONT);
			UIManager.put("TabbedPane.font", PLAIN_FONT);
			UIManager.put("Panel.font", PLAIN_FONT);
			UIManager.put("Menu.font", PLAIN_FONT);
			UIManager.put("CheckBoxMenuItem.font", PLAIN_FONT);
			UIManager.put("RadioButtonMenuItem.font", PLAIN_FONT);
			UIManager.put("MenuItem.font", PLAIN_FONT);

			UIManager.put("TextArea.font", TEXT_FONT);
		}
		org.eclipse.swt.graphics.Color color = parent.getBackground();

		java.awt.Color background = new Color(color.getRed(), color.getGreen(),
				color.getBlue());

		if (background != null) {
			UIManager.put("Table.background", background);
			UIManager.put("Tree.background", background);
			UIManager.put("Label.background", background);
			UIManager.put("Button.background", background);
			UIManager.put("ToolBar.background", background);
			UIManager.put("TabbedPane.background", background);

			UIManager.put("Panel.background", background);
			UIManager.put("Menu.background", background);
			UIManager.put("CheckBoxMenuItem.background", background);
			UIManager.put("RadioButtonMenuItem.background", background);
			UIManager.put("MenuItem.background", background);
			UIManager.put("ComboBox.background", background);
			UIManager.put("List.background", Color.white);
			UIManager.put("Dialog.background", background);
			UIManager.put("CheckBox.background", background);
			UIManager.put("TitledBorder.background", background);

			UIManager.put("TextField.background", Color.white);

			UIManager.put("Table.background", background);
			UIManager.put("Tree.background", Color.white);
			UIManager.put("Label.background", background);
			UIManager.put("Button.background", background);
			UIManager.put("ToolBar.background", background);
			UIManager.put("TabbedPane.background", background);
			UIManager.put("Panel.background", background);
			UIManager.put("Menu.background", background);
			UIManager.put("CheckBoxMenuItem.background", background);
			UIManager.put("RadioButtonMenuItem.background", background);
			UIManager.put("MenuItem.background", background);

			UIManager.put("TextArea.background", Color.white);
		}
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}