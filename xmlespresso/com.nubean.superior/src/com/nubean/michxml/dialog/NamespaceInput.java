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

package com.nubean.michxml.dialog;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.Namespace;

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

public class NamespaceInput extends JPanel {
	private static final long serialVersionUID = -2584072726057050294L;

	protected JComboBox prefixComboBox, uriComboBox;

	protected Namespace ns;

	/**
	 * constructor with no arguments
	 */
	public NamespaceInput(Object[] prefix, Object[] uri) {
		super();
		ns = new Namespace("", "");
		init(prefix, uri);
	}

	private void init(Object[] prefix, Object[] uri) {
		setLayout(new GridBagLayout());

		JLabel prefixl = new JLabel(LocalizedResources.applicationResources
				.getString("namespace.prefix"));
		prefixComboBox = new JComboBox(prefix);
		prefixComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = prefixComboBox.getSelectedIndex();
				if (i > 0)
					uriComboBox.setSelectedIndex(i);
			}
		});
		prefixComboBox.setEditable(true);

		JLabel uril = new JLabel(LocalizedResources.applicationResources
				.getString("namespace.uri"));
		uriComboBox = new JComboBox(uri);
		uriComboBox.setEditable(true);
		uriComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = uriComboBox.getSelectedIndex();
				if (i > 0)
					prefixComboBox.setSelectedIndex(i);
			}
		});

		Insets inset = new Insets(5, 10, 5, 10);
		GridBagConstraints constr = new java.awt.GridBagConstraints(0, 0, 1, 1,
				0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				inset, 0, 0);
		add(prefixl, constr);

		constr = new java.awt.GridBagConstraints(1, 0, 1, 1, 1, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(prefixComboBox, constr);

		constr = new java.awt.GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(uril, constr);

		constr = new java.awt.GridBagConstraints(1, 1, 1, 1, 1, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(uriComboBox, constr);

	}

	public Namespace getCurrentNamespace() {
		ns.prefix = (String) prefixComboBox.getSelectedItem();
		ns.uri = (String) uriComboBox.getSelectedItem();
		return ns;
	}

	public static Namespace showDialog(Component component, String title,
			Object[] prefix, Object[] uri, Point p) {
		final NamespaceInput pane = new NamespaceInput(prefix, uri);
		NamespaceTracker ok = new NamespaceTracker(pane);
		NamespaceCancelTracker cancel = new NamespaceCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new NamespaceInputDialog.Closer());
		dialog.addComponentListener(new NamespaceInputDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getCurrentNamespace();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, NamespaceInput chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new NamespaceInputDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}

}

class NamespaceInputDialog extends JDialog {
	private static final long serialVersionUID = 147297954074187454L;
	public NamespaceInputDialog(Component c, String title, boolean modal,
			NamespaceInput chooserPane, ActionListener okListener,
			ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		String okString = LocalizedResources.applicationResources
				.getString("ok");
		String cancelString = LocalizedResources.applicationResources
				.getString("cancel");

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(chooserPane, BorderLayout.CENTER);

		/*
		 * Create Lower button panel
		 */
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));

		JButton okButton = new JButton(okString);
		getRootPane().setDefaultButton(okButton);
		okButton.setActionCommand(LocalizedResources.applicationResources
				.getString("ok"));
		if (okListener != null) {
			okButton.addActionListener(okListener);
		}
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPane.add(okButton);

		JButton cancelButton = new JButton(cancelString);

		// The following few lines are used to register esc to close the dialog
		ActionListener cancelKeyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((AbstractButton) e.getSource()).doClick();
			}
		};
		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		cancelButton.registerKeyboardAction(cancelKeyAction, cancelKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// end esc handling
		cancelButton.setActionCommand(LocalizedResources.applicationResources
				.getString("cancel"));
		if (cancelListener != null) {
			cancelButton.addActionListener(cancelListener);
		}

		buttonPane.add(cancelButton);

		contentPane.add(buttonPane, BorderLayout.SOUTH);
		pack();
		setSize(new Dimension(400, 200));
	}

	static class Closer extends WindowAdapter implements Serializable {
		private static final long serialVersionUID = -6208462158856023567L;

		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
			Serializable {
		private static final long serialVersionUID = 7834785648391905253L;

		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class NamespaceTracker implements ActionListener {
	NamespaceInput input;

	Namespace namespace;

	public NamespaceTracker(NamespaceInput c) {
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
		namespace = input.getCurrentNamespace();
		close();
	}

	public Namespace getCurrentNamespace() {
		return namespace;
	}
}

class NamespaceCancelTracker implements ActionListener {
	NamespaceInput input;

	public NamespaceCancelTracker(NamespaceInput c) {
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
