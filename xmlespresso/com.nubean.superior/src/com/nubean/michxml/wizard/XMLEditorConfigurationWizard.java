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

package com.nubean.michxml.wizard;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.nubean.michutil.FontChooser;
import com.nubean.michutil.LocalizedResources;
import com.nubean.michutil.StyleItem;
import com.nubean.michutil.StyleListModel;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLEditorConfiguration;

public class XMLEditorConfigurationWizard extends JTabbedPane {

	private static final long serialVersionUID = 9008805451643144190L;
	private XMLEditorConfiguration editorConfig;

	public XMLEditorConfigurationWizard(XMLEditorConfiguration editorConfig) {
		super(JTabbedPane.BOTTOM);

		this.editorConfig = editorConfig;
		add(LocalizedResources.applicationResources.getString("http.proxy"),
				makeHttpProxyPanel());
		if (editorConfig.isIntegrated())
			add(LocalizedResources.applicationResources.getString("style"),
					makeStylePanel());
		StyleListModel listModel = new StyleListModel(editorConfig
				.getStyleContext());
		add(LocalizedResources.applicationResources.getString("settings"),
				makeSettingsPanel(listModel));
		if (editorConfig.isIntegrated())
			add(LocalizedResources.applicationResources.getString("options"),
					makeOptionsPanel());
	}

	private JPanel makeHttpProxyPanel() {
		JPanel proxyPanel = new JPanel();
		proxyPanel
				.setBorder(new TitledBorder(
						LocalizedResources.applicationResources
								.getString("http.proxy")));

		proxyPanel.setLayout(new GridLayout(2, 1));

		JPanel panel = new JPanel();
		JLabel phl = new JLabel(LocalizedResources.applicationResources
				.getString("http.proxy.host"));
		// proxyPanel.add(phl);
		JTextField phf = new JTextField(20);
		String proxyHost = System.getProperty("http.proxyHost");
		if (proxyHost != null) {
			phf.setText(proxyHost);
			editorConfig.setProxyHost(proxyHost);
		}

		panel.add(phl);
		panel.add(phf);

		proxyPanel.add(panel);

		phf.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent fe) {
				JTextField tf = (JTextField) fe.getSource();
				editorConfig.setProxyHost(tf.getText());
				editorConfig.setSettingsChanged(true);
			}

