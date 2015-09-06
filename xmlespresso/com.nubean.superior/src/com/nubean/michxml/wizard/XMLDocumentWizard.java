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

import com.nubean.michbase.CatalogEntry;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.project.Project;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import com.nubean.michutil.*;
import com.nubean.michxml.XMLDocumentDescriptor;

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

public class XMLDocumentWizard extends JPanel {
	private static final long serialVersionUID = 1738026012244186028L;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel mainPanel = new JPanel();

	JPanel buttonPanel = new JPanel();

	JButton backButton;

	JButton nextButton;

	JButton finishButton = new JButton();

	JButton cancelButton = new JButton();

	JTabbedPane stepPane = new JTabbedPane();

	JPanel generalPanel = new JPanel();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JLabel nameLabel = new JLabel();

	JLabel pathLabel = new JLabel();

	JTextField nametf = new JTextField(80);

	JTextField pathtf = new JTextField(80);

	JButton browse;

	JPanel specPanel = new JPanel();

	GridBagLayout gridBagLayout2 = new GridBagLayout();

	JLabel schemaLabel = new JLabel();

	JLabel dtdLabel = new JLabel();

	JLabel publicLabel = new JLabel();

	JLabel descLabel = new JLabel();

	JTextField schematf = new JTextField(80);

	JTextField dtdtf = new JTextField(80);

	JTextField publictf = new JTextField(80);

	JButton browseSchema;

	JButton browseDtd;

	JPanel nsPanel = new JPanel();

	GridBagLayout gridBagLayout3 = new GridBagLayout();

	JLabel extLabel = new JLabel();

	JLabel rootLabel = new JLabel();

	JLabel encodingLabel = new JLabel();

	JComboBox encodingcb = new JComboBox(CommonUtils.encodings);

	JComboBox extcb = new JComboBox(CommonUtils.extensions);

	JTextField roottf = new JTextField(20);

	JLabel nsprefixLabel = new JLabel();

	JTextField nsprefixtf = new JTextField(20);

	JLabel nsuriLabel = new JLabel();

	JTextField nsuritf = new JTextField(80);

	JTextArea descta = new JTextArea(5, 30);

	JCheckBox standalonecb;

	private boolean useSchemaTargetNamespace;

