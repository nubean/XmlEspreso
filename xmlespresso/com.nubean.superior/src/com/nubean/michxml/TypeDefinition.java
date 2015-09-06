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

import java.util.regex.*;
import java.util.*;
import java.math.*;
import org.w3c.dom.*;

import com.nubean.michbase.CommonUtils;

import java.text.*;

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

public class TypeDefinition {
	protected Pattern regexp;

	protected int length, minLength, maxLength;

	protected BigDecimal minExclusive, maxExclusive, minInclusive,
			maxInclusive;

	protected int totalDigits, fractionDigits;

	protected String base, pattern, initValue;

	protected Vector<String> enums;

	protected XMLSchema schema;

	protected boolean list, union;

	protected TypeDefinition listTypeDef, unionTypeDef;

	protected Vector<TypeDefinition> childTypeDefs;

	public TypeDefinition(org.w3c.dom.Node node, XMLSchema schema) {
		this.schema = schema;
		init(node);
	}

	public boolean isList() {
		return list;
	}

	public String getInitValue() {
		return initValue;
	}

	public boolean isUnion() {
		return union;
	}

	public String getBase() {
		return base;
	}

	public Pattern getPattern() {
		return regexp;
	}

	protected String getPattern(String base) {
		String pattern = ".*";

		if (base.equals("float") || base.equals("double")) {
			pattern = "(NaN)|([\\-+]?INF)|([\\-+]?((\\p{Digit}+([.](\\p{Digit})*)?)|((\\p{Digit})*[.](\\p{Digit})+))([eE][\\-]?(\\p{Digit})+)?)";
		} else if (base.equals("decimal")) {
			pattern = "[\\-+]?(((\\p{Digit})+([.](\\p{Digit})*)?)|((\\p{Digit})*[.](\\p{Digit})+))";
		} else if (base.equals("integer")) {
			pattern = "[\\-+]?(\\p{Digit})+";
		} else if (base.equals("positiveInteger")) {
			pattern = "[+]?(\\p{Digit})+";
			minInclusive = new BigDecimal(1);
		} else if (base.equals("nonPositiveInteger")) {
			pattern = "(-(\\p{Digit})+)|(0)";
			maxInclusive = new BigDecimal(0);
		} else if (base.equals("negativeInteger")) {
			pattern = "-(\\p{Digit})+";
			maxInclusive = new BigDecimal(-1);
		} else if (base.equals("nonNegativeInteger")) {
			pattern = "[+]?(\\p{Digit})+";
			minInclusive = new BigDecimal(0);
		} else if (base.equals("long")) {
			pattern = "[\\-+]?(\\p{Digit}){0,19}";
			minInclusive = new BigDecimal("-9223372036854775808");
			maxInclusive = new BigDecimal("9223372036854775807");
		} else if (base.equals("int")) {
			pattern = "[\\-+]?(\\p{Digit}){0,10}";
			minInclusive = new BigDecimal(-2147483648);
			maxInclusive = new BigDecimal(2147483647);
		} else if (base.equals("short")) {
			pattern = "[\\-+]?(\\p{Digit}){0,5}";
			minInclusive = new BigDecimal(-32768);
			maxInclusive = new BigDecimal(32767);
		} else if (base.equals("byte")) {
			pattern = "[\\-+]?(\\p{Digit}){0,3}";
			minInclusive = new BigDecimal(-128);
			maxInclusive = new BigDecimal(127);
		} else if (base.equals("unsignedLong")) {
			pattern = "(\\p{Digit}){0,20}";
			minInclusive = new BigDecimal(0);
			maxInclusive = new BigDecimal("18446744073709551615");
		} else if (base.equals("unsignedInt")) {
			pattern = "(\\p{Digit}){0,11}";
			minInclusive = new BigDecimal(0);
			maxInclusive = new BigDecimal("4294967295");
		} else if (base.equals("unsignedShort")) {
			pattern = "(\\p{Digit}){0,5}";
			minInclusive = new BigDecimal(0);
			maxInclusive = new BigDecimal(65535);
		} else if (base.equals("unsignedByte")) {
			pattern = "(\\p{Digit}){0,3}";
			minInclusive = new BigDecimal(0);
			maxInclusive = new BigDecimal(255);
		} else if (base.equals("date")) {
			pattern = "(-)?(\\d\\d\\d\\d-\\d\\d-\\d\\d)";
			initValue = "0000-00-00";
		} else if (base.equals("time")) {
			pattern = "\\d\\d:\\d\\d:\\d\\d((\\.)(\\d)+)?((Z)|((-|+)\\d\\d:\\d\\d))?";
			initValue = "00:00:00";
		} else if (base.equals("dateTime")) {
			pattern = "((-)?(\\d\\d\\d\\d-\\d\\d-\\d\\d))T(\\d\\d:\\d\\d:\\d\\d((\\.)(\\d)+)?((Z)|((-|+)\\d\\d:\\d\\d))?)";
			initValue = "0000-00-00T00:00:00";
		} else if (base.equals("gYear")) {
			pattern = "(-)?(\\d\\d\\d\\d)((Z)|((-|+)\\d\\d:\\d\\d))?";
			initValue = "0000";
		} else if (base.equals("gYearMonth")) {
			pattern = "(-)?(\\d\\d\\d\\d)-(\\d\\d)((Z)|((-|+)\\d\\d:\\d\\d))?";
			initValue = "0000-00";
		} else if (base.equals("gMonth")) {
			pattern = "--\\d\\d((Z)|((-|+)\\d\\d:\\d\\d))?";
			initValue = "--00";
		} else if (base.equals("gDay")) {
			pattern = "---\\d\\d((Z)|((-|+)\\d\\d:\\d\\d))?";
			initValue = "---00";
		} else if (base.equals("gMonthDay")) {
			pattern = "--\\d\\d-\\d\\d((Z)|((-|+)\\d\\d:\\d\\d))?";
			initValue = "--00-00";
		} else if (base.equals("duration")) {
			pattern = "(-)?P(\\d+Y)?(\\d+M)?(\\d+D)?(\\d+T)?(\\d+H)?(\\d+M)?(\\d+S)?";
		}

		return pattern;
	}