			public void focusGained(FocusEvent fe) {
			}
		});

		panel = new JPanel();
		JLabel ppl = new JLabel(LocalizedResources.applicationResources
				.getString("http.proxy.port"));
		panel.add(ppl);
		JTextField ppf = new JTextField(20);
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyPort != null) {
			ppf.setText(proxyPort);
			editorConfig.setProxyPort(proxyPort);
		}

		ppf.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent fe) {
				JTextField tf = (JTextField) fe.getSource();
				editorConfig.setProxyPort(tf.getText());
				editorConfig.setSettingsChanged(true);
			}

			public void focusGained(FocusEvent fe) {
			}
		});
		panel.add(ppf);

		proxyPanel.add(panel);

		return proxyPanel;
	}

	private JPanel makeStylePanel() {
		JPanel stylePanel = new JPanel();
		stylePanel.setBorder(new TitledBorder(
				LocalizedResources.applicationResources.getString("style")));

		stylePanel.setLayout(new GridLayout(2, 1));
		JRadioButton rb1 = new JRadioButton(
				LocalizedResources.applicationResources
						.getString("tag.style.single.line"));
		rb1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JRadioButton rb = (JRadioButton) e.getSource();
				if (rb.isSelected()
						&& editorConfig.getTagStyle() != XMLAbstractEditor.TAG_NOT_INDENT) {
					editorConfig.setTagStyle(XMLAbstractEditor.TAG_NOT_INDENT);
					editorConfig.setSettingsChanged(true);
				}
			}
		});
		rb1
				.setSelected(editorConfig.getTagStyle() == XMLAbstractEditor.TAG_NOT_INDENT);
		JRadioButton rb2 = new JRadioButton(
				LocalizedResources.applicationResources
						.getString("tag.style.split.line"));

		rb2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JRadioButton rb = (JRadioButton) e.getSource();
				if (rb.isSelected()
						&& editorConfig.getTagStyle() != XMLAbstractEditor.TAG_INDENT) {
					editorConfig.setTagStyle(XMLAbstractEditor.TAG_INDENT);
					editorConfig.setSettingsChanged(true);
				}
			}
		});
		rb2
				.setSelected(editorConfig.getTagStyle() == XMLAbstractEditor.TAG_INDENT);
		// JTextArea ta1 = new JTextArea("<tag1><tag2><tag2><tag1>");
		// JTextArea ta2 = new
		// JTextArea("<tag1>\n\t<tag2></tag2>\n</tag1>");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rb1);
		bg.add(rb2);
		stylePanel.add(rb1);
		// stylePanel.add(ta1);
		stylePanel.add(rb2);
		// stylePanel.add(ta2);
		return stylePanel;
	}

	private JPanel makeOptionsPanel() {
		JPanel prefsPanel = new JPanel();
		prefsPanel.setBorder(new TitledBorder(
				LocalizedResources.applicationResources.getString("options")));

		JCheckBox cb = new JCheckBox(LocalizedResources.applicationResources
				.getString("validate.schema"), editorConfig.getValidateSchema());
		cb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if (cb.isSelected() && !editorConfig.getValidateSchema()) {
					editorConfig.setValidateSchema(true);
					editorConfig.setSettingsChanged(true);
				} else if (!cb.isSelected() && editorConfig.getValidateSchema()) {
					editorConfig.setValidateSchema(false);
					editorConfig.setSettingsChanged(true);
				}
			}
		});
		prefsPanel.add(cb);
		return prefsPanel;
	}

	private JPanel makeSettingsPanel(ListModel listModel) {
		final JPanel fontPanel = new JPanel();
		fontPanel.setLayout(new GridLayout(1, 2));

		final JTextField tf = new JTextField(20);
		final JList itemList = new JList(listModel);
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList list = (JList) e.getSource();
				StyleItem sel = (StyleItem) list.getSelectedValue();
				tf.setFont(sel.getFont());
				tf.setText(sel.getTitle());
				tf.setBackground(sel.getBackground());
				tf.setForeground(sel.getForeground());
			}
		});
		JScrollPane sp = new JScrollPane(itemList);
		sp.setBorder(new TitledBorder(LocalizedResources.applicationResources
				.getString("elements")));

		fontPanel.add(sp);

		JPanel itemPanel = new JPanel();
		itemPanel.setLayout(new GridBagLayout());
		itemPanel.setBorder(new TitledBorder(
				LocalizedResources.applicationResources.getString("settings")));

		JButton changeFont = new JButton(
				LocalizedResources.applicationResources
						.getString("change.font"));
		changeFont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StyleItem sel = (StyleItem) itemList.getSelectedValue();
				if (sel != null && sel.getFont() != null) {
					Font font = FontChooser.showDialog(
							XMLEditorConfigurationWizard.this,
							LocalizedResources.applicationResources
									.getString("select.font"), sel.getFont());
					if (font != null) {
						sel.setFont(font);
						tf.setFont(sel.getFont());
						editorConfig.setSettingsChanged(true);
					}
				}
			}
		});

		JButton changeFg = new JButton(LocalizedResources.applicationResources
				.getString("change.foreground"));
		changeFg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StyleItem sel = (StyleItem) itemList.getSelectedValue();
				if (sel != null && sel.getForeground() != null) {
					Color fg = JColorChooser.showDialog(
							XMLEditorConfigurationWizard.this,
							LocalizedResources.applicationResources
									.getString("select.color"), sel
									.getForeground());
					if (fg != null) {
						sel.setForeground(fg);
						tf.setForeground(fg);
						editorConfig.setSettingsChanged(true);
					}
				}
			}
		});

		JButton changeBg = new JButton(LocalizedResources.applicationResources
				.getString("change.background"));
		changeBg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StyleItem sel = (StyleItem) itemList.getSelectedValue();
				if (sel != null && sel.getBackground() != null) {
					Color bg = JColorChooser.showDialog(
							XMLEditorConfigurationWizard.this,
							LocalizedResources.applicationResources
									.getString("select.color"), sel
									.getForeground());
					if (bg != null) {
						sel.setBackground(bg);
						tf.setBackground(bg);
						editorConfig.setSettingsChanged(true);
					}
				}
			}
		});
		itemPanel.add(tf, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		itemPanel.add(changeFont, new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		itemPanel.add(changeFg, new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		itemPanel.add(changeBg, new GridBagConstraints(0, 3, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		fontPanel.add(itemPanel);

		return fontPanel;
	}

}
