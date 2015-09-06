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

import org.xml.sax.*;
import org.w3c.dom.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.XMLBuilder;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;

import java.io.*;
import java.util.*;

public class XMLSchema {
	private org.w3c.dom.Document schema;

	private String targetNamespace;

	private HashMap<String, Node> elements, attributes, constraints, attributeGroups;

	private HashMap<String, Node> modelGroups, notations, simpleTypes, complexTypes;

	private Vector<XMLSchema> includes, noNamespaceImports;

	private HashMap<String, XMLSchema> imports, tldimports;

	private ErrorHandler errorHandler;

	private boolean validate;

	private HashMap<String, String> prefix2Uri, uri2Prefix;

	private String schemaLocation;

	private org.w3c.dom.Node anyNode;

	private XMLSchema container;

	private TaglibDef tld;

	public void setTld(TaglibDef tld) {
		this.tld = tld;
	}

	public TaglibDef getTld() {
		return tld;
	}

	public String getTargetNameSpace() {
		return targetNamespace;
	}

	public Document getDocument() {
		return schema;
	}

	public HashMap<String, String> getNamespaces() {
		return this.prefix2Uri;
	}

	public XMLSchema(XMLSchema container, String uri, boolean validate,
			ErrorHandler errorHandler) throws Exception {
		this.container = container;
		if (container != null)
			schemaLocation = CommonUtils.parseRelativeUri(
					container.schemaLocation, uri);
		else
			schemaLocation = uri;

		InputStream is = CommonUtils.parseURL(getClass().getClassLoader(),
				schemaLocation);
		this.validate = validate;
		this.errorHandler = errorHandler;

		DocumentBuilder builder = null;

		synchronized (XMLBuilder.class) {

			builder = (validate ? XMLBuilder.validatingBuilder
					: XMLBuilder.nonValidatingBuilder);

			builder.setErrorHandler(errorHandler);
			try {
				schema = builder.parse(is);
			} catch (java.io.FileNotFoundException e) {
				if (validate) {
					is = CommonUtils.parseURL(getClass().getClassLoader(),
							schemaLocation);
					schema = builder.parse(is);
				}
			}
		}
		initSchema(schema);
		preOrderTraverse(schema);

	}

	private void preOrderTraverse(Node node) throws Exception {
		if (node == null)
			return;

		NodeList nodeList = node.getChildNodes();
		int count = nodeList.getLength();
		for (int i = 0; i < count; i++) {
			Node child = nodeList.item(i);
			if (child != null)
				preOrderTraverse(child);
		}

		NamedNodeMap attrs = node.getAttributes();
		String nodeName = CommonUtils.getUnqualifiedNodeName(node);

		if (attrs != null) {
			if (nodeName.equals("include")) {
				Attr schemaLocation = (Attr) attrs
						.getNamedItem("schemaLocation");
				includeSchema(schemaLocation.getValue());
			} else if (nodeName.equals("import")) {
				Attr schemaLocation = (Attr) attrs
						.getNamedItem("schemaLocation");
				Attr namespace = (Attr) attrs.getNamedItem("namespace");
				importSchema(namespace.getValue(), schemaLocation.getValue());
			} else {
				Node name = attrs.getNamedItem("name");
				String parentNodeName = CommonUtils.getUnqualifiedNodeName(node
						.getParentNode());
				if (name != null && parentNodeName.equals("schema")) {
					mapNode(node);
				}
			}
		}
	}

