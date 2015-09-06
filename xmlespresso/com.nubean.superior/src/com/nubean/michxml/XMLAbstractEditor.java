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

/**
 * <p>Title: Michigan XML Editor</p>
 * <p>Description: This edits an XML document based on an XML schema.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Nubean LLC</p>
 * @author Ajay Vohra
 * @version 1.0
 */
import java.awt.Color;
import java.util.Vector;

import javax.swing.tree.*;
import javax.swing.event.*;

import com.nubean.michbase.Editor;

public interface XMLAbstractEditor extends Editor {
	public final static Color HIGHLIGHT_COLOR = new Color(14410725);
	
	public final static int TAG_INDENT = 0;

	public final static int TAG_NOT_INDENT = 1;

	public XMLTreeModel getXMLTreeModel();
	
	public XMLModel getXMLModel();

	public boolean isEmpty(XMLNode node);

	public boolean canInsertTextInNode(XMLNode parent);

	public void insertSubElement(XMLNode parent, XMLNode before);

	public void insertFragment(String fragment, XMLNode parent, XMLNode before);

	public void insertComment(XMLNode parent, XMLNode before);

	public void insertProcInstr(XMLNode node, XMLNode insertBefore);

	public void insertTextNode(String text, XMLNode paremt, XMLNode before);

	public void insertTextInSimpleType(String text);

	public void insertCDATAInSimpleType(String text);

	public void insertNode(XMLNode parent, XMLNode newNode, int pos);

	public XMLNode insertElementOfType(SchemaNode typeNode);

	public void showAttributes(XMLNode node);

	public void removeNode(XMLNode parent, XMLNode node);

	public void nodeChanged(XMLNode node, String oldValue);

	public String getEncoding();

	public int getTagStyle();

	public void setCaretPosition(int offset);

	public String getDocType();

	public String getXmlProcInstr();

	public void setTreeSelectionPath(TreePath path);

	public void setAttribute(String name, String value);

	public void setUndoInProgress(boolean flag);

	public boolean getUndoInProgress();

	public void setLocked(boolean lock);

	public boolean getLocked();

	public void fireUndoableEditEvent(UndoableEditEvent e);

	public XMLSchema getSchema();

	public String getStyleSheet();

	public XMLNode getComplexTypeNode();

	public XMLNode getSimpleTypeNode();

	public TreePath getComplexTypePath();

	public TreePath getSimpleTypePath();

	public void setComplexTypePath(TreePath path);

	public void setSimpleTypePath(TreePath path);

	public void setComplexTypeNode(XMLNode node);

	public void setSimpleTypeNode(XMLNode node);

	public org.w3c.dom.Document getXml();

	public boolean isDocumentParsed();

	public void validateUsingDtd();

	public void validateUsingSchema();
	
	public void format();
	
	public boolean isTextMode();

	public void toggleTextMode();
	
	public boolean isWellFormed();
	
	public Vector<Object> getElementContentAssist(XMLNode node, XMLNode before, int documentOffset);
	
	public Vector<Object> getAttributeContentAssist(XMLNode parent, int documentOffset);
	
	public Vector<Object> getParserContentAssist(int documentOffset);
}