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

package com.nubean.michxml;

import javax.swing.tree.*;
import java.util.*;
import org.w3c.dom.*;
import javax.swing.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michutil.*;

public class SchemaNode implements TreeNode, Iconable, Comparable<SchemaNode> {
	private static final String ANY_ELEMENT = CommonUtils.getNameWithNamespace(
			"any", "##any");

	private String qualifiedNodeName, nodeValue;

	private int nodeType;

	private Vector<SchemaNode> childrenVector;
	private Vector<Node> attributesVector;
	private Vector<SchemaNode> terminalsVector;

	private SchemaNode parent;

	private boolean editNamespace;

	private Integer minOccurs, maxOccurs;

	private boolean mixed, anyNamespace, simpleContent;;

	private Properties attributes = new Properties();

	private XMLSchema schema;

	private TypeDefinition typeDef;

	private SchemaNode typeNode;

	private boolean empty;

	private org.w3c.dom.Node resolve, domNode;

	
	private Character cc;

	private SchemaNode proxyNode;

	private String documentation;

	private int insertPos;

	public void setProxyNode(SchemaNode node) {
		this.proxyNode = node;
	}

	public SchemaNode getProxyNode() {
		return proxyNode;
	}

	public TaglibDef getTld() {
		return schema.getTld();
	}

	@SuppressWarnings("unused")
	private void printTerms() {
		getTerminals();
		System.out.print("terminals " + getTagName() + " {");
		int count = (terminalsVector != null ? terminalsVector.size() : 0);
		for (int i = 0; i < count; i++)
			System.out.print(((SchemaNode) terminalsVector.elementAt(i)).toString()
					+ ",");
		System.out.println("}");
	}
	
	public org.w3c.dom.Node getDomNode() {
		return domNode;
	}

	public Vector<SchemaNode> getTerminals() {
		if (terminalsVector == null) {
			Vector<SchemaNode> terms = new Vector<SchemaNode>(8, 4);
			addTerminals(this, terms);
			terminalsVector = terms;
		}
		return terminalsVector;

	}

	private void addTerminals(SchemaNode node, Vector<SchemaNode> terms) {
		String name = node.getNodeName();

		if (name.equals("element")) {
			terms.add(node);
			SchemaNode typeNode = node.getTypeNode();
			if (typeNode != null && typeNode != node) {
				typeNode.addTerminals(typeNode, terms);
			}
			return;
		}

		if (name.equals("any")) {
			terms.add(node);
			return;
		}
		int count = (node != null ? node.childCount() : 0);
		for (int i = 0; i < count; i++) {
			addTerminals(node.child(i), terms);
		}
	}

	public void setInsertPosition(int pos) {
		this.insertPos = pos;
	}

	public int getInsertPosition() {
		return insertPos;
	}

	public void setCharCode(Character cc) {
		this.cc = cc;
	}

	public Character getCharCode() {
		return cc;
	}

	public void setEditNamespace(boolean ns) {
		this.editNamespace = ns;
	}

	public boolean isEmpty() {
		if (empty) {
			getMixed(domNode);
		}
		return empty;
	}

	public boolean isMixed() {
		return mixed;
	}

	public boolean isResolved() {
		return resolve == null;
	}

	public boolean isSimpleContent() {
		return simpleContent;
	}

	public TypeDefinition getTypeDef() {
		return typeDef;
	}

	public String getDocumentation() {
		return documentation;
	}

	public boolean isAnyNamespace() {
		return anyNamespace;
	}

	public boolean getEditNamespace() {
		return editNamespace;
	}

	public ImageIcon getIcon() {
		if (nodeType == Node.ATTRIBUTE_NODE)
			return IconLoader.attrIcon;

		ImageIcon icon = null;
		String name = getNodeName();

		if (name.equals("element") || name.equals("any")) {
			icon = IconLoader.elementIcon;
		} else if (name.equals("complexType")) {
			icon = IconLoader.complexTypeIcon;
		} else if (name.equals("simpleType")) {
			icon = IconLoader.simpleTypeIcon;
		} else {
			icon = IconLoader.typeIcon;
		}
		return icon;
	}

