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
import javax.swing.text.*;
import javax.swing.border.*;
import java.util.*;
import javax.help.*;

import com.nubean.michbase.EditorConfiguration;
import com.nubean.michutil.LocalizedResources;
import com.nubean.wizard.factory.EditorConfigurationWizardFactory;

import java.net.URL;

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

public class ProjectConfigurationWizard extends JPanel {
	private ProjectConfiguration copy;

	private ProjectConfiguration orig;

	JButton okButton, applyButton, cancelButton;

	private Project project;

	private JLabel status;

	public ProjectConfiguration getProjectConfiguration() {
		return copy;
	}

	public void apply() {
		Cursor cursor = this.getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (copy.isSettingsChanged() && project != null) {
			project.configureProject(copy);
		}
		copy.setSettingsChanged(false);

		setCursor(cursor);
		status.setText(LocalizedResources.applicationResources
				.getString("apply.succesful"));
	}

	public ProjectConfigurationWizard(Project project) {
		try {

			this.project = project;
			this.orig = project.getProjectConfiguration();
			copy = (ProjectConfiguration) orig.clone();
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
		helpButton = new JButton("Help");
		CSH.setHelpIDString(helpButton, LocalizedResources.applicationResources
				.getString("configuring.project.properties"));
		helpButton.addActionListener(new CSH.DisplayHelpFromSource(hb));
		return helpButton;
	}

	private void init() throws Exception {
		JPanel mainPanel = new JPanel();
		JTabbedPane stepPane = new JTabbedPane();
		JPanel buttonPanel = new JPanel();
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.add(buttonPanel, BorderLayout.CENTER);
		Box box = Box.createHorizontalBox();
		box.add(status = new JLabel(LocalizedResources.applicationResources
				.getString("ready")));
		box.add(Box.createHorizontalGlue());

		lowerPanel.add(box, BorderLayout.SOUTH);

		okButton = new JButton(LocalizedResources.applicationResources
				.getString("ok"));
		buttonPanel.add(okButton);

		cancelButton = new JButton(LocalizedResources.applicationResources
				.getString("cancel"));
		buttonPanel.add(cancelButton);
		applyButton = new JButton(LocalizedResources.applicationResources
				.getString("apply"));
		buttonPanel.add(applyButton);
		buttonPanel.add(buildHelpButton());

		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new GridBagLayout());

		mainPanel.setLayout(new GridBagLayout());
		mainPanel.add(stepPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		mainPanel.add(lowerPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		stepPane.add(generalPanel, LocalizedResources.applicationResources
				.getString("general"));

		JPanel tabSizePanel = new JPanel();
		tabSizePanel.setLayout(new BorderLayout());
		tabSizePanel.setBorder(new TitledBorder("Tab Size"));
		String[] tabs = { "1", "2", "4", "8"};
		JComboBox tcb = new JComboBox(tabs);
		tcb.setEditable(true);
		tcb.getEditor().setItem(orig.getTabsize());
		
		
		tabSizePanel.add(tcb, BorderLayout.CENTER);
		tcb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String tsize = (String)cb.getEditor().getItem();
				copy.setTabsize(tsize);
				copy.setSettingsChanged(true);
			}
		});
		
