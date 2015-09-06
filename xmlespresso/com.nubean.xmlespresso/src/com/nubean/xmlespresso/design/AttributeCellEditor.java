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

package com.nubean.xmlespresso.design;

import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.nubean.michxml.attr.AttributeName;
import com.nubean.michxml.attr.AttributeValue;
import com.nubean.michxml.attr.BoolAttributeValue;
import com.nubean.michxml.attr.EnumAttributeValue;
import com.nubean.michxml.attr.REAttributeValue;

public class AttributeCellEditor extends CellEditor {

	private Object value;

	private Composite panel;

	private Control control;

	public AttributeCellEditor(Composite parent) {
		super(parent);
	}

	public AttributeCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	private void getControlValue() {

		if (control != null && value != null) {

			if (control instanceof Text) {
				Text text = (Text) control;

				if (value instanceof AttributeValue) {
					AttributeValue av = (AttributeValue) value;
					av.setValue(text.getText());
				} else if (value instanceof AttributeName) {
					AttributeName aname = (AttributeName) value;
					if (aname.getType() == AttributeName.ANY)
						aname.setValue(text.getText());
				}

			} else if (control instanceof Combo) {
				Combo c = (Combo) control;
				if (value instanceof AttributeValue) {
					AttributeValue av = (AttributeValue) value;
					av.setValue(c.getText());
				}

			} else if (control instanceof Button) {
				Button b = (Button) control;
				if (value instanceof BoolAttributeValue) {
					BoolAttributeValue av = (BoolAttributeValue) value;
					av.setBoolValue(b.getSelection());
				}
			}
		}

	}
	
	@Override
	protected Control createControl(Composite parent) {
		panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new FillLayout(SWT.HORIZONTAL));
		return panel;
	}

	private void createValueControl(Composite parent) {
		if (control != null)
			control.dispose();

		if (value != null) {
			if (value instanceof BoolAttributeValue) {
				BoolAttributeValue b = (BoolAttributeValue) value;
				Button cb = new Button(parent, SWT.CHECK);
				cb.setSelection(b.getBoolValue());
				control = cb;
			} else if (value instanceof EnumAttributeValue) {
				EnumAttributeValue e = (EnumAttributeValue) value;

				Combo combo = new Combo(parent, SWT.READ_ONLY);
				Vector ev = e.getEnums();
				int nitems = (ev != null ? ev.size() : 0);
				String[] items = new String[nitems];
				int index = 0;
				for (int i = 0; i < nitems; i++) {
					items[i] = ev.get(i).toString();
					if (items[i].equals(e.getValue())) {
						index = i;
					}
				}

				combo.setItems(items);
				combo.select(index);
				control = combo;

			} else if (value instanceof REAttributeValue) {
				REAttributeValue re = (REAttributeValue) value;

				Text text = new Text(parent, SWT.SHADOW_NONE);
				if (re.getValue() != null)
					text.setText(re.getValue());
				control = text;

				text.addVerifyListener(new VerifyListener() {
					public void verifyText(VerifyEvent e) {
						REAttributeValue re = (REAttributeValue) value;
						Text source = (Text) e.getSource();
						String newText = source.getText() + e.text;
						e.doit = re.getTyepdef().isValid(newText);
					}
				});
			} else if (value instanceof AttributeValue) {
				AttributeValue av = (AttributeValue) value;

				Text text = new Text(parent, SWT.SHADOW_NONE);
				if (av.getValue() != null)
					text.setText(av.getValue());

				control = text;
			} else if (value instanceof AttributeName) {
				AttributeName aname = (AttributeName) value;

				if (aname.getType() == AttributeName.ANY) {
					Text text = new Text(parent, SWT.SHADOW_NONE);
					if (aname.getValue() != null)
						text.setText(aname.getValue());
					control = text;
				}

			}
		}
		
		if(control != null) {
			control.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					keyReleaseOccured(e);
				}
			});
		}

	}

	@Override
	protected Object doGetValue() {
		getControlValue();
		return value;
	}

	@Override
	protected void doSetFocus() {
		if (control != null) {
			control.setFocus();
		} else {
			panel.setFocus();
		}

	}

	@Override
	protected void doSetValue(Object value) {
		this.value = value;
		createValueControl(panel);
		panel.layout();
		panel.update();
	}

}