	public TreeNode getChildAt(int index) {
		return child(index);
	}

	public int getChildCount() {
		return childCount();
	}

	public int getIndex(TreeNode node) {
		return index((SchemaNode) node);
	}

	public Enumeration<SchemaNode> children() {
		return childrenVector.elements();
	}

	public boolean isLeaf() {
		String nodeName = getNodeName();
		return nodeName.equals("element") || nodeName.equals("any")
				|| childCount() == 0;
	}

	public boolean getAllowsChildren() {
		return nodeType == Node.ELEMENT_NODE;
	}

	public void replace(SchemaNode newNode, SchemaNode node) {
		int index = index(node);
		childrenVector.remove(index);
		insertElementAt(newNode, index);
	}

	public String getQNodeName() {
		return qualifiedNodeName;
	}

	public String getNodeName() {
		return CommonUtils.getUnqualifiedNodeName(qualifiedNodeName);
	}

	public int getNodeType() {
		return nodeType;
	}

	public boolean equals(Object obj) {
		boolean retval = false;
		try {
			SchemaNode node = (SchemaNode) obj;
			retval = qualifiedNodeName.equals(node.qualifiedNodeName);
		} catch (Throwable e) {
			super.equals(obj);
		}

		return retval;
	}

	public boolean isNillable() {
		String nodeName = getNodeName();
		boolean nillable = getMinOccurs() == 0;
		if (nillable || nodeName.equals("element") || nodeName.equals("choice")
				|| nodeName.equals("any"))
			return nillable;

		int count = childCount();
		for (int i = 0; i < count; i++) {
			SchemaNode child = child(i);
			nillable = child.isNillable();
			if (!nillable)
				break;
		}

		return nillable;
	}

