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

import javax.swing.*;

import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michutil.LocalizedResources;

import java.awt.*;
import java.awt.event.*;

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

public class ProjectSave extends JPanel {
	private static final long serialVersionUID = -7544550969472353133L;

	private Vector<DocumentDescriptor> list;

	JButton all, none, ok, cancel;

	private JList items;

	public ProjectSave(Vector<DocumentDescriptor> list) {
		this.list = list;
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		items = new JList(list);
		items.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		items.setCellRenderer(new ListItemRenderer());
		items.setVisibleRowCount(10);

		add(new JScrollPane(items), BorderLayout.CENTER);
		JPanel bpanel = new JPanel();
		bpanel.add(all = new JButton(LocalizedResources.applicationResources
				.getString("all")));
		all.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});
		bpanel.add(none = new JButton(LocalizedResources.applicationResources
				.getString("none")));
		none.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNone();
			}
		});
		bpanel.add(ok = new JButton(LocalizedResources.applicationResources
				.getString("ok")));
		bpanel.add(cancel = new JButton(LocalizedResources.applicationResources
				.getString("cancel")));
		add(bpanel, BorderLayout.SOUTH);
	}

	public void selectAll() {
		items.setSelectionInterval(0, list.size() - 1);
	}

	public void selectNone() {
		items.clearSelection();
	}

	public Vector<DocumentDescriptor> getSelected() {
		Vector<DocumentDescriptor> sel = new Vector<DocumentDescriptor>();
		int[] si = items.getSelectedIndices();
		int count = (si != null ? si.length : 0);
		for (int i = 0; i < count; i++) {
			sel.add(list.elementAt(si[i]));
		}
		return sel;
	}

	public static Vector<DocumentDescriptor> showDialog(Component component,
			String title, Point p, Vector<DocumentDescriptor> list) {
		final ProjectSave pane = new ProjectSave(list);
		ProjectSaveTracker finish = new ProjectSaveTracker(pane);
		ProjectSaveCancelTracker cancel = new ProjectSaveCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, finish,
				cancel);
		dialog.addWindowListener(new ProjectSaveDialog.Closer());
		dialog.addComponentListener(new ProjectSaveDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return finish.getSelected();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, ProjectSave listChooser, ActionListener okListener,
			ActionListener cancelListener) {

		return new ProjectSaveDialog(c, title, modal, listChooser, okListener,
				cancelListener);
	}
}

class ProjectSaveDialog extends JDialog {
	private static final long serialVersionUID = 6544731199994891738L;

	public ProjectSaveDialog(Component c, String title, boolean modal,
			ProjectSave listChooser, ActionListener okListener,
			ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(listChooser, BorderLayout.CENTER);

		// The following few lines are used to register esc to close the dialog
		ActionListener cancelKeyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((AbstractButton) e.getSource()).doClick();
			}
		};
		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		listChooser.cancel.registerKeyboardAction(cancelKeyAction,
				cancelKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		listChooser.cancel.addActionListener(cancelListener);
		listChooser.ok.addActionListener(okListener);

		pack();
		setSize(new Dimension(400, 200));
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

class ProjectSaveTracker implements ActionListener {
	ProjectSave input;

	Vector<DocumentDescriptor> info;

	public ProjectSaveTracker(ProjectSave c) {
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
		info = input.getSelected();
		close();
	}

	public Vector<DocumentDescriptor> getSelected() {
		return info;
	}
}

class ProjectSaveCancelTracker implements ActionListener {
	ProjectSave input;

	public ProjectSaveCancelTracker(ProjectSave c) {
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

class ListItemRenderer extends JCheckBox implements ListCellRenderer {
	private static final long serialVersionUID = -957894889676452927L;

	public ListItemRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		this.setText(value.toString());
		this.setSelected(isSelected);
		return this;
	}
}