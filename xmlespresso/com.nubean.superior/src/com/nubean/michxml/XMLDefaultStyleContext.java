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

import javax.swing.text.*;

import com.nubean.michbase.EditorConfiguration;

import java.awt.*;

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

public class XMLDefaultStyleContext extends StyleContext {
	private static final long serialVersionUID = -5479600365996885742L;
	public static int TAB_STOP_LENGTH = 16;
	

	public XMLDefaultStyleContext() {
		super();

		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, new Color(63, 127, 127));
		StyleConstants.setFontFamily(attr, "Verdana");
		StyleConstants.setFontSize(attr, EditorConfiguration.DEFAULT_FONT_SIZE);

		Style root = this.addStyle("document", null);
		root.addAttributes(attr);

		this.addStyle("white-space", root);

		Style elementStyle = this.addStyle("element", root);
		Style startTagStyle = this.addStyle("start-tag", elementStyle);
		this.addStyle("empty-tag", root);
		this.addStyle("end-tag", elementStyle);

		Style contentStyle = this.addStyle("content", elementStyle);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, new Color(127, 0, 127));
		Style attrStyle = this.addStyle("attribute", startTagStyle);
		attrStyle.addAttributes(attr);
		

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.blue);
		Style attrValueStyle = this.addStyle("value", attrStyle);
		attrValueStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.black);
		Style textStyle = this.addStyle("text", contentStyle);
		textStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.black);
		Style cdataStyle = this.addStyle("cdata", contentStyle);
		cdataStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.blue);
		Style piStyle = this.addStyle("proc-instr", root);
		piStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.blue.darker());
		Style xmlDeclStyle = this.addStyle("xml-decl", root);
		xmlDeclStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.gray);
		Style attlistDeclStyle = this.addStyle("attlist-decl", root);
		attlistDeclStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.darkGray);
		Style elementDeclStyle = this.addStyle("element-decl", root);
		elementDeclStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.magenta);
		Style notationDeclStyle = this.addStyle("notation-decl", root);
		notationDeclStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.orange);
		Style entityDeclStyle = this.addStyle("entity-decl", root);
		entityDeclStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, new Color(63, 127, 127));
		Style piDataStyle = this.addStyle("proc-instr-data", piStyle);
		piDataStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, new Color(115, 121, 247));
		Style commentStyle = this.addStyle("comment", root);
		commentStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, new Color(115, 121, 247));
		Style commentDataStyle = this.addStyle("comment-data", commentStyle);
		commentDataStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, new Color(63, 127, 127));
		Style docTypeStyle = this.addStyle("doctype", root);
		docTypeStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.blue.darker());
		Style dtDataStyle = this.addStyle("doctype-data", docTypeStyle);
		dtDataStyle.addAttributes(attr);
	}
}