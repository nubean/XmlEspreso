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

import com.wutka.dtd.*;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michxml.XMLWriter;

import java.io.*;

import org.w3c.dom.*;
import java.net.*;

public class DtdSchema {
	private DTD dtdParse;

	private Document document;

	private Element schemaElement;

	private Element dtdElement, complexTypeElement;

	private Element annotationElement, documentationElement;

	Attr attrDocumentationElement;

	private Element extension;

	private URL dtdUrl = null;

	private URL systemUrl;

	private String nsPrefix, nsUri;

	public DtdSchema(String nsUri, String nsPrefix) {
		this.nsPrefix = nsPrefix;
		this.nsUri = nsUri;
	}

	public void parseDtd(URL dtdUrl) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			document = documentBuilder.newDocument();
			schemaElement = document.createElement("xs:schema");
			document.appendChild(schemaElement);
			Attr attrSchemaElement = document.createAttribute("xmlns:xs");
			attrSchemaElement.setValue(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaElement.setAttributeNode(attrSchemaElement);

			attrSchemaElement = document.createAttribute("xmlns:xsi");
			attrSchemaElement
					.setValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			schemaElement.setAttributeNode(attrSchemaElement);

			attrSchemaElement = document.createAttribute("xmlns:xml");
			attrSchemaElement.setValue(XMLConstants.XML_NS_URI);
			schemaElement.setAttributeNode(attrSchemaElement);

			if (nsUri != null && nsUri.trim().length() > 0) {
				Attr attrNamespaceElement = document
						.createAttribute(XMLConstants.XMLNS_ATTRIBUTE);
				attrNamespaceElement.setValue(nsUri.trim());
				schemaElement.setAttributeNode(attrNamespaceElement);
				if (nsPrefix != null && nsPrefix.trim().length() > 0) {
					attrNamespaceElement = document
							.createAttribute(CommonUtils.XMLNS_PREFIX
									+ nsPrefix.trim());
					attrNamespaceElement.setValue(nsUri.trim());
					schemaElement.setAttributeNode(attrNamespaceElement);
				}
				attrNamespaceElement = document
						.createAttribute("targetNamespace");
				attrNamespaceElement.setValue(nsUri.trim());
				schemaElement.setAttributeNode(attrNamespaceElement);
			}
			annotationElement = document.createElement("xs:annotation");
			schemaElement.appendChild(annotationElement);
			documentationElement = document.createElement("xs:documentation");
			attrDocumentationElement = document.createAttribute("xml:lang");
			attrDocumentationElement.setValue("en");
			documentationElement.setAttributeNode(attrDocumentationElement);
			documentationElement.appendChild(document
					.createTextNode("Generated from DTD."));
			annotationElement.appendChild(documentationElement);

		} catch (ParserConfigurationException excp) {
			excp.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.dtdUrl = dtdUrl;

		try {
			String dtdFile = dtdUrl.getFile();
			int index = dtdFile.lastIndexOf('/');
			String dtdFilePath = "dtds" + dtdFile.substring(index);

			InputStream is = getClass().getClassLoader().getResourceAsStream(
					dtdFilePath);
			if (is != null) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				parseDtd(br);
			} else {
				is = dtdUrl.openConnection().getInputStream();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				parseDtd(br);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void createComment(DTDComment dtdItem) {
		DTDComment dtdComment = (DTDComment) dtdItem;
		annotationElement = document.createElement("xs:annotation");
		schemaElement.appendChild(annotationElement);
		documentationElement = document.createElement("xs:documentation");
		attrDocumentationElement = document.createAttribute("xml:lang");
		attrDocumentationElement.setValue("en");
		documentationElement.setAttributeNode(attrDocumentationElement);
		String dtdText = dtdComment.text;
		documentationElement.appendChild(document.createTextNode(dtdText));
		annotationElement.appendChild(documentationElement);

	}

	private void createNotation(DTDNotation dtdItem) {
		DTDNotation dtdNotation = (DTDNotation) dtdItem;
		DTDExternalID dtdExternalID = dtdNotation.getExternalID();
		String system = dtdExternalID.getSystem();
		String notationName = dtdNotation.name;
		Element notationElement = document.createElement("xs:notation");
		schemaElement.appendChild(notationElement);
		Attr attrNotationElement = document.createAttribute("name");
		attrNotationElement.setValue(notationName);
		notationElement.setAttributeNode(attrNotationElement);
		Attr attrNotation = document.createAttribute("system");
		attrNotation.setValue(system);
		notationElement.setAttributeNode(attrNotation);
	}

	private void parseExternalID(DTDEntity dtdEntity) {

		String system = null;
		String ndata = null;

		DTDExternalID dtdExternalID = dtdEntity.externalID;
		if (dtdExternalID != null) {
			system = dtdExternalID.system;
		}
		ndata = dtdEntity.ndata;

		if ((system != null) && (ndata == null)) {
			try {
				String dtdUrlStr = dtdUrl.toString();
				int index = dtdUrlStr.lastIndexOf("\\");
				if (index == -1)
					index = dtdUrlStr.lastIndexOf("/");
				String baseUrl = dtdUrlStr.substring(0, index + 1);
				systemUrl = new URL(baseUrl + system);
			} catch (MalformedURLException excep) {
			}

			try {

				String systemFile = systemUrl.getFile();
				int index = systemFile.lastIndexOf('/');
				String dtdFilePath = "dtds" + systemFile.substring(index);

				InputStream is = getClass().getClassLoader()
						.getResourceAsStream(dtdFilePath);
				if (is != null) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					parseDtd(br);
				} else {

					InputStream isExternalFile = systemUrl.openConnection()
							.getInputStream();
					BufferedReader brExternalFile = new BufferedReader(
							new InputStreamReader(isExternalFile));
					parseDtd(brExternalFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void parseEntity(DTDEntity dtdItem) {
		DTDEntity dtdEntity = (DTDEntity) dtdItem;
		parseExternalID(dtdEntity);

	}

	private void parseDtd(Reader dtd) throws IOException {
		DTDParser dtdParser = new DTDParser(dtd);
		dtdParse = dtdParser.parse(true);

		java.util.Vector dtdItems = dtdParse.items;
		Iterator iter = dtdItems.iterator();
		while (iter.hasNext()) {
			java.lang.Object dtdItem = iter.next();
			if (dtdItem instanceof com.wutka.dtd.DTDComment) {
				createComment((DTDComment) dtdItem);
			} else if (dtdItem instanceof com.wutka.dtd.DTDElement) {
				DTDElement dtdElement = (DTDElement) dtdItem;
				parseDtdElement(dtdParse, dtdElement, schemaElement);
			} else if (dtdItem instanceof com.wutka.dtd.DTDAttribute) {

			} else if (dtdItem instanceof com.wutka.dtd.DTDNotation) {
				createNotation((DTDNotation) dtdItem);
			}

			else if (dtdItem instanceof com.wutka.dtd.DTDEntity) {
				parseEntity((DTDEntity) dtdItem);
			}
		}

	}

	private void setCardinality(DTDCardinal dtdCardinal, Element elementCardinal) {

		if (dtdCardinal.equals(DTDCardinal.NONE)) {
		}
		if (dtdCardinal.equals(DTDCardinal.OPTIONAL)) {
			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("0");
			elementCardinal.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("1");
			elementCardinal.setAttributeNode(attrMaxOccurs);
		}
		if (dtdCardinal.equals(DTDCardinal.ZEROMANY)) {
			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("0");
			elementCardinal.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("unbounded");
			elementCardinal.setAttributeNode(attrMaxOccurs);
		}
		if (dtdCardinal.equals(DTDCardinal.ONEMANY)) {
			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("1");
			elementCardinal.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("unbounded");
			elementCardinal.setAttributeNode(attrMaxOccurs);
		}

	}

	private void setRefCardinality(DTDCardinal dtdCardinal, String elementName,
			Element elementCardinal) {

		Element elementRef = document.createElement("xs:element");
		Attr attrElementRef = document.createAttribute("ref");
		attrElementRef.setValue(elementName);
		elementRef.setAttributeNode(attrElementRef);
		elementCardinal.appendChild(elementRef);
		if (dtdCardinal.equals(DTDCardinal.NONE)) {

		}
		if (dtdCardinal.equals(DTDCardinal.OPTIONAL)) {

			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("0");
			elementRef.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("1");
			elementRef.setAttributeNode(attrMaxOccurs);
			elementCardinal.appendChild(elementRef);
		}
		if (dtdCardinal.equals(DTDCardinal.ZEROMANY)) {

			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("0");
			elementRef.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("unbounded");
			elementRef.setAttributeNode(attrMaxOccurs);
			elementCardinal.appendChild(elementRef);
		}
		if (dtdCardinal.equals(DTDCardinal.ONEMANY)) {

			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("1");
			elementRef.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("unbounded");
			elementRef.setAttributeNode(attrMaxOccurs);
			elementCardinal.appendChild(elementRef);
		}

	}

	private void parseDtdItem(DTDItem dtdItem, Element documentElement) {
		DTDName dtdName;
		dtdName = (com.wutka.dtd.DTDName) dtdItem;
		String elementName = dtdName.value;
		DTDCardinal dtdCardinal = dtdItem.getCardinal();
		setRefCardinality(dtdCardinal, elementName, documentElement);

	}

	private void parseSubSequence(DTDItem dtdItem, Element sequenceElement) {
		Element enumSeqElement = document.createElement("xs:sequence");
		sequenceElement.appendChild(enumSeqElement);

		DTDCardinal dtdSequenceCardinal = dtdItem.getCardinal();
		setCardinality(dtdSequenceCardinal, enumSeqElement);

		DTDSequence enumSequence = (DTDSequence) dtdItem;
		java.util.Vector dtdSeqItems = enumSequence.getItemsVec();
		Iterator iter = dtdSeqItems.iterator();
		while (iter.hasNext()) {

			DTDItem sequenceItem = (DTDItem) (iter.next());

			if (sequenceItem instanceof com.wutka.dtd.DTDName) {

				parseDtdItem(sequenceItem, enumSeqElement);

			}

			if (sequenceItem instanceof com.wutka.dtd.DTDChoice) {
				parseSubChoice(sequenceItem, enumSeqElement);

			}

			if (sequenceItem instanceof com.wutka.dtd.DTDSequence) {
				parseSubSequence(sequenceItem, enumSeqElement);

			}

		}

	}

	private void parseSubChoice(DTDItem dtdItem, Element choiceElement) {

		Element enumChoiceElement = document.createElement("xs:choice");
		choiceElement.appendChild(enumChoiceElement);

		DTDCardinal dtdChoiceCardinal = dtdItem.getCardinal();
		setCardinality(dtdChoiceCardinal, enumChoiceElement);

		DTDChoice enumChoice = (DTDChoice) dtdItem;
		java.util.Vector dtdChoiceItems = enumChoice.getItemsVec();
		Iterator iter = dtdChoiceItems.iterator();

		while (iter.hasNext()) {

			DTDItem dtdChoiceItem = (DTDItem) (iter.next());

			if (dtdChoiceItem instanceof com.wutka.dtd.DTDName) {
				parseDtdItem(dtdChoiceItem, enumChoiceElement);

			}

			if (dtdChoiceItem instanceof com.wutka.dtd.DTDChoice) {
				parseSubChoice(dtdChoiceItem, enumChoiceElement);

			}

			if (dtdChoiceItem instanceof com.wutka.dtd.DTDSequence) {
				parseSubSequence(dtdChoiceItem, enumChoiceElement);

			}

		}

	}

	private void parseSequence(DTDItem dtdItem) {

		Element seqElement = document.createElement("xs:sequence");
		complexTypeElement.appendChild(seqElement);
		DTDCardinal dtdSequenceCardinal = dtdItem.getCardinal();
		setCardinality(dtdSequenceCardinal, seqElement);
		DTDSequence dtdItemSequence = (DTDSequence) dtdItem;
		java.util.Vector dtdSequenceItems = dtdItemSequence.getItemsVec();
		Iterator itr = dtdSequenceItems.iterator();
		while (itr.hasNext()) {
			DTDItem dtdSeqItem = (DTDItem) itr.next();
			if (dtdSeqItem instanceof com.wutka.dtd.DTDName) {
				parseDtdItem(dtdSeqItem, seqElement);

			}

			if (dtdSeqItem instanceof com.wutka.dtd.DTDChoice) {
				parseSubChoice(dtdSeqItem, seqElement);

			}
			if (dtdSeqItem instanceof com.wutka.dtd.DTDSequence) {
				parseSubSequence(dtdSeqItem, seqElement);

			}
		}
	}

	private void parseChoice(DTDItem dtdItem) {

		Element choiceElement;
		DTDChoice dtdItemChoice = (DTDChoice) dtdItem;
		java.util.Vector dtdChoiceItems = dtdItemChoice.getItemsVec();
		if (dtdChoiceItems.size() == 1) {

			DTDName choiceDTDNameItem = null;
			Element seqElement = document.createElement("xs:sequence");
			complexTypeElement.appendChild(seqElement);
			if ((dtdChoiceItems.elementAt(0)) instanceof com.wutka.dtd.DTDName) {
				choiceDTDNameItem = (com.wutka.dtd.DTDName) dtdChoiceItems
						.elementAt(0);
				String choiceElementName = choiceDTDNameItem.value;

				DTDCardinal dtdCardinal = dtdItemChoice.getCardinal();
				setRefCardinality(dtdCardinal, choiceElementName, seqElement);

			}
		}
		if (dtdChoiceItems.size() > 1) {

			choiceElement = document.createElement("xs:choice");
			complexTypeElement.appendChild(choiceElement);
			DTDCardinal dtdCardinal = dtdItemChoice.getCardinal();
			setCardinality(dtdCardinal, choiceElement);

			Iterator itr = dtdChoiceItems.iterator();
			while (itr.hasNext()) {
				DTDItem dtdCItem = (DTDItem) itr.next();
				if (dtdCItem instanceof com.wutka.dtd.DTDName) {

					parseDtdItem(dtdCItem, choiceElement);

				}

				if (dtdCItem instanceof com.wutka.dtd.DTDChoice) {

					parseSubChoice(dtdCItem, choiceElement);

				}
				if (dtdCItem instanceof com.wutka.dtd.DTDSequence) {

					parseSubSequence(dtdCItem, choiceElement);

				}
			}
		}

	}

	private void parseMixed(DTDItem dtdItem) {
		DTDName dtdChoiceElement = null;
		DTDMixed dtdItemMixed = (DTDMixed) dtdItem;
		java.util.Vector dtdMixedItems = dtdItemMixed.getItemsVec();
		Iterator iter = dtdMixedItems.iterator();
		if (dtdMixedItems.size() > 1) {
			Element mixedElement = document.createElement("xs:choice");
			complexTypeElement.appendChild(mixedElement);
			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("0");
			mixedElement.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("unbounded");
			mixedElement.setAttributeNode(attrMaxOccurs);

			Attr attrMixedElement = document.createAttribute("mixed");
			attrMixedElement.setValue("true");
			complexTypeElement.setAttributeNode(attrMixedElement);
			while (iter.hasNext()) {
				DTDItem mixedItem = (DTDItem) (iter.next());
				if (mixedItem instanceof com.wutka.dtd.DTDName) {

					parseDtdItem(mixedItem, mixedElement);

				}

				if (mixedItem instanceof com.wutka.dtd.DTDChoice) {
					parseSubChoice(mixedItem, mixedElement);

				}
				if (mixedItem instanceof com.wutka.dtd.DTDSequence) {
					parseSubSequence(mixedItem, mixedElement);

				}
			}
		}

	}

	private void parseAttributes(DTDItem dtdItem, DTDElement dtdElement) {
		java.util.Hashtable elementAttributes = (dtdElement.attributes);
		java.util.Enumeration attributeEnum = elementAttributes.elements();
		String use = "optional";
		String attrEnum = "";
		while (attributeEnum.hasMoreElements()) {
			DTDAttribute dtdElementAttr = (DTDAttribute) attributeEnum
					.nextElement();
			String attrName = dtdElementAttr.name;
			String attrDefaultValue = dtdElementAttr.defaultValue;
			DTDDecl attrDtdDecl = dtdElementAttr.decl;
			if (attrDtdDecl.equals(DTDDecl.IMPLIED)) {
				use = "optional";
			}
			if (attrDtdDecl.equals(DTDDecl.REQUIRED)) {
				use = "required";
			}
			String attributeType = "nmtoken";
			if (!((dtdElementAttr.type instanceof String)
					|| (dtdElementAttr.type instanceof DTDEnumeration) || (dtdElementAttr.type instanceof DTDNotationList))) {
				attributeType = "attrString";
			}
			if ((dtdElementAttr.type instanceof String)
					|| attributeType.equals("attrString")) {

				Element elementAttr = document.createElement("xs:attribute");
				attrName = attrName.substring(attrName.indexOf(":") + 1);
				Attr elementAttrName = document.createAttribute("name");
				elementAttrName.setValue(attrName);
				elementAttr.setAttributeNode(elementAttrName);
				Attr elementAttrType = document.createAttribute("type");
				elementAttrType.setValue("xs:string");
				elementAttr.setAttributeNode(elementAttrType);
				Attr elementAttrUse = document.createAttribute("use");
				elementAttrUse.setValue(use);
				elementAttr.setAttributeNode(elementAttrUse);
				if (attrDefaultValue != null) {
					Attr elementAttrDefault = (attrDtdDecl
							.equals(DTDDecl.FIXED) ? document
							.createAttribute("fixed") : document
							.createAttribute("default"));
					elementAttrDefault.setValue(attrDefaultValue);
					elementAttr.setAttributeNode(elementAttrDefault);
				}
				if (!(dtdItem instanceof com.wutka.dtd.DTDPCData)) {
					complexTypeElement.appendChild(elementAttr);
				}
				if (dtdItem instanceof com.wutka.dtd.DTDPCData) {
					extension.appendChild(elementAttr);
				}
			}
			if (dtdElementAttr.type instanceof com.wutka.dtd.DTDEnumeration) {
				DTDEnumeration attrDtdEnumeration = (DTDEnumeration) dtdElementAttr.type;
				java.util.Vector attrEnumVec = attrDtdEnumeration.getItemsVec();
				Iterator iter = attrEnumVec.iterator();

				Element elementAttr = document.createElement("xs:attribute");
				attrName = attrName.substring(attrName.indexOf(":") + 1);
				Attr elementAttrName = document.createAttribute("name");
				elementAttrName.setValue(attrName);
				elementAttr.setAttributeNode(elementAttrName);
				Element simpleTypeElement = document
						.createElement("xs:simpleType");
				elementAttr.appendChild(simpleTypeElement);
				Element restrictionTypeElement = document
						.createElement("xs:restriction");
				simpleTypeElement.appendChild(restrictionTypeElement);
				Attr simpleTypeAttrName = document.createAttribute("base");
				simpleTypeAttrName.setValue("xs:string");
				restrictionTypeElement.setAttributeNode(simpleTypeAttrName);
				while (iter.hasNext()) {
					attrEnum = (String) iter.next();
					Element enumElement = document
							.createElement("xs:enumeration");
					Attr enumValue = document.createAttribute("value");
					enumValue.setValue(attrEnum);
					enumElement.setAttributeNode(enumValue);
					restrictionTypeElement.appendChild(enumElement);
				}
				if (!(dtdItem instanceof com.wutka.dtd.DTDPCData)) {
					complexTypeElement.appendChild(elementAttr);
				}
				if (dtdItem instanceof com.wutka.dtd.DTDPCData) {
					extension.appendChild(elementAttr);
				}
			}
			if (dtdElementAttr.type instanceof DTDNotationList) {
				DTDNotationList attrNotationList = (DTDNotationList) dtdElementAttr.type;

				Element elementAttr = document.createElement("xs:attribute");
				attrName = attrName.substring(attrName.indexOf(":") + 1);
				Attr elementAttrName = document.createAttribute("name");
				elementAttrName.setValue(attrName);
				elementAttr.setAttributeNode(elementAttrName);
				Element simpleType = document.createElement("xs:simpleType");
				elementAttr.appendChild(simpleType);
				Attr attrBase = document.createAttribute("base");
				attrBase.setValue("xs:notation");
				simpleType.setAttributeNode(attrBase);
				String[] notationArray = attrNotationList.getItems();
				int attrArraySize = notationArray.length;
				for (int i = 0; i < attrArraySize; i++) {
					Element attrNotation = document
							.createElement("xs:enumeration");
					simpleType.appendChild(attrNotation);
					Attr enumValue = document.createAttribute("value");
					enumValue.setValue(notationArray[i]);
					attrNotation.setAttributeNode(enumValue);
				}
				if (!(dtdItem instanceof com.wutka.dtd.DTDPCData)) {
					complexTypeElement.appendChild(elementAttr);
				}
				if (dtdItem instanceof com.wutka.dtd.DTDPCData) {
					extension.appendChild(elementAttr);
				}
			}
		}

	}

	public void parseDtdElement(DTD dtdParse,
			com.wutka.dtd.DTDElement parseElement,
			org.w3c.dom.Element schemaElement) throws DOMException {
		String dtdElementName = parseElement.name;
		dtdElement = document.createElement("xs:element");
		Attr attrElement = document.createAttribute("name");
		attrElement.setValue(dtdElementName);
		dtdElement.setAttributeNode(attrElement);

		DTDItem dtdItem = parseElement.getContent();
		if (dtdItem instanceof com.wutka.dtd.DTDMixed) {
			DTDMixed dtdItemMixed = (DTDMixed) dtdItem;
			java.util.Vector dtdMixedItems = dtdItemMixed.getItemsVec();
			if (dtdMixedItems.size() == 1) {
				dtdItem = (DTDPCData) (dtdMixedItems.elementAt(0));
			}
		}

		if (!((dtdItem instanceof com.wutka.dtd.DTDPCData) && ((parseElement.attributes)
				.size() == 0))) {
			complexTypeElement = document.createElement("xs:complexType");
			dtdElement.appendChild(complexTypeElement);
		}

		if ((dtdItem instanceof com.wutka.dtd.DTDPCData)
				&& ((parseElement.attributes).size() == 0)) {

			Attr elementAttrType = document.createAttribute("type");
			elementAttrType.setValue("xs:string");
			dtdElement.setAttributeNode(elementAttrType);

		}
		if ((dtdItem instanceof com.wutka.dtd.DTDPCData)
				&& ((parseElement.attributes).size() != 0)) {

			Element simpleContent = document.createElement("xs:simpleContent");
			complexTypeElement.appendChild(simpleContent);
			extension = document.createElement("xs:extension");
			Attr baseExtension = document.createAttribute("base");
			baseExtension.setValue("xs:string");
			extension.setAttributeNode(baseExtension);
			simpleContent.appendChild(extension);
		}

		if ((dtdItem instanceof com.wutka.dtd.DTDEmpty)
				&& ((parseElement.attributes).size() == 0)) {
			Element complexContentElement = document
					.createElement("xs:complexContent");
			complexTypeElement.appendChild(complexContentElement);
			Element restrictionElement = document
					.createElement("xs:restriction");
			Attr baseRestriction = document.createAttribute("base");
			baseRestriction.setValue("xs:anyType");
			restrictionElement.setAttributeNode(baseRestriction);
			complexContentElement.appendChild(restrictionElement);

		}
		if (dtdItem instanceof com.wutka.dtd.DTDAny) {
			Element seqElement = document.createElement("xs:sequence");
			Element anyElement = document.createElement("xs:any");

			Attr attrMinOccurs = document.createAttribute("minOccurs");
			attrMinOccurs.setValue("0");
			anyElement.setAttributeNode(attrMinOccurs);
			Attr attrMaxOccurs = document.createAttribute("maxOccurs");
			attrMaxOccurs.setValue("unbounded");
			anyElement.setAttributeNode(attrMaxOccurs);
			seqElement.appendChild(anyElement);
			complexTypeElement.appendChild(seqElement);

			Element anyAttribute = document.createElement("xs:anyAttribute");
			complexTypeElement.appendChild(anyAttribute);

		}

		if (dtdItem instanceof com.wutka.dtd.DTDContainer) {

			if (dtdItem instanceof com.wutka.dtd.DTDSequence) {

				parseSequence(dtdItem);

			}
			if (dtdItem instanceof com.wutka.dtd.DTDChoice) {

				parseChoice(dtdItem);

			}
			if (dtdItem instanceof com.wutka.dtd.DTDMixed) {
				parseMixed(dtdItem);

			}
		}
		parseAttributes(dtdItem, parseElement);

		schemaElement.appendChild(dtdElement);
	}

	public Document getDocument() {
		return document;
	}

	public void print(PrintWriter pw) {
		pw.println("<?xml version='1.0' encoding='UTF-8'?>");
		pw.println("<!DOCTYPE xs:schema PUBLIC '-//W3C//DTD XMLSCHEMA 200102//EN'");
		pw.println("  'http://www.w3.org/2001/XMLSchema.dtd'");
		pw.println(">");

		XMLWriter xmlw = new XMLWriter(pw, true, "UTF-8");
		xmlw.print(document);

	}

}