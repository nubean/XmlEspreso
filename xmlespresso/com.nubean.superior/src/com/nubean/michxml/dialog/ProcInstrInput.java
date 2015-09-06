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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.ProcInstr;

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

public class ProcInstrInput extends JPanel {
	private static final long serialVersionUID = 3393696271473912547L;

	protected JTextField targettf, datatf;

	protected ProcInstr pi;

	/**
	 * constructor with no arguments
	 */
	public ProcInstrInput() {
		super();
		init();
	}

	private void init() {
		setLayout(new GridBagLayout());

		JLabel targetl = new JLabel(LocalizedResources.applicationResources
				.getString("target"));
		targettf = new JTextField(40);

		JLabel datal = new JLabel(LocalizedResources.applicationResources
				.getString("data"));
		datatf = new JTextField(40);

		Insets inset = new Insets(5, 10, 5, 10);
		GridBagConstraints constr = new java.awt.GridBagConstraints(0, 0, 1, 1,
				0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				inset, 0, 0);
		add(targetl, constr);

		constr = new java.awt.GridBagConstraints(1, 0, 1, 1, 1, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(targettf, constr);

		constr = new java.awt.GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(datal, constr);

		constr = new java.awt.GridBagConstraints(1, 1, 1, 1, 1, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(datatf, constr);

	}

	public ProcInstr getCurrentProcInstr() {
		pi = new ProcInstr(targettf.getText(), datatf.getText());
		return pi;
	}

	public static ProcInstr showDialog(Component component, String title,
			Point p) {
		final ProcInstrInput pane = new ProcInstrInput();
		ProcInstrTracker ok = new ProcInstrTracker(pane);
		ProcInstrCancelTracker cancel = new ProcInstrCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new ProcInstrInputDialog.Closer());
		dialog.addComponentListener(new ProcInstrInputDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getCurrentProcInstr();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, ProcInstrInput chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new ProcInstrInputDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}

}

class ProcInstrInputDialog extends JDialog {
	private static final long serialVersionUID = 411352763445115140L;

	public ProcInstrInputDialog(Component c, String title, boolean modal,
			ProcInstrInput chooserPane, ActionListener okListener,
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

class ProcInstrTracker implements ActionListener {
	ProcInstrInput input;

	ProcInstr namespace;

	public ProcInstrTracker(ProcInstrInput c) {
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
		namespace = input.getCurrentProcInstr();
		close();
	}

	public ProcInstr getCurrentProcInstr() {
		return namespace;
	}
}

class ProcInstrCancelTracker implements ActionListener {
	ProcInstrInput input;

	public ProcInstrCancelTracker(ProcInstrInput c) {
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
