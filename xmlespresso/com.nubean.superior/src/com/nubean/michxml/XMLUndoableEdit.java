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

import javax.swing.undo.*;
import org.w3c.dom.*;

import com.nubean.michutil.LocalizedResources;

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

public class XMLUndoableEdit extends AbstractUndoableEdit {
	private static final long serialVersionUID = 8216676323527618336L;

	public static final int NODE_INSERT = 0;

	public static final int NODE_REMOVE = 1;

	public static final int NODE_CHANGE = 2;

	private XMLNode node, parent;

	private int action, pos;

	private String oldValue;

	private boolean hasUndoBeenDone;

	private XMLAbstractEditor editor;

	public XMLUndoableEdit(XMLAbstractEditor editor, XMLNode parent,
			XMLNode node, int pos, String oldValue, int action) {
		this.node = node;
		this.editor = editor;
		this.parent = parent;
		this.action = action;
		this.pos = pos;
		this.oldValue = oldValue;
	}

	public boolean isSignificant() {
		return true;
	}

	public boolean addEdit(UndoableEdit e) {
		XMLUndoableEdit ue = (XMLUndoableEdit) e;
		if (ue.parent == this.parent && ue.node == this.node
				&& ue.action == XMLUndoableEdit.NODE_CHANGE
				&& ue.action == this.action) {
			ue.die();
			return true;
		}
		return false;
	}

	public boolean replaceEdit(UndoableEdit e) {
		return false;
	}

	public boolean canUndo() {
		return !hasUndoBeenDone;
	}

	public boolean canRedo() {
		return hasUndoBeenDone;
	}

	public void undo() throws CannotUndoException {
		editor.setUndoInProgress(true);
		org.w3c.dom.Node domNode = node.getDomNode();

		switch (action) {
		case NODE_INSERT:
			editor.removeNode(parent, node);
			break;
		case NODE_REMOVE:
			editor.insertNode(parent, node, pos);
			break;
		case NODE_CHANGE:
			String curValue = null;

			switch (domNode.getNodeType()) {
			case Node.PROCESSING_INSTRUCTION_NODE:
				org.w3c.dom.ProcessingInstruction pi = (org.w3c.dom.ProcessingInstruction) domNode;
				curValue = pi.getData();
				pi.setData(oldValue);
				break;
			case Node.COMMENT_NODE:
				org.w3c.dom.Comment comment = (org.w3c.dom.Comment) domNode;
				curValue = comment.getData();
				comment.setData(oldValue);
				break;
			default:
				curValue = domNode.getNodeValue();
				domNode.setNodeValue(oldValue);
				break;
			}

			editor.nodeChanged(node, curValue);
			oldValue = curValue;
			break;
		}

		hasUndoBeenDone = true;
		editor.setUndoInProgress(false);
	}

	public void redo() throws CannotRedoException {
		editor.setUndoInProgress(true);
		org.w3c.dom.Node domNode = node.getDomNode();

		switch (action) {
		case NODE_INSERT:
			editor.insertNode(parent, node, pos);
			break;
		case NODE_REMOVE:
			editor.removeNode(parent, node);
			break;
		case NODE_CHANGE:
			String curValue = null;

			switch (domNode.getNodeType()) {
			case Node.PROCESSING_INSTRUCTION_NODE:
				org.w3c.dom.ProcessingInstruction pi = (org.w3c.dom.ProcessingInstruction) domNode;
				curValue = pi.getData();
				pi.setData(oldValue);
				break;
			case Node.COMMENT_NODE:
				org.w3c.dom.Comment comment = (org.w3c.dom.Comment) domNode;
				curValue = comment.getData();
				comment.setData(oldValue);
				break;
			default:
				curValue = domNode.getNodeValue();
				domNode.setNodeValue(oldValue);
				break;
			}

			editor.nodeChanged(node, curValue);
			oldValue = curValue;
			break;
		}

		hasUndoBeenDone = false;
		editor.setUndoInProgress(false);
	}

	public String getPresentationName() {
		return node.getDomNode().getNodeName();
	}

	private String getActionName() {
		String name = "";
		switch (action) {
		case NODE_REMOVE:
			name = " remove ";
			break;
		case NODE_CHANGE:
			name = " modify ";
			break;
		case NODE_INSERT:
			name = " insert ";
			break;
		}
		return name;
	}

	public String getUndoPresentationName() {
		StringBuffer sb = new StringBuffer();
		sb.append(LocalizedResources.applicationResources.getString("undo"));
		sb.append(getActionName());
		sb.append(node.getDomNode().getNodeName());
		sb.append(" under ").append(parent.getDomNode().getNodeName());
		return sb.toString();
	}

	public String getRedoPresentationName() {
		StringBuffer sb = new StringBuffer();
		sb.append(LocalizedResources.applicationResources.getString("redo"));
		sb.append(getActionName()).append(' ');
		sb.append(node.getDomNode().getNodeName());
		return sb.toString();
	}

}
