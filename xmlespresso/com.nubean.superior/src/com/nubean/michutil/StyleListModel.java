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

package com.nubean.michutil;

import javax.swing.DefaultListModel;
import java.util.*;
import javax.swing.text.*;

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

public class StyleListModel extends DefaultListModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9002927149854803050L;

	public StyleListModel(StyleContext ctxt) {
		int index = 0;
		Enumeration styleNames = ctxt.getStyleNames();
		ArrayList alist = new ArrayList(32);
		while (styleNames.hasMoreElements()) {
			String sname = (String) styleNames.nextElement();
			StyleItem item = new StyleItem(ctxt.getStyle(sname),
					getStyleName(sname));
			alist.add(item);
		}
		
		Collections.sort(alist);
		Iterator it = alist.iterator();
		while(it.hasNext()) {
			add(index++, it.next());
		}
	}

	private String getStyleName(String element) {
		StringBuffer sb = new StringBuffer(element.replace('-', ' '));
		char c = sb.charAt(0);
		sb.setCharAt(0, Character.toUpperCase(c));
		return sb.toString();
	}
}