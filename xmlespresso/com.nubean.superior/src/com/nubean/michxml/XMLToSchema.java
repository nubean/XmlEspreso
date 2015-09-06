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

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class XMLToSchema {

	private Document document;

	private Element schemaElement;

	private LinkedList<String> schemaElementsLinkedList;

	private void parseRootElement(Element rootElement) {

		schemaElementsLinkedList = new LinkedList<String>();
		// Get Top Level Elements

		// Add schema element for root element.

		String rootElementName = rootElement.getTagName();

		Element xmlElement = document.createElement("xs:element");

		schemaElement.appendChild(xmlElement);
		schemaElementsLinkedList.add(rootElementName);

		Attr attrElementName = document.createAttribute("name");
		attrElementName.setValue(rootElementName);
		xmlElement.setAttributeNode(attrElementName);

		NodeList rootNodeList = rootElement.getChildNodes();
		int len = rootNodeList.getLength();

		LinkedList<String> rootLinkedList = new LinkedList<String>();

		for (int i = 0; i < len; i++) {

			if (((rootNodeList.item(i)).getNodeType()) == Node.ELEMENT_NODE) {
				Element elem = (Element) rootNodeList.item(i);
				String elemName = elem.getTagName();

				if (!(rootLinkedList.contains(elemName)))
					rootLinkedList.add(elemName);
			}
		}

		generateSchemaElement(rootLinkedList, rootElement, xmlElement);

		// Add schema elements for top level elements.
		int subElementsLength = rootLinkedList.size();

		for (int i = 0; i < subElementsLength; i++) {

			String subElementName = (String) rootLinkedList.get(i);
			NodeList nodeList = rootElement
					.getElementsByTagName(subElementName);
			if (!(schemaElementsLinkedList.contains(subElementName))) {
				parseNodeList(nodeList, subElementName);
			}
		}

	}

	private void generateSchemaElement(LinkedList<String> subElements, Element element,
			Element xmlElement) {

		try {
			int subElementsLength = subElements.size();
			Element extensionElement = null;
			Element complexTypeElement = null;

			if (subElementsLength == 0) {

				complexTypeElement = document.createElement("xs:complexType");
				xmlElement.appendChild(complexTypeElement);

				Element simpleContentElement = document
						.createElement("xs:simpleContent");

				complexTypeElement.appendChild(simpleContentElement);
				extensionElement = document.createElement("xs:extension");

				simpleContentElement.appendChild(extensionElement);

				Attr attrAttribute = document.createAttribute("base");
				attrAttribute.setValue("xs:string");
				extensionElement.setAttributeNode(attrAttribute);

				Element attrElement = document.createElement("xs:anyAttribute");

				extensionElement.appendChild(attrElement);

			}
			// Add element refs for sub elements
			if (subElementsLength > 0) {

				complexTypeElement = document.createElement("xs:complexType");

				xmlElement.appendChild(complexTypeElement);

				Element choiceElement = document.createElement("xs:choice");
				complexTypeElement.appendChild(choiceElement);

				Attr attrAttribute = document.createAttribute("maxOccurs");
				attrAttribute.setValue("unbounded");
				choiceElement.setAttributeNode(attrAttribute);

				Element anyElement = document.createElement("xs:any");
				choiceElement.appendChild(anyElement);

				for (int i = 0; i < subElementsLength; i++) {

					String subElementName = (String) subElements.get(i);
					Element refElement = document.createElement("xs:element");
					choiceElement.appendChild(refElement);

					Attr refAttr = document.createAttribute("ref");
					refAttr.setValue(subElementName);
					refElement.setAttributeNode(refAttr);
				}

			}
			// Add attributes

			if (element.hasAttributes()) {

				NamedNodeMap nodeMap = element.getAttributes();
				int lengthNodeMap = nodeMap.getLength();

				for (int l = 0; l < lengthNodeMap; l++) {
					Node attrNode = nodeMap.item(l);
					String attrNodeName = attrNode.getNodeName();

					Element attrElement = document
							.createElement("xs:attribute");
					Attr attrAttribute = document.createAttribute("name");
					attrAttribute.setValue(attrNodeName);
					attrElement.setAttributeNode(attrAttribute);
					Attr attrAttributeType = document.createAttribute("type");
					attrAttributeType.setValue("xs:string");
					attrElement.setAttributeNode(attrAttributeType);

					if (subElementsLength == 0) {

						extensionElement.appendChild(attrElement);

					}

					if (subElementsLength > 0) {

						complexTypeElement.appendChild(attrElement);

					}

				}

			}

			if (subElementsLength > 0) {

				Element attrElement = document.createElement("xs:anyAttribute");
				complexTypeElement.appendChild(attrElement);

			}

		} catch (Exception e) {
		}
	}

	private void parseNodeList(NodeList nodeList, String elementName) {

		try {

			LinkedList<String> elementLinkedList = new LinkedList<String>();
			LinkedList<String> attributeLinkedList = new LinkedList<String>();
			boolean emptyElement = true;

			for (int i = 0; i < nodeList.getLength(); i++) {

				Element element = (Element) nodeList.item(i);
				if (!(element.getChildNodes().getLength() == 0)) {
					emptyElement = false;

				}
			}

			Element xmlElement = document.createElement("xs:element");

			schemaElement.appendChild(xmlElement);
			schemaElementsLinkedList.add(elementName);

			Attr attrElementName = document.createAttribute("name");
			attrElementName.setValue(elementName);
			xmlElement.setAttributeNode(attrElementName);

			for (int i = 0; i < nodeList.getLength(); i++) {

				Element element = (Element) nodeList.item(i);

				if (element.hasAttributes()) {

					NamedNodeMap nodeMap = element.getAttributes();
					int lengthNodeMap = nodeMap.getLength();

					for (int l = 0; l < lengthNodeMap; l++) {
						Node attrNode = nodeMap.item(l);
						String attrNodeName = attrNode.getNodeName();
						if (!(attributeLinkedList.contains(attrNodeName)))
							attributeLinkedList.add(attrNodeName);

					}
				}

				NodeList elementNodeList = element.getChildNodes();
				int len = elementNodeList.getLength();

				for (int j = 0; j < len; j++) {

					if (((elementNodeList.item(j)).getNodeType()) == Node.ELEMENT_NODE) {
						Element elem = (Element) elementNodeList.item(j);
						String elemName = elem.getTagName();

						if (!(elementLinkedList.contains(elemName)))
							elementLinkedList.add(elemName);
					}
				}
			}

			generateSchemaElement(elementLinkedList, attributeLinkedList,
					xmlElement, emptyElement);

			int subElementsLength = elementLinkedList.size();

			for (int i = 0; i < subElementsLength; i++) {
				LinkedList<NodeList> nodeListLinkedList = new LinkedList<NodeList>();
				String subElementName = (String) elementLinkedList.get(i);

				for (int j = 0; j < nodeList.getLength(); j++) {
					Element element = (Element) nodeList.item(j);

					NodeList nodeListSubElements = element
							.getElementsByTagName(subElementName);
					nodeListLinkedList.add(nodeListSubElements);

				}
				if (!(schemaElementsLinkedList.contains(subElementName))) {
					parseNodeList(nodeListLinkedList, subElementName);
				}
			}
			// For each of sub elements

		} catch (Exception e) {
		}

	}

	private void parseNodeList(LinkedList<NodeList> nodeListLinkedList, String elementName) {

		try {
			LinkedList<String> elementLinkedList = new LinkedList<String>();
			LinkedList<String> attributeLinkedList = new LinkedList<String>();
			boolean emptyElement = true;

			for (int i = 0; i < nodeListLinkedList.size(); i++) {

				NodeList nodeList = (NodeList) nodeListLinkedList.get(i);

				for (int j = 0; j < nodeList.getLength(); j++) {

					Element element = (Element) nodeList.item(j);
					if (!(element.getChildNodes().getLength() == 0)) {
						emptyElement = false;

					}
				}
			}

			Element xmlElement = document.createElement("xs:element");

			schemaElement.appendChild(xmlElement);
			schemaElementsLinkedList.add(elementName);

			Attr attrElementName = document.createAttribute("name");
			attrElementName.setValue(elementName);
			xmlElement.setAttributeNode(attrElementName);

			for (int i = 0; i < nodeListLinkedList.size(); i++) {

				NodeList nodeList = (NodeList) nodeListLinkedList.get(i);

				for (int j = 0; j < nodeList.getLength(); j++) {

					Element element = (Element) nodeList.item(j);

					if (element.hasAttributes()) {

						NamedNodeMap nodeMap = element.getAttributes();
						int lengthNodeMap = nodeMap.getLength();

						for (int l = 0; l < lengthNodeMap; l++) {
							Node attrNode = nodeMap.item(l);
							String attrNodeName = attrNode.getNodeName();
							if (!(attributeLinkedList.contains(attrNodeName)))
								attributeLinkedList.add(attrNodeName);

						}
					}

					NodeList elementNodeList = element.getChildNodes();
					int len = elementNodeList.getLength();

					for (int x = 0; x < len; x++) {

						if (((elementNodeList.item(x)).getNodeType()) == Node.ELEMENT_NODE) {
							Element elem = (Element) elementNodeList.item(x);
							String elemName = elem.getTagName();

							if (!(elementLinkedList.contains(elemName)))
								elementLinkedList.add(elemName);
						}
					}
				}
			}
			generateSchemaElement(elementLinkedList, attributeLinkedList,
					xmlElement, emptyElement);

			int subElementsLength = elementLinkedList.size();

			for (int i = 0; i < subElementsLength; i++) {
				LinkedList<NodeList> nodeLinkedList = new LinkedList<NodeList>();
				String subElementName = (String) elementLinkedList.get(i);

				for (int j = 0; j < nodeListLinkedList.size(); j++) {
					NodeList nodeList = (NodeList) nodeListLinkedList.get(j);

					for (int x = 0; x < nodeList.getLength(); x++) {
						Element element = (Element) nodeList.item(x);

						NodeList nodeListSubElements = element
								.getElementsByTagName(subElementName);
						nodeLinkedList.add(nodeListSubElements);

					}
				}
				if (!(schemaElementsLinkedList.contains(subElementName))) {
					parseNodeList(nodeLinkedList, subElementName);
				}

			}
			// For each of sub elements

		} catch (Exception e) {
		}

	}

	private void generateSchemaElement(LinkedList<String> subElements,
			LinkedList<String> attributes, Element xmlElement, boolean emptyElement) {

		Element extensionElement = null;
		Element complexTypeElement = null;
		try {
			int subElementsLength = subElements.size();

			if (emptyElement) {

				complexTypeElement = document.createElement("xs:complexType");
				xmlElement.appendChild(complexTypeElement);

				Element attrElement = document.createElement("xs:anyAttribute");
				complexTypeElement.appendChild(attrElement);

			}

			if (!emptyElement && subElementsLength == 0) {

				complexTypeElement = document.createElement("xs:complexType");
				xmlElement.appendChild(complexTypeElement);

				Element simpleContentElement = document
						.createElement("xs:simpleContent");
				complexTypeElement.appendChild(simpleContentElement);

				extensionElement = document.createElement("xs:extension");
				simpleContentElement.appendChild(extensionElement);

				Attr attrAttribute = document.createAttribute("base");
				attrAttribute.setValue("xs:string");
				extensionElement.setAttributeNode(attrAttribute);

				Element attrElement = document.createElement("xs:anyAttribute");
				extensionElement.appendChild(attrElement);

			}
			// Add element refs for sub elements
			if (subElementsLength > 0) {

				complexTypeElement = document.createElement("xs:complexType");
				xmlElement.appendChild(complexTypeElement);

				Element choiceElement = document.createElement("xs:choice");
				complexTypeElement.appendChild(choiceElement);

				Attr attrAttribute = document.createAttribute("maxOccurs");
				attrAttribute.setValue("unbounded");
				choiceElement.setAttributeNode(attrAttribute);

				Element anyElement = document.createElement("xs:any");
				choiceElement.appendChild(anyElement);

				for (int i = 0; i < subElementsLength; i++) {

					String subElementName = (String) subElements.get(i);
					Element refElement = document.createElement("xs:element");
					choiceElement.appendChild(refElement);

					Attr refAttr = document.createAttribute("ref");
					refAttr.setValue(subElementName);
					refElement.setAttributeNode(refAttr);
				}

			}
			// Add attributes

			if (attributes.size() > 0) {

				for (int l = 0; l < attributes.size(); l++) {

					String attrNodeName = (String) attributes.get(l);

					Element attrElement = document
							.createElement("xs:attribute");
					Attr attrAttribute = document.createAttribute("name");
					attrAttribute.setValue(attrNodeName);
					attrElement.setAttributeNode(attrAttribute);
					Attr attrAttributeType = document.createAttribute("type");
					attrAttributeType.setValue("xs:string");
					attrElement.setAttributeNode(attrAttributeType);

					if (emptyElement) {
						complexTypeElement.appendChild(attrElement);
					}

					if (!emptyElement && subElementsLength == 0) {

						extensionElement.appendChild(attrElement);

					}

					if (subElementsLength > 0) {

						complexTypeElement.appendChild(attrElement);

					}

				}

			}

			if (subElementsLength > 0) {

				Element attrElement = document.createElement("xs:anyAttribute");
				complexTypeElement.appendChild(attrElement);

			}

		} catch (Exception e) {
		}
	}

	private File generateSchema(Document xmlDocument ) {

		File schemaFile = null;
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();

			document = documentBuilder.newDocument();
			schemaElement = document.createElement("xs:schema");

			Attr attrSchemaElement = document.createAttribute("xmlns:xs");
			attrSchemaElement.setValue(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaElement.setAttributeNode(attrSchemaElement);
			document.appendChild(schemaElement);

			Element rootElement = xmlDocument.getDocumentElement();

			parseRootElement(rootElement);

			TransformerFactory tffactory = TransformerFactory.newInstance();
			Transformer transformer = tffactory.newTransformer();

			schemaFile = File.createTempFile("xmlespresso", ".xsd");

			FileOutputStream xmlOut = new FileOutputStream(schemaFile);

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(xmlOut);
			transformer.transform(source, result);

		} catch (Exception e) {
		}
		return schemaFile;

	}

	public static File getSchemaForXML(Document xmlDocument) {

		File schemaFile = null;
		if (xmlDocument != null) {
			XMLToSchema xmlToSchema = new XMLToSchema();
			schemaFile = xmlToSchema.generateSchema(xmlDocument);
		}

		return schemaFile;
	}

}