	private class NextAction extends AbstractAction {
		public NextAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				int index = stepPane.getSelectedIndex();
				stepPane.setSelectedIndex(index + 1);
				String root = roottf.getText();
				String schema = schematf.getText();
				String dtd = dtdtf.getText();
				if (index + 1 == stepPane.getTabCount() - 1) {
					nextButton.setEnabled(false);
					if ((standalonecb.isSelected() || ((schema != null && schema
							.trim().length() > 0) || (dtd != null && dtd.trim()
							.length() > 0)))
							&& (root != null && root.trim().length() > 0))
						finishButton.setEnabled(true);
				}
				stepPane.setEnabledAt(index + 1, true);
				backButton.setEnabled(true);
				if ((root == null || root.trim().length() == 0)
						|| (!standalonecb.isSelected() && ((schema == null || schema
								.trim().length() == 0) && (dtd == null && dtd
								.trim().length() == 0)))) {
					nextButton.setEnabled(false);
					backButton.setEnabled(false);
					finishButton.setEnabled(false);
				}
			} catch (Exception ex) {
			}
		}
	}

	private class BackAction extends AbstractAction {
		public BackAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				int index = stepPane.getSelectedIndex();
				stepPane.setSelectedIndex(index - 1);
				if (index - 1 == 0)
					backButton.setEnabled(false);
				nextButton.setEnabled(true);
			} catch (Exception ex) {
			}
		}
	}

	public XMLDocumentWizard(Project pi, CatalogEntry catalogEntry) {
		try {
			init(pi, catalogEntry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isStandAlone() {
		return (schematf.getText() == null || schematf.getText().trim()
				.length() == 0)
				&& (dtdtf.getText() == null || dtdtf.getText().trim().length() == 0);
	}

	private void toggleStandalone(boolean standalone) {
		schematf.setEnabled(!standalone);
		dtdtf.setEnabled(!standalone);
		publictf.setEnabled(!standalone);

		schemaLabel.setEnabled(!standalone);
		dtdLabel.setEnabled(!standalone);
		publicLabel.setEnabled(!standalone);
	}

	private void init(Project pi, CatalogEntry catalogEntry) throws Exception {
		backButton = new JButton(new BackAction(
				LocalizedResources.applicationResources.getString("back")));
		nextButton = new JButton(new NextAction(
				LocalizedResources.applicationResources.getString("next")));
		browse = new JButton(new BrowseAction(pathtf,
				JFileChooser.DIRECTORIES_ONLY,
				LocalizedResources.applicationResources.getString("select"),
				LocalizedResources.applicationResources
						.getString("project.directory"), null));
		browseSchema = new JButton(new BrowseAction(schematf,
				JFileChooser.FILES_ONLY,
				LocalizedResources.applicationResources.getString("open"),
				LocalizedResources.applicationResources
						.getString("schema.file"), null));
		browseDtd = new JButton(new BrowseAction(dtdtf,
				JFileChooser.FILES_ONLY,
				LocalizedResources.applicationResources.getString("open"),
				LocalizedResources.applicationResources.getString("dtd.file"),
				null));
		standalonecb = new JCheckBox(LocalizedResources.applicationResources
				.getString("standalone"), true);

		this.setLayout(borderLayout1);

		backButton.setEnabled(false);
		backButton.setActionCommand("back");
		backButton.setText(LocalizedResources.applicationResources
				.getString("<back"));
		nextButton.setActionCommand("next");
		nextButton.setText(LocalizedResources.applicationResources
				.getString("next>"));
		finishButton.setEnabled(false);
		finishButton.setActionCommand("finish");
		finishButton.setText(LocalizedResources.applicationResources
				.getString("finish"));
		cancelButton.setActionCommand("cancel");
		cancelButton.setText(LocalizedResources.applicationResources
				.getString("cancel"));
		stepPane.setPreferredSize(new Dimension(600, 400));
		generalPanel.setPreferredSize(new Dimension(600, 380));
		generalPanel.setLayout(gridBagLayout1);
		nameLabel.setText(LocalizedResources.applicationResources
				.getString("name"));
		pathLabel.setText(LocalizedResources.applicationResources
				.getString("directory"));
		descLabel.setText(LocalizedResources.applicationResources
				.getString("description"));
		nametf.setColumns(30);
		nametf.setText(LocalizedResources.applicationResources
				.getString("document"));
		CaretListener cl = new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				switch (stepPane.getSelectedIndex()) {
				case 0:
					String name = nametf.getText();
					if (name == null || name.trim().length() == 0) {
						nextButton.setEnabled(false);
					} else {
						nextButton.setEnabled(true);
					}
					break;
				case 1:
					String root = roottf.getText();
					String schema = schematf.getText();
					String dtd = dtdtf.getText();
					if (root == null
							|| root.trim().length() == 0
							|| (!standalonecb.isSelected() && ((schema == null || schema
									.trim().length() == 0) && (dtd == null && dtd
									.trim().length() == 0)))) {
						nextButton.setEnabled(false);
					} else {
						nextButton.setEnabled(true);
					}
					break;
				default:
					break;
				}
			}
		};

		nametf.addCaretListener(cl);
		roottf.addCaretListener(cl);
		schematf.addCaretListener(cl);
		schematf.setText(catalogEntry.getProperty("schema"));
		dtdtf.setText(catalogEntry.getProperty("dtd"));
		publictf.setText(catalogEntry.getProperty("public"));
		roottf.setText(catalogEntry.getProperty("root"));

		pathtf.setColumns(30);
		pathtf.setText(pi.getOutputPath());
		descta.setText(catalogEntry.getProperty("description"));
		browse.setActionCommand("browse");
		browse.setText(LocalizedResources.applicationResources
				.getString("browse"));

		specPanel.setLayout(gridBagLayout2);
		standalonecb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				boolean state = cb.isSelected();
				browseSchema.setEnabled(!state);
				browseDtd.setEnabled(!state);

				String root = roottf.getText();
				String schema = schematf.getText();

				toggleStandalone(state);

				if (!state) {
					if ((root == null || root.trim().length() == 0
							|| schema == null || schema.trim().length() == 0)) {
						nextButton.setEnabled(false);
						backButton.setEnabled(false);
					}
				} else {
					nextButton.setEnabled(true);
					backButton.setEnabled(true);
				}
			}
		});
		browseSchema.setEnabled(false);
		browseDtd.setEnabled(false);
		schemaLabel.setText(LocalizedResources.applicationResources
				.getString("schema.path.uri"));
		schemaLabel.setEnabled(false);
		dtdLabel.setText(LocalizedResources.applicationResources
				.getString("dtd.path.uri"));
		dtdLabel.setEnabled(false);
		publicLabel.setText(LocalizedResources.applicationResources
				.getString("dtd.public.id"));
		publicLabel.setEnabled(false);
		schematf.setColumns(30);
		schematf.setEnabled(false);
		dtdtf.setColumns(30);
		dtdtf.setEnabled(false);
		publictf.setColumns(30);
		publictf.setEnabled(false);
		browseSchema.setActionCommand("browse");
		browseSchema.setText(LocalizedResources.applicationResources
				.getString("browse"));
		browseDtd.setActionCommand("browse");
		browseDtd.setText(LocalizedResources.applicationResources
				.getString("browse"));
		encodingLabel = new JLabel(LocalizedResources.applicationResources
				.getString("encoding"));
		encodingcb.setMaximumRowCount(5);
		encodingcb.setSelectedItem(pi.getEncoding());
		nsPanel.setPreferredSize(new Dimension(600, 400));
		nsPanel.setLayout(gridBagLayout3);
		extLabel.setText(LocalizedResources.applicationResources
				.getString("file.extension"));
		rootLabel.setText(LocalizedResources.applicationResources
				.getString("root.element"));
		roottf.setColumns(30);

		Object ext = catalogEntry.getProperty("ext");
		if (ext != null)
			extcb.setSelectedItem(ext);
		extcb.setMaximumRowCount(5);

		nsprefixLabel.setText(LocalizedResources.applicationResources
				.getString("target.namespace.prefix"));
		nsprefixtf.setColumns(30);
		nsprefixtf.setText(catalogEntry.getProperty("targetPrefix"));
		nsuriLabel.setText(LocalizedResources.applicationResources
				.getString("target.namespace.uri"));
		nsuritf.setColumns(30);
		nsuritf.setText(catalogEntry.getProperty("targetNamespace"));
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(backButton, null);
		buttonPanel.add(nextButton, null);
		buttonPanel.add(finishButton, null);
		buttonPanel.add(cancelButton, null);
		mainPanel.add(stepPane, null);
		stepPane.add(generalPanel, LocalizedResources.applicationResources
				.getString("general"));
		generalPanel.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(pathLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(descLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(nametf, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(pathtf, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(browse, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));

		generalPanel.add(descta, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 0, 0));

		stepPane.add(specPanel, LocalizedResources.applicationResources
				.getString("spec"));
		specPanel.add(standalonecb, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		specPanel.add(schemaLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		specPanel.add(dtdLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));

		specPanel.add(publicLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));

		specPanel.add(schematf, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));
		specPanel.add(dtdtf, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));
		specPanel.add(publictf, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));
		specPanel.add(browseSchema, new GridBagConstraints(3, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		specPanel.add(browseDtd, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));

		specPanel.add(extLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		specPanel.add(extcb, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));

		specPanel.add(encodingLabel, new GridBagConstraints(2, 4, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 15, 5, 5), 0, 0));
		specPanel.add(encodingcb, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));

		specPanel.add(rootLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		specPanel.add(roottf, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));

		stepPane.add(nsPanel, LocalizedResources.applicationResources
				.getString("target.namespace"));

		nsPanel.add(nsprefixLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		nsPanel.add(nsprefixtf, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));
		nsPanel.add(nsuriLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		nsPanel.add(nsuritf, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));

		JCheckBox cb = new JCheckBox(LocalizedResources.applicationResources
				.getString("schema.namespace"), false);
		cb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				useSchemaTargetNamespace = cb.isSelected();
				nsuritf.setEnabled(!useSchemaTargetNamespace);
				nsprefixtf.setEnabled(!useSchemaTargetNamespace);
				nsuritf.setEditable(!useSchemaTargetNamespace);
				nsprefixtf.setEditable(!useSchemaTargetNamespace);
			}
		});

		nsPanel.add(cb, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5,
						5, 5, 5), 0, 0));

		stepPane.setEnabledAt(1, false);
		stepPane.setEnabledAt(2, false);
		stepPane.setSelectedIndex(0);
		standalonecb.setSelected(this.isStandAlone());
		toggleStandalone(this.isStandAlone());
	}

	public DocumentDescriptor getDocumentDescriptor() {
		XMLDocumentDescriptor di = new XMLDocumentDescriptor();
		di.setName(nametf.getText());
		di.setPath(pathtf.getText());

		String schema = schematf.getText();
		if (schema != null
				&& ((schema.indexOf(":/") == 1) || (schema.indexOf(":\\") == 1) || schema
						.indexOf("/") == 0)) {
			schema = "file:///" + schema;
		}
		di.setSchemaLocation(schema);

		String dtd = dtdtf.getText();
		if (dtd != null
				&& ((dtd.indexOf(":/") == 1) || (dtd.indexOf(":\\") == 1) || dtd
						.indexOf("/") == 0)) {
			dtd = "file:///" + dtd;
		}
		di.setDtdLocation(dtd);

		di.setDescription(this.descta.getText());
		if (!useSchemaTargetNamespace) {
			di.setNSUri(nsuritf.getText());
			di.setNSPrefix(this.nsprefixtf.getText());
		} else {
			di.setNSUri(null);
			di.setNSPrefix(null);
		}
		di.setRootElement(this.roottf.getText());
		di.setDtdPublicId(this.publictf.getText());
		di.setEncoding(encodingcb.getSelectedItem().toString());
		di.setExt(extcb.getSelectedItem().toString());
		di.setUseSchemaTargetNamespace(this.useSchemaTargetNamespace);
		return di;
	}

	public static DocumentDescriptor showDialog(Component component,
			String title, Project pi, CatalogEntry catalogEntry, Point p) {
		final XMLDocumentWizard pane = new XMLDocumentWizard(pi, catalogEntry);
		XMLDocumentWizardTracker finish = new XMLDocumentWizardTracker(pane);
		XMLDocumentWizardCancelTracker cancel = new XMLDocumentWizardCancelTracker(
				pane);
		JDialog dialog = createDialog(component, title, true, pane, finish,
				cancel);
		dialog.addWindowListener(new XMLDocumentWizardDialog.Closer());
		dialog
				.addComponentListener(new XMLDocumentWizardDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog
		// down...

		DocumentDescriptor dd = finish.getDocumentDescriptor();
		if (dd != null)
			dd.setMimeType(catalogEntry.getMimeType());
		return dd;
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, XMLDocumentWizard chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new XMLDocumentWizardDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}
}

