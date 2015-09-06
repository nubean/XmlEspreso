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

package com.nubean.michxml.design;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.w3c.dom.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.nubean.michide.HtmlPanel;
import com.nubean.michutil.*;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.TypeDefinition;
import com.nubean.michxml.XMLAbstractEditor;

/**
 * <p>
 * Title: Michigan XML Editor
 * </p>
 * <p>
 * Description: This edits an XML document based on an XML schema.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: Nubean LLC
 * </p>
 * 
 * @author Ajay Vohra
 * @version 1.0
 */

public class SimpleTypePanel extends JPanel {
	private static final long serialVersionUID = -1932247387912918146L;

	private String label;

	private Component comp;

	private JCheckBox cdata = new JCheckBox("CDATA");

	private JButton ok, apply, cancel;

	private XMLAbstractEditor editor;

	public SimpleTypePanel(SchemaNode simpleType, XMLAbstractEditor editor) {
		this.editor = editor;
		JComponent htmlView = null;
		String documentation = simpleType.getDocumentation();
		if (documentation != null)
			htmlView = new HtmlPanel(documentation, IconLoader.backIcon,
					IconLoader.homeIcon);

		if (simpleType.getTagName() != null)
			label = simpleType.getTagName();

		Node textNode = editor.getSimpleTypeNode().getTextNode();
		if (textNode == null) {
			textNode = editor.getSimpleTypeNode().getCDATANode();
			if (textNode != null) {
				cdata.setSelected(true);
				cdata.setEnabled(false);
			} else {
				cdata.setEnabled(true);
			}
		} else {
			cdata.setSelected(false);
			cdata.setEnabled(false);
		}

		String curValue = (textNode != null ? textNode.getNodeValue() : null);

		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new TitledBorder(label));
		TypeDefinition typeDef = simpleType.getTypeDef();

		JPanel compPanel = new JPanel();
		if (typeDef.getEnumerations() != null
				&& typeDef.getEnumerations().size() > 0) {
			Vector<String> enums = typeDef.getEnumerations();
			JComboBox cb = new JComboBox(enums);
			comp = cb;
			cb.setEditable(false);
			cb.setMaximumRowCount(5);
			compPanel.add(comp);
		} else if (typeDef.getBase().equals("boolean")) {
			JCheckBox cb = new JCheckBox(label);
			cb.setSelected(curValue != null && curValue.equals("true"));
			comp = cb;
			compPanel.add(comp);
		} else {
			JTextArea re = new RETextArea(curValue, 20, 40, typeDef);
			comp = re;
			compPanel.add(new JScrollPane(comp,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		}

		panel.add(compPanel, BorderLayout.CENTER);
		apply = new JButton(new SimpleTypeApplyAction(
				LocalizedResources.applicationResources.getString("apply"),
				comp));

		cancel = new JButton(new SimpleTypeCancelAction(
				LocalizedResources.applicationResources.getString("cancel")));
		ok = new JButton(new SimpleTypeOkAction(
				LocalizedResources.applicationResources.getString("ok"), comp));

		JPanel bpanel = new JPanel();
		bpanel.add(ok);
		bpanel.add(cancel);
		bpanel.add(apply);

		panel.add(bpanel, BorderLayout.SOUTH);

		JPanel cbpanel = new JPanel();
		cbpanel.add(cdata);

		panel.add(cbpanel, BorderLayout.NORTH);

		if (htmlView != null) {
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					panel, htmlView);
			splitPane.setContinuousLayout(true);
			splitPane.setDividerLocation(200);
			add(splitPane, BorderLayout.CENTER);
		} else
			add(panel, BorderLayout.CENTER);

	}

	private class SimpleTypeCancelAction extends AbstractAction {
		private static final long serialVersionUID = -5485284776697039182L;

		public SimpleTypeCancelAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				editor.setTreeSelectionPath(editor.getSimpleTypePath()
						.getParentPath());
			} catch (Exception ex) {

			}
		}
	}

	private class SimpleTypeApplyAction extends AbstractAction {
		private static final long serialVersionUID = -3873806179351823059L;

		public SimpleTypeApplyAction(String name, Object text) {
			super(name);
			this.putValue("text", text);
		}

		public void actionPerformed(ActionEvent e) {
			Component tf = (Component) this.getValue("text");
			String value = "";

			if (tf instanceof JTextComponent)
				value = ((JTextComponent) tf).getText();
			else if (tf instanceof JComboBox)
				value = (String) ((JComboBox) tf).getSelectedItem();
			else if (tf instanceof JCheckBox) {
				JCheckBox cb = (JCheckBox) tf;
				value = (cb.isSelected() ? "true" : "false");
			}
			if (cdata.isSelected())
				editor.insertCDATAInSimpleType(value);
			else
				editor.insertTextInSimpleType(value);
		}
	}

	private class SimpleTypeOkAction extends AbstractAction {
		private static final long serialVersionUID = -5823719599582789379L;

		public SimpleTypeOkAction(String name, Object text) {
			super(name);
			this.putValue("text", text);
		}

		public void actionPerformed(ActionEvent e) {
			Component tf = (Component) this.getValue("text");
			String value = "";

			if (tf instanceof JTextComponent)
				value = ((JTextComponent) tf).getText();
			else if (tf instanceof JComboBox)
				value = (String) ((JComboBox) tf).getSelectedItem();
			else if (tf instanceof JCheckBox) {
				JCheckBox cb = (JCheckBox) tf;
				value = (cb.isSelected() ? "true" : "false");
			}
			if (cdata.isSelected())
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