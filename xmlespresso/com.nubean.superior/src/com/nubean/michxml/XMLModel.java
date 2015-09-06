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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.nubean.michbase.XMLBuilder;
import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.project.Project;
import com.nubean.michutil.LocalizedResources;

public class XMLModel {

	private org.w3c.dom.Document xml;

	private XMLDocumentDescriptor docInfo;

	private String xmlPi;

	private File schemaFile;

	private HashMap<String, String> schemaLocations;

	private XMLSchema schema;

	private XMLEditorConfiguration editorConfig;

	private boolean standalone;

	private Vector<String> nsPrefix;

	private Vector<String> nsUri;

	private org.w3c.dom.Node startNode;

	protected Project project;

	private SchemaNode anySchemaNode;

	private char ccode;

	private HashMap<String, Character> codeMap;

	private boolean parsed;

	private HashMap<String, String> documentNamespace;

	public XMLModel(XMLEditorConfiguration editorConfig,
			XMLDocumentDescriptor docInfo) {
		this.docInfo = docInfo;
		this.editorConfig = editorConfig;
		nsPrefix = new Vector<String>(5, 5);
		nsUri = new Vector<String>(5, 5);
		this.documentNamespace = new HashMap<String, String>();
	}

	public XMLSchema getSchema() {
		return schema;
	}

	public void setSchema(XMLSchema schema) {
		this.schema = schema;
	}

	public File getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(File file) {
		this.schemaFile = file;
	}

	public Character getCharCode(String name) {
		Character cc = (Character) codeMap.get(name);

		if (cc == null) {
			if (name.equals("any")) {
				cc = new Character(Character.MAX_VALUE);
			} else {
				cc = getNextCharCode();
				// System.out.println("Adding chartacter code:" + name + "=" +
				// cc);
				codeMap.put(name, cc);
			}
		}
		return cc;
	}

	public Vector<String> getNSPrefix() {
		return nsPrefix;
	}

	public Vector<String> getNSUri() {
		return nsUri;
	}

	public HashMap<String, String> getDocumentNamespace() {
		return documentNamespace;
	}
	
	private void importTaglibs() {

		if (docInfo.getTaglibs() != null) {
			Vector<TaglibDef> v = docInfo.getTaglibs();
			int count = (v != null ? v.size() : 0);
			Vector<TaglibDef> taglibvector = new Vector<TaglibDef>(count);

			for (int i = 0; i < count; i++) {
				try {
					TaglibDef tld = (TaglibDef) ((TaglibDef) v.elementAt(i))
							.clone();
					// convert tld to schema and fix schema location
					// before giving it to schema
					tld.schemaLocation = "file:///"
							+ TLD2Schema.getSchemaFromTLD(
									new File(tld.schemaLocation), tld.nsURI)
									.getCanonicalPath();
					taglibvector.add(tld);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			schema.importTaglibs(taglibvector);
		}

	}

	private void getSchemaNamespaces() {
		try {
			HashMap<String, String> namespaces = schema.getNamespaces();
			Iterator<String> it = namespaces.keySet().iterator();
			while (it.hasNext()) {
				String prefix = (String) it.next();
				String uri = (String) namespaces.get(prefix);

				nsPrefix.add(prefix);
				nsUri.add(uri);

			}
		} catch (Exception e) {

		}
	}

	public boolean initDocument() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" version='1.0' ").append(" encoding='")
				.append(docInfo.getEncoding());
		sb.append("' ");

		xmlPi = sb.toString();

		synchronized (XMLBuilder.class) {
			xml = XMLBuilder.nonValidatingBuilder.newDocument();
		}
		initComments();
		initMaps();

		initStartNode();
		parseDocumentNamesapce(xml);
		return true;
	}

	public void initMaps() {
		if (codeMap == null)
			codeMap = new HashMap<String, Character>();
		else
			codeMap.clear();
	}