class XMLDocumentWizardDialog extends JDialog {
	private XMLDocumentWizard chooserPane;

	public XMLDocumentWizardDialog(Component c, String title, boolean modal,
			XMLDocumentWizard chooserPane, ActionListener okListener,
			ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);

		this.chooserPane = chooserPane;

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(chooserPane, BorderLayout.CENTER);

		// The following few lines are used to register esc to close the dialog
		ActionListener cancelKeyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((AbstractButton) e.getSource()).doClick();
			}
		};
		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		chooserPane.cancelButton.registerKeyboardAction(cancelKeyAction,
				cancelKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		chooserPane.cancelButton.addActionListener(cancelListener);
		chooserPane.finishButton.addActionListener(okListener);

		pack();
		setSize(new Dimension(650, 500));
	}

	static class Closer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter {
		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class XMLDocumentWizardTracker implements ActionListener {
	XMLDocumentWizard input;

	DocumentDescriptor info;

	public XMLDocumentWizardTracker(XMLDocumentWizard c) {
		input = c;
	}

	private void close() {
		Component parent = input.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		info = input.getDocumentDescriptor();
		close();
	}

	public DocumentDescriptor getDocumentDescriptor() {
		return info;
	}
}

class XMLDocumentWizardCancelTracker implements ActionListener {
	XMLDocumentWizard input;

	public XMLDocumentWizardCancelTracker(XMLDocumentWizard c) {
		input = c;
	}

	private void close() {
		Component parent = input.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		close();
	}
}