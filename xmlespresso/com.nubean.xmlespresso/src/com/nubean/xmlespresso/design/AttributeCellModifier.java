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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

import com.nubean.michxml.attr.AttributeName;
import com.nubean.michxml.attr.AttributeValue;

public class AttributeCellModifier implements ICellModifier {

	private String[] properties;

	public AttributeCellModifier(String[] properties) {
		super();
		this.properties = properties;
	}

	private int findPropertyIndex(String property) {
		int count = (properties != null ? properties.length : 0);
		int index = 0;
		for (int i = 0; i < count; i++) {
			if (properties[i].equals(property)) {
				index = i;
				break;
			}
		}
		return index;
	}

	@Override
	public boolean canModify(Object element, String property) {
		try {
			int index = findPropertyIndex(property);
			Object[] row = (Object[]) element;

			Object value = row[index];
			if (value instanceof AttributeValue)
				return true;

			if (value instanceof AttributeName) {
				AttributeName an = (AttributeName) value;
				return (an.getType() == AttributeName.ANY);
			}
		} catch (Exception e) {

		}
		return false;
	}

	@Override
	public Object getValue(Object element, String property) {
		try {
			int index = findPropertyIndex(property);
			Object[] row = (Object[]) element;

			return row[index];
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public void modify(Object element, String property, Object value) {
		try {
			TableItem tableItem = (TableItem) element;
			int index = findPropertyIndex(property);

			String text = null;
			if(value instanceof AttributeValue) {
				text = ((AttributeValue)value).getValue();
			} else if(value instanceof AttributeName) {
				text = ((AttributeName)value).getValue();
			}
			tableItem.setText(index, text);
			tableItem.setData(property, value);
		} catch (Exception e) {

		}

	}

}