	protected void init(org.w3c.dom.Node node) {
		String nodeName = CommonUtils.getUnqualifiedNodeName(node);
		boolean derived = false;

		if (node.getNodeType() == Node.ATTRIBUTE_NODE
				&& nodeName.equals("type")) {
			Attr attr = (Attr) node;
			base = attr.getValue();
			Node namedNode = schema.getType(base);
			if (namedNode != null) {
				init(namedNode);
				return;
			}
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (nodeName.equals("simpleType")) {
				org.w3c.dom.Element element = (org.w3c.dom.Element) node;
				base = element.getAttribute("base");
				Node namedNode = schema.getSimpleType(base);
				if (namedNode != null) {
					init(namedNode);
					derived = true;
				}

				org.w3c.dom.Node unionNode = this.getUnion(node);
				if (unionNode != null) {
					unionTypeDef = new TypeDefinition(unionNode, schema);
					union = true;
				} else {
					org.w3c.dom.Node listNode = this.getList(node);
					if (listNode != null) {
						listTypeDef = new TypeDefinition(listNode, schema);
						list = true;
					}
				}

			} else if (nodeName.equals("complexType")) {
				Node scn = CommonUtils.getChildByName(node, "simpleContent");
				if (scn != null) {
					org.w3c.dom.Element element = (org.w3c.dom.Element) getExtension(scn);
					if (element != null) {
						base = element.getAttribute("base");
						Node namedNode = schema.getType(base);
						if (namedNode != null) {
							init(namedNode);
							derived = true;
						}
					} else {
						element = (org.w3c.dom.Element) getRestriction(scn);
						if (element != null) {
							base = element.getAttribute("base");
							Node namedNode = schema.getType(base);
							if (namedNode != null) {
								init(namedNode);
								derived = true;
							}
							this.processRestrictionNode(element);
						}
					}
				}
			} else if (nodeName.equals("list")) {
				list = true;
				org.w3c.dom.Node itemTypeNode = ((org.w3c.dom.Element) node)
						.getAttributeNode("itemType");
				if (itemTypeNode != null) {
					init(itemTypeNode);
					return;
				} else {
					org.w3c.dom.Node simpleTypeNode = CommonUtils
							.getChildByName(node, "simpleType");
					init(simpleTypeNode);
					return;
				}
			} else if (nodeName.equals("union")) {
				union = true;
				org.w3c.dom.Node memberTypes = ((org.w3c.dom.Element) node)
						.getAttributeNode("memberTypes");
				if (memberTypes != null) {
					StringTokenizer st = new StringTokenizer(memberTypes
							.getNodeValue());
					childTypeDefs = new Vector<TypeDefinition>(8, 4);
					while (st.hasMoreTokens()) {
						String memberType = st.nextToken();
						Node simpleType = schema.getSimpleType(memberType);
						if (simpleType != null) {
							TypeDefinition td = new TypeDefinition(simpleType,
									schema);
							childTypeDefs.add(td);
						}
					}
				} else {
					NodeList nodeList = node.getChildNodes();
					int count = (nodeList != null ? nodeList.getLength() : 0);
					childTypeDefs = new Vector<TypeDefinition>(count);
					for (int i = 0; i < count; i++) {
						Node child = nodeList.item(i);
						if (CommonUtils.getUnqualifiedNodeName(child).equals(
								"simpleType")) {
							TypeDefinition td = new TypeDefinition(child,
									schema);
							childTypeDefs.add(td);
						}
					}
				}
			}
		}

		if (!derived && !list && !union) {
			if (base.equals("Name")) {
				pattern = "(([a-zA-Z_:])[0-9a-zA-Z_:.\\-]*)";
			} else if (base.equals("NCName") || base.equals("ID")
					|| base.equals("IDREF") || base.equals("NMTOKEN")
					|| base.equals("ENTITY")) {
				pattern = "(([a-zA-Z_])[0-9a-zA-Z_.\\-]*)";
			} else if (base.equals("NMTOKENS") || base.equals("ENTITIES")) {
				pattern = "(([a-zA-Z_])[ 0-9a-zA-Z_.\\-]*)";
			} else if (base.equals("NOTATION") || base.equals("QName")) {
				pattern = "((([a-zA-Z_])[0-9a-zA-Z_.\\-]*):)?(([a-zA-Z_])[0-9a-zA-Z_.\\-]*)";
			} else if (base.equals("language")) {
				pattern = "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]{1,8})(-[a-zA-Z]{1,8})*";
			} else if (base.equals("hexBinary")) {
				pattern = "(([0-9]|[a-fA-F])([0-9]|[a-fA-F]))*";
			} else if (pattern == null) {
				pattern = getPattern(base);
			}
			try {
				regexp = Pattern.compile("^" + pattern + "$");
			} catch (java.util.regex.PatternSyntaxException e) {
				e.printStackTrace();
			}
		}
		if (node.getNodeType() == Node.ELEMENT_NODE)
			getFacets((org.w3c.dom.Element) node);
	}

