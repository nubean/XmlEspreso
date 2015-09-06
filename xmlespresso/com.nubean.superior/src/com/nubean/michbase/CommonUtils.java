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

package com.nubean.michbase;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.w3c.dom.*;

import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.SchemaTreeModel;
import com.nubean.michxml.XMLModel;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLSchema;
import com.nubean.michxml.XMLTreeModel;
import com.nubean.michxml.XMLWriter;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

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

public class CommonUtils {

	public static String[] encodings = { "UTF-8", "US-ASCII", "UTF-16",
			"ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4",
			"ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
			"ISO-8859-9", "ISO-2022-JP", "SHIFT_JIS", "EUC-JP", "GB2312",
			"BIG5", "EUC-KR", "ISO-2022-KR", "KO18-R", "EBCDIC-CP-US",
			"EBCDIC-CP-CA", "EBCDIC-CP-NC", "EBCDIC-CP-DK", "EBCDIC-CP-NO",
			"EBCDIC-CP-FI", "EBCDIC-CP-SE", "EBCDIC-CP-IT", "EBCDIC-CP-ES",
			"EBCDIC-CP-GB" };

	public static String[] extensions = { ".xml", ".xsl", ".xslt", ".xs",
			".xsd", ".tld", ".html", ".xhtml", ".jsp", ".jspx", ".wsdl",
			".jsf", ".xjb" };

	public static String[] stringTypes = { "string", "normalizedString",
			"token", "NCName", "language" };

	public static String[] numericTypes = { "float", "double", "decimal",
			"integer", "long", "int", "short", "byte", "positiveInteger",
			"nonPositiveInteger", "negativeInteger", "nonNegativeInteger",
			"usignedLong", "unsignedInt", "unsignedShort", "usignedByte" };

	public static String[] dateTimeTypes = { "date", "time", "dateTime",
			"gYear", "gYearMonth", "gYear", "gMonth", "gMonthDay", "gDay" };

	public static String[] legacyTypes = { "ID", "IDREF", "IDREFS", "ENTITY",
			"ENTITIES", "NMTOKEN", "NMTOKENS", "NOTATION" };

	public static String[] otherTypes = { "QName", "boolean", "hexBinary",
			"base64Binary", "anyURI" };

	public static char[] regexpReserved = { '.', '*', '?', '+', '{', '}', '[',
			']', '(', ')', '\\', '-', '^', '$', '!', '>', '<', '=', ':', '|',
			'&', '\'', ',' };

	public static final String ANY_ELEMENT = CommonUtils.getNameWithNamespace(
			"any", "##any");

	public static final String XMLNS_PREFIX = XMLConstants.XMLNS_ATTRIBUTE + ":";
	
	public static boolean isREReservedChar(char c) {
		int count = regexpReserved.length;
		for (int i = 0; i < count; i++) {
			if (regexpReserved[i] == c)
				return true;
		}
		return false;
	}

	public static boolean contains(String[] array, String value) {
		int count = array.length;
		for (int i = 0; i < count; i++) {
			if (array[i].equals(value))
				return true;
		}
		return false;
	}

	public static String unescape(String value) {
		String ret = replace("&amp;", "&", value);
		ret = replace("&lt;", "<", ret);
		ret = replace("&gt;", ">", ret);
		return ret;
	}

	public static String escape(String value) {
		String ret = replace("&", "&amp;", value);
		ret = replace("<", "&lt;", ret);
		ret = replace(">", "&gt;", ret);
		return ret;
	}

	public static String replace(String s1, String s2, String value) {
		int index = value.indexOf(s1);
		if (index == -1)
			return value;

		StringBuffer sb = new StringBuffer(value);

		int len = s1.length();

		sb.replace(index, index + len, s2);
		return sb.toString();
	}

	public static boolean isNumericType(String type) {
		return contains(numericTypes, type);
	}

	public static boolean isStringType(String type) {
		return contains(stringTypes, type);
	}

	public static boolean isDateTimeType(String type) {
		return contains(dateTimeTypes, type);
	}

	public static boolean isLegacyType(String type) {
		return contains(legacyTypes, type);
	}

	public static boolean isOtherType(String type) {
		return contains(otherTypes, type);
	}

	public static String getUnqualifiedNodeName(org.w3c.dom.Node node) {
		String retval = node.getNodeName();
		retval = retval.substring(retval.indexOf(":") + 1);
		return retval;
	}

