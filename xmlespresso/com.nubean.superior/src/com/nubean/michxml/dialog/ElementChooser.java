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

import javax.swing.JPanel;

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

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.SchemaNode;

import java.util.*;

public class ElementChooser extends JPanel {
	private static final long serialVersionUID = 2612425732503476178L;

	protected JList name;

	protected Vector<?> elements;

	protected Object element;

	protected boolean hasNamespace;

	/**
	 * constructor with no arguments
	 */
	public ElementChooser(Vector<?> elements, boolean editNamespace) {
		super();
		this.elements = elements;
		element = elements.elementAt(0);
		this.hasNamespace = editNamespace;
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		ListSelectionListener il = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				element = name.getSelectedValue();
			}
		};

		name = new JList(elements);

		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {

				if (e.getClickCount() == 2) {
					ElementChooser.this.dispatchEvent(e);
				}
			}

		};
		name.addMouseListener(ml);

		name.setSelectedIndex(0);
		name.setVisibleRowCount(5);
		name.addListSelectionListener(il);
		JScrollPane nameView = new JScrollPane(name,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		nameView.setBorder(new TitledBorder(
				LocalizedResources.applicationResources.getString("elements")));
		add(nameView, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		JCheckBox cb = null;
		cb = new JCheckBox(LocalizedResources.applicationResources
				.getString("edit.namespace"), hasNamespace);
		cb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				hasNamespace = cb.isSelected();
			}
		});
		panel.add(cb);
		add(panel, BorderLayout.SOUTH);
	}

	public Object getCurrentElement() {
		if (element instanceof SchemaNode) {
			SchemaNode sn = (SchemaNode) element;
			sn.setEditNamespace(hasNamespace);
		}
		return element;
	}

	public static Object showDialog(Component component, String title,
			Vector<?> elements, Point p, boolean editNamespace) {
		final ElementChooser pane = new ElementChooser(elements, editNamespace);
		ElementTracker ok = new ElementTracker(pane);
		CancelTracker cancel = new CancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new ElementChooserDialog.Closer());
		dialog.addComponentListener(new ElementChooserDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getCurrentElement();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, ElementChooser chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new ElementChooserDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}

}

class ElementChooserDialog extends JDialog {
	private static final long serialVersionUID = 8293637323551357235L;
	public ElementChooserDialog(Component c, String title, boolean modal,
			ElementChooser chooserPane, final ActionListener okListener,
			ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		contentPane.add(chooserPane, BorderLayout.CENTER);

		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {

				if (e.getClickCount() == 2) {
					okListener.actionPerformed(new ActionEvent(e.getSource(), e
							.getID(), "OK"));
				}
			}

		};
		chooserPane.addMouseListener(ml);
		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		chooserPane.registerKeyboardAction(cancelListener, cancelKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke okKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ENTER);
		chooserPane.registerKeyboardAction(okListener, okKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		contentPane.add(new JLabel(LocalizedResources.applicationResources
				.getString("esc.notice")), BorderLayout.SOUTH);

		pack();
		setSize(new Dimension(300, 250));
		setLocationRelativeTo(c);
	}

	static class Closer extends WindowAdapter implements Serializable {
		private static final long serialVersionUID = -7407060520313269834L;

		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
			Serializable {
		private static final long serialVersionUID = -7393925944170211734L;

		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class ElementTracker implements ActionListener {
	ElementChooser chooser;

	Object element;

	public ElementTracker(ElementChooser c) {
		chooser = c;
	}

	private void close() {
		Component parent = chooser.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		element = chooser.getCurrentElement();
		close();
	}

	public Object getCurrentElement() {
		return element;
	}
}

class CancelTracker implements ActionListener {
	ElementChooser chooser;

	public CancelTracker(ElementChooser c) {
		chooser = c;
	}

	private void close() {
		Component parent = chooser.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		close();
	}
}
