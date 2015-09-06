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

package com.nubean.michxml.design.attr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.attr.AnyAttributeName;
import com.nubean.michxml.attr.AnyAttributeNameEditor;
import com.nubean.michxml.attr.AttributeName;
import com.nubean.michxml.attr.AttributePropertySheetModel;
import com.nubean.michxml.attr.AttributeValue;
import com.nubean.michxml.attr.BoolAttributeValue;
import com.nubean.michxml.attr.EnumAttributeValue;
import com.nubean.michxml.attr.REAttributeValue;

public class AttributePropertySheet extends JTable {

	private static final long serialVersionUID = 6547905069278195263L;
	private XMLAbstractEditor editor;

	public AttributePropertySheet(SchemaNode node, XMLAbstractEditor editor) {
		super();
		setModel(new AttributePropertySheetModel(node, editor));
		this.editor = editor;
	}


	public TableCellEditor getCellEditor(int row, int col) {
		TableCellEditor ce = null;
		Object obj = getModel().getValueAt(row, col);
		if (obj instanceof AttributeValue) {
			AttributeValue value = (AttributeValue) obj;

			switch (value.getType()) {
			case AttributeValue.BOOL:
				ce = new BoolAttributeValueEditor(editor,
						(BoolAttributeValue) value);
				break;
			case AttributeValue.RE:
				ce = new REAttributeValueEditor(editor,
						(REAttributeValue) value);
				break;
			case AttributeValue.ENUM:
				ce = new EnumAttributeValueEditor(editor,
						(EnumAttributeValue) value);
				break;

			default:
				ce = new AttributeValueEditor(editor, (AttributeValue) value);
				break;
			}
		} else if (obj instanceof AttributeName) {
			AttributeName value = (AttributeName) obj;
			switch (value.getType()) {

			case AttributeName.ANY:
				ce = new AnyAttributeNameEditor(editor,
						(AnyAttributeName) value);
				break;
			}
		}

		return ce;
	}

	public TableCellRenderer getCellRenderer(int row, int col) {
		TableCellRenderer cr = null;
		Object obj = getModel().getValueAt(row, col);
		if (obj instanceof AttributeValue) {
			AttributeValue value = (AttributeValue) obj;

			switch (value.getType()) {
			case AttributeValue.BOOL:
				cr = new BoolAttributeValueRenderer(true);
				break;
			default:
				cr = new AttributeValueRenderer(true);
				break;
			}
		} else if (obj instanceof AttributeName) {
			cr = new AttributeNameRenderer(true);
		}

		return cr;
	}

	public static void showDialog(Component component, String title,
			SchemaNode node, XMLAbstractEditor editor, Point p) {
		final AttributePropertySheet pane = new AttributePropertySheet(node,
				editor);
		AttributeTracker ok = new AttributeTracker(pane);
		AttributeCancelTracker cancel = new AttributeCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, ok, cancel);
		dialog.addWindowListener(new AttributeChooserDialog.Closer());
		dialog
				.addComponentListener(new AttributeChooserDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, AttributePropertySheet chooserPane,
			ActionListener okListener, ActionListener cancelListener) {

		return new AttributeChooserDialog(c, title, modal, chooserPane,
				okListener, cancelListener);
	}
}

class AttributeChooserDialog extends JDialog {
	private AttributePropertySheet chooserPane;

	public AttributeChooserDialog(Component c, String title, boolean modal,
			AttributePropertySheet chooserPane,
			final ActionListener okListener, ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

		this.chooserPane = chooserPane;

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		JScrollPane sp = new JScrollPane(chooserPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setBorder(new TitledBorder(
				LocalizedResources.applicationResources
						.getString("attributes")));
		contentPane.add(sp, BorderLayout.CENTER);

		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		chooserPane.registerKeyboardAction(cancelListener, cancelKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		contentPane.add(new JLabel(LocalizedResources.applicationResources
				.getString("attributes.notice")), BorderLayout.SOUTH);

		pack();
		setSize(new Dimension(300, 250));
		setLocationRelativeTo(c);
	}

	static class Closer extends WindowAdapter implements Serializable {
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
			Serializable {
		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class AttributeTracker implements ActionListener {
	AttributePropertySheet chooser;

	public AttributeTracker(AttributePropertySheet c) {
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

class AttributeCancelTracker implements ActionListener {
	AttributePropertySheet chooser;

	public AttributeCancelTracker(AttributePropertySheet c) {
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
