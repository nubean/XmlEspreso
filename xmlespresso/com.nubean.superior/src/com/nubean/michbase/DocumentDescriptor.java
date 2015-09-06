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

import javax.swing.tree.*;
import java.util.*;

import com.nubean.michutil.*;
import javax.swing.ImageIcon;

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

public abstract class DocumentDescriptor implements
		Comparator<DocumentDescriptor>, TreeNode, Iconable {
	protected String name;

	protected String path;

	protected String description;

	protected String mimeType;

	protected TreeNode parent;

	protected String ext;

	protected DocumentDescriptor() {
	}

	protected DocumentDescriptor(String name, String path, String mimeType) {
		this.name = name;
		this.path = path;
		this.mimeType = mimeType;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIndex(TreeNode node) {
		return -1;
	}

	public TreeNode getParent() {
		return parent;
	}

	public TreeNode getChildAt(int index) {
		return null;
	}

	public int compare(DocumentDescriptor o1, DocumentDescriptor o2) {
		return o1.toString().compareTo(o2.toString());
	}

	public boolean equals(Object o) {
		boolean retval = false;
		try {
			DocumentDescriptor info = (DocumentDescriptor) o;
			retval = info.path.equals(path) && info.name.equals(name);
		} catch (Exception e) {

		}
		return retval;
	}

	public int getChildCount() {
		return 0;
	}

	public String toString() {
		return name;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public boolean isDirty() {
		return (editor != null ? editor.isDirty() : false);
	}

	public void setDirty(boolean dirty) {
		if (editor != null)
			editor.setDirty(dirty);
	}

	public Editor getEditor() {
		return editor;
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}

	public void setDoNotSave(boolean ds) {
		this.doNotSave = ds;
	}

	public boolean getDoNotSave() {
		return this.doNotSave;
	}

	public abstract String toXMLString();

	public abstract void printXml(java.io.PrintWriter pw, String indent);

	public abstract void readElement(org.w3c.dom.Element element);

	public Enumeration children() {
		return null;
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public boolean isLeaf() {
		return true;
	}

	public abstract ImageIcon getIcon();

	protected transient Editor editor;

	protected transient boolean doNotSave;

}