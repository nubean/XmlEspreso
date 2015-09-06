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

package com.nubean.michutil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class GenericInput extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7389324155277512877L;
	protected JTextField inputtf;

	/**
	 * constructor with no arguments
	 */
	public GenericInput(String message, Icon icon, String text) {
		super();
		init(message, icon, text);
	}

	private void init(String msg, Icon icon, String text) {
		setLayout(new GridBagLayout());

		JLabel msgLabel = new JLabel(msg);
		msgLabel.setIcon(icon);

		inputtf = new JTextField(120);
		inputtf.setText(text);

		Insets inset = new Insets(5, 10, 5, 10);
		GridBagConstraints constr = new java.awt.GridBagConstraints(0, 0, 1, 1,
				0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				inset, 0, 0);

		add(msgLabel, constr);

		constr = new java.awt.GridBagConstraints(1, 0, 1, 1, 1, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(inputtf, constr);

	}

	public String getGeneric() {
		return inputtf.getText();
	}

	public static String showDialog(Component component, String title,
			String msg, Icon icon, String text, Point p) {
		final GenericInput pane = new GenericInput(msg, icon, text);
		GenericOkTracker ok = new GenericOkTracker(pane);
		GenericCancelTracker cancel = new GenericCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new GenericInputDialog.Closer());
		dialog.addComponentListener(new GenericInputDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getGeneric();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, GenericInput chooserPane, ActionListener okListener,
			ActionListener cancelListener) {

		return new GenericInputDialog(c, title, modal, chooserPane, okListener,
				cancelListener);
	}

}

class GenericInputDialog extends JDialog {
	private static final long serialVersionUID = -5503420884911959736L;

	public GenericInputDialog(Component c, String title, boolean modal,
			GenericInput chooserPane, ActionListener okListener,
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
		setSize(new Dimension(400, 100));
	}

	static class Closer extends WindowAdapter implements Serializable {
		private static final long serialVersionUID = 7669715165915709584L;

		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
			Serializable {
		private static final long serialVersionUID = 1103822105284823712L;

		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class GenericOkTracker implements ActionListener {
	GenericInput input;

	String element;

	public GenericOkTracker(GenericInput c) {
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
		element = input.getGeneric();
		close();
	}

	public String getGeneric() {
		return element;
	}
}

class GenericCancelTracker implements ActionListener {
	GenericInput input;

	public GenericCancelTracker(GenericInput c) {
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
