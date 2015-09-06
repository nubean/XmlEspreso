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

package com.nubean.java;

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

public class JavaDefaultStyleContext extends StyleContext {
	private static final long serialVersionUID = 8966604897810460598L;
	public static int TAB_STOP_LENGTH = 16;
	

	public JavaDefaultStyleContext() {
		super();

		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.GRAY);
		StyleConstants.setBold(attr, true);
		StyleConstants.setFontFamily(attr, "Verdana");
		StyleConstants.setFontSize(attr, EditorConfiguration.DEFAULT_FONT_SIZE);

		Style textStyle = this.addStyle("Text", null);
		textStyle.addAttributes(attr);
		
		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.black);
		Style javaStyle = this.addStyle("Java", textStyle);
		javaStyle.addAttributes(attr);
		
		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr,Color.CYAN.darker());
		StyleConstants.setBold(attr, true);
		Style packageStyle = this.addStyle("Package", javaStyle);
		packageStyle.addAttributes(attr);
		

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.BLUE.darker());
		StyleConstants.setBold(attr, true);
		Style importStyle = this.addStyle("Import", javaStyle);
		importStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.BLUE);
		StyleConstants.setBold(attr, true);
		Style typeStyle = this.addStyle("TypeDecl", javaStyle);
		typeStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setBold(attr, true);
		StyleConstants.setForeground(attr, Color.GRAY);
		Style fieldStyle = this.addStyle("Field", javaStyle);
		fieldStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.GRAY);
		Style methodStyle = this.addStyle("Method",javaStyle);
		methodStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.LIGHT_GRAY);
		StyleConstants.setBold(attr, true);
		Style localStyle = this.addStyle("LocalVariable", javaStyle);
		localStyle.addAttributes(attr);

		attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.white);
		StyleConstants.setForeground(attr, Color.GREEN);
		StyleConstants.setBold(attr, false);
		Style annotationStyle = this.addStyle("Annotation", javaStyle);
		annotationStyle.addAttributes(attr);

		
	}
}