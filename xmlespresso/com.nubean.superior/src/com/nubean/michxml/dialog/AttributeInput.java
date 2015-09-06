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

public class AttributeInput extends JPanel {
	private static final long serialVersionUID = 8546227637610035664L;
	protected JComboBox attributetf;

	/**
	 * constructor with no arguments
	 */
	public AttributeInput(Object[] input) {
		super();
		init(input);
	}

	private void init(Object[] input) {
		setLayout(new GridBagLayout());

		JLabel attribute = new JLabel(
				LocalizedResources.applicationResources
						.getString("attribute.name"));
		attributetf = new JComboBox(input);
		attributetf.setEditable(true);

		Insets inset = new Insets(5, 10, 5, 10);
		GridBagConstraints constr = new java.awt.GridBagConstraints(0, 0, 1, 1,
				0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				inset, 0, 0);
		add(attribute, constr);

		constr = new java.awt.GridBagConstraints(0, 1, 1, 1, 1, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, inset,
				0, 0);
		add(attributetf, constr);

	}

	public String getAttribute() {
		return (String) attributetf.getSelectedItem();
	}

	public static String showDialog(Component component, String title, Point p,
			Object[] input) {
		final AttributeInput pane = new AttributeInput(input);
		AttributeOkTracker ok = new AttributeOkTracker(pane);
		AttributeInputCancel cancel = new AttributeInputCancel(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new AttributeInputDialog.Closer());
		dialog.addComponentListener(new AttributeInputDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getAttribute();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, AttributeInput chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new AttributeInputDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}

}

class AttributeInputDialog extends JDialog {
	private static final long serialVersionUID = 5354717639641719143L;

	public AttributeInputDialog(Component c, String title, boolean modal,
			AttributeInput chooserPane, ActionListener okListener,
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

		ActionListener okKeyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((AbstractButton) e.getSource()).doClick();
			}
		};
		KeyStroke okKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ENTER);

		okButton.registerKeyboardAction(okKeyAction, okKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
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
		setSize(new Dimension(200, 80));
	}

	static class Closer extends WindowAdapter implements Serializable {
		private static final long serialVersionUID = -6388759449180280690L;

		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
			Serializable {
		private static final long serialVersionUID = 220757040182702335L;

		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class AttributeOkTracker implements ActionListener {
	AttributeInput input;

	String attribute;

	public AttributeOkTracker(AttributeInput c) {
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
		attribute = input.getAttribute();
		close();
	}

	public String getAttribute() {
		return attribute;
	}
}

class AttributeInputCancel implements ActionListener {
	AttributeInput input;

	public AttributeInputCancel(AttributeInput c) {
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
