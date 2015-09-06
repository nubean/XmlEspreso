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

package com.nubean.michbase.wizard;

import javax.swing.*;

import com.nubean.michbase.CatalogEntry;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.factory.DocumentDescriptorFactory;
import com.nubean.michbase.project.Project;

import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;

import com.nubean.michutil.*;

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

public class DefaultDocumentWizard extends JPanel {
	private static final long serialVersionUID = 1298580874817528107L;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel mainPanel = new JPanel();

	JPanel buttonPanel = new JPanel();

	JButton finishButton = new JButton();

	JButton cancelButton = new JButton();

	JTabbedPane stepPane = new JTabbedPane();

	JPanel generalPanel = new JPanel();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JLabel nameLabel = new JLabel();

	JLabel pathLabel = new JLabel();

	JLabel descLabel = new JLabel();

	JTextField nametf = new JTextField();

	JTextField pathtf = new JTextField();

	JButton browse;

	JTextArea descta = new JTextArea();

	CatalogEntry catalogEntry;

	public DefaultDocumentWizard(Project pi, CatalogEntry catalogEntry) {
		try {
			init(pi, catalogEntry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init(Project pi, CatalogEntry catalogEntry) throws Exception {
		this.catalogEntry = catalogEntry;
		this.setLayout(borderLayout1);
		this.setPreferredSize(new Dimension(600, 450));
		mainPanel.setPreferredSize(new Dimension(600, 400));
		buttonPanel.setPreferredSize(new Dimension(600, 50));

		finishButton.setEnabled(false);
		finishButton.setActionCommand("ok");
		finishButton.setText(LocalizedResources.applicationResources
				.getString("ok"));
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
		nametf.setText("document");
		CaretListener cl = new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				String name = nametf.getText();
				if (name == null || name.length() == 0) {
					finishButton.setEnabled(false);
				} else {
					finishButton.setEnabled(true);
				}
			}
		};

		nametf.addCaretListener(cl);

		pathtf.setColumns(30);
		pathtf.setText(pi.getOutputPath());
		descta.setColumns(30);
		descta.setRows(5);
		descta.setText(catalogEntry.getProperty("description"));

		browse = new JButton(new BrowseAction(pathtf,
				JFileChooser.DIRECTORIES_ONLY,
				LocalizedResources.applicationResources.getString("select"),
				LocalizedResources.applicationResources
						.getString("project.directory"), null));

		browse.setActionCommand("browse");
		browse.setText(LocalizedResources.applicationResources
				.getString("browse"));

		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

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

		stepPane.setSelectedIndex(0);
	}

	public DocumentDescriptor getDocumentDescriptor() {
		DocumentDescriptor di = DocumentDescriptorFactory
				.createNewDocumentDescriptor(catalogEntry.getMimeType());
		di.setName(nametf.getText());
		di.setPath(pathtf.getText());

		di.setDescription(this.descta.getText());

		return di;
	}

	public static DocumentDescriptor showDialog(Component component,
			String title, Project pi, CatalogEntry catalogEntry, Point p) {
		final DefaultDocumentWizard pane = new DefaultDocumentWizard(pi,
				catalogEntry);
		DocumentWizardTracker finish = new DocumentWizardTracker(pane);
		DocumentWizardCancelTracker cancel = new DocumentWizardCancelTracker(
				pane);
		JDialog dialog = createDialog(component, title, true, pane, finish,
				cancel);
		dialog.addWindowListener(new DocumentWizardDialog.Closer());
		dialog.addComponentListener(new DocumentWizardDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		DocumentDescriptor dd = finish.getDocumentDescriptor();
		if (dd != null)
			dd.setMimeType(catalogEntry.getMimeType());
		return dd;
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, DefaultDocumentWizard chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new DocumentWizardDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}
}

class DocumentWizardDialog extends JDialog {
	private DefaultDocumentWizard chooserPane;

	public DocumentWizardDialog(Component c, String title, boolean modal,
			DefaultDocumentWizard chooserPane, ActionListener okListener,
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

class DocumentWizardTracker implements ActionListener {
	DefaultDocumentWizard input;

	DocumentDescriptor info;

	public DocumentWizardTracker(DefaultDocumentWizard c) {
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

class DocumentWizardCancelTracker implements ActionListener {
	DefaultDocumentWizard input;

	public DocumentWizardCancelTracker(DefaultDocumentWizard c) {
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