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

import javax.swing.JPanel;
import java.util.*;
/**
 * <p>Title: Michigan XML Editor</p>
 * <p>Description: This edits an XML document based on an XML schema.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Nubean LLC</p>
 * @author Ajay Vohra
 * @version 1.0
 */

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class FontChooser extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1804281662279254985L;

	protected JList name, size;

	protected JTextArea textArea;

	protected Font font;

	protected ButtonGroup style;

	protected ButtonModel[] buttonModel;

	protected JPanel previewPanel;

	protected Vector<ChangeListener> changeListeners;

	protected Box mainBox;

	protected String[] sizes = { "8", "9", "10", "11", "12", "14", "16", "18",
			"20", "22", "24", "26", "28", "36", "48", "72" };

	protected String[] styles = { "Regular", "Italic", "Bold", "Bold Italic" };

	/**
	 * constructor with no arguments
	 */

	public FontChooser() {
		super();
		changeListeners = new Vector<ChangeListener>();
		init();
	}

	private void init() {
		EtchedBorder eb = new EtchedBorder(EtchedBorder.LOWERED);
		setLayout(new BorderLayout());
		Border bl = new BevelBorder(BevelBorder.LOWERED);
		buttonModel = new ButtonModel[styles.length];

		mainBox = Box.createVerticalBox();

		Box top = Box.createHorizontalBox();

		ListSelectionListener il = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateFont();
			}
		};

		ItemListener al = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					updateFont();
			}
		};

		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		name = new JList(names);
		name.setVisibleRowCount(5);
		name.addListSelectionListener(il);
		name.setBackground(Color.white);
		JScrollPane nameView = new JScrollPane(name,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		nameView
				.setBorder(new TitledBorder(eb,
						LocalizedResources.applicationResources
								.getString("font.name")));

		top.add(nameView);

		top.add(Box.createHorizontalStrut(20));

		size = new JList(sizes);
		size.setVisibleRowCount(5);
		size.addListSelectionListener(il);
		size.setBackground(Color.white);
		size.setBorder(bl);

		JScrollPane sizeView = new JScrollPane(size,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		sizeView
				.setBorder(new TitledBorder(eb,
						LocalizedResources.applicationResources
								.getString("font.size")));
		top.add(sizeView);
		mainBox.add(top);
		mainBox.add(Box.createVerticalStrut(10));

		style = new ButtonGroup();
		JPanel stylePanel = new JPanel();
		stylePanel
				.setBorder(new TitledBorder(eb,
						LocalizedResources.applicationResources
								.getString("font.style")));
		for (int i = 0; i < styles.length; i++) {
			JRadioButton rb = new JRadioButton(styles[i]);
			rb.setActionCommand(styles[i]);
			rb.addItemListener(al);
			buttonModel[i] = rb.getModel();
			stylePanel.add(rb);
			style.add(rb);
		}

		mainBox.add(stylePanel);
		mainBox.add(Box.createVerticalStrut(10));

		previewPanel = new JPanel();
		previewPanel.setLayout(new BorderLayout());
		previewPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), LocalizedResources.applicationResources
				.getString("font.preview")));
		textArea = new JTextArea("AaBbCcXxYyZz0@*&^1234!");
		textArea.setBackground(Color.white);
		textArea.setEditable(false);
		textArea.setBorder(bl);
		previewPanel.add(textArea, BorderLayout.CENTER);

		mainBox.add(previewPanel);

		add(mainBox);
		updateFont();
	}

	/**
	 * return preview panel
	 */
	public JComponent getPreviewPanel() {
		return previewPanel;
	}

	/**
	 * set preview panel
	 */
	public void setPreviewPanel(JComponent c) {
		previewPanel.setVisible(false);
		mainBox.remove(previewPanel);

		if (c != null)
			mainBox.add(c, -1);
	}

	/**
	 * Returns the current font
	 */
	public Font getCurrentFont() {
		return font;
	}

	/**
	 * set current font
	 */
	public void setCurrentFont(Font f) {
		if (f == null)
			return;

		ListModel model = name.getModel();
		int c = model.getSize();
		int i = 0;
		String newName = f.getName();

		for (i = 0; i < c; i++) {
			String s = (String) model.getElementAt(i);

			if (s.equalsIgnoreCase(newName)) {
				name.setSelectedIndex(i);
				break;
			}
		}

		if (i == c)
			return;

		ListModel smodel = size.getModel();
		c = smodel.getSize();
		String newSize = Integer.toString(f.getSize());
		for (i = 0; i < c; i++) {
			String s = (String) smodel.getElementAt(i);

			if (s.equalsIgnoreCase(newSize)) {
				size.setSelectedIndex(i);
				break;
			}
		}

		if (i == c) {
			setCurrentFont(font);
			return;
		}

		c = buttonModel.length;
		String newStyle = null;
		switch (f.getStyle()) {
		case Font.PLAIN:
			newStyle = styles[0];
			break;
		case Font.ITALIC:
			newStyle = styles[1];
			break;
		case Font.BOLD:
			newStyle = styles[2];
			break;
		case Font.BOLD + Font.ITALIC:
			newStyle = styles[3];
			break;
		default:
			newStyle = styles[0];
			break;
		}

		for (i = 0; i < c; i++) {
			String s = buttonModel[i].getActionCommand();

			if (s.equalsIgnoreCase(newStyle)) {
				style.setSelected(buttonModel[i], true);
				break;
			}
		}

		if (i == c) {
			setCurrentFont(font);
			return;
		}

		font = f;
		updateFont();
		revalidate();
		repaint();
	}

	public void fireChangeListeners(ChangeEvent e) {
		Vector<ChangeListener> list = (Vector<ChangeListener>) changeListeners
				.clone();
		for (int i = 0; i < list.size(); i++) {
			ChangeListener listener = (ChangeListener) list.elementAt(i);
			if (listener != null) {
				listener.stateChanged(e);
			}
		}
	}

	public void addChangeListener(ChangeListener cl) {
		changeListeners.addElement(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		changeListeners.removeElement(cl);
	}

	protected void updateFont() {
		ButtonModel selStyle = style.getSelection();

		if (selStyle == null)
			return;

		String fontStyle = selStyle.getActionCommand();

		int cstyle = Font.PLAIN;

		if (fontStyle.equalsIgnoreCase(styles[0]))
			cstyle = Font.PLAIN;
		else if (fontStyle.equalsIgnoreCase(styles[1]))
			cstyle = Font.ITALIC;
		else if (fontStyle.equalsIgnoreCase(styles[2]))
			cstyle = Font.BOLD;
		else if (fontStyle.equalsIgnoreCase(styles[3])) {
			cstyle = Font.BOLD + Font.ITALIC;
		}
		try {
			font = new Font((String) name.getSelectedValue(), cstyle, Integer
					.parseInt((String) size.getSelectedValue()));
			if (textArea.isVisible()) {
				textArea.setFont(font);
				textArea.revalidate();
				textArea.repaint();
			}
			ChangeEvent e = new ChangeEvent(this);
			fireChangeListeners(e);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Shows a modal font-chooser dialog and blocks until the dialog is hidden.
	 * If the user presses the "OK" button, then this method hides/disposes the
	 * dialog and returns the selected font. If the user presses the "Cancel"
	 * button or closes the dialog without pressing "OK", then this method
	 * hides/disposes the dialog and returns null.
	 * 
	 * @param component
	 *            the parent Component for the dialog
	 * @param title
	 *            the String containing the dialog's title
	 * @param initialFont
	 *            the initial Font set when the font-chooser is shown
	 */
	public static Font showDialog(Component component, String title,
			Font initialFont) {
		final FontChooser pane = new FontChooser();
		pane.setCurrentFont(initialFont);

		FontTracker ok = new FontTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, null);
		dialog.addWindowListener(new FontChooserDialog.Closer());
		dialog.addComponentListener(new FontChooserDialog.DisposeOnClose());

		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getFont();
	}

	/**
	 * Creates and returns a new dialog containing the specified FontChooser
	 * pane along with "OK", "Cancel", and "Reset" buttons. If the "OK" or
	 * "Cancel" buttons are pressed, the dialog is automatically hidden (but not
	 * disposed). If the "Reset" button is pressed, the font-chooser's font will
	 * be reset to the font which was set the last time setVisible(true) was
	 * invoked on the dialog and the dialog will remain showing.
	 * 
	 * @param c
	 *            the parent component for the dialog
	 * @param title
	 *            the title for the dialog
	 * @param modal
	 *            a boolean. When true, the remainder of the program is inactive
	 *            until the dialog is closed.
	 * @param chooserPane
	 *            the font-chooser to be placed inside the dialog
	 * @param okListener
	 *            the ActionListener invoked when "OK" is pressed
	 * @param cancelListener
	 *            the ActionListener invoked when "Cancel" is pressed
	 */
	public static JDialog createDialog(Component c, String title,
			boolean modal, FontChooser chooserPane, ActionListener okListener,
			ActionListener cancelListener) {

		return new FontChooserDialog(c, title, modal, chooserPane, okListener,
				cancelListener);
	}

}

class FontChooserDialog extends JDialog {
	private static final long serialVersionUID = 141170823029682963L;

	private Font initialFont;

	private FontChooser chooserPane;

	public FontChooserDialog(Component c, String title, boolean modal,
			FontChooser chooserPane, ActionListener okListener,
			ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);

		this.chooserPane = chooserPane;

		String okString = LocalizedResources.applicationResources
				.getString("ok");
		String cancelString = LocalizedResources.applicationResources
				.getString("cancel");
		String resetString = LocalizedResources.applicationResources
				.getString("reset");

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
		cancelButton.setActionCommand("cancel");
		if (cancelListener != null) {
			cancelButton.addActionListener(cancelListener);
		}
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPane.add(cancelButton);

		JButton resetButton = new JButton(resetString);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		buttonPane.add(resetButton);
		contentPane.add(buttonPane, BorderLayout.SOUTH);

		pack();
		setSize(new Dimension(400, 400));
		setLocationRelativeTo(c);
	}

	public void setVisible(boolean b) {
		initialFont = chooserPane.getCurrentFont();
		super.setVisible(b);
	}

	public void reset() {
		chooserPane.setCurrentFont(initialFont);
	}

	static class Closer extends WindowAdapter implements Serializable {
		private static final long serialVersionUID = 1060903659380947395L;

		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
			Serializable {
		private static final long serialVersionUID = -2869936170416919799L;

		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class FontTracker implements ActionListener, Serializable {
	private static final long serialVersionUID = -7096049355631492804L;

	FontChooser chooser;

	Font font;

	public FontTracker(FontChooser c) {
		chooser = c;
	}

	public void actionPerformed(ActionEvent e) {
		font = chooser.getCurrentFont();
	}

	public Font getFont() {
		return font;
	}
}
