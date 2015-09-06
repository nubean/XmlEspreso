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
import java.awt.event.*;

import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.nubean.michbase.editor.IDEditor;
import com.nubean.michutil.LocalizedResources;

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

public class FindAndReplaceDialog extends JDialog {
	private static final long serialVersionUID = 7005644143065325654L;

	private IDEditor editor;

	private int start;

	private JButton findButton, replaceButton, replaceFindButton,
			replaceAllButton;

	private JCheckBox casecb, regexcb, wrapcb, incrementalcb;

	private JComboBox findComboBox, replaceComboBox;

	private DocumentListener documentListener;

	private JRadioButton forwardrb, backwardrb, allrb, selectedLinesrb;

	private JLabel status;

	public FindAndReplaceDialog(Component c, String title, boolean modal) {
		super(JOptionPane.getFrameForComponent(c), title, modal);
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setEditor(IDEditor editor) {
		this.editor = editor;
	}

	private void init() throws Exception {
		Container contentPane = getContentPane();
		JPanel panel = new JPanel();
		contentPane.setLayout(new BorderLayout());
		panel.setLayout(new GridLayout(4, 1));
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.add(panel, BorderLayout.CENTER);

		status = new JLabel();

		// The following few lines are used to register esc to close the
		// dialog
		ActionListener cancelKeyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((AbstractButton) e.getSource()).doClick();
			}
		};

		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		panel.registerKeyboardAction(cancelKeyAction, cancelKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		JPanel panel1 = new JPanel();
		panel.add(panel1);

		panel1.setLayout(new GridLayout(2, 2));

		JLabel findLabel = new JLabel(LocalizedResources.applicationResources
				.getString("find.label"));
		panel1.add(findLabel);

		findComboBox = new JComboBox();
		panel1.add(findComboBox);
		findComboBox.setEditable(true);

		JLabel replaceLabel = new JLabel(
				LocalizedResources.applicationResources
						.getString("replace.label"));
		panel1.add(replaceLabel);

		replaceComboBox = new JComboBox();
		panel1.add(replaceComboBox);
		replaceComboBox.setEditable(true);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayout(1, 2));
		panel.add(panel2);

		JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayout(2, 1));
		panel3.setBorder(new TitledBorder(
				LocalizedResources.applicationResources
						.getString("direction.title")));

		forwardrb = new JRadioButton(LocalizedResources.applicationResources
				.getString("forward.button"));
		panel3.add(forwardrb);
		forwardrb.setSelected(true);

		backwardrb = new JRadioButton(LocalizedResources.applicationResources
				.getString("backward.button"));
		panel3.add(backwardrb);

		ButtonGroup directionGroup = new ButtonGroup();
		directionGroup.add(forwardrb);
		directionGroup.add(backwardrb);
		panel2.add(panel3);

		JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayout(2, 1));
		panel4.setBorder(new TitledBorder(
				LocalizedResources.applicationResources
						.getString("scope.title")));

		allrb = new JRadioButton(LocalizedResources.applicationResources
				.getString("all.button"));
		allrb.setSelected(true);
		panel4.add(allrb);

		selectedLinesrb = new JRadioButton(
				LocalizedResources.applicationResources
						.getString("selectedLines.button"));
		panel4.add(selectedLinesrb);

		ButtonGroup scopeGroup = new ButtonGroup();
		scopeGroup.add(allrb);
		scopeGroup.add(selectedLinesrb);
		panel2.add(panel4);

		JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayout(2, 2));
		panel5.setBorder(new TitledBorder(
				LocalizedResources.applicationResources
						.getString("options.title")));

		casecb = new JCheckBox(LocalizedResources.applicationResources
				.getString("case.button"));
		panel5.add(casecb);

		wrapcb = new JCheckBox(LocalizedResources.applicationResources
				.getString("wrap.button"));
		panel5.add(wrapcb);

		regexcb = new JCheckBox(LocalizedResources.applicationResources
				.getString("regex.button"));
		regexcb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				incrementalcb.setEnabled(!regexcb.isSelected());
			}
		});

		panel5.add(regexcb);

		incrementalcb = new JCheckBox(LocalizedResources.applicationResources
				.getString("incremental.button"));
		panel5.add(incrementalcb);

		documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				find();
			}

			public void insertUpdate(DocumentEvent e) {
				find();
			}

			public void removeUpdate(DocumentEvent e) {
				find();
			}
		};

		incrementalcb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int stateChanged = e.getStateChange();

				if (stateChanged == ItemEvent.SELECTED) {
					JTextComponent editor = (JTextComponent) findComboBox
							.getEditor().getEditorComponent();
					editor.getDocument().addDocumentListener(documentListener);
				} else {
					JTextComponent editor = (JTextComponent) findComboBox
							.getEditor().getEditorComponent();
					editor.getDocument().removeDocumentListener(
							documentListener);
				}
			}
		});

		panel.add(panel5);

		JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayout(2, 2));

		findButton = new JButton(LocalizedResources.applicationResources
				.getString("find"));
		panel6.add(findButton);
		findButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				find();
			}
		});

		replaceFindButton = new JButton(LocalizedResources.applicationResources
				.getString("replace/find"));
		panel6.add(replaceFindButton);
		replaceFindButton.setEnabled(false);
		replaceFindButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replace();
				find();
			}
		});

		replaceButton = new JButton(LocalizedResources.applicationResources
				.getString("replace"));
		panel6.add(replaceButton);
		replaceButton.setEnabled(false);
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replace();
			}
		});

		replaceAllButton = new JButton(LocalizedResources.applicationResources
				.getString("replaceAll"));
		panel6.add(replaceAllButton);
		panel.add(panel6);
		replaceAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceAll();
			}
		});

		JPanel closePanel = new JPanel();
		closePanel.setLayout(new BorderLayout());
		JButton closeButton = new JButton(
				LocalizedResources.applicationResources.getString("close"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceButton.setEnabled(false);
				replaceFindButton.setEnabled(false);
				FindAndReplaceDialog.this.setVisible(false);
			}
		});
		closePanel.add(status, BorderLayout.WEST);
		closePanel.add(closeButton, BorderLayout.EAST);
		getContentPane().add(closePanel, BorderLayout.SOUTH);

		pack();
		setSize(new Dimension(350, 300));

	}

	public void setFindString(String findString) {
		if (findComboBox != null) {
			JTextComponent tc = (JTextComponent) findComboBox.getEditor()
					.getEditorComponent();
			tc.setText(findString);
		}
	}

	private void replaceAll() {
		editor.beginUndoEditSession();
		do {
			find();
			replace();
		} while (start >= 0);
		editor.endUndoEditSession();
	}

	public void replace() {
		editor.beginUndoEditSession();
		if (start >= 0) {
			String text = replaceComboBox.getEditor().getItem().toString();

			editor.replace(text);
			addItemToComboBox(replaceComboBox, text);
		}
		editor.endUndoEditSession();
	}

	public void find() {
		status.setText("");
		String text = findComboBox.getEditor().getItem().toString();

		start = editor.find(text, casecb.isSelected(), wrapcb.isSelected(),
				forwardrb.isSelected(), regexcb.isSelected(), allrb
						.isSelected(), incrementalcb.isSelected());

		if (start == -1) {
			status.setText(LocalizedResources.applicationResources
					.getString("string.notfound.label"));
			replaceButton.setEnabled(false);
			replaceFindButton.setEnabled(false);
		} else {
			replaceButton.setEnabled(true);
			replaceFindButton.setEnabled(true);
		}
		if (!incrementalcb.isSelected()) {
			addItemToComboBox(findComboBox, text);
		}
	}

	private void addItemToComboBox(JComboBox cb, String text) {

		if (text != null) {
			int count = cb.getItemCount();
			boolean found = false;
			for (int i = 0; i < count; i++) {
				if (cb.getItemAt(i).equals(text)) {
					cb.setSelectedIndex(i);
					found = true;
					break;
				}
			}
			if (!found) {
				cb.insertItemAt(text, 0);
				cb.setSelectedIndex(0);
			}
		}
	}

	public static FindAndReplaceDialog createDialog(Component c, String title,
			boolean modal) {

		FindAndReplaceDialog dialog = new FindAndReplaceDialog(c, title, modal);
		dialog.addWindowListener(dialog.new Closer());

		return dialog;
	}

	private class Closer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			replaceButton.setEnabled(false);
			replaceFindButton.setEnabled(false);
			Window w = e.getWindow();
			w.setVisible(false);
			editor.endFindReplaceSession();
		}

		public void windowOpened(WindowEvent e) {
			editor.beginFindReplaceSession();
		}
	}

}
