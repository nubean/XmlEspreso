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
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;

import com.nubean.michxml.elem.*;

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

public class XMLDocument extends DefaultStyledDocument implements
		TreeModelListener {
	private static final long serialVersionUID = 3486395659195075315L;

	private XMLAbstractEditor editor;

	private XMLAbstractElement documentElement;

	private boolean inited, locked;

	public XMLDocument(XMLAbstractEditor editor, StyleContext styleContext) {

		super(styleContext);
		this.editor = editor;
	}

	public XMLDocument(StyleContext styleContext) {
		super(styleContext);
	}

	public void setRootElement(XMLAbstractElement rootElement) {
		this.documentElement = rootElement;
		if (documentElement != null)
			inited = true;
		else
			inited = false;
	}

	public XMLAbstractElement getRootElement() {
		return documentElement;
	}

	public void insertContent(int where, String str, AttributeSet attrs)
			throws BadLocationException {
		super.insertString(where, str, attrs);
		if (inited) {
			if (editor != null) {
				editor.setCaretPosition(where + str.length());
			}
		}
	}

	public void insertString(int where, String str, AttributeSet attrs)
			throws BadLocationException {

		XMLAbstractElement element = (documentElement != null ? (XMLAbstractElement) documentElement
				.positionToElement(where)
				: null);

		if (element != null) {
			if (editor == null || editor.isTextMode()) {
				insertContent(where, str, element.getAttributes());
			} else
				element.insertString(where, str);
		} else {
			if (editor == null || editor.isTextMode()
					|| str.trim().length() == 0) {
				insertContent(where, str, null);
			}
		}
	}

	public int find(int start, String text) {
		if (text == null || text.length() == 0)
			return -1;

		try {
			int len = this.getLength();
			int slen = text.length();
			for (int i = start; i < len - 1; i++) {
				String tmp = this.getText(i, slen);
				if (tmp.equals(text)) {
					return i;
				}
			}
		} catch (BadLocationException e) {
		}
		return -1;
	}

	public boolean isEditable(int where, int len) {

		if (editor == null || editor.isTextMode())
			return true;

		XMLAbstractElement ele = (documentElement != null ? (XMLAbstractElement) documentElement
				.positionToElement(where)
				: null);

		return ele != null && ele.isEditable()
				&& ele.getEndOffset() >= where + len;
	}

	public void replace(int where, int len, String text) {

		if (text == null)
			text = "";

		XMLAbstractElement ele = (documentElement != null ? (XMLAbstractElement) documentElement
				.positionToElement(where)
				: null);
		boolean editable = (ele != null && ele.isEditable() && ele
				.getEndOffset() >= where + len)
				|| editor == null || editor.isTextMode();
		if (editable) {
			try {
				if (editor == null || editor.isTextMode()) {
					if (editor != null) {
						editor.beginUndoEditSession();
						removeContent(where, len);
						if (text != null && text.length() > 0)
							insertContent(where, text, (ele != null ? ele
									.getAttributes() : null));
						editor.endUndoEditSession();
					}
				} else
					ele.replace(where, len, text);
			} catch (BadLocationException e) {
			}
		}
	}

	public void removeContent(int where, int len) throws BadLocationException {
		super.remove(where, len);
	}

	public void remove(int where, int len) throws BadLocationException {

		if (editor == null || editor.isTextMode()) {
			removeContent(where, len);
		} else {
			XMLAbstractElement element = (XMLAbstractElement) documentElement
					.positionToElement(where);
			if (element != null) {
				element.remove(where, len);
			} else {
				removeContent(where, len);
			}
		}
	}

	public XMLAbstractElement findElementWithNode(XMLAbstractElement subTree,
			XMLNode node) {
		if (subTree == null || node == null)
			return null;

		Stack<Element> elementStack = new Stack<Element>();
		HashMap<XMLAbstractElement, Stack<Integer>> stackMap = new HashMap<XMLAbstractElement, Stack<Integer>>(
				17, 0.85f);

		elementStack.push(subTree);

		while (!elementStack.empty()) {
			XMLAbstractElement element = (XMLAbstractElement) elementStack
					.peek();

			if (element.getXMLNode() == node)
				return element;

			int count = element.getElementCount();

			if (count == 0) {
				elementStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(element);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(element, indexStack);
					elementStack.push(element.getElement(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						elementStack.push(element.getElement(top));
						indexStack.push(new Integer(top + 1));
						continue;
					} else {
						Object key = elementStack.pop();
						stackMap.remove(key);
						continue;
					}
				}
			}
		}

		return null;
	}

	public XMLAbstractElement findElementWithNode(XMLNode node) {
		if (documentElement == null || node == null)
			return null;

		if (documentElement.getXMLNode() == node) {
			return documentElement;
		}

		int tcc = documentElement.getElementCount();

		for (int i = 0; i < tcc; i++) {
			XMLAbstractElement subtree = (XMLAbstractElement) documentElement
					.getElement(i);
			XMLAbstractElement match = findElementWithNode(subtree, node);
			if (match != null) {
				return match;
			}
		}

		return null;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		if (documentElement != null)
			documentElement.setLocked(locked);
	}

	public boolean getLocked() {
		return locked;
	}

	// TreeModelListener methods
	public void treeNodesChanged(TreeModelEvent e) {
		if (documentElement.getLocked())
			return;

		int[] childIndicies = e.getChildIndices();
		Object[] children = e.getChildren();
		int count = (childIndicies != null ? childIndicies.length : 0);
		for (int i = 0; i < count; i++) {
			XMLNode node = (XMLNode) children[i];
			XMLAbstractElement ele = findElementWithNode(node);
			if (ele != null) {
				ele.treeNodesChanged(e);
			}
		}
		if (count == 0) {
			XMLNode parent = (XMLNode) e.getTreePath().getLastPathComponent();
			XMLAbstractElement parentElement = findElementWithNode(parent);
			parentElement.treeNodesChanged(e);
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {
		if (documentElement.getLocked())
			return;
		TreePath path = e.getTreePath();
		XMLNode parent = (XMLNode) path.getLastPathComponent();
		XMLAbstractElement parentElement = findElementWithNode(parent);

		int[] childIndicies = e.getChildIndices();
		int count = (childIndicies != null ? childIndicies.length : 0);
		for (int i = 0; i < count; i++) {
			if (parentElement != null) {
				parentElement.treeNodesInserted(e);
			}
		}
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		if (documentElement.getLocked())
			return;
		TreePath path = e.getTreePath();
		XMLNode parent = (XMLNode) path.getLastPathComponent();
		XMLAbstractElement parentElement = findElementWithNode(parent);

		if (parentElement != null) {
			parentElement.treeNodesRemoved(e);
		}
	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

	public XMLAbstractEditor getEditor() {
		return editor;
	}

	public void setEditor(XMLAbstractEditor editor) {
		this.editor = editor;
	}

}