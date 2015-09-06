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

import java.util.*;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.nubean.michbase.CommonUtils;
import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.TypeDefinition;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLNode;

public class AttributePropertySheetModel extends AbstractTableModel {

	private static final long serialVersionUID = -2846992905459078706L;

	private String columnNames[] = new String[2];

	private Object[][] rowData;

	public AttributePropertySheetModel(SchemaNode node, XMLAbstractEditor editor) {
		super();
		setColumnNames();
		if (node.isSimpleContent()) {
			SchemaNode simpleContent = CommonUtils.getChildByName(node,
					"simpleContent");
			node = CommonUtils.getChildByName(simpleContent, "extension");
			if (node == null) {
				node = CommonUtils.getChildByName(simpleContent, "restriction");
			}
		}
		buildModel(node, editor);
	}

	private void buildModel(SchemaNode node, XMLAbstractEditor editor) {
		Vector<Node> attrsVector = CommonUtils.getAttributeNodes(node);
		int count = (attrsVector != null ? attrsVector.size() : 0);

		rowData = new Object[count][2];

		for (int i = 0; i < count; i++) {
			Node attrNode = attrsVector.elementAt(i);
			buildAttributeModel(i, attrNode, node, editor);
		}

	}

	private void buildAnyAttributeModel(int row, org.w3c.dom.Node node,
			SchemaNode snode, XMLAbstractEditor editor) {
		AnyAttributeName nameValue = new AnyAttributeName();
		rowData[row][0] = nameValue;
		AttributeValue value = new AttributeValue();
		value.setName(nameValue);
		rowData[row][1] = value;
	}

	public void buildAttributeModel(int row, org.w3c.dom.Node node,
			SchemaNode snode, XMLAbstractEditor editor) {
		String nodeName = CommonUtils.getUnqualifiedNodeName(node);
		if (nodeName.equals("anyAttribute")) {
			buildAnyAttributeModel(row, node, snode, editor);
			return;
		}

		AttributeName nameValue = null;
		NamedNodeMap attrs = node.getAttributes();
		Attr name = (Attr) attrs.getNamedItem("name");
		boolean required = false;
		String defaultValue = null;
		if (name == null) {
			Attr ref = (Attr) attrs.getNamedItem("ref");
			String refName = ref.getValue();
			node = snode.getSchema().getAttribute(refName);
			if (node != null) {
				attrs = node.getAttributes();
				name = (Attr) attrs.getNamedItem("name");
			} else {
				nameValue = new AttributeName();
				nameValue.setValue(refName);
				rowData[row][0] = nameValue;
			}
		}

		if (name != null) {
			nameValue = new AttributeName();
			nameValue.setValue(name.getValue());
			rowData[row][0] = nameValue;
		}

		Attr use = (Attr) attrs.getNamedItem("use");
		if (use != null && use.getValue().equals("required"))
			required = true;
		Attr def = (Attr) attrs.getNamedItem("default");
		if (def != null)
			defaultValue = def.getValue();

		Node type = (node != null ? CommonUtils.getTypeNode(node) : null);

		String attrName = (String) nameValue.getValue();

		XMLNode xmlNode = editor.getComplexTypeNode();
		NamedNodeMap selAttrs = xmlNode.getDomNode().getAttributes();
		Attr selAttr = (selAttrs != null ? (Attr) selAttrs
				.getNamedItem(attrName) : null);
		String curValue = (selAttr != null ? selAttr.getValue() : null);

		if (curValue == null || curValue.length() == 0) {
			curValue = defaultValue;
		}

		AttributeValue value = null;

		if (type == null) {
			value = new AttributeValue();
			value.setValue(curValue);
		} else {

			TypeDefinition typeDef = new TypeDefinition(type, xmlNode
					.getSchemaNode().getSchema());

			if (curValue == null || curValue.length() == 0)
				curValue = typeDef.getInitValue();
			if (typeDef.getEnumerations() != null
					&& typeDef.getEnumerations().size() > 0) {
				value = new EnumAttributeValue(typeDef.getEnumerations());
				value.setValue(curValue);

			} else if (typeDef.getBase().equals("boolean")) {
				value = new BoolAttributeValue();
				if (curValue != null)
					value.setValue(curValue);

			} else {
				value = new REAttributeValue(typeDef);
				value.setValue(curValue);
			}
		}

		value.setRequired(required);
		value.setName(nameValue);

		rowData[row][1] = value;

	}

	private void setColumnNames() {
		columnNames[0] = LocalizedResources.applicationResources
				.getString("name");
		columnNames[1] = LocalizedResources.applicationResources
				.getString("value");
	}

	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	public int getRowCount() {
		return rowData.length;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int row, int col) {
		return rowData[row][col];
	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;

		try {
			Object obj = getValueAt(row, col);
			if (obj instanceof AttributeValue) {
				ret = true;
			} else if (obj instanceof AttributeName) {
				AttributeName value = (AttributeName) obj;
				switch (value.getType()) {

				case AttributeName.ANY:
					ret = true;
					break;
				default:
					ret = false;
					break;
				}
			}
		} catch (Exception e) {

		}
		return ret;

	}

	public void setValueAt(Object value, int row, int col) {
		rowData[row][col] = value;
		fireTableCellUpdated(row, col);
	}

}