	public static String getUnqualifiedNodeName(String name) {
		String retval = name;
		retval = retval.substring(retval.indexOf(":") + 1);
		return retval;
	}

	public static String getHexString(Character c) {
		int ival = Character.getNumericValue(c.charValue());
		StringBuffer sb = new StringBuffer(Integer.toHexString(ival));
		int count = sb.length();
		for (int i = count; i < 4; i++) {
			sb.insert(0, '0');
		}
		sb.insert(0, "\\u");
		return sb.toString();
	}

	public static String expandTabs(int ntabs) {
		StringBuffer tabs = new StringBuffer();
		for (int i = 0; i < ntabs; i++)
			tabs.append(' ');
		return tabs.toString();
	}

	public static void copyToFile(InputStream input, File to)
			throws java.io.IOException {
		FileOutputStream os = new FileOutputStream(to);
		byte[] buf = new byte[1024];
		int nread = -1;

		while ((nread = input.read(buf)) > 0) {
			os.write(buf, 0, nread);
		}
		os.close();
	}

	public static String getUnqualifiedElementName(String name) {
		String element = null;

		if (name != null) {
			StringTokenizer st = new StringTokenizer(name, " ");
			if (st.hasMoreTokens())
				element = st.nextToken();
		}

		return element;
	}

	public static String getNameWithNamespace(String name, String namespace) {
		StringBuffer sb = new StringBuffer(64);

		if (name != null)
			sb.append(CommonUtils.getUnqualifiedNodeName(name));
		if (namespace != null) {
			sb.append(" {").append(namespace).append("}");
		} else {
			sb.append(" {}");
		}
		return sb.toString();

	}

	public static boolean isFirstLineXmlProcInstr(File file) {
		boolean ret = false;

		try {
			InputStream is = new FileInputStream(file);
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			String line = br.readLine().trim();
			br.close();

			ret = (line.startsWith("<?") && line.indexOf("xml") > 1 && line
					.endsWith("?>"));
		} catch (Exception e) {

		}

		return ret;
	}

	public static org.w3c.dom.Attr[] sortAttributeNodes(
			org.w3c.dom.NamedNodeMap attrs) {

		int len = (attrs != null) ? attrs.getLength() : 0;
		org.w3c.dom.Attr array[] = new org.w3c.dom.Attr[len];
		for (int i = 0; i < len; i++) {
			array[i] = (org.w3c.dom.Attr) attrs.item(i);
		}
		for (int i = 0; i < len - 1; i++) {
			String name = array[i].getNodeName();
			int index = i;
			for (int j = i + 1; j < len; j++) {
				String curName = array[j].getNodeName();
				if (curName.compareTo(name) < 0) {
					name = curName;
					index = j;
				}
			}
			if (index != i) {
				org.w3c.dom.Attr temp = array[i];
				array[i] = array[index];
				array[index] = temp;
			}
		}

		return array;

	}

	public static String encode(java.awt.Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();

		StringBuffer sb = new StringBuffer();
		sb.append("0x");

		String red = Integer.toHexString(r);
		if (red.length() < 2) {
			sb.append("0");
		}
		sb.append(red);

		String green = Integer.toHexString(g);
		if (green.length() < 2) {
			sb.append("0");
		}
		sb.append(green);

		String blue = Integer.toHexString(b);
		if (blue.length() < 2) {
			sb.append("0");
		}
		sb.append(blue);

		return sb.toString();
	}