	private void includeSchema(String uri) {
		try {
			XMLSchema includeSchema = new XMLSchema(this, uri, validate,
					errorHandler);
			String tns = includeSchema.getTargetNameSpace();
			if ((targetNamespace != null && tns != null && tns
					.equals(targetNamespace))
					|| (tns == null)
					|| (targetNamespace == null && tns == null))
				includes.add(includeSchema);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private XMLSchema getImportedSchema(String namespace) {
		if (imports.containsKey(namespace)) {
			return imports.get(namespace);
		} else {
			return container.getImportedSchema(namespace);
		}
	}

	private boolean isImported(String namespace) {
		boolean imported = false;

		if (imports.containsKey(namespace)) {
			imported = true;
		} else if (container != null) {
			imported = container.isImported(namespace);
			if (imported) {
				imports.put(namespace, getImportedSchema(namespace));
			}
		}
		return imported;

	}

	public void importSchemas(HashMap<String, String> ischemas) {
		if (ischemas != null) {
			Set<String> keys = ischemas.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String ns = it.next();
				String uri = ischemas.get(ns);
				importSchema(ns, uri);
			}
		}
	}

	public void importTaglibs(Vector<TaglibDef> taglibs) {
		int count = (taglibs != null ? taglibs.size() : 0);
		for (int i = 0; i < count; i++) {
			TaglibDef td = (TaglibDef) taglibs.elementAt(i);
			if (td.nsPrefix != null && td.nsURI != null
					&& td.schemaLocation != null) {
				importTaglib((TaglibDef) td.clone());
				putNamespace(td.nsURI, td.nsPrefix);
			}
		}
	}

	public boolean isTaglibImported(String namespace) {
		return (namespace != null && tldimports.containsKey(namespace));
	}

	private void importTaglib(TaglibDef tld) {
		try {
			if (isTaglibImported(tld.nsURI))
				return;

			XMLSchema tldSchema = new XMLSchema(this, tld.schemaLocation,
					false, null);
			tldSchema.setTld(tld);

			tldimports.put(tld.nsURI, tldSchema);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isJSP() {
		return (targetNamespace != null && targetNamespace
				.equals("http://java.sun.com/JSP/Page"));
	}

	/**
	 * 
	 * @return vector of schema nodes corresponding to taglib elements
	 */

	public Vector<SchemaNode> getTaglibSchemaNodes() {
		Vector<SchemaNode> ret = new Vector<SchemaNode>(1, 20);
		Collection<String> keys = tldimports.keySet();
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			String nsURI = (String) it.next();
			XMLSchema tld = (XMLSchema) tldimports.get(nsURI);

			Collection<Node> tes = tld.elements.values();
			Iterator<Node> it2 = tes.iterator();

			while (it2.hasNext()) {
				Node ele = (Node) it2.next();
				SchemaNode snode = new SchemaNode(ele, tld);
				ret.add(snode);
			}
		}
		return ret;
	}

	private void importSchema(String namespace, String uri) {
		try {
			if (isImported(namespace))
				return;

			XMLSchema importSchema = new XMLSchema(this, uri, validate,
					errorHandler);

			if ((targetNamespace != null && namespace != null && !namespace
					.equals(targetNamespace))
					|| (namespace == null)
					|| (targetNamespace == null && namespace != null)) {
				if (namespace != null)
					imports.put(namespace, importSchema);
				else
					noNamespaceImports.add(importSchema);
				HashMap<String, String> ins = importSchema.getNamespaces();
				Set<String> keys = ins.keySet();
				Iterator<String> it = keys.iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (key != null && key.trim().length() > 0)
						putNamespace( ins.get(key), key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public SchemaNode resolveReferenceSchemaNode(String nodeName, String ref) {
		if (ref == null)
			return null;

		SchemaNode ret = null;
		if (nodeName.equals("element")) {
			ret = getElementSchemaNode(ref);
		} else if (nodeName.equals("simpleType")) {
			ret = getSimpleTypeSchemaNode(ref);
		} else if (nodeName.equals("complexType")) {
			ret = getComplexTypeSchemaNode(ref);
		} else if (nodeName.equals("attribute")) {
			ret = getAttributeSchemaNode(ref);
		} else if (nodeName.equals("attributeGroup")) {
			ret = getAttributeGroupSchemaNode(ref);
		} else if (nodeName.equals("notation")) {
			Node node = getNotation(ref);
			if (node != null)
				ret = new SchemaNode(node, this);
		} else if (nodeName.equals("group")) {
			ret = getModelGroupSchemaNode(ref);
		}
		return ret;

	}

	private void mapNode(Node node) {
		String nodeName = CommonUtils.getUnqualifiedNodeName(node);
		NamedNodeMap attrs = node.getAttributes();
		Attr name = (Attr) attrs.getNamedItem("name");
		if (nodeName.equals("element")) {
			this.putElement(name.getValue(), node);
		} else if (nodeName.equals("simpleType")) {
			this.putSimpleType(name.getValue(), node);
		} else if (nodeName.equals("complexType")) {
			this.putComplexType(name.getValue(), node);
		} else if (nodeName.equals("attribute")) {
			this.putAttribute(name.getValue(), node);
		} else if (nodeName.equals("attributeGroup")) {
			this.putAttributeGroup(name.getValue(), node);
		} else if (nodeName.equals("notation")) {
			this.putNotation(name.getValue(), node);
		} else if (nodeName.equals("group")) {
			this.putModelGroup(name.getValue(), node);
		}
	}

	private void initSchema(Document document) {
		elements = new HashMap<String, Node>(23, 0.87f);
		attributes = new HashMap<String, Node>(31, 0.87f);
		attributeGroups = new HashMap<String, Node>(5, 0.87f);
		constraints = new HashMap<String, Node>(5, 0.87f);
		simpleTypes = new HashMap<String, Node>(23, 0.87f);
		complexTypes = new HashMap<String, Node>(37, 0.87f);
		notations = new HashMap<String, Node>(23, 0.87f);
		modelGroups = new HashMap<String, Node>(7, 0.87f);
		attributeGroups = new HashMap<String, Node>(7, 0.87f);

		includes = new Vector<XMLSchema>(4, 2);
		imports = new HashMap<String, XMLSchema>(5, 0.87f);
		tldimports = new HashMap<String, XMLSchema>(5, 0.87f);
		prefix2Uri = new HashMap<String, String>(13, 0.87f);
		prefix2Uri.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
		uri2Prefix = new HashMap<String, String>(13, 0.87f);
		uri2Prefix.put(XMLConstants.NULL_NS_URI, XMLConstants.DEFAULT_NS_PREFIX);
		noNamespaceImports = new Vector<XMLSchema>(1, 1);

		NodeList nodeList = document.getChildNodes();
		Node node = null;
		int count = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < count; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& CommonUtils.getUnqualifiedNodeName(node)
							.equals("schema"))
				break;
		}

		NamedNodeMap attrs = (node != null ? node.getAttributes() : null);
		count = (attrs != null ? attrs.getLength() : 0);
		for (int i = 0; i < count; i++) {
			Attr attr = (Attr) attrs.item(i);
			String aname = attr.getName();
			String avalue = attr.getValue();

			if (aname.equals("targetNamespace")) {
				targetNamespace = avalue;
			} else if (aname.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
				putNamespace(avalue, XMLConstants.DEFAULT_NS_PREFIX);
			} else if (aname.startsWith(CommonUtils.XMLNS_PREFIX)) {
				putNamespace(avalue, CommonUtils.getUnqualifiedNodeName(aname));
			}
		}

	}

	private void putNamespace(String uri, String prefix) {
		prefix2Uri.put(prefix, uri);
		uri2Prefix.put(uri, prefix);
	}

	public String getUri(String prefix) {
		return (prefix != null ? (String) prefix2Uri.get(prefix) : null);
	}

	public String getNSPrefix(String uri) {
		return (uri != null ? (String) uri2Prefix.get(uri) : null);
	}

	private void putElement(String name, Node node) {
		elements.put(name, node);
	}

	private void putAttribute(String name, Node node) {
		attributes.put(name, node);
	}

	private void putAttributeGroup(String name, Node node) {
		attributeGroups.put(name, node);
	}

	private void putModelGroup(String name, Node node) {
		modelGroups.put(name, node);
	}

	private void putSimpleType(String name, Node node) {
		simpleTypes.put(name, node);
	}

	private void putComplexType(String name, Node node) {
		complexTypes.put(name, node);
	}

	@SuppressWarnings("unused")
	private void putConstraint(String name, Node node) {
		constraints.put(name, node);
	}

	private void putNotation(String name, Node node) {
		notations.put(name, node);
	}

	@SuppressWarnings("unused")
	private String getDefaultNamespace() {
		return (String) prefix2Uri.get(XMLConstants.DEFAULT_NS_PREFIX);
	}

	private String getPrefix(String name) {
		String prefix = null;
		try {
			prefix = name.substring(0, name.indexOf(":"));
		} catch (Exception e) {
		}
		return prefix;
	}

	public Node getElement(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		if (uri == null || uri.equals(targetNamespace)) {
			Node element = (Node) elements.get(CommonUtils
					.getUnqualifiedNodeName(name));

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (element == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				element = is.getElement(name);
			}

			if (element == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					element = (Node) parent.elements.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (element != null)
						break;
					parent = parent.container;
				}
			}
			if (element == null)
				element = importElement(name);

			return element;
		} else {
			return importElement(uri, name);
		}
	}

	public Node getElement(String uri, String name) {

		if (uri == null || uri.equals(targetNamespace)) {
			Node element = (Node) elements.get(CommonUtils
					.getUnqualifiedNodeName(name));

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (element == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				element = is.getElement(name);
			}
			if (element == null)
				element = importElement(name);

			return element;
		} else {
			return importElement(uri, name);
		}
	}

	public SchemaNode getElementSchemaNode(String name) {

		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node element = (Node) elements.get(CommonUtils
					.getUnqualifiedNodeName(name));

			if (element != null)
				snode = new SchemaNode(element, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				element = is.getElement(name);
				if (element != null)
					snode = new SchemaNode(element, this);
			}

			if (snode == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					element = (Node) container.elements.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (element != null) {
						snode = new SchemaNode(element, this);
						break;
					}
					parent = parent.container;
				}

			}

			if (snode == null)
				snode = importElementSchemaNode(name);

		} else {
			snode = importElementSchemaNode(uri, name);
		}

		return snode;
	}

	public SchemaNode getElementSchemaNode(String uri, String name) {

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node element = (Node) elements.get(CommonUtils
					.getUnqualifiedNodeName(name));

			if (element != null)
				snode = new SchemaNode(element, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				element = is.getElement(name);
				if (element != null)
					snode = new SchemaNode(element, this);
			}
			if (snode == null)
				snode = importElementSchemaNode(name);

		} else {
			snode = importElementSchemaNode(uri, name);
		}

		return snode;
	}

