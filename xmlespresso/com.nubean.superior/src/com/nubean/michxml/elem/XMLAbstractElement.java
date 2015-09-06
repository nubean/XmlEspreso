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

package com.nubean.michxml.elem;

import javax.swing.text.*;
import javax.swing.event.*;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLAbstractEditor;

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

public interface XMLAbstractElement extends Element, TreeModelListener {
	public static final int ATTRIBUTE = 0;

	public static final int CONTENT = 1;

	public static final int DOCTYPE = 2;

	public static final int ELEMENT = 3;

	public static final int END_TAG = 4;

	public static final int LABEL = 5;

	public static final int PROC_INSTR = 6;

	public static final int DOCUMENT_ELEMENT = 7;

	public static final int DOC_ROOT = 8;

	public static final int START_TAG = 9;

	public static final int TEXT = 10;

	public static final int VALUE = 11;

	public static final int WHITE_SPACE = 12;

	public static final int PROC_INSTR_DATA = 13;

	public static final int DOC_TYPE_DATA = 14;

	public static final int COMMENT = 15;

	public static final int COMMENT_DATA = 16;

	public static final int CDATA = 17;
	
	public static final int XML_DECL = 18;
	
	public static final int EMPTY_TAG = 19;
	
	public static final int ATTLIST_DECL = 20;
	
	public static final int ELEMENT_DECL = 21;
	
	public static final int NOTATION_DECL = 22;
	
	public static final int ENTITY_DECL = 23;

	public static int INDENT = 2;

	public static int FORWARD_BIAS = 1, BACKWARD_BIAS = -1;

	public void insertString(int offset, String str)
			throws BadLocationException;

	public void insertEvent(XMLAbstractElement source, int offset, String str)
			throws BadLocationException;

	public void remove(int offset, int len) throws BadLocationException;

	public void replace(int offset, int len, String str)
			throws BadLocationException;

	public void removeEvent(XMLAbstractElement source, int offset, int len)
			throws BadLocationException;

	public XMLAbstractElement positionToElement(int pos);

	public XMLAbstractElement spanToElement(int pos, int len);

	public XMLNode getXMLNode();

	public XMLAbstractEditor getEditor();

	public int getIndent();

	public int getType();

	public boolean getLocked();

	public void setLocked(boolean lock);

	public XMLAbstractElement getNextSibling();

	public XMLAbstractElement getPrevSibling();

	public boolean isEditable();

	public int getBias(int offset);

	public int getTrimStartOffset();

	public int getTrimEndOffset();

	public void dump();
	
	public void applyAttributes();
	
	public boolean isWhitespace();
}