	public Vector<String> getEnumerations() {
		return enums;
	}

	protected org.w3c.dom.Node getExtension(org.w3c.dom.Node root) {
		return CommonUtils.getChildByName(root, "extension");
	}

	protected org.w3c.dom.Node getRestriction(org.w3c.dom.Node root) {
		return CommonUtils.getChildByName(root, "restriction");
	}

	protected org.w3c.dom.Node getList(org.w3c.dom.Node root) {
		return CommonUtils.getChildByName(root, "list");
	}

	protected org.w3c.dom.Node getUnion(org.w3c.dom.Node root) {
		return CommonUtils.getChildByName(root, "union");
	}

	protected void getFacets(org.w3c.dom.Element node) {
		if (list || union)
			return;

		Node rnode = getRestriction(node);

		this.processRestrictionNode(rnode);
	}

	private void processRestrictionNode(Node rnode) {
		if (rnode == null)
			return;

		String rexp = null;

		NodeList rnodeList = rnode.getChildNodes();
		for (int k = 0; k < rnodeList.getLength(); k++) {
			Node rn = rnodeList.item(k);

			NamedNodeMap vattrs = rn.getAttributes();
			if (vattrs == null)
				continue;

			Attr va = (Attr) vattrs.getNamedItem("value");
			if (va == null)
				continue;

			String name = CommonUtils.getUnqualifiedNodeName(rn);
			String value = va.getValue();
			if (name.equals("length")) {
				length = Integer.parseInt(value);
			} else if (name.equals("minLength")) {
				minLength = Integer.parseInt(value);
			} else if (name.equals("maxLength")) {
				maxLength = Integer.parseInt(value);
			} else if (name.equals("minInclusive")) {
				minInclusive = parseBoundsValue(value);
			} else if (name.equals("maxInclusive")) {
				maxInclusive = parseBoundsValue(value);
			} else if (name.equals("minExclusive")) {
				minExclusive = parseBoundsValue(value);
			} else if (name.equals("maxExclusive")) {
				maxExclusive = parseBoundsValue(value);
			} else if (name.equals("pattern")) {
				if (rexp != null)
					rexp = "(" + value + ")|" + rexp;
				else
					rexp = value;
			} else if (name.equals("totalDigits")) {
				totalDigits = Integer.parseInt(value);
			} else if (name.equals("fractionDigits")) {
				fractionDigits = Integer.parseInt(value);
			} else if (name.equals("enumeration")) {
				if (enums == null)
					enums = new Vector<String>(10, 10);
				value = CommonUtils.unescape(value);
				enums.add(value);
			}
		}

		try {
			if (rexp != null)
				regexp = Pattern.compile("^" + rexp + "$");
		} catch (java.util.regex.PatternSyntaxException re) {
		}

	}