	public String getPattern() {
		if (nodeType != Node.ELEMENT_NODE)
			return null;

		StringBuffer sb = new StringBuffer();
		String nodeName = getNodeName();

		if (nodeName.equals("element") || nodeName.equals("any")) {
			if (cc != null) {
				sb.append('(').append(cc.charValue()).append(')');
				sb.append(getRepeat());
			} else {
				System.out.println("char code is null:" + this);
			}
		} else if (nodeName.equals("sequence")
				|| nodeName.equals("complexType") || nodeName.equals("group")) {
			int count = childCount();
			boolean first = true;
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String cpattern = child.getPattern();
				if (cpattern != null && cpattern.length() > 0) {
					if (first) {
						sb.append('(');
						first = false;
					}
					sb.append(cpattern);
				}
			}
			if (!first) {
				sb.append(')');
				sb.append(getRepeat());
			}
		} else if (nodeName.equals("choice")) {
			int count = childCount();
			boolean first = true;
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String cpattern = child.getPattern();
				if (cpattern != null && cpattern.length() > 0) {
					if (first) {
						sb.append('(');
						first = false;
					}
					sb.append(cpattern);
					if (i < count - 1)
						sb.append('|');
				}
			}
			if (!first) {
				sb.append(')');
				sb.append(getRepeat());
			}
		} else if (nodeName.equals("all")) {
			int count = childCount();
			Vector<String> pv = new Vector<String>(count);
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String cpattern = child.getPattern();
				if (cpattern != null && cpattern.length() > 0) {
					pv.add(cpattern);
				}
			}
			sb.append(getAllPattern(pv)).append(getRepeat());
		}
		return sb.toString();
	}

	public String getPrintPattern() {
		if (nodeType != Node.ELEMENT_NODE)
			return null;

		StringBuffer sb = new StringBuffer();
		String nodeName = getNodeName();

		if (nodeName.equals("element") || nodeName.equals("any")) {
			if (cc != null) {
				sb.append(getPrintTerminals());
				sb.append(getRepeat());
			} else {
				System.out.println("char code is null:" + this);
			}
		} else if (nodeName.equals("sequence")
				|| nodeName.equals("complexType") || nodeName.equals("group")) {
			int count = childCount();
			boolean first = true;
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String cpattern = child.getPrintPattern();
				if (cpattern != null && cpattern.length() > 0) {
					if (first) {
						sb.append('(');
						first = false;
					}
					sb.append(cpattern);
				}
			}
			if (!first) {
				sb.append(')');
				sb.append(getRepeat());
			}
		} else if (nodeName.equals("choice")) {
			int count = childCount();
			boolean first = true;
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String cpattern = child.getPrintPattern();
				if (cpattern != null && cpattern.length() > 0) {
					if (first) {
						sb.append('(');
						first = false;
					}
					sb.append(cpattern);
					if (i < count - 1)
						sb.append('|');
				}
			}
			if (!first) {
				sb.append(')');
				sb.append(getRepeat());
			}
		} else if (nodeName.equals("all")) {
			int count = childCount();
			Vector<String> pv = new Vector<String>(count);
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String cpattern = child.getPrintPattern();
				if (cpattern != null && cpattern.length() > 0) {
					pv.add(cpattern);
				}
			}
			sb.append(getAllPattern(pv)).append(getRepeat());
		}
		return sb.toString();
	}

	public boolean isDescendant(SchemaNode ancestor) {
		boolean retval = false;
		SchemaNode p = parent;
		while (p != null) {
			if (p == ancestor) {
				retval = true;
				break;
			}
			p = p.parent;
		}
		return retval;
	}

	public TreeNode getParent() {
		return parent;
	}

	public String getRepeat() {
		String repeat = "";
		int min = getMinOccurs();
		int max = getMaxOccurs();
		if (min == 0 && max == Integer.MAX_VALUE) {
			repeat = "*";
		} else if (min == 0 && max == 1) {
			repeat = "?";
		} else if (min == 1 && max == Integer.MAX_VALUE) {
			repeat = "+";
		} else if (min != 1 || max != 1) {
			repeat = "{" + min + "," + max + "}";
		}
		return repeat;
	}

	public String getAllPattern(Vector<String> v) {
		String[] sa = new String[v.size()];
		v.toArray(sa);
		StringBuffer sb = new StringBuffer();
		int count = sa.length;
		sb.append('(');
		for (int i = 0; i < count; i++) {
			sb.append('(');
			for (int j = 0; j < count; j++) {
				sb.append(sa[(j + i) % count]);
			}
			sb.append(')');
			if (i < count - 1)
				sb.append('|');
		}
		sb.append(')');
		return sb.toString();
	}

	public Vector<SchemaNode> getFirstSet() {
		Vector<SchemaNode> ret = new Vector<SchemaNode>(4, 4);
		String nodeName = getNodeName();
		if (nodeName.equals("element") || nodeName.equals("any")) {
			ret.add(this);
		} else if (nodeName.equals("sequence")) {
			int count = childCount();
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				Vector<SchemaNode> firstSet = child.getFirstSet();
				for (int j = 0; j < firstSet.size(); j++) {
					ret.add(firstSet.elementAt(j));
				}
				if (child.isNillable())
					continue;
				else
					break;
			}
		} else if (nodeName.equals("all") || nodeName.equals("choice")) {
			int count = childCount();
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				Vector<SchemaNode> firstSet = child.getFirstSet();
				for (int j = 0; j < firstSet.size(); j++) {
					ret.add(firstSet.elementAt(j));
				}
			}
		} else if (nodeName.equals("group")) {
			int count = childCount();
			for (int i = 0; i < count; i++) {
				SchemaNode child = child(i);
				String childNodeName = child.getNodeName();
				if (childNodeName.equals("all")
						|| childNodeName.equals("sequence")
						|| childNodeName.equals("choice")) {
					Vector<SchemaNode> firstSet = child.getFirstSet();
					for (int j = 0; j < firstSet.size(); j++) {
						ret.add(firstSet.elementAt(j));
					}
					break;
				}
			}
		}
		return ret;
	}

	public String getAttribute(String name) {
		return attributes.getProperty(name);
	}

	public String getTagName() {
		return getAttribute("name");
	}

	public String getRefName() {
		return getAttribute("ref");
	}

	public String getTypeName() {
		return getAttribute("type");
	}

	public int getMinOccurs() {
		return (minOccurs != null && minOccurs.intValue() >= 0 ? minOccurs
				.intValue() : 1);
	}

	public void setMaxOccurs(int occurs) {
		maxOccurs = new Integer(occurs);
	}

	public void setMinOccurs(int occurs) {
		minOccurs = new Integer(occurs);
	}

	public int getLeafMinOccurs() {
		int min = getMinOccurs();
		SchemaNode parent = this;
		while ((parent = (SchemaNode) parent.getParent()) != null) {
			int pmin = parent.getMinOccurs();
			min = Math.min(min, pmin);
		}
		return min;
	}

	public int getLeafMaxOccurs() {
		int max = getMaxOccurs();
		SchemaNode parent = this;
		while ((parent = (SchemaNode) parent.getParent()) != null) {
			int pmax = parent.getMaxOccurs();
			max = Math.max(max, pmax);
		}
		return max;
	}

	public int getMaxOccurs() {
		return (maxOccurs != null && maxOccurs.intValue() >= 0 ? maxOccurs
				.intValue() : 1);
	}

	private Integer getMaxOccurs(org.w3c.dom.Node node) {
		Integer max = null;
		try {
			NamedNodeMap attrs = node.getAttributes();
			Node maxOccursNode = attrs.getNamedItem("maxOccurs");
			if (maxOccursNode != null) {
				String value = maxOccursNode.getNodeValue();
				if (value.equals("unbounded"))
					max = new Integer(Integer.MAX_VALUE);
				else
					max = new Integer(maxOccursNode.getNodeValue());
			}
		} catch (Exception e) {
		}
		return max;
	}

	public boolean isMinOccurs() {
		return minOccurs != null;
	}

	public boolean isMaxOccurs() {
		return maxOccurs != null;
	}

	private boolean getMixed(org.w3c.dom.Node node) {
		boolean retval = false;
		try {
			NamedNodeMap attrs = node.getAttributes();
			Node mn = attrs.getNamedItem("mixed");
			if (mn != null) {
				retval = new Boolean(mn.getNodeValue()).booleanValue();
				empty = !mixed;
			}
		} catch (Exception e) {
		}
		return retval;
	}

	private boolean isAnyOrOtherNamespace(org.w3c.dom.Node node) {
		boolean retval = false;
		try {
			org.w3c.dom.Element anyElement = (org.w3c.dom.Element) node;
			String namespace = anyElement.getAttribute("namespace");
			if (namespace.equals("##any") || namespace.equals("##other"))
				retval = true;
			else {
				if (getTagName().equals("any"))
					retval = true;
			}
		} catch (Exception e) {
		}
		return retval;
	}

	public SchemaNode getNodeByName(String name) {
		Stack<SchemaNode> nodeStack = new Stack<SchemaNode>();
		HashMap<SchemaNode, Stack<Integer>> stackMap = new HashMap<SchemaNode, Stack<Integer>>(
				17, 0.85f);

		nodeStack.push(this);

		while (!nodeStack.empty()) {
			SchemaNode node = (SchemaNode) nodeStack.peek();
			String nodeName = node.getNodeName();

			if (CommonUtils.equal(nodeName, name))
				return node;

			int count = node.childCount();
			if (count == 0) {
				nodeStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(node);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(node, indexStack);
					nodeStack.push(node.child(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						nodeStack.push(node.child(top));
						indexStack.push(new Integer(top + 1));
						continue;
					} else {
						Object key = nodeStack.pop();
						stackMap.remove(key);
						continue;
					}
				}
			}
		}

		return null;
	}

	private Integer getMinOccurs(org.w3c.dom.Node node) {
		Integer min = null;
		try {
			NamedNodeMap attrs = node.getAttributes();
			Node mon = attrs.getNamedItem("minOccurs");
			if (mon != null) {
				min = new Integer(mon.getNodeValue());
			}
		} catch (Exception e) {
		}
		return min;
	}

	private void getAttributes(org.w3c.dom.Node node) {
		try {

			NamedNodeMap attrs = node.getAttributes();
			int count = (attrs != null ? attrs.getLength() : 0);
			for (int i = 0; i < count; i++) {
				Attr an = (Attr) attrs.item(i);
				attributes.setProperty(an.getName(), an.getValue());
				if (an.getName().equals("type"))
					empty = false;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init(org.w3c.dom.Node node, XMLSchema schema) {
		this.domNode = node;
		empty = true;
		this.schema = schema;
		qualifiedNodeName = node.getNodeName();
		childrenVector = new Vector<SchemaNode>(8, 4);
		attributesVector = new Vector<Node>(4, 4);
		getAttributes(node);
		nodeType = node.getNodeType();
		nodeValue = node.getNodeValue();
		minOccurs = getMinOccurs(node);
		maxOccurs = getMaxOccurs(node);
		mixed = getMixed(node);
		anyNamespace = isAnyOrOtherNamespace(node);
		documentation = CommonUtils.getDocumentationSource(node);
		String nodeName = getNodeName();
		simpleContent = CommonUtils.isSimpleContent(node);
		if (nodeName.equals("simpleType") || isSimpleContent()) {
			typeDef = new TypeDefinition(node, schema);
		} else if (getTypeName() != null) {
			Element ele = (Element) node;
			typeDef = new TypeDefinition(ele.getAttributeNode("type"), schema);
		}
		this.insertPos = -1;
	}

	public SchemaNode getTypeNode() {
		if (typeNode != null)
			return typeNode;

		Stack<SchemaNode> nodeStack = new Stack<SchemaNode>();
		HashMap<SchemaNode, Stack<Integer>> stackMap = new HashMap<SchemaNode, Stack<Integer>>(
				17, 0.85f);

		nodeStack.push(this);

		while (!nodeStack.empty()) {
			SchemaNode node = (SchemaNode) nodeStack.peek();

			if (node.getTypeName() != null) {
				SchemaNode tnode = node.getSchema().getTypeSchemaNode(
						node.getTypeName());
				if (tnode != null) {
					typeNode = tnode;
					return typeNode;
				} else {
					return node;
				}
			}

			String nodeName = node.getNodeName();
			if (nodeName.equals("simpleType") || nodeName.equals("complexType")) {
				typeNode = node;
				return typeNode;
			}
			int count = node.childCount();
			if (count == 0) {
				nodeStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(node);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(node, indexStack);
					nodeStack.push(node.child(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						nodeStack.push(node.child(top));
						indexStack.push(new Integer(top + 1));
						continue;
					} else {
						Object key = nodeStack.pop();
						stackMap.remove(key);
						continue;
					}
				}
			}
		}

		return null;
	}

	public XMLSchema getSchema() {
		return schema;
	}

	private SchemaNode(XMLSchema schema, String name, String value, int type,
			int minOccurs, int maxOccurs) {
		this.schema = schema;
		qualifiedNodeName = name;
		nodeValue = value;
		nodeType = type;
		this.minOccurs = new Integer(minOccurs);
		this.maxOccurs = new Integer(maxOccurs);
		childrenVector = new Vector<SchemaNode>(8, 4);
		attributesVector = new Vector<Node>(4, 4);
	}

	private SchemaNode(org.w3c.dom.Node node, XMLSchema schema,
			boolean visitSubTree) {
		init(node, schema);

		if (visitSubTree) {
			if (getNodeName().equals("complexType") && !isSimpleContent()) {
				buildCanonicalComplexType(node);
			} else
				visitSubTree(node);
		} else {
			if (getRefName() == null && getTypeName() == null)
				resolve = node;
		}
	}

	private void visitSubTree(org.w3c.dom.Node node) {
		if (nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			if (element.getAttributes() != null) {
				NamedNodeMap attrs = element.getAttributes();
				for (int k = 0; k < attrs.getLength(); k++) {
					Attr attr = (Attr) attrs.item(k);
					if (attr.getName().equals("ref")
							|| attr.getName().equals("name"))
						continue;

					add(new SchemaNode(attr, schema, false));
				}
			}
		}
		org.w3c.dom.NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);

			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String childNodeName = CommonUtils.getUnqualifiedNodeName(child);
			if (childNodeName.equals("attributeGroup")
					|| childNodeName.equals("attribute")
					|| childNodeName.equals("anyAttribute")) {
				attributesVector.add(child);
			} else if (childNodeName.equals("complexType")
					&& !CommonUtils.isSimpleContent(child)) {
				add(getCanonicalComplexType(child));
			} else if (childNodeName.equals("element")
					|| childNodeName.equals("any")) {
				add(new SchemaNode(child, schema, false));
			} else if (!childNodeName.equals("annotation")) {
				add(new SchemaNode(child, schema));
			}
		}
	}

	public void resolve() {
		visitSubTree(resolve);
		resolve = null;
	}

	public Vector<Node> getAttributes() {
		return attributesVector;
	}

	// Construct an Adapter node from a DOM node
	public SchemaNode(org.w3c.dom.Node node, XMLSchema schema) {
		init(node, schema);
		if (getNodeName().equals("complexType") && !isSimpleContent()) {
			buildCanonicalComplexType(node);
			return;
		} else
			visitSubTree(node);
	}

	public void add(SchemaNode node) {
		childrenVector.add(node);
		node.parent = this;
		if (empty && node.getNodeType() != Node.ATTRIBUTE_NODE)
			empty = false;
	}

	private void insertElementAt(SchemaNode node, int pos) {
		childrenVector.insertElementAt(node, pos);
		node.parent = this;
	}

	public void remove(SchemaNode child) {
		childrenVector.remove(child);
		child.parent = null;
	}

	public SchemaNode getMatchingNode(String name) {
		SchemaNode snode = null;
		getTerminals();
		int count = terminalsVector.size();
		for (int i = 0; i < count; i++) {
			SchemaNode node = (SchemaNode) terminalsVector.elementAt(i);
			String ename = node.toString();
			if (ename != null && CommonUtils.equal(ename, name)) {
				snode = node;
				break;
			}
		}

		return snode;
	}

	public String getPrintTerminals() {
		StringBuffer sb = new StringBuffer();

		getTerminals();
		sb.append("{");
		int count = (terminalsVector != null ? terminalsVector.size() : 0);
		for (int i = 0; i < count; i++)
			sb.append(((SchemaNode) terminalsVector.elementAt(i)).toString());
		sb.append("}");
		return sb.toString();
	}

	private String getPrefix(String name) {
		String prefix = null;
		try {
			prefix = name.substring(0, name.indexOf(":"));
		} catch (Exception e) {
		}
		return prefix;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (nodeType == Node.ATTRIBUTE_NODE) {
			sb.append(qualifiedNodeName).append('=').append(nodeValue);
		} else if (nodeType == Node.ELEMENT_NODE) {
			String retval = getNodeName();

			if (retval.equals("element")) {
				if (getTagName() != null)
					retval = getTagName();
				else if (getRefName() != null) {
					retval = getRefName();
				}
			}

			if (retval.equals("any") || isAnyNamespace()) {
				sb.append(ANY_ELEMENT);
			} else {

				String prefix = getPrefix(retval);

				if (prefix == null) {
					sb.append(CommonUtils.getNameWithNamespace(retval, schema
							.getTargetNameSpace()));
				} else {
					sb.append(CommonUtils.getNameWithNamespace(retval, schema
							.getUri(prefix)));
				}
			}
		}
		return sb.toString();
	}

	private SchemaNode getCanonicalComplexType(SchemaNode snode) {
		Node complexType = snode.domNode;
		Node complexContent = CommonUtils.getChildByName(complexType,
				"complexContent");
		SchemaNode complexTypeSchemaNode = null;
		if (complexContent != null) {
			complexTypeSchemaNode = new SchemaNode(complexType, snode
					.getSchema(), false);
			Node extension = CommonUtils.getNodeByName(complexContent,
					"extension");
			if (extension != null) {
				addExtension(complexTypeSchemaNode, extension);
			} else {
				Node restriction = CommonUtils.getNodeByName(complexContent,
						"restriction");
				if (restriction != null) {

					addRestriction(complexTypeSchemaNode, restriction);
				}
			}
		} else
			complexTypeSchemaNode = new SchemaNode(complexType, snode
					.getSchema());
		resolveReferences(complexTypeSchemaNode);
		// complexTypeSchemaNode.printTerms();
		return complexTypeSchemaNode;
	}

	private SchemaNode getCanonicalComplexType(Node node) {
		Node complexType = node;
		Node complexContent = CommonUtils.getChildByName(complexType,
				"complexContent");
		SchemaNode complexTypeSchemaNode = null;
		if (complexContent != null) {
			complexTypeSchemaNode = new SchemaNode(node, schema, false);
			Node extension = CommonUtils.getNodeByName(complexContent,
					"extension");
			if (extension != null) {
				addExtension(complexTypeSchemaNode, extension);
			} else {
				Node restriction = CommonUtils.getNodeByName(complexContent,
						"restriction");
				if (restriction != null) {

					addRestriction(complexTypeSchemaNode, restriction);
				}
			}
		} else
			complexTypeSchemaNode = new SchemaNode(node, schema);
		resolveReferences(complexTypeSchemaNode);
		// complexTypeSchemaNode.printTerms();
		return complexTypeSchemaNode;
	}

	private void buildCanonicalComplexType(Node node) {
		Node complexType = node;
		Node complexContent = CommonUtils.getChildByName(complexType,
				"complexContent");
		if (complexContent != null) {
			Node extension = CommonUtils.getNodeByName(complexContent,
					"extension");
			if (extension != null) {
				addExtension(this, extension);
			} else {
				Node restriction = CommonUtils.getNodeByName(complexContent,
						"restriction");
				if (restriction != null)
					addRestriction(this, restriction);
			}
		} else
			visitSubTree(node);
		resolveReferences(this);
	}

	private void addRestriction(SchemaNode complexType, Node restriction) {
		Vector<Node> attrs = complexType.getAttributes();

		String base = ((org.w3c.dom.Element) restriction).getAttribute("base");
		if (base != null && base.length() > 0) {
			SchemaNode baseNode = complexType.getSchema()
					.getComplexTypeSchemaNode(base);

			SchemaNode baseSchemaNode = getCanonicalComplexType(baseNode);
			attrs.addAll(baseSchemaNode.getAttributes());
		}

		NodeList nodeList = restriction.getChildNodes();
		int nchild = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < nchild; i++) {
			Node child = nodeList.item(i);
			if (child == null || child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String childNodeName = CommonUtils.getUnqualifiedNodeName(child);

			if (childNodeName.equals("attributeGroup")
					|| childNodeName.equals("attribute")
					|| childNodeName.equals("anyAttribute")) {
				attrs.add(child);
			} else {
				complexType.add(new SchemaNode(child, complexType.getSchema()));
			}
		}
	}

	private void addExtension(SchemaNode complexType, Node extension) {

		Vector<Node> attrs = complexType.getAttributes();
		SchemaNode newSequence = new SchemaNode(complexType.getSchema(),
				"sequence", null, Node.ELEMENT_NODE, 1, 1);

		String base = ((org.w3c.dom.Element) extension).getAttribute("base");

		if (base != null && base.length() > 0) {
			SchemaNode baseNode = complexType.getSchema()
					.getComplexTypeSchemaNode(base);
			if (baseNode == null) {

				System.out.println("base node is null:" + base + ":"
						+ complexType.getSchema().getTargetNameSpace());
			}
			SchemaNode baseSchemaNode = getCanonicalComplexType(baseNode);

			int nchild = baseSchemaNode.childCount();
			for (int i = 0; i < nchild; i++) {
				SchemaNode child = baseSchemaNode.child(i);

				if (child.getNodeType() == Node.ELEMENT_NODE) {
					newSequence.add(child);
				}
			}
			attrs.addAll(baseSchemaNode.getAttributes());

			NodeList nodeList = extension.getChildNodes();
			nchild = (nodeList != null ? nodeList.getLength() : 0);
			for (int i = 0; i < nchild; i++) {
				Node child = nodeList.item(i);
				if (child == null)
					continue;
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					String name = CommonUtils.getUnqualifiedNodeName(child);
					if (name.equals("attribute")
							|| name.equals("attributeGroup")
							|| name.equals("anyAttribute")) {
						attrs.add(child);
					} else {
						newSequence.add(new SchemaNode(child, complexType
								.getSchema()));
					}
				}
			}
		}

		complexType.add(newSequence);
	}

	private void resolveReferences(SchemaNode node) {
		int nchild = node.childCount();
		for (int i = 0; i < nchild; i++) {
			SchemaNode child = node.child(i);
			if (child.getNodeType() != Node.ELEMENT_NODE
					|| child.getNodeName().equals("element")
					|| child.getNodeName().equals("any"))
				continue;

			String ref = child.getRefName();
			if (ref != null) {
				SchemaNode newSchemaNode = node.getSchema()
						.resolveReferenceSchemaNode(child.getNodeName(), ref);
				if (newSchemaNode == null) {
					continue;
				}

				if (newSchemaNode.getNodeType() == Node.ELEMENT_NODE) {
					if (!newSchemaNode.isMinOccurs())
						newSchemaNode.setMinOccurs(child.getMinOccurs());
					if (!newSchemaNode.isMaxOccurs())
						newSchemaNode.setMaxOccurs(child.getMaxOccurs());
				}

				node.replace(newSchemaNode, child);
				child = newSchemaNode;
			}
			resolveReferences(child);

		}
	}

	public SchemaNode getChildByName(SchemaNode root, String name) {
		int count = root.childCount();
		for (int i = 0; i < count; i++) {
			SchemaNode child = root.child(i);
			if (CommonUtils.equal(child.getNodeName(), name))
				return child;
		}
		return null;
	}

	/*
	 * Return children, index, and count values
	 */
	public int index(SchemaNode child) {
		for (int i = 0; i < childrenVector.size(); i++) {
			if (childrenVector.elementAt(i) == child)
				return i;
		}
		return -1; // Should never get here.
	}

	public SchemaNode child(int searchIndex) {
		SchemaNode retval = null;

		try {
			retval = (SchemaNode) childrenVector.elementAt(searchIndex);
		} catch (Exception e) {
		}

		return retval;
	}

	public int childCount() {
		return childrenVector.size();
	}

	/**
	 * Compares this object with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * 
	 * In the foregoing description, the notation <tt>sgn(</tt> <i>expression
	 * </i> <tt>)</tt> designates the mathematical <i>signum </i> function,
	 * which is defined to return one of <tt>-1</tt>,<tt>0</tt>, or <tt>1</tt>
	 * according to whether the value of <i>expression </i> is negative, zero or
	 * positive.
	 * 
	 * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>. (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 * <p>
	 * 
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 * <p>
	 * 
	 * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for all
	 * <tt>z</tt>.
	 * <p>
	 * 
	 * It is strongly recommended, but <i>not </i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates this
	 * condition should clearly indicate this fact. The recommended language is
	 * "Note: this class has a natural ordering that is inconsistent with
	 * equals."
	 * 
	 * @param o
	 *            the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 * 
	 * @throws ClassCastException
	 *             if the specified object's type prevents it from being
	 *             compared to this Object.
	 * 
	 */
	public int compareTo(SchemaNode o) {
		return toString().compareTo(o.toString());
	}

}
