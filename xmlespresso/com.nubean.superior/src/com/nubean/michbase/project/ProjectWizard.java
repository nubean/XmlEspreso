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

package com.nubean.michbase.project;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.beans.*;
import javax.help.*;
import java.net.URL;

import com.nubean.michide.Encodings;
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

public class ProjectWizard extends JPanel {
	private String rootPath;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel mainPanel = new JPanel();

	JPanel buttonPanel = new JPanel();

	JButton backBtn;

	JButton nextBtn;

	JButton finishBtn = new JButton();

	JButton cancelButton = new JButton();

	JTabbedPane stepPane = new JTabbedPane();

	JPanel generalPanel = new JPanel();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JLabel nameLabel = new JLabel();

	JLabel pathLabel = new JLabel();

	JTextField nametf = new JTextField();

	JTextField pathtf = new JTextField();

	JButton browse;

	JEditorPane genInfoPane = new JEditorPane();

	JPanel pathsPanel = new JPanel();

	GridBagLayout gridBagLayout2 = new GridBagLayout();

	JLabel outputPathLabel = new JLabel();

	JLabel backupPathLabel = new JLabel();

	JLabel descLabel = new JLabel();

	JLabel workingDirLabel = new JLabel();

	JTextField outputpathtf = new JTextField();

	JTextField backuppathtf = new JTextField();

	JTextField workingdirtf = new JTextField();

	JButton browseOut;

	JButton browseBack;

	JButton browseWorking;

	JEditorPane jEditorPane1 = new JEditorPane();

	JPanel settingsPanel = new JPanel();

	GridBagLayout gridBagLayout3 = new GridBagLayout();

	JLabel encodingLabel = new JLabel();

	JComboBox encodingcb = new JComboBox(Encodings.encodings);

	JLabel titleLabel = new JLabel();

	JTextField titletf = new JTextField();

	JLabel authorLabel = new JLabel();

	JTextField authortf = new JTextField();

	JLabel versionLabel = new JLabel();

	JTextField versiontf = new JTextField();

	JLabel copyrightLabel = new JLabel();

	JScrollPane copyRightScrollPane = new JScrollPane();

	JTextArea copyrightta = new JTextArea();

	JTextArea descta = new JTextArea();

