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

import javax.swing.*;
import javax.swing.table.TableCellEditor;

import org.w3c.dom.Node;

import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.attr.AttributeName;
import com.nubean.michxml.attr.REAttributeValue;
import com.nubean.michxml.design.RETextField;

import java.awt.Component;
import java.awt.event.*;

public class REAttributeValueEditor extends AbstractCellEditor implements
		TableCellEditor {
	private static final long serialVersionUID = -1999247853370790135L;

	private REAttributeValue avalue;

	private RETextField tf;

	private XMLAbstractEditor editor;

	private void applyAttribute() {
		if (editor.getComplexTypeNode() == null
				|| editor.getComplexTypeNode().getDomNode().getNodeType() != Node.ELEMENT_NODE)
			return;

		if (avalue != null) {
			AttributeName name = (AttributeName) avalue.getName();
			if (name != null) {
				editor.setAttribute(name.getValue(), avalue.getValue());
			}
		}
	}

	public REAttributeValueEditor(XMLAbstractEditor editor,
			REAttributeValue value) {
		super();
		this.editor = editor;
		this.avalue = value;
		tf = new RETextField(value.getValue(), 20, avalue.getTyepdef());

		tf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				avalue.setValue(tf.getText());
				fireEditingStopped();
				applyAttribute();
			}
		});
	}

	public Object getCellEditorValue() {
		return avalue;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean selected, int row, int column) {
		tf.setOpaque(true);

		return tf;
	}
}