		JPanel keymapPanel = new JPanel();
		keymapPanel.setLayout(new BorderLayout());
		keymapPanel.setBorder(new TitledBorder("Keymap"));
		String[] keymaps = { "Default", "Emacs" };
		JComboBox cb = new JComboBox(keymaps);
		if (orig.getKeymap().equals(keymaps[0]))
			cb.setSelectedIndex(0);
		else
			cb.setSelectedIndex(1);
		keymapPanel.add(cb, BorderLayout.CENTER);
		cb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String kmap = (String) cb.getSelectedItem();
				copy.setKeymap(kmap);
				copy.setSettingsChanged(true);
			}
		});

		JPanel backupPanel = new JPanel();
		backupPanel.setLayout(new GridBagLayout());
		JCheckBox backupcb = new JCheckBox(
				LocalizedResources.applicationResources.getString("backup"),
				copy.isBackup());
		copy.setBackupInterval(Math.min(60, copy.getBackupInterval()));
		final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 60, copy
				.getBackupInterval());
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider s = (JSlider) e.getSource();
				copy.setBackupInterval(s.getValue());
			}
		});
		slider.setEnabled(copy.isBackup());
		backupcb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				copy.setBackup(cb.isSelected());
				slider.setEnabled(copy.isBackup());
			}
		});

		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setPaintTrack(true);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		Hashtable labels = new Hashtable();
		labels.put(new Integer(0), new JLabel("0"));
		labels.put(new Integer(30), new JLabel("30"));
		labels.put(new Integer(60), new JLabel("60 Min"));

		slider.setName(LocalizedResources.applicationResources
				.getString("backup.interval"));

		slider.setLabelTable(labels);

		backupPanel.add(backupcb, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		backupPanel.add(slider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		backupPanel.setBorder(new TitledBorder(
				LocalizedResources.applicationResources.getString("backup")));

		
		generalPanel.add(keymapPanel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		
		generalPanel.add(tabSizePanel, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		generalPanel.add(backupPanel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));

		Set keys = copy.getEditorConfigurations().keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			EditorConfiguration value = (EditorConfiguration) copy
					.getEditorConfigurations().get(key);
			String title = value.getTitle();
			Component view = EditorConfigurationWizardFactory
					.newEditorConfiguration(value);
			stepPane.add(title, view);
		}
		setLayout(new GridBagLayout());
		add(mainPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
	}

	public static ProjectConfiguration showDialog(Component component,
			String title, Point p, Project project) {
		final ProjectConfigurationWizard pane = new ProjectConfigurationWizard(
				project);
		ProjectConfigurationTracker finish = new ProjectConfigurationTracker(
				pane);
		ProjectConfigurationApplier apply = new ProjectConfigurationApplier(
				pane);
		ProjectConfigurationCancelTracker cancel = new ProjectConfigurationCancelTracker(
				pane);
		JDialog dialog = createDialog(component, title, true, pane, finish,
				cancel, apply);
		dialog.addWindowListener(new ProjectConfigurationDialog.Closer());
		dialog
				.addComponentListener(new ProjectConfigurationDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return finish.getProjectConfiguration();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, ProjectConfigurationWizard chooserPane,
			ActionListener okListener, ActionListener cancelListener,
			ActionListener applyListener) {

		return new ProjectConfigurationDialog(c, title, modal, chooserPane,
				okListener, cancelListener, applyListener);
	}
}

class ProjectConfigurationDialog extends JDialog {
	private ProjectConfigurationWizard chooserPane;

	public ProjectConfigurationDialog(Component c, String title, boolean modal,
			ProjectConfigurationWizard chooserPane, ActionListener okListener,
			ActionListener cancelListener, ActionListener applyListener) {
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
		chooserPane.okButton.addActionListener(okListener);
		chooserPane.applyButton.addActionListener(applyListener);

		pack();
		setSize(new Dimension(500, 350));
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

class ProjectConfigurationTracker implements ActionListener {
	ProjectConfigurationWizard tool;

	ProjectConfiguration props;

	public ProjectConfigurationTracker(ProjectConfigurationWizard c) {
		tool = c;
	}

	private void close() {
		Component parent = tool.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		tool.apply();
		props = tool.getProjectConfiguration();
		close();
	}

	public ProjectConfiguration getProjectConfiguration() {
		return props;
	}
}

class ProjectConfigurationApplier implements ActionListener {
	ProjectConfigurationWizard tool;

	public ProjectConfigurationApplier(ProjectConfigurationWizard c) {
		tool = c;
	}

	public void actionPerformed(ActionEvent e) {
		tool.apply();
	}
}

class ProjectConfigurationCancelTracker implements ActionListener {
	ProjectConfigurationWizard input;

	public ProjectConfigurationCancelTracker(ProjectConfigurationWizard c) {
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