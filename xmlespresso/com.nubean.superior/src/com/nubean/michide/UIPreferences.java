/*
 * UIPreferences.java
 *
 * Created on November 5, 2002, 2:39 PM
 */

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

package com.nubean.michide;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.BorderUIResource.LineBorderUIResource;

/**
 * 
 * @author Ajay Vohra
 */
public class UIPreferences {

	public static void initUIDefaults() {
		FontUIResource PLAIN_FONT = new FontUIResource(new Font("Tahoma",
				Font.PLAIN, 11));
		LineBorderUIResource LINE_BORDER = new LineBorderUIResource(Color.GRAY,
				1);

		ColorUIResource WHITE = new ColorUIResource(Color.WHITE);
		ColorUIResource BLACK = new ColorUIResource(Color.BLACK);

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
			UIManager.put("Dialog.font", PLAIN_FONT);
			UIManager.put("CheckBox.font", PLAIN_FONT);
			UIManager.put("TitledBorder.font", PLAIN_FONT);

			UIManager.put("TextField.font", PLAIN_FONT);

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

			UIManager.put("TextArea.font", PLAIN_FONT);
		}

		if (WHITE != null) {
			UIManager.put("Viewport.background", WHITE);
			UIManager.put("TextPane.background", WHITE);
			UIManager.put("Tree.background", WHITE);
			UIManager.put("Table.background", WHITE);
			UIManager.put("TabbedPane.tabselectedbg", WHITE);
			UIManager.put("TabbedPane.tabselectedfg", BLACK);
		}
		UIManager.put("TextArea.border", LINE_BORDER);
	}
}