	public static Node getChildByName(Node root, String name) {
		NodeList nodeList = root.getChildNodes();
		int count = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < count; i++) {
			Node child = nodeList.item(i);
			if (equal(child.getNodeName(), name))
				return child;
		}
		return null;
	}

	public static SchemaNode getChildByName(SchemaNode root, String name) {

		int count = root.childCount();
		for (int i = 0; i < count; i++) {
			SchemaNode child = root.child(i);
			if (equal(child.getNodeName(), name))
				return child;
		}
		return null;
	}

	public static Vector<Node> getChildrenByName(Node root, String name) {

		NodeList nodeList = root.getChildNodes();
		int count = (nodeList != null ? nodeList.getLength() : 0);
		Vector<Node> children = new Vector<Node>(count);
		for (int i = 0; i < count; i++) {
			Node child = nodeList.item(i);
			if (equal(child.getNodeName(), name))
				children.add(child);
		}
		return children;
	}

	public static Node getChildByType(Node root, int type) {
		NodeList nodeList = root.getChildNodes();
		int count = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < count; i++) {
			Node child = nodeList.item(i);
			if (child.getNodeType() == type)
				return child;
		}
		return null;
	}

	public static Node getNodeByName(Node root, String name) {
		if (root == null)
			return null;

		Stack<Node> nodeStack = new Stack<Node>();
		HashMap<Node, Stack<Integer>> stackMap = new HashMap<Node, Stack<Integer>>(
				17, 0.85f);

		nodeStack.push(root);

		while (!nodeStack.empty()) {
			org.w3c.dom.Node node = (org.w3c.dom.Node) nodeStack.peek();
			String nodeName = node.getNodeName();

			if (equal(nodeName, name))
				return node;

			NodeList nodeList = node.getChildNodes();
			int count = (nodeList != null ? nodeList.getLength() : 0);
			if (count == 0) {
				nodeStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(node);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(node, indexStack);
					nodeStack.push(nodeList.item(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						nodeStack.push(nodeList.item(top));
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

	public static Node getNodeByType(Node root, int type) {
		if (root == null)
			return null;

		Stack<Node> nodeStack = new Stack<Node>();
		HashMap<Node, Stack<Integer>> stackMap = new HashMap<Node, Stack<Integer>>(
				17, 0.85f);

		nodeStack.push(root);

		while (!nodeStack.empty()) {
			org.w3c.dom.Node node = (org.w3c.dom.Node) nodeStack.peek();

			if (node.getNodeType() == type)
				return node;

			NodeList nodeList = node.getChildNodes();
			int count = (nodeList != null ? nodeList.getLength() : 0);
			if (count == 0) {
				nodeStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(node);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(node, indexStack);
					nodeStack.push(nodeList.item(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						nodeStack.push(nodeList.item(top));
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

	public static Node getAnnotation(Node root) {
		return CommonUtils.getNodeByName(root, "annotation");
	}

	public static String getDocumentationSource(Node node) {
		String retval = null;

		Node documentation = (node != null ? CommonUtils.getNodeByName(node,
				"documentation") : null);
		if (documentation == null)
			return retval;

		try {
			NamedNodeMap attrs = documentation.getAttributes();
			if (attrs != null) {
				Node attr = attrs.getNamedItem("source");
				if (attr != null) {
					retval = attr.getNodeValue();
				}
			}
		} catch (Exception e) {
		}
		return retval;
	}

	public static String getDocumentation(Node node, String css) {
		String retval = null;

		Node documentation = (node != null ? CommonUtils.getNodeByName(node,
				"documentation") : null);
		if (documentation == null)
			return retval;

		try {
			NamedNodeMap attrs = documentation.getAttributes();
			if (attrs != null) {
				Node attr = attrs.getNamedItem("source");
				if (attr != null) {
					return attr.getNodeValue();
				}
			}
			Node html = CommonUtils.getNodeByName(documentation, "html");
			if (html != null) {
				StringWriter sw = new StringWriter(1024);
				PrintWriter pw = new PrintWriter(sw);
				XMLWriter xmlw = new XMLWriter(pw, true, null);

				xmlw.print(html);
				pw.close();
				retval = sw.toString();
			} else if (documentation.getNodeValue() != null) {
				retval = documentation.getNodeValue();
			}

		} catch (Exception e) {
		}

		try {

			if (retval == null) {
				StringBuffer sb = new StringBuffer();
				NodeList nodeList = documentation.getChildNodes();
				int count = nodeList != null ? nodeList.getLength() : 0;
				for (int i = 0; i < count; i++) {
					Node child = nodeList.item(i);
					if (child.getNodeType() == Node.TEXT_NODE)
						sb.append(child.getNodeValue());
				}

				if (sb.length() > 0) {
					sb.insert(0, "<html><head><style type='text/css'>" + css
							+ "</style></head><body><pre>");
					sb.append("</pre></body></html>");
					retval = sb.toString();
				}
			}
		} catch (Exception e) {
		}
		return retval;
	}

	public static String getFilenameNoSuffix(String name) {
		if (name == null)
			return name;

		int index = -1;
		if ((index = name.indexOf(".")) != -1) {
			return name.substring(0, index);
		}
		return name;
	}

	public static Node getTypeNode(org.w3c.dom.Node root) {
		if (root == null)
			return null;

		Stack<Node> nodeStack = new Stack<Node>();
		HashMap<Node, Stack<Integer>> stackMap = new HashMap<Node, Stack<Integer>>(
				17, 0.85f);

		nodeStack.push(root);

		while (!nodeStack.empty()) {
			org.w3c.dom.Node node = (org.w3c.dom.Node) nodeStack.peek();

			NamedNodeMap attrs = node.getAttributes();
			if (attrs != null) {
				Node type = attrs.getNamedItem("type");
				if (type != null)
					return type;
			}
			String nodeName = CommonUtils.getUnqualifiedNodeName(node);

			if (nodeName.equals("simpleType"))
				return node;
			else if (nodeName.equals("complexType"))
				return node;

			NodeList nodeList = node.getChildNodes();
			int count = (nodeList != null ? nodeList.getLength() : 0);
			if (count == 0) {
				nodeStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(node);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(node, indexStack);
					nodeStack.push(nodeList.item(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						nodeStack.push(nodeList.item(top));
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

	public static Vector<Node> getAttributeNodes(SchemaNode root) {
		if (root == null)
			return null;

		Vector<Node> attrs = new Vector<Node>();

		Vector<Node> v = root.getAttributes();
		for (int i = 0; i < v.size(); i++) {

			org.w3c.dom.Node node = (org.w3c.dom.Node) v.elementAt(i);
			String nodeName = CommonUtils.getUnqualifiedNodeName(node);

			if (nodeName.equals("attribute") || nodeName.equals("anyAttribute")) {
				attrs.add(node);
			} else if (nodeName.equals("attributeGroup")) {
				Vector<Node> attrg = getAttributeGroup(node, root.getSchema());
				if (attrg != null)
					attrs.addAll(attrg);
			}
		}
		return attrs;
	}

	private static Vector<Node> getAttributeGroup(org.w3c.dom.Node node,
			XMLSchema schema) {
		NamedNodeMap attrs = node.getAttributes();
		Attr name = (Attr) attrs.getNamedItem("name");
		if (name == null) {
			Attr ref = (Attr) attrs.getNamedItem("ref");
			String refName = ref.getValue();
			node = schema.getAttributeGroup(refName);
		}
		Vector<Node> attrv = new Vector<Node>(8, 8);
		NodeList nodeList = node.getChildNodes();
		int nchild = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < nchild; i++) {
			org.w3c.dom.Node child = nodeList.item(i);
			String nodeName = CommonUtils.getUnqualifiedNodeName(child);
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
					&& (nodeName.equals("attribute") || nodeName
							.equals("anyAttribute")))
				attrv.add(child);
		}
		return attrv;
	}

	public static boolean isMixed(org.w3c.dom.Node node) {
		boolean retval = false;
		if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			retval = ((org.w3c.dom.Element) node).getAttribute("mixed").equals(
					"true");
		}
		return retval;
	}

	public static String getContent(org.w3c.dom.Node node) {
		StringBuffer sb = new StringBuffer();
		NodeList nodeList = node.getChildNodes();
		int count = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < count; i++) {
			org.w3c.dom.Node child = nodeList.item(i);
			if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
				sb.append(child.getNodeValue());
		}
		return sb.toString();
	}

	public static InputStream parseURL(ClassLoader classLoader, String uri)
			throws java.io.IOException, java.net.MalformedURLException {
		URL url = null;
		InputStream input = null;
		if (uri.startsWith("http://") || uri.startsWith("file:///")) {
			url = new URL(uri);
			URLConnection connection = url.openConnection();
			input = connection.getInputStream();
		} else {
			url = classLoader.getResource(uri);
			URLConnection connection = url.openConnection();
			input = connection.getInputStream();
		}

		return input;
	}

	public static String parseRelativeUri(String puri, String ruri) {
		String uri = null;
		ruri = ruri.trim();
		puri = puri.trim();
		if (ruri.startsWith("http://") || ruri.startsWith("file:///")) {
			uri = ruri;
		} else {
			String sep = File.separator;
			int index = puri.lastIndexOf(sep);
			if (index == -1)
				index = puri.lastIndexOf("/");

			uri = puri.substring(0, index + 1) + ruri;
		}
		return uri;
	}

	public static boolean isQualified(String name) {
		return (name != null && name.indexOf(":") > 0);
	}

	public static boolean equal(String ename, String tname) {
		boolean retval = false;
		if (ename != null && tname != null) {
			if (CommonUtils.isQualified(tname)
					&& CommonUtils.isQualified(ename)) {
				retval = ename.equals(tname);
			} else {
				retval = CommonUtils.getUnqualifiedNodeName(ename).equals(
						CommonUtils.getUnqualifiedNodeName(tname));
			}
		}
		return retval;
	}

	public static boolean isSimpleContent(org.w3c.dom.Node node) {
		String nodeName = getUnqualifiedNodeName(node);
		return nodeName.equals("complexType")
				&& (getChildByName(node, "simpleContent") != null);
	}

	public static Vector<SchemaNode> getInsertableElements(XMLModel xmlModel,
			SchemaTreeModel stm, XMLTreeModel etm, int proposedPos) {
		HashSet<String> set = new HashSet<String>(23, 0.87f);
		Vector<SchemaNode> isn = new Vector<SchemaNode>(32, 4);
		try {
			Vector<SchemaNode> terms = stm.getTerminals();
			int count = (terms != null ? terms.size() : 0);

			String pattern = stm.getPattern();
			RegExp re = new RegExp(pattern);
			Automaton automata = re.toAutomaton();

			pattern = "^" + pattern + "$";
			Pattern compile = Pattern.compile(pattern);

			for (int i = 0; i < count; i++) {
				SchemaNode snode = (SchemaNode) terms.elementAt(i);
				String sname = snode.toString();
				if (getInsertBefore(xmlModel, compile, automata, etm, snode,
						proposedPos) >= 0 && !set.contains(sname)) {
					isn.add(snode);
					set.add(sname);
				}
			}
		} catch (Exception e) {

		}
		return isn;
	}

	public static int getInsertBefore(XMLModel xmlModel, Pattern compile,
			Automaton automata, XMLTreeModel etm, SchemaNode typeNode,
			int proposedPos) {

		int pos = -1;

		try {
			StringBuffer match = new StringBuffer(etm.getPattern());

			
			Character cc = (typeNode.getTld() == null ? xmlModel
					.getCharCode(typeNode.toString()) : xmlModel
					.getCharCode(ANY_ELEMENT));

			
			
			if (proposedPos >= 0) {
				match.insert(proposedPos, cc.charValue());
//				System.out.println("match:"+ match +" to " + compile.pattern() +":"+ proposedPos +":"+ cc);
				Matcher matcher = compile.matcher(match.toString());
				if (matcher.matches()) {
					pos = proposedPos;
				} else {
					if (partialMatch(automata, match.toString())) {
						pos = proposedPos;
					}
				}
			} else {
				int count = match.length();
				for (int i = count; i >= 0; i--) {
					match.insert(i, cc.charValue());

//					System.out.println("try match:"+ match +" to " + compile.pattern() +":"+ proposedPos +":"+ cc);
					
					Matcher matcher = compile.matcher(match.toString());
					if (matcher.matches()) {
						pos = etm.getCanonicalPosition(i);
//						System.out.println("complete match:"+ pos);
						break;
					} else {
						if (partialMatch(automata, match.toString())) {
							pos = etm.getCanonicalPosition(i);
//							System.out.println("partial match:"+ pos);
							break;
						} else {
							match.replace(i, i + 1, "");
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return pos;
	}

	private static boolean partialMatch(Automaton automata, String input) {
		boolean ret = true;
		RunAutomaton ra = new RunAutomaton(automata);
		int state = ra.getInitialState();

		for (int i = 0; i < input.length(); i++) {
			state = ra.step(state, input.charAt(i));
			if (state == -1) {
				ret = false;
				break;
			}
		}

		return ret;
	}

	private static String getPrefix(String name) {
		String prefix = null;
		try {
			prefix = name.substring(0, name.indexOf(":"));
		} catch (Exception e) {
		}
		return prefix;
	}

	public static void setCharCode(XMLModel xmlModel, SchemaNode node) {
		Vector<SchemaNode> terminals = node.getTerminals();
		// System.out.println("setting character code for: "
		// + node.getPrintTerminals());
		int count = terminals.size();
		for (int i = 0; i < count; i++) {
			SchemaNode child = terminals.elementAt(i);
			Character cc = xmlModel.getCharCode(child.toString());
			if (child.getCharCode() == null)
				child.setCharCode(cc);
		}
	}

	public static SchemaNode getTypeNode(XMLModel xmlModel, XMLNode lnode,
			boolean standalone) {
		SchemaNode node = getSchemaNode(xmlModel, lnode, standalone)
				.getTypeNode();
		setCharCode(xmlModel, node);
		return node;
	}

	public static SchemaNode getSchemaNode(XMLModel xmlModel, XMLNode lnode,
			boolean standalone) {
		SchemaNode node = lnode.getSchemaNode();
		if (node == null) {
			String key = lnode.getDomNode().getNodeName();
			XMLNode parent = (XMLNode) lnode.getParent();
			String namespaceURI = lnode.getNamespace();
			if (namespaceURI == null) {
				if (xmlModel.getSchema() != null) {
					namespaceURI = xmlModel.getSchema().getTargetNameSpace();
				}
			} else if (namespaceURI.trim().length() == 0) {
				namespaceURI = null;
			}
			if (parent != null) {
				SchemaNode pnode = parent.getSchemaNode();
				if (pnode != null) {

					node = pnode.getMatchingNode(CommonUtils
							.getNameWithNamespace(key, namespaceURI));
				}
			}
			if (node == null) {
				String name = (!standalone ? lnode.toString() : "any");
				if (xmlModel.getSchema() != null) {
					node = xmlModel.getSchema().getElementSchemaNode(
							namespaceURI, name);
				}
			}
		}

		if (node != null && node.getTagName() == null) {
			String nname = node.getNodeName();
			String refName = node.getRefName();
			if (refName != null) {
				String prefix = getPrefix(refName);

				if (prefix != null && xmlModel.getSchema() != null)
					node = xmlModel.getSchema().resolveReferenceSchemaNode(
							nname, refName);
				else if (node.getSchema() != null)
					node = node.getSchema().resolveReferenceSchemaNode(nname,
							refName);
			} else {
				if (nname.equals("any") && standalone) {
					node = xmlModel.getSchema().getElementSchemaNode(nname);
				}
			}
		}

		if (node == null || (!standalone && node.getNodeName().equals("any"))) {
			if (xmlModel.getSchema() != null) {
				Node enode = xmlModel.getSchema().getAnyTypeNode();
				node = new SchemaNode(enode, xmlModel.getSchema());
			}
		}
		if (!node.isResolved())
			node.resolve();

		if (node.getCharCode() == null)
			node.setCharCode(xmlModel.getCharCode(node.toString()));
		lnode.setSchemaNode(node);

		return node;
	}

	public static final String addEscapes(String str) {
		StringBuffer retval = new StringBuffer();
		char ch;
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case 0:
				continue;
			case '\b':
				retval.append("\\b");
				continue;
			case '\t':
				retval.append("\\t");
				continue;
			case '\n':
				retval.append("\\n");
				continue;
			case '\f':
				retval.append("\\f");
				continue;
			case '\r':
				retval.append("\\r");
				continue;
			case '\"':
				retval.append("\\\"");
				continue;
			case '\'':
				retval.append("\\\'");
				continue;
			case '\\':
				retval.append("\\\\");
				continue;
			default:
				if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
					String s = "0000" + Integer.toString(ch, 16);
					retval.append("\\u"
							+ s.substring(s.length() - 4, s.length()));
				} else {
					retval.append(ch);
				}
				continue;
			}
		}
		return retval.toString();
	}

	public static Object deserialize(String xml) {
		Object obj = null;
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(xml.getBytes());

			org.w3c.dom.Document doc = null;

			synchronized (XMLBuilder.class) {
				XMLBuilder.nonValidatingBuilder.setErrorHandler(null);
				doc = (org.w3c.dom.Document) XMLBuilder.nonValidatingBuilder
						.parse(bi);
			}
			Element element = doc.getDocumentElement();
			String className = element.getAttribute("class");

			Class klass = Class.forName(className);
			obj = klass.newInstance();
			Method m = klass.getMethod("readElement",
					new Class[] { org.w3c.dom.Element.class });
			m.invoke(obj, new Object[] { element });

		} catch (Exception e) {
		}
		return obj;
	}

	public static String getMimeType(File file) {
		String mimeType = "text";
		try {
			String fname = file.getName();
			int index = fname.lastIndexOf(".");
			if (index > 0) {
				String ext = fname.substring(index);
				if (CommonUtils.contains(CommonUtils.extensions, ext)) {
					mimeType = "text/xml";
				} else if (ext.equals(".dtd")) {
					mimeType = "text/dtd";
				} else if (ext.equals(".css")) {
					mimeType = "text/css";
				} else if (ext.equals(".jj") || ext.equals(".jjt")) {
					mimeType = "text/javacc";
				} else if (ext.equals(".java")) {
					mimeType = "text/java";
				}
			} else if (CommonUtils.isFirstLineXmlProcInstr(file)) {
				mimeType = "text/xml";
			}
		} catch (Exception e) {
		}
		return mimeType;
	}
}