	protected java.util.Date parseDateTime(String value) {
		String fmt = null;

		if (base.equals("date")) {
			fmt = "yyyy-MM-ddz";
		} else if (base.equals("time")) {
			fmt = "HH:mm:ss.SSSz";
		} else if (base.equals("dateTime")) {
			fmt = "yyyy-MM-ddTHH:mm:ss.SSSz";
		} else if (base.equals("gYear")) {
			fmt = "yyyyz";
		} else if (base.equals("gYearMonth")) {
			fmt = "yyyyMMz";
		} else if (base.equals("gMonth")) {
			fmt = "--MMz";
		} else if (base.equals("gMonthDay")) {
			fmt = "--MM-ddz";
		} else if (base.equals("gDay")) {
			fmt = "---ddz";
		}

		value = value.trim();
		if (value.endsWith("Z")) {
			value = value.substring(0, value.length() - 1) + "GMT";
		} else if (value.indexOf("+") > 0) {
			int index = value.indexOf("+");
			value = value.substring(0, index) + "GMT" + value.substring(index);
		} else if (value.indexOf("-") > 0) {
			int index = value.indexOf("-");
			value = value.substring(0, index) + "GMT" + value.substring(index);
		}
		try {
			SimpleDateFormat df = new SimpleDateFormat(fmt);
			return df.parse(fmt);
		} catch (Exception e) {

		}
		return null;

	}

	protected BigDecimal parseBoundsValue(String value) {
		if (CommonUtils.isNumericType(base)) {
			return new BigDecimal(value);
		} else if (CommonUtils.isDateTimeType(base)) {
			java.util.Date date = parseDateTime(value);
			if (date != null)
				return new BigDecimal(date.getTime());
		}
		return null;

	}

	protected boolean verifyTotalDigits(String value) {
		BigDecimal bd = new BigDecimal(value);
		if (bd.unscaledValue().toString().length() > totalDigits)
			return false;
		else
			return true;
	}

	protected boolean verifyFractionDigits(String value) {
		BigDecimal bd = new BigDecimal(value);
		if (bd.scale() > fractionDigits)
			return false;
		else
			return true;
	}

	protected boolean verifyBounds(String value) {
		if (minInclusive != null && minExclusive != null)
			return false;
		else if (maxExclusive != null && maxInclusive != null)
			return false;

		if (minInclusive != null) {
			if (maxInclusive != null)
				return (maxInclusive.compareTo(minInclusive) > 0);
			else if (maxExclusive != null)
				return (maxExclusive.compareTo(minInclusive) > 0);

		} else if (minExclusive != null) {

			if (maxInclusive != null)
				return (maxInclusive.compareTo(minExclusive) > 0);
			else if (maxExclusive != null)
				return (maxExclusive.compareTo(minExclusive) > 0);

		}

		BigDecimal bd = parseBoundsValue(value);
		if (bd == null)
			return false;

		if (minInclusive != null) {
			if (bd.compareTo(minInclusive) < 0)
				return false;
		}

		if (maxInclusive != null) {
			if (bd.compareTo(maxInclusive) > 0)
				return false;
		}

		if (maxExclusive != null) {
			if (bd.compareTo(maxExclusive) >= 0)
				return false;
		}

		if (minExclusive != null) {
			if (bd.compareTo(minExclusive) <= 0)
				return false;
		}
		return true;
	}

	protected boolean isListValid(String value) {
		StringTokenizer st = new StringTokenizer(value);
		boolean valid = true;
		int nitems = 0;
		while (st.hasMoreTokens()) {
			String listValue = st.nextToken();
			valid = listTypeDef.isValid(listValue);
			if (!valid)
				break;
			nitems++;
		}
		if (valid) {
			if ((minLength > 0 && nitems < minLength)
					|| (maxLength > 0 && nitems > maxLength)
					|| (length > 0 && nitems != length))
				valid = false;
		}
		return valid;
	}

	protected boolean isUnionValid(String value) {
		if (this.unionTypeDef != null)
			return this.unionTypeDef.isValid(value);

		int count = childTypeDefs.size();
		for (int i = 0; i < count; i++) {
			TypeDefinition td = (TypeDefinition) childTypeDefs.elementAt(i);
			if (td.isValid(value))
				return true;
		}
		return false;

	}

	public boolean isValid(String value) {

		boolean retval = true;

		if (union) {
			retval = isUnionValid(value);
		} else if (list) {
			if (listTypeDef != null)
				retval = isListValid(value);
		}

		if (!retval)
			return false;

		if (enums != null && enums.size() > 0 && !enums.contains(value))
			return false;

		if (base != null) {
			if (CommonUtils.isStringType(base)) {
				if (length > 0 && value.length() != length)
					return false;
				else if (minLength > 0 && value.length() < minLength)
					return false;
				else if (maxLength > 0 && value.length() > maxLength)
					return false;
			} else if (CommonUtils.isNumericType(base)) {
				if (!verifyBounds(value))
					return false;
				if (!verifyTotalDigits(value))
					return false;
				if (!verifyFractionDigits(value))
					return false;
			} else if (CommonUtils.isDateTimeType(base)) {
				if (!verifyBounds(value))
					return false;
			}
		}

		if (regexp != null) {
			Matcher matcher = regexp.matcher(value);
			retval = matcher.matches();
		}
		return retval;
	}
}