	public Node getAttribute(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		Node attribute = null;
		if (uri == null || uri.equals(targetNamespace)) {
			attribute = (Node) attributes.get(CommonUtils
					.getUnqualifiedNodeName(name));
			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (attribute == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				attribute = is.getAttribute(name);
			}
			if (attribute == null) {
				if (uri != null && container != null
						&& container.getTargetNameSpace() != null
						&& container.getTargetNameSpace().equals(uri)) {
					attribute = (Node) container.attributes.get(CommonUtils
							.getUnqualifiedNodeName(name));
				}
			}
			if (attribute == null)
				attribute = importAttribute(name);

		} else {
			attribute = importAttribute(uri, name);
		}
		return attribute;
	}

	private SchemaNode getAttributeSchemaNode(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node attribute = (Node) attributes.get(CommonUtils
					.getUnqualifiedNodeName(name));
			if (attribute != null)
				snode = new SchemaNode(attribute, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				snode = is.getAttributeSchemaNode(name);
			}
			if (snode == null) {
				if (uri != null && container != null
						&& container.getTargetNameSpace() != null
						&& container.getTargetNameSpace().equals(uri)) {
					attribute = (Node) container.attributes.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (attribute != null)
						snode = new SchemaNode(attribute, this);
				}

			}
			if (snode == null)
				snode = importAttributeSchemaNode(name);

		} else {
			snode = importAttributeSchemaNode(uri, name);
		}
		return snode;
	}

	private SchemaNode importAttributeSchemaNode(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		SchemaNode snode = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (snode == null && it.hasNext()) {
				ischema = (XMLSchema) it.next();
				snode = ischema.getAttributeSchemaNode(name);
			}

		} else {
			snode = ischema.getAttributeSchemaNode(name);
		}
		return snode;
	}

	private SchemaNode importAttributeSchemaNode(String name) {
		int count = noNamespaceImports.size();
		SchemaNode snode = null;
		for (int i = 0; (snode == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			snode = ischema.getAttributeSchemaNode(name);
		}
		return snode;
	}

	private Node importAttribute(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		Node node = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (node == null && it.hasNext()) {
				ischema = (XMLSchema) it.next();
				node = ischema.getAttribute(name);
			}

		} else {
			node = ischema.getAttribute(name);
		}
		return node;
	}

	private Node importAttribute(String name) {
		int count = noNamespaceImports.size();
		Node node = null;
		for (int i = 0; (node == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			node = ischema.getAttribute(name);
		}
		return node;
	}

	public Node getAttributeGroup(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		Node attributeGroup = null;
		if (uri == null || uri.equals(targetNamespace)) {
			attributeGroup = (Node) attributeGroups.get(CommonUtils
					.getUnqualifiedNodeName(name));
			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (attributeGroup == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				attributeGroup = is.getAttributeGroup(name);
			}
			if (attributeGroup == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					attributeGroup = (Node) container.attributeGroups
							.get(CommonUtils.getUnqualifiedNodeName(name));
					if (attributeGroup != null)
						break;
					parent = parent.container;
				}

			}
			if (attributeGroup == null)
				attributeGroup = importAttributeGroup(name);

		} else {
			attributeGroup = importAttributeGroup(uri, name);
		}
		return attributeGroup;

	}

	private SchemaNode getAttributeGroupSchemaNode(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node group = (Node) attributeGroups.get(CommonUtils
					.getUnqualifiedNodeName(name));
			if (group != null)
				snode = new SchemaNode(group, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				snode = is.getAttributeGroupSchemaNode(name);
			}
			if (snode == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					group = (Node) container.attributeGroups.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (group != null) {
						snode = new SchemaNode(group, this);
						break;
					}
					parent = parent.container;
				}

			}
			if (snode == null)
				snode = importAttributeGroupSchemaNode(name);
		} else {
			snode = importAttributeGroupSchemaNode(uri, name);
		}
		return snode;
	}

	private SchemaNode importAttributeGroupSchemaNode(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		SchemaNode snode = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (snode == null && it.hasNext()) {
				ischema = it.next();
				snode = ischema.getAttributeGroupSchemaNode(name);
			}

		} else {
			snode = ischema.getAttributeGroupSchemaNode(name);
		}
		return snode;
	}

	private SchemaNode importAttributeGroupSchemaNode(String name) {
		int count = noNamespaceImports.size();
		SchemaNode snode = null;
		for (int i = 0; (snode == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			snode = ischema.getAttributeGroupSchemaNode(name);
		}
		return snode;
	}

	private Node importAttributeGroup(String nsUri, String name) {
		XMLSchema ischema =  imports.get(nsUri);
		Node node = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (node == null && it.hasNext()) {
				ischema = it.next();
				node = ischema.getAttributeGroup(name);
			}

		} else {
			node = ischema.getAttributeGroup(name);
		}
		return node;
	}

	private Node importAttributeGroup(String name) {
		int count = noNamespaceImports.size();
		Node node = null;
		for (int i = 0; (node == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			node = ischema.getAttributeGroup(name);
		}
		return node;
	}

	private Node importSimpleType(String nsUri, String name) {
		XMLSchema ischema = imports.get(nsUri);
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			Node simpleType = null;
			while (simpleType == null && it.hasNext()) {
				ischema =  it.next();
				simpleType = ischema.getSimpleType(name);
			}
			return simpleType;
		} else
			return ischema.getSimpleType(name);
	}

	private SchemaNode importSimpleTypeSchemaNode(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		SchemaNode snode = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (snode == null && it.hasNext()) {
				ischema =  it.next();
				snode = ischema.getSimpleTypeSchemaNode(name);
			}

		} else {
			snode = ischema.getSimpleTypeSchemaNode(name);
		}
		return snode;
	}

	private SchemaNode importComplexTypeSchemaNode(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		SchemaNode snode = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (snode == null && it.hasNext()) {
				ischema =  it.next();
				snode = ischema.getComplexTypeSchemaNode(name);
			}

		} else {
			snode = ischema.getComplexTypeSchemaNode(name);
		}
		return snode;
	}

	private Node importComplexType(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			Node simpleType = null;
			while (simpleType == null && it.hasNext()) {
				ischema =  it.next();
				simpleType = ischema.getComplexType(name);
			}
			return simpleType;
		} else
			return ischema.getComplexType(name);
	}

	private Node importElement(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			Node element = null;
			while (element == null && it.hasNext()) {
				ischema =  it.next();
				element = ischema.getElement(name);
			}
			return element;
		} else
			return ischema.getElement(name);
	}

	private SchemaNode importElementSchemaNode(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		SchemaNode snode = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (snode == null && it.hasNext()) {
				ischema = (XMLSchema) it.next();
				snode = ischema.getElementSchemaNode(name);
			}
		} else {
			snode = ischema.getElementSchemaNode(name);
		}

		return snode;
	}

	private Node importElement(String name) {
		int count = noNamespaceImports.size();
		Node type = null;
		for (int i = 0; i < count; i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			type = ischema.getElement(name);
			if (type != null)
				break;
		}
		return type;
	}

	private SchemaNode importElementSchemaNode(String name) {
		int count = noNamespaceImports.size();
		SchemaNode snode = null;
		for (int i = 0; (snode == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			snode = ischema.getElementSchemaNode(name);
		}
		return snode;
	}

	private Node importSimpleType(String name) {
		int count = noNamespaceImports.size();
		Node type = null;
		for (int i = 0; i < count; i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			type = ischema.getSimpleType(name);
			if (type != null)
				break;
		}
		return type;
	}

	private SchemaNode importSimpleTypeSchemaNode(String name) {
		int count = noNamespaceImports.size();
		SchemaNode snode = null;
		for (int i = 0; (snode == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			snode = ischema.getSimpleTypeSchemaNode(name);
		}
		return snode;
	}

	private SchemaNode importComplexTypeSchemaNode(String name) {
		int count = noNamespaceImports.size();
		SchemaNode snode = null;
		for (int i = 0; (snode == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			snode = ischema.getComplexTypeSchemaNode(name);
		}
		return snode;
	}

	private Node importComplexType(String name) {
		int count = noNamespaceImports.size();
		Node type = null;
		for (int i = 0; i < count; i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			type = ischema.getComplexType(name);
			if (type != null)
				break;
		}
		return type;
	}

	public Node getSimpleType(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		if (uri == null || uri.equals(targetNamespace)) {
			Node simpleType = (Node) simpleTypes.get(CommonUtils
					.getUnqualifiedNodeName(name));
			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (simpleType == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				simpleType = is.getSimpleType(name);
			}
			if (simpleType == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					simpleType = (Node) container.simpleTypes.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (simpleType != null)
						break;
					parent = parent.container;
				}

			}

			if (simpleType == null)
				simpleType = importSimpleType(name);

			return simpleType;
		} else {
			return importSimpleType(uri, name);
		}
	}

	public SchemaNode getSimpleTypeSchemaNode(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node simpleType = (Node) simpleTypes.get(CommonUtils
					.getUnqualifiedNodeName(name));
			if (simpleType != null)
				snode = new SchemaNode(simpleType, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				snode = is.getSimpleTypeSchemaNode(name);
			}
			if (snode == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					simpleType = (Node) container.simpleTypes.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (simpleType != null) {
						snode = new SchemaNode(simpleType, this);
						break;
					}
					parent = parent.container;
				}

			}
			if (snode == null)
				snode = importSimpleTypeSchemaNode(name);

		} else {
			snode = importSimpleTypeSchemaNode(uri, name);
		}
		return snode;
	}

	public SchemaNode getComplexTypeSchemaNode(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		if (uri != null && uri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
			if (name.equals(prefix + ":anyType")) {
				return new SchemaNode(getAnyTypeNode(), this);
			}
		}

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node complexType = (Node) complexTypes.get(CommonUtils
					.getUnqualifiedNodeName(name));

			if (complexType != null)
				snode = new SchemaNode(complexType, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				snode = is.getComplexTypeSchemaNode(name);
			}
			if (snode == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					complexType = (Node) container.complexTypes.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (complexType != null) {
						snode = new SchemaNode(complexType, this);
						break;
					}
					parent = parent.container;
				}

			}
			if (snode == null)
				snode = importComplexTypeSchemaNode(name);

		} else {
			snode = importComplexTypeSchemaNode(uri, name);
		}
		return snode;
	}

	public Node getAnyTypeNode() {
		if (anyNode == null) {
			org.w3c.dom.Document doc = schema;

			org.w3c.dom.Element anye = doc.createElement("element");
			anye.setAttribute("name", "any");

			org.w3c.dom.Element ct = doc.createElement("complexType");
			ct.setAttribute("mixed", "true");
			org.w3c.dom.Element se = doc.createElement("sequence");
			ct.appendChild(se);

			org.w3c.dom.Element any = doc.createElement("any");
			any.setAttribute("namespace", "##any");
			any.setAttribute("processContents", "skip");
			any.setAttribute("minOccurs", "0");
			any.setAttribute("maxOccurs", "unbounded");
			se.appendChild(any);

			any = doc.createElement("anyAttribute");
			any.setAttribute("namespace", "##any");
			any.setAttribute("processContents", "skip");
			ct.appendChild(any);

			anye.appendChild(ct);

			anyNode = anye;
		}
		return anyNode;
	}

	public Node getComplexType(String name) {
		String uqname = CommonUtils.getUnqualifiedNodeName(name);
		String prefix = getPrefix(name);
		String uri = getUri(prefix);
		if (uri != null && uri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
			if (uqname.equals("anyType")) {
				return getAnyTypeNode();
			}
		}
		if (uri == null || uri.equals(targetNamespace)) {
			Node complexType = (Node) complexTypes.get(uqname);
			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (complexType == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				complexType = is.getComplexType(name);
			}
			if (complexType == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					complexType = (Node) container.complexTypes.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (complexType != null)
						break;
					parent = parent.container;
				}

			}
			if (complexType == null)
				complexType = importComplexType(name);

			return complexType;
		} else {
			return importComplexType(uri, name);
		}
	}

	public Node getType(String name) {
		Node type = getSimpleType(name);

		if (type == null)
			type = getComplexType(name);

		if (type == null) {
			String prefix = getPrefix(name);
			String uri = getUri(prefix);
			if (uri == null || !uri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
				type = getAnyTypeNode();
		}

		return type;
	}

	public SchemaNode getTypeSchemaNode(String name) {
		SchemaNode type = getSimpleTypeSchemaNode(name);

		if (type == null)
			type = getComplexTypeSchemaNode(name);

		if (type == null) {
			String prefix = getPrefix(name);
			String uri = getUri(prefix);
			if (uri == null || !uri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
				Node typeNode = getAnyTypeNode();
				type = new SchemaNode(typeNode, this);
			}
		}

		return type;
	}

	private SchemaNode getModelGroupSchemaNode(String name) {
		String prefix = getPrefix(name);
		String uri = getUri(prefix);

		SchemaNode snode = null;

		if (uri == null || uri.equals(targetNamespace)) {
			Node group = (Node) modelGroups.get(CommonUtils
					.getUnqualifiedNodeName(name));
			if (group != null)
				snode = new SchemaNode(group, this);

			int nincludes = (includes != null ? includes.size() : 0);
			for (int i = 0; (snode == null) && i < nincludes; i++) {
				XMLSchema is = (XMLSchema) includes.elementAt(i);
				snode = is.getModelGroupSchemaNode(name);
			}
			if (snode == null) {
				XMLSchema parent = container;
				while (uri != null && parent != null
						&& parent.getTargetNameSpace() != null
						&& parent.getTargetNameSpace().equals(uri)) {
					group = (Node) container.modelGroups.get(CommonUtils
							.getUnqualifiedNodeName(name));
					if (group != null) {
						snode = new SchemaNode(group, this);
						break;
					}
					parent = parent.container;
				}

			}
			if (snode == null)
				snode = importModelGroupSchemaNode(name);
		} else {
			snode = importModelGroupSchemaNode(uri, name);
		}
		return snode;
	}

	private SchemaNode importModelGroupSchemaNode(String nsUri, String name) {
		XMLSchema ischema = (XMLSchema) imports.get(nsUri);
		SchemaNode snode = null;
		if (ischema == null) {
			Collection<XMLSchema> col = imports.values();
			Iterator<XMLSchema> it = col.iterator();
			while (snode == null && it.hasNext()) {
				ischema = (XMLSchema) it.next();
				snode = ischema.getModelGroupSchemaNode(name);
			}

		} else {
			snode = ischema.getModelGroupSchemaNode(name);
		}
		return snode;
	}

	private SchemaNode importModelGroupSchemaNode(String name) {
		int count = noNamespaceImports.size();
		SchemaNode snode = null;
		for (int i = 0; (snode == null) && (i < count); i++) {
			XMLSchema ischema = (XMLSchema) noNamespaceImports.elementAt(i);
			snode = ischema.getModelGroupSchemaNode(name);
		}
		return snode;
	}

	public Node getConstraint(String name) {
		Node constr = (Node) constraints.get(CommonUtils
				.getUnqualifiedNodeName(name));
		int nincludes = (includes != null ? includes.size() : 0);
		for (int i = 0; (constr == null) && i < nincludes; i++) {
			XMLSchema is = (XMLSchema) includes.elementAt(i);
			constr = is.getConstraint(name);
		}
		return constr;
	}

	public Node getNotation(String name) {
		Node not = (Node) notations.get(CommonUtils
				.getUnqualifiedNodeName(name));
		int nincludes = (includes != null ? includes.size() : 0);
		for (int i = 0; (not == null) && i < nincludes; i++) {
			XMLSchema is = (XMLSchema) includes.elementAt(i);
			not = is.getNotation(name);
		}
		return not;
	}

}