	private class NextAction extends AbstractAction {
		public NextAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				int index = stepPane.getSelectedIndex();
				stepPane.setSelectedIndex(index + 1);
				if (index + 1 == stepPane.getTabCount() - 1) {
					nextBtn.setEnabled(false);
					finishBtn.setEnabled(true);
				}
				stepPane.setEnabledAt(index + 1, true);
				backBtn.setEnabled(true);
			} catch (Exception ex) {
				ex.printStackTrace();
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
					backBtn.setEnabled(false);
				nextBtn.setEnabled(true);
				finishBtn.setEnabled(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public ProjectWizard() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JButton buildHelpButton() {
		JButton helpButton = null;
		HelpSet hs = null;
		try {
			URL hsUrl = HelpSet
					.findHelpSet(null, "javahelp/XMLEditorHelp.hs");
			hs = new HelpSet(null, hsUrl);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		HelpBroker hb = hs.createHelpBroker();
		helpButton = new JButton(LocalizedResources.applicationResources
				.getString("help"));
		CSH.setHelpIDString(helpButton, "CreatingANewProject");
		helpButton.addActionListener(new CSH.DisplayHelpFromSource(hb));
		return helpButton;
	}

	private void init() throws Exception {
		backBtn = new JButton(new BackAction(
				LocalizedResources.applicationResources.getString("back")));
		nextBtn = new JButton(new NextAction(
				LocalizedResources.applicationResources.getString("next")));
		browse = new JButton(new BrowseAction(pathtf,
				JFileChooser.DIRECTORIES_ONLY,
				LocalizedResources.applicationResources.getString("select"),
				LocalizedResources.applicationResources
						.getString("project.directory"), null));

		browseOut = new JButton(new BrowseAction(outputpathtf,
				JFileChooser.DIRECTORIES_ONLY,
				LocalizedResources.applicationResources.getString("select"),
				LocalizedResources.applicationResources
						.getString("project.document.directory"), null));
		browseBack = new JButton(new BrowseAction(backuppathtf,
				JFileChooser.DIRECTORIES_ONLY,
				LocalizedResources.applicationResources.getString("select"),
				LocalizedResources.applicationResources
						.getString("project.document.backup.directory"), null));
		browseWorking = new JButton(new BrowseAction(workingdirtf,
				JFileChooser.DIRECTORIES_ONLY,
				LocalizedResources.applicationResources.getString("select"),
				LocalizedResources.applicationResources
						.getString("project.working.directory"), null));

		this.setLayout(borderLayout1);
		this.setPreferredSize(new Dimension(600, 450));
		mainPanel.setPreferredSize(new Dimension(600, 400));
		buttonPanel.setPreferredSize(new Dimension(600, 50));
		backBtn.setEnabled(false);
		backBtn.setActionCommand("back");
		backBtn.setText(LocalizedResources.applicationResources
				.getString("<back"));
		nextBtn.setActionCommand("next");
		nextBtn.setText(LocalizedResources.applicationResources
				.getString("next>"));
		finishBtn.setEnabled(false);
		finishBtn.setActionCommand("finish");
		finishBtn.setText(LocalizedResources.applicationResources
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
		nametf.setText("myProject");
		CaretListener cl = new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				JTextField tf = (JTextField) e.getSource();
				String text = tf.getText();

				String outputPath = rootPath + File.separator + text
						+ File.separator + "docs";
				outputpathtf.setText(outputPath);

				String backupPath = rootPath + File.separator + text
						+ File.separator + "bak";
				backuppathtf.setText(backupPath);

				String workingPath = rootPath + File.separator + text;
				workingdirtf.setText(workingPath);

				if (text == null || text.length() == 0) {
					nextBtn.setEnabled(false);
				} else {
					nextBtn.setEnabled(true);
				}
			}
		};

		nametf.addCaretListener(cl);

		pathtf.setColumns(30);
		pathtf.setEditable(false);
		rootPath = System.getProperty("user.home") + File.separator + ".xmle4j";
		File file = new File(rootPath);
		if (!file.exists())
			file.mkdirs();
		String projectPath = rootPath + File.separator + "projects";
		file = new File(projectPath);
		if (!file.exists())
			file.mkdirs();

		pathtf.setText(projectPath);

		descta.setColumns(30);
		descta.setRows(5);
		browse.setActionCommand("browse");
		browse.setText(LocalizedResources.applicationResources
				.getString("browse"));
		genInfoPane.setOpaque(false);
		genInfoPane.setPreferredSize(new Dimension(220, 40));
		genInfoPane.setEditable(false);
		genInfoPane.setText(LocalizedResources.applicationResources
				.getString("project.name.path"));
		pathsPanel.setPreferredSize(new Dimension(600, 400));
		pathsPanel.setLayout(gridBagLayout2);
		outputPathLabel.setText(LocalizedResources.applicationResources
				.getString("output.path"));
		backupPathLabel.setText(LocalizedResources.applicationResources
				.getString("backup.path"));
		workingDirLabel.setText(LocalizedResources.applicationResources
				.getString("working.directory"));
		outputpathtf.setColumns(30);
		outputpathtf.setEditable(false);
		String outputPath = rootPath + File.separator + nametf.getText()
				+ File.separator + "docs";
		outputpathtf.setText(outputPath);

		backuppathtf.setColumns(30);
		backuppathtf.setEditable(false);

		String backupPath = rootPath + File.separator + nametf.getText()
				+ File.separator + "bak";
		backuppathtf.setText(backupPath);

		workingdirtf.setColumns(30);
		String workingPath = rootPath + File.separator + nametf.getText();
		workingdirtf.setText(workingPath);
		workingdirtf.setEditable(false);

		browseOut.setActionCommand("browse");
		browseOut.setText(LocalizedResources.applicationResources
				.getString("browse"));
		browseBack.setActionCommand("browse");
		browseBack.setText(LocalizedResources.applicationResources
				.getString("browse"));
		browseWorking.setActionCommand("browse");
		browseWorking.setText(LocalizedResources.applicationResources
				.getString("browse"));
		jEditorPane1.setMinimumSize(new Dimension(424, 50));
		jEditorPane1.setOpaque(false);
		jEditorPane1.setEditable(false);
		jEditorPane1.setText(LocalizedResources.applicationResources
				.getString("dir.paths"));
		settingsPanel.setPreferredSize(new Dimension(600, 400));
		settingsPanel.setLayout(gridBagLayout3);
		encodingLabel.setText(LocalizedResources.applicationResources
				.getString("encoding"));
		encodingcb.setMaximumRowCount(5);
		encodingcb.setSelectedIndex(0);

		titleLabel.setText(LocalizedResources.applicationResources
				.getString("project.title"));
		titletf.setColumns(30);
		authorLabel.setText(LocalizedResources.applicationResources
				.getString("author"));
		authortf.setColumns(30);
		versionLabel.setText(LocalizedResources.applicationResources
				.getString("version"));
		versiontf.setColumns(30);
		copyrightLabel.setText(LocalizedResources.applicationResources
				.getString("copyright"));
		copyrightta.setColumns(30);
		copyrightta.setRows(5);
		copyRightScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		copyRightScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(backBtn, null);
		buttonPanel.add(nextBtn, null);
		buttonPanel.add(finishBtn, null);
		buttonPanel.add(cancelButton, null);
		buttonPanel.add(buildHelpButton());
		mainPanel.add(stepPane, null);
		stepPane.add(generalPanel, LocalizedResources.applicationResources
				.getString("general"));
		generalPanel.add(nameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(pathLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(descLabel, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(nametf, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(pathtf, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(browse, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		generalPanel.add(genInfoPane, new GridBagConstraints(0, 0, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 0, 0));
		generalPanel.add(descta, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 0, 0));

		stepPane.add(pathsPanel, LocalizedResources.applicationResources
				.getString("paths"));
		pathsPanel.add(outputPathLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		pathsPanel.add(backupPathLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		pathsPanel.add(workingDirLabel, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		pathsPanel.add(outputpathtf, new GridBagConstraints(1, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		pathsPanel.add(backuppathtf, new GridBagConstraints(1, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		pathsPanel.add(workingdirtf, new GridBagConstraints(1, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		pathsPanel.add(browseOut, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		pathsPanel.add(browseBack, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		pathsPanel.add(browseWorking, new GridBagConstraints(2, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		pathsPanel.add(jEditorPane1, new GridBagConstraints(0, 0, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		stepPane.add(settingsPanel, LocalizedResources.applicationResources
				.getString("settings"));
		settingsPanel.add(encodingLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(encodingcb, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(titleLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(titletf, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 0, 0));
		settingsPanel.add(authorLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(authortf, new GridBagConstraints(1, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(versionLabel, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(versiontf, new GridBagConstraints(1, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(copyrightLabel, new GridBagConstraints(0, 4, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		settingsPanel.add(copyRightScrollPane, new GridBagConstraints(1, 5, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		copyRightScrollPane.getViewport().add(copyrightta, null);

		stepPane.setEnabledAt(1, false);
		stepPane.setEnabledAt(2, false);
		stepPane.setSelectedIndex(0);
	}

	public Project getProject() {
		Project pi = new Project(true);
		pi.name = nametf.getText();
		pi.author = authortf.getText();
		pi.backupPath = backuppathtf.getText();
		pi.copyright = copyrightta.getText();
		pi.description = descta.getText();
		pi.encoding = (String) encodingcb.getSelectedItem();
		pi.projectPath = pathtf.getText();
		pi.outputPath = outputpathtf.getText();
		pi.title = titletf.getText();
		pi.version = versiontf.getText();
		pi.workingPath = workingdirtf.getText();
		return pi;
	}

	public static Project showDialog(Component component, String title, Point p) {
		final ProjectWizard pane = new ProjectWizard();
		ProjectWizardTracker finish = new ProjectWizardTracker(pane);
		ProjectWizardCancelTracker cancel = new ProjectWizardCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, finish,
				cancel);
		dialog.addWindowListener(new ProjectWizardDialog.Closer());
		dialog.addComponentListener(new ProjectWizardDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return finish.getProject();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, ProjectWizard chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new ProjectWizardDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}
}

class ProjectWizardDialog extends JDialog {
	private ProjectWizard chooserPane;

	public ProjectWizardDialog(Component c, String title, boolean modal,
			ProjectWizard chooserPane, ActionListener okListener,
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
		chooserPane.finishBtn.addActionListener(okListener);

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

class ProjectWizardTracker implements ActionListener {
	ProjectWizard input;

	Project info;

	public ProjectWizardTracker(ProjectWizard c) {
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
		info = input.getProject();
		close();
	}

	public Project getProject() {
		return info;
	}
}

class ProjectWizardCancelTracker implements ActionListener {
	ProjectWizard input;

	public ProjectWizardCancelTracker(ProjectWizard c) {
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
