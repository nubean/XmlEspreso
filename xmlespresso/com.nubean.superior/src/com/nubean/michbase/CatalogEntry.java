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

import java.util.*;
import org.w3c.dom.*;

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

public class CatalogEntry implements Comparable<CatalogEntry> {
	protected String mimeType;

	protected Properties properties;

	public CatalogEntry() {
		properties = new Properties();
	}

	public String getMimeType() {
		return mimeType;
	}

	public String toString() {
		return properties.getProperty("name");
	}

	public String getProperty(String property) {
		return properties.getProperty(property);
	}

	public void readElement(Element element) {
		mimeType = element.getAttribute("mimeType");

		NodeList nodeList = element.getChildNodes();
		int count = (nodeList != null ? nodeList.getLength() : 0);
		String css = " body {font-name=Tahoma;font-size=10;}";
		for (int i = 0; i < count; i++) {
			Node child = nodeList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = child.getNodeName();
				String value = null;
				if (nodeName.equals("documentation"))
					value = com.nubean.michbase.CommonUtils.getDocumentation(child,
							css);
				else
					value = com.nubean.michbase.CommonUtils.getContent(child);
				properties.put(nodeName, value.trim());
			}
		}
	}

	public String dump() {
		StringBuffer sb = new StringBuffer();
		Iterator<Object> it = properties.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			if (key.equals("documentation"))
				continue;
			sb.append("\t{");
			sb.append(key).append('=').append(properties.get(key)).append(',');
			sb.append("\t}\n");
		}
		return sb.toString();
	}

	/**
	 * Compares this object with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * 
	 * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt>
	 * designates the mathematical <i>signum</i> function, which is defined to
	 * return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to
	 * whether the value of <i>expression</i> is negative, zero or positive.
	 * 
	 * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt>
	 * for all <tt>x</tt> and <tt>y</tt>. (This implies that
	 * <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 * <p>
	 * 
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 * <p>
	 * 
	 * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.
	 * <p>
	 * 
	 * It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact. The recommended
	 * language is "Note: this class has a natural ordering that is inconsistent
	 * with equals."
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
	public int compareTo(CatalogEntry o) {
		return toString().compareTo(o.toString());
	}

}