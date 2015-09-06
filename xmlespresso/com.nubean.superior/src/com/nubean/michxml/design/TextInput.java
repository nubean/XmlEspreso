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

package com.nubean.michxml.design;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

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

public class TextInput extends JPanel {
	private static final long serialVersionUID = -6741955246572638566L;
	protected JTextArea text;

	/**
	 * constructor with no arguments
	 */
	public TextInput() {
		super();
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		text = new JTextArea(40, 5);
		add(new JScrollPane(text), BorderLayout.CENTER);
	}

	public String getCurrentText() {
		return text.getText();
	}

	public static String showDialog(Component component, String title, Point p) {
		final TextInput pane = new TextInput();
		TextTracker ok = new TextTracker(pane);
		TextCancelTracker cancel = new TextCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new TextInputDialog.Closer());
		dialog.addComponentListener(new TextInputDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getCurrentText();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, TextInput chooserPane, ActionListener okListener,
			ActionListener cancelListener) {

		return new TextInputDialog(c, title, modal, chooserPane, okListener,
				cancelListener);
	}

}

class TextInputDialog extends JDialog {
	private static final long serialVersionUID = -7400157718925305132L;

	public TextInputDialog(Component c, String title, boolean modal,
			TextInput chooserPane, ActionListener okListener,
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

class TextTracker implements ActionListener {
	TextInput input;

	String text;

	public TextTracker(TextInput c) {
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
		text = input.getCurrentText();
		close();
	}

	public String getCurrentText() {
		return text;
	}
}

class TextCancelTracker implements ActionListener {
	TextInput input;

	public TextCancelTracker(TextInput c) {
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