	public void initStartNode() {
		if (docInfo.getRootElement() == null
				|| docInfo.getRootElement().trim().length() == 0) {
			docInfo.setRootElement("any");
		}
		startNode = schema.getElement(!standalone ? docInfo.getRootElement()
				: "any");

		if (startNode != null) {
			org.w3c.dom.Element element = null;

			try {
				org.w3c.dom.Attr attr = null;
				if (docInfo.getNSUri() != null && docInfo.getNSPrefix() != null
						&& docInfo.getNSUri().trim().length() > 0
						&& docInfo.getNSPrefix().trim().length() > 0) {
					element = xml.createElementNS(
							docInfo.getNSUri(),
							docInfo.getNSPrefix() + ":"
									+ docInfo.getRootElement());
					attr = xml.createAttributeNS(
							XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							CommonUtils.XMLNS_PREFIX + docInfo.getNSPrefix());
					attr.setValue(docInfo.getNSUri());
					element.setAttributeNodeNS(attr);
				} else if (schema.getTargetNameSpace() != null) {
					element = xml.createElementNS(schema.getTargetNameSpace(),
							docInfo.getRootElement());
					attr = xml.createAttributeNS(
							XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							XMLConstants.XMLNS_ATTRIBUTE);
					attr.setValue(schema.getTargetNameSpace());
					element.setAttributeNodeNS(attr);
				} else {
					element = xml.createElement(docInfo.getRootElement());
				}

				if (!standalone
						&& docInfo.getSchemaLocation() != null
						&& (docInfo.getSchemaLocation().trim()
								.startsWith("http"))) {

					StringBuffer sb = new StringBuffer();
					attr = xml.createAttributeNS(
							XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi");
					attr.setValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
					element.setAttributeNodeNS(attr);

					if (docInfo.getNSUri() != null
							&& docInfo.getNSUri().trim().length() > 0) {
						attr = xml.createAttributeNS(
								XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
								"xsi:schemaLocation");
						sb.append(docInfo.getNSUri()).append(' ');
					} else {
						attr = xml.createAttributeNS(
								XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
								"xsi:noNamespaceSchemaLocation");
					}

					sb.append(docInfo.getSchemaLocation());

					attr.setValue(sb.toString());
					element.setAttributeNodeNS(attr);
				}
			} catch (Exception e) {
				element = xml.createElement(docInfo.getRootElement());
			}

			xml.appendChild(element);
		}
	}

	public org.w3c.dom.Document getDocument() {
		return xml;
	}

	public void initComments() {

		if (project != null && project.getCopyright() != null) {
			org.w3c.dom.Comment comment = xml
					.createComment(LocalizedResources.applicationResources
							.getString("copyright") + project.getCopyright());
			xml.appendChild(comment);
		}
		if (project != null && project.getTitle() != null) {
			org.w3c.dom.Comment comment = xml
					.createComment(LocalizedResources.applicationResources
							.getString("title") + project.getTitle());
			xml.appendChild(comment);
		}

		if (project != null && project.getVersion() != null) {
			org.w3c.dom.Comment comment = xml
					.createComment(LocalizedResources.applicationResources
							.getString("version") + project.getVersion());
			xml.appendChild(comment);
		}

		if (project != null && project.getAuthor() != null) {
			org.w3c.dom.Comment comment = xml
					.createComment(LocalizedResources.applicationResources
							.getString("author") + project.getAuthor());
			xml.appendChild(comment);
		}

		if (project != null && project.getDescription() != null) {
			org.w3c.dom.Comment comment = xml
					.createComment(LocalizedResources.applicationResources
							.getString("project.description")
							+ project.getDescription());
			xml.appendChild(comment);
		}

		if (docInfo.getDescription() != null) {
			org.w3c.dom.Comment comment = xml
					.createComment(LocalizedResources.applicationResources
							.getString("document.description")
							+ docInfo.getDescription());
			xml.appendChild(comment);
		}

	}

	private File getDocumentFile() {
		File file = null;
		try {
			file = new File(docInfo.getPath());
			String fileName = docInfo.getName();
			file = new File(file, fileName);

			return file;
		} catch (Exception e) {
			file = null;
		}
		return file;
	}

	private void initCharacterCodes(XMLNode node) {
		int nenodes = node.childCount();
		for (int j = 0; j < nenodes; j++) {
			XMLNode echild = node.child(j);
			if (echild.getDomNode().getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
				continue;
			SchemaNode snode = (standalone ? anySchemaNode
					: getSchemaNode(echild));
			echild.setCharCode(snode.getCharCode());
			echild.setSchemaNode(snode);
			this.initCharacterCodes(echild);
		}
	}

	private void setCharCode(SchemaNode node) {
		Vector<SchemaNode> terminals = node.getTerminals();
		// System.out.println("setting character code for: "
		// + node.getPrintTerminals());
		int count = terminals.size();
		for (int i = 0; i < count; i++) {
			SchemaNode child = (SchemaNode) terminals.elementAt(i);
			Character cc = getCharCode(child.toString());
			if (child.getCharCode() == null)
				child.setCharCode(cc);
		}
	}

	@SuppressWarnings("unused")
	private SchemaNode getTypeNode(XMLNode lnode) {
		SchemaNode node = getSchemaNode(lnode).getTypeNode();
		setCharCode(node);
		return node;
	}

	private String getPrefix(String name) {
		String prefix = null;
		try {
			prefix = name.substring(0, name.indexOf(":"));
		} catch (Exception e) {
		}
		return prefix;
	}

	private SchemaNode getSchemaNode(XMLNode lnode) {
		SchemaNode node = lnode.getSchemaNode();
		if (node == null) {
			String key = lnode.getDomNode().getNodeName();
			XMLNode parent = (XMLNode) lnode.getParent();
			String namespaceURI = lnode.getNamespace();
			if (namespaceURI == null)
				namespaceURI = schema.getTargetNameSpace();
			else if (namespaceURI.trim().length() == 0) {
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
				node = schema.getElementSchemaNode(namespaceURI, name);
			}
		}

		if (node != null && node.getTagName() == null) {
			String nname = node.getNodeName();
			String refName = node.getRefName();
			if (refName != null) {
				String prefix = getPrefix(refName);

				if (prefix != null)
					node = schema.resolveReferenceSchemaNode(nname, refName);
				else
					node = node.getSchema().resolveReferenceSchemaNode(nname,
							refName);
			} else {
				if (nname.equals("any") && standalone) {
					node = schema.getElementSchemaNode(nname);
				}
			}
		}

		if (node == null || (!standalone && node.getNodeName().equals("any"))) {
			Node enode = schema.getAnyTypeNode();
			node = new SchemaNode(enode, schema);
		}
		if (!node.isResolved())
			node.resolve();

		if (node.getCharCode() == null)
			node.setCharCode(getCharCode(node.toString()));
		lnode.setSchemaNode(node);

		return node;
	}

	private synchronized Character getNextCharCode() {
		while (!Character.isLetterOrDigit(++ccode)
				|| CommonUtils.isREReservedChar(ccode)) {
			;
		}
		return new Character(ccode);
	}

	public org.w3c.dom.Node parseComment(String string) {
		org.w3c.dom.Node node = null;
		try {
			int start = 4;
			int end = string.lastIndexOf("-->");
			String content = string.substring(start, end);
			node = xml.createComment(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}

	public org.w3c.dom.Node parseProcInstr(String string) {
		org.w3c.dom.Node node = null;
		try {
			int start = 2;
			int end = string.lastIndexOf(">");
			String content = string.substring(start, end);
			StringTokenizer st = new StringTokenizer(content);
			String target = st.nextToken();
			String data = content.substring(content.indexOf(target)
					+ target.length());
			node = xml.createProcessingInstruction(target, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}

	private void addNamespace(Node parent, StringBuffer sb) {
		String xmlns = null;
		String prefix = null;
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == ':') {
				prefix = sb.substring(1, i);
				xmlns = getAttributeNodeNS(parent, prefix);
			} else if (sb.charAt(i) == '>') {
				if (xmlns != null && sb.indexOf(CommonUtils.XMLNS_PREFIX) == -1) {
					StringBuffer ns = new StringBuffer();
					ns.append(' ');
					ns.append(CommonUtils.XMLNS_PREFIX).append(prefix)
							.append('=').append('"');
					ns.append(xmlns).append('"');
					sb.insert(i, ns.toString());
				}
				break;
			}
		}
	}

	private String getAttributeNodeNS(Node parent, String prefix) {
		if (prefix == null || prefix.trim().length() == 0)
			return null;
		String xmlns = null;
		while (xmlns == null && parent != null
				&& parent.getNodeType() == Node.ELEMENT_NODE) {
			org.w3c.dom.Element ele = (org.w3c.dom.Element) parent;
			xmlns = ele.getAttribute(CommonUtils.XMLNS_PREFIX + prefix);
			if (xmlns.length() == 0)
				xmlns = null;
			parent = parent.getParentNode();
		}
		return xmlns;
	}

	public void setDocument(org.w3c.dom.Document xml) {
		this.xml = xml;
	}

	private HashMap<?, ?> deepCopy(HashMap<?, ?> map) {
		HashMap<Object, Object> copy = (map != null ? new HashMap<Object, Object>(
				13, 0.87f) : null);

		if (map != null) {
			Set<?> set = map.keySet();
			Iterator<?> it = set.iterator();
			while (it.hasNext()) {
				Object key = it.next();
				Object value = map.get(key);

				copy.put(key, value);
			}
		}
		return copy;
	}

	private boolean deepEqual(HashMap<?, ?> map, HashMap<?, ?> map2) {
		boolean ret = true;
		if (map != null && map2 != null) {
			Set<?> set = map.keySet();
			Iterator<?> it = set.iterator();
			while (it.hasNext()) {
				Object key = it.next();
				Object value = map.get(key);

				Object value2 = map2.get(key);

				if (!value.equals(value2)) {
					ret = false;
					break;
				}
			}
		}
		return ret;
	}

	private void parseDocumentNamesapce(org.w3c.dom.Document document) {
		documentNamespace.clear();
		
		Node node = document.getDocumentElement();
		NamedNodeMap attrs = (node != null ? node.getAttributes() : null);
		int count = (attrs != null ? attrs.getLength() : 0);
		for (int i = 0; i < count; i++) {
			Attr attr = (Attr) attrs.item(i);
			String aname = attr.getName();
			String avalue = attr.getValue();

			if (aname.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
				documentNamespace.put(avalue, XMLConstants.DEFAULT_NS_PREFIX);
			} else if (aname.startsWith(CommonUtils.XMLNS_PREFIX)) {
				documentNamespace.put(avalue,
						CommonUtils.getUnqualifiedNodeName(aname));
			}
		}
	}

	public void refreshDocument(String text, ErrorHandler docHandler,
			ErrorHandler schemaHandler) throws Exception {
		StringReader sr = new StringReader(text);

		synchronized (XMLBuilder.class) {
			xml = getBuilder(docHandler).parse(new InputSource(sr));
		}

		String sl = docInfo.getSchemaLocation();
		String dl = docInfo.getDtdLocation();
		HashMap<?, ?> saveMap = deepCopy(schemaLocations);
		parseSchemaLocation(xml);
		parseDocumentType(xml);

		if (schema == null
				|| (sl != null && !sl.equals(docInfo.getSchemaLocation()))
				|| (dl != null && !dl.equals(docInfo.getDtdLocation()))
				|| !deepEqual(saveMap, schemaLocations)) {
			parseSchema(schemaHandler);
		}
		parsed = true;
		initMaps();
		parseDocumentNamesapce(xml);
	}

	public void refresh(String text, ErrorHandler docHandler,
			ErrorHandler schemaHandler) throws Exception {
		StringReader sr = new StringReader(text);

		synchronized (XMLBuilder.class) {
			xml = getBuilder(docHandler).parse(new InputSource(sr));
		}

		parseSchemaLocation(xml);
		parseDocumentType(xml);

		parseSchema(schemaHandler);
		parsed = true;
		initMaps();
		parseDocumentNamesapce(xml);
	}

	private DocumentBuilder getBuilder(ErrorHandler xmlCheckHandler) {
		DocumentBuilder builder = XMLBuilder.nonValidatingBuilder;
		builder.setErrorHandler(xmlCheckHandler);
		return builder;
	}

	public org.w3c.dom.Node parseElement(Node parent, String string) {
		org.w3c.dom.Node node = null;
		try {
			StringBuffer sb = new StringBuffer(string.trim());
			addNamespace(parent, sb);
			sb.insert(0, "<?xml version=\"1.0\" ?>\n");
			StringReader sr = new StringReader(sb.toString());
			synchronized (XMLBuilder.class) {
				node = getBuilder(null).parse(new InputSource(sr));
			}
			node = copyNode(parent,
					((org.w3c.dom.Document) node).getDocumentElement());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}

	private Node copyNode(Node parent, Node node) {
		String nsURI = node.getNamespaceURI();
		String prefix = node.getPrefix();
		boolean sameNS = (nsURI != null
				&& nsURI.equals(parent.getNamespaceURI()) && prefix != null && prefix
				.equals(parent.getPrefix()));
		org.w3c.dom.Node newNode = null;
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			newNode = xml.createElementNS(nsURI, node.getNodeName());
			org.w3c.dom.Element enode = (org.w3c.dom.Element) newNode;
			org.w3c.dom.Element ele = (org.w3c.dom.Element) node;

			NamedNodeMap nodeMap = ele.getAttributes();
			int count = (nodeMap != null ? nodeMap.getLength() : 0);
			for (int i = 0; i < count; i++) {
				Attr attr = (Attr) nodeMap.item(i);
				String attrName = attr.getNodeName();
				if (attrName
						.equals(CommonUtils.XMLNS_PREFIX + node.getPrefix())
						&& sameNS) {
					continue;
				}
				Attr cattr = (Attr) copyNode(node, attr);
				enode.setAttributeNodeNS(cattr);
			}

			NodeList nodeList = node.getChildNodes();
			count = (nodeList != null ? nodeList.getLength() : 0);
			for (int i = 0; i < count; i++) {
				Node cnode = copyNode(node, nodeList.item(i));
				if (cnode != null)
					newNode.appendChild(cnode);
			}
			break;
		case Node.TEXT_NODE:
			if (node.getNodeValue().trim().length() > 0)
				newNode = xml.createTextNode(CommonUtils.escape(node
						.getNodeValue()));
			break;
		case Node.CDATA_SECTION_NODE:
			newNode = xml.createCDATASection(node.getNodeValue());
			break;
		case Node.ATTRIBUTE_NODE:
			Attr attr = xml.createAttributeNS(nsURI, node.getNodeName());
			attr.setValue(node.getNodeValue());
			newNode = attr;
			break;
		}
		return newNode;
	}

	public org.w3c.dom.Node parseFragment(Node parent, String string) {
		org.w3c.dom.Node node = null;
		try {
			string = string.trim();
			if (string.startsWith("<!--") && string.endsWith("-->"))
				return parseComment(string);
			else if (string.startsWith("<!") && string.endsWith(">"))
				return parseProcInstr(string);
			else if (string.startsWith("<") && string.endsWith(">"))
				return parseElement(parent, string);
		} catch (Exception e) {
		}
		return node;
	}

	public void parseEncoding(Document doc) {
		try {
			int offset = -1;
			for (int i = 0; i < doc.getLength(); i++) {
				if (doc.getText(i, 1).charAt(0) == '\n') {
					offset = i;
					break;
				}
			}
			String line = doc.getText(0, offset);
			int index = 0;
			if ((index = line.indexOf("encoding")) != -1) {
				index += "encoding".length();
				while (line.charAt(index) != '=')
					index++;
				index++;
				StringTokenizer st = new StringTokenizer(line.substring(index),
						"'\"");
				String encoding = st.nextToken().trim();
				String[] enc = CommonUtils.encodings;
				boolean found = false;
				for (int i = 0; i < enc.length; i++) {
					if (encoding.equalsIgnoreCase(enc[i])) {
						docInfo.setEncoding(encoding);
						found = true;
						break;
					}
				}
				if (!found) {
					docInfo.setEncoding("UTF-8");
				}
			} else {
				docInfo.setEncoding("UTF-8");
			}
		} catch (Exception e) {
			docInfo.setEncoding("UTF-8");
			e.printStackTrace();
		}
	}

	public String getXmlProcInstr() {
		StringBuffer sb = new StringBuffer();
		if (xmlPi != null)
			sb.append(xmlPi);
		return sb.toString();
	}

	private XMLNode getDocumentElementNode(XMLNode root) {
		XMLNode ret = null;
		int count = root.childCount();
		org.w3c.dom.Document doc = (org.w3c.dom.Document) root.getDomNode();
		for (int i = 0; i < count; i++) {
			XMLNode child = root.child(i);
			if (child.getDomNode() == doc.getDocumentElement()) {
				ret = child;
				break;
			}
		}
		return ret;
	}

	public void initDocCharacterCodes(XMLNode root) {
		try {
			XMLNode docElement = getDocumentElementNode(root);
			String startNodeName = docInfo.getRootElement();
			if (startNodeName == null) {
				startNodeName = docElement.getDomNode().getNodeName();
			}
			startNode = schema.getElement(docElement.getNamespace(),
					startNodeName);

			SchemaNode node = new SchemaNode(startNode, schema);
			if (standalone)
				anySchemaNode = node;
			setCharCode(node);
			docElement.setSchemaNode(node);
			initCharacterCodes(docElement);

		} catch (Exception e) {
		}
	}

	public void parseXmlPI(File file) {
		try {
			InputStream is = new FileInputStream(file);
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			String line = null;
			line = br.readLine();

			br.close();

			int bi = line.indexOf("xml");
			int ei = line.indexOf("?>");
			xmlPi = line.substring(bi + 3, ei);

			int index = 0;
			if ((index = line.indexOf("encoding")) != -1) {
				index += "encoding".length();
				while (line.charAt(index) != '=')
					index++;
				index++;
				StringTokenizer st = new StringTokenizer(line.substring(index),
						"'\"");
				String encoding = st.nextToken().trim();
				if (CommonUtils.contains(CommonUtils.encodings, encoding)) {
					docInfo.setEncoding(encoding);
				}
			}
		} catch (Exception e) {
		}

	}

	private void convertDtdToSchema() throws Exception {
		if (docInfo.getSchemaLocation() != null
				&& docInfo.getSchemaLocation().trim().length() > 0)
			return;

		FileWriter fw = null;
		PrintWriter pw = null;

		DtdSchema parser = new DtdSchema(docInfo.getNSUri(),
				docInfo.getNSPrefix());
		schemaFile = File.createTempFile("michide", ".xsd");
		schemaFile.deleteOnExit();
		if (docInfo.getDtdLocation().trim().startsWith("http://")) {
			// dtd has a URL
			URL url = new URL(docInfo.getDtdLocation());
			parser.parseDtd(url);
			fw = new FileWriter(schemaFile);
			pw = new PrintWriter(fw);
		} else if (docInfo.getDtdLocation().trim().startsWith("file:///")) {
			// dtd location must point to a file location
			parser.parseDtd(new URL(docInfo.getDtdLocation().trim()));
			fw = new FileWriter(schemaFile);
			pw = new PrintWriter(fw);
		} else {
			try {
				// try to see if the dtd location points to a resource file
				URL url = getClass().getClassLoader().getResource(
						docInfo.getDtdLocation());
				parser.parseDtd(url);
				fw = new FileWriter(schemaFile);
				pw = new PrintWriter(fw);
			} catch (Exception e) {
				// dtd location must point to a local file location
				parser.parseDtd(new URL("file:///" + docInfo.getDtdLocation()));
				fw = new FileWriter(schemaFile);
				pw = new PrintWriter(fw);
			}
		}
		if (pw != null) {
			parser.print(pw);
			pw.close();
		}
	}

	private void convertInternalDtdToSchema(File dtdFile) throws Exception {
		FileWriter fw = null;
		PrintWriter pw = null;

		DtdSchema parser = new DtdSchema(docInfo.getNSUri(),
				docInfo.getNSPrefix());
		schemaFile = File.createTempFile("michide", ".xsd");
		schemaFile.deleteOnExit();

		// dtd location must point to a file location
		parser.parseDtd(new URL("file:///" + dtdFile.getAbsolutePath()));
		fw = new FileWriter(schemaFile);
		pw = new PrintWriter(fw);

		if (pw != null) {
			parser.print(pw);
			pw.close();
		}
	}

	private void parseSchemaLocation(org.w3c.dom.Document xml) {
		try {
			org.w3c.dom.Element root = xml.getDocumentElement();
			NamedNodeMap attrs = root.getAttributes();
			int count = (attrs != null ? attrs.getLength() : 0);
			for (int i = 0; i < count; i++) {
				Attr attr = (Attr) attrs.item(i);
				String name = CommonUtils.getUnqualifiedNodeName(attr);
				if (name.equals("noNamespaceSchemaLocation")) {
					docInfo.setSchemaLocation(attr.getNodeValue());
					break;
				} else if (name.equals("schemaLocation")) {
					if (schemaLocations == null)
						schemaLocations = new HashMap<String, String>(7, 0.9f);

					String value = attr.getNodeValue();
					StringTokenizer st = new StringTokenizer(value, " ");

					while (st.hasMoreTokens()) {
						schemaLocations.put(st.nextToken(), st.nextToken());
					}
					if (docInfo.getSchemaLocation() == null) {
						// set one of the locations as the schema location
						// if we have no schema location
						// the rest will be imported into the selected schema
						// after the selected schema is parsed
						Collection<String> values = schemaLocations.values();
						Iterator<String> it = values.iterator();
						if (it.hasNext()) {
							String slocation = it.next();
							docInfo.setSchemaLocation(slocation);
							schemaLocations.remove(slocation);
							if (schemaLocations.size() == 0)
								schemaLocations = null;
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseDocumentType(org.w3c.dom.Document xml) {
		try {
			DocumentType docType = xml.getDoctype();
			if (docType != null) {
				String system = docType.getSystemId();
				if (system != null && system.trim().length() > 0) {
					if (system.indexOf(File.separatorChar) == -1)
						system = docInfo.getPath() + File.separator + system;
					docInfo.setDtdLocation(system);
					this.convertDtdToSchema();
				} else {
					String inline = docType.getInternalSubset();
					if (inline != null && inline.trim().length() > 0) {
						File dtdFile = File.createTempFile(docInfo.getName(),
								".dtd");
						dtdFile.deleteOnExit();
						FileWriter fw = new FileWriter(dtdFile);
						PrintWriter pw = new PrintWriter(fw);
						pw.println(inline);
						pw.close();
						this.convertInternalDtdToSchema(dtdFile);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void validateUsingDtd(ErrorHandler errorHandler) throws Exception {
		File file = getDocumentFile();
		XMLReader parser = XMLReaderFactory.createXMLReader();

		parser.setErrorHandler(errorHandler);
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser.setFeature("http://xml.org/sax/features/validation", true);
		parser.parse(file.getAbsolutePath());
	}

	private boolean validateSchema() {
		return editorConfig != null && editorConfig.getValidateSchema();
	}

	private void buildSchema(String uri, ErrorHandler errorHandler)
			throws Exception {

		if (uri.equals("http://www.nubean.com/schemas/any.xsd")) {
			standalone = true;
		}

		boolean validate = validateSchema();

		schema = new XMLSchema(null, uri, validate, errorHandler);

		if (schemaFile == null) {
			InputStream is = CommonUtils.parseURL(getClass().getClassLoader(),
					uri);
			schemaFile = File.createTempFile("michide", ".xsd");
			schemaFile.deleteOnExit();
			CommonUtils.copyToFile(is, schemaFile);
			is.close();
		}

		String tns = schema.getTargetNameSpace();
		if (tns != null && tns.trim().length() > 0
				&& docInfo.getUseSchemaTargetNamespace()) {
			docInfo.setNSUri(schema.getTargetNameSpace());
			docInfo.setNSPrefix(schema.getNSPrefix(schema.getTargetNameSpace()));
			nsPrefix.add(docInfo.getNSPrefix());
			nsUri.add(docInfo.getNSUri());
		}
	}

	public void parseSchema(ErrorHandler errorHandler) throws Exception {
		String uri = "http://www.nubean.com/schemas/any.xsd";

		if (docInfo.getDtdLocation() != null
				&& docInfo.getDtdLocation().trim()
						.equals("http://www.w3.org/2001/XMLSchema.dtd")) {
			docInfo.setSchemaLocation("http://www.nubean.com/schemas/schema.xsd");
		}

		if (docInfo.getSchemaLocation() != null
				&& docInfo.getSchemaLocation().trim().length() > 0) {
			uri = docInfo.getSchemaLocation();
		} else if (docInfo.getDtdLocation() != null
				&& docInfo.getDtdLocation().trim().length() > 0) {
			this.convertDtdToSchema();
			uri = "file:///" + schemaFile.getAbsolutePath();
		} else if (schemaFile != null) {
			uri = "file:///" + schemaFile.getCanonicalPath();
		} else if (docInfo.getName().endsWith(".xsd")
				|| docInfo.getName().endsWith(".xs")) {
			docInfo.setSchemaLocation("http://www.nubean.com/schemas/schema.xsd");
			uri = docInfo.getSchemaLocation();
		} else {
			try {
				if (xml != null)
					schemaFile = XMLToSchema.getSchemaForXML(xml);
				if (schemaFile != null) {
					schemaFile.deleteOnExit();
					uri = "file:///" + schemaFile.getCanonicalPath();
				}
			} catch (Exception e) {
				// ignore
			}
		}

		try {
			buildSchema(uri, errorHandler);

			// import schema locaitons parsed from document
			if (schemaLocations != null)
				schema.importSchemas(schemaLocations);

			// import schema locations for taglib schemas
			importTaglibs();

			getSchemaNamespaces();
		} catch (Exception e) {

			standalone = true;
			uri = "http://www.nubean.com/schemas/any.xsd";
			buildSchema(uri, errorHandler);
		}

	}

	private boolean hasSchemaOrDTD() {

		return (docInfo.getSchemaLocation() != null && docInfo
				.getSchemaLocation().trim().length() > 0)
				|| (docInfo.getDtdLocation() != null && docInfo
						.getDtdLocation().trim().length() > 0);
	}

	public void parseDocument(ErrorHandler errorHandler) throws Exception {
		try {
			File file = getDocumentFile();
			synchronized (XMLBuilder.class) {
				xml = getBuilder(errorHandler).parse(file);
			}
			long ts = file.lastModified();
			if (!hasSchemaOrDTD() || (ts > docInfo.getTimestamp())) {
				docInfo.setRootElement(xml.getDocumentElement().getNodeName());
				if (xml.getDocumentElement().getLocalName().equals("schema")) {
					docInfo.setDtdLocation("http://www.w3.org/2001/XMLSchema.dtd");
				}
				parseSchemaLocation(xml);
				parseDocumentType(xml);
				parseDocumentNamesapce(xml);
			}

		} finally {
			parsed = true;
		}
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	public boolean isParsed() {
		return parsed;
	}
}
