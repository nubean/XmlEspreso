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

package com.nubean.michxml.attr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.nubean.michxml.XMLAbstractEditor;

public class AnyAttributeNameEditor extends AbstractCellEditor implements
		TableCellEditor {

	private static final long serialVersionUID = 4977202998636962596L;

	private AnyAttributeName avalue;

	private JTextField tf;

	public AnyAttributeNameEditor(XMLAbstractEditor editor,
			AnyAttributeName value) {
		super();
		this.avalue = value;
		tf = new JTextField(value.getValue());

		tf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				avalue.setValue(tf.getText());
				fireEditingStopped();
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