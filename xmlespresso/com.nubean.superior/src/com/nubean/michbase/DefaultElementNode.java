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

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeNode;

import com.nubean.michutil.Iconable;

public abstract class DefaultElementNode implements Iconable, TreeNode, Element {

	protected int lastIndex = -1;

	protected int startOffset;

	protected int endOffset;

	public abstract Object[] getChildren();

	public abstract Object getParentObject();

	public boolean isLeaf() {
		return (getChildCount() == 0);
	}

	public AttributeSet getAttributes() {
		AttributeSet attrs = null;
		Document doc = getDocument();
		if (doc != null && doc instanceof StyledDocument) {
			StyledDocument sd = (StyledDocument) doc;
			attrs = sd.getStyle(getName());
		}
		return attrs;
	}

	public Element getParentElement() {
		return (Element) getParentObject();
	}

	public Element getElement(int index) {
		return (Element) getChildren()[index];
	}

	public int getElementIndex(int offset) {
		int nchildren = getChildCount();
		Object[] children = getChildren();
		int index;
		int lower = 0;
		int upper = nchildren - 1;
		int mid = 0;
		int p0 = getStartOffset();
		int p1;

		if (nchildren == 0) {
			return 0;
		}
		if (offset >= getEndOffset()) {
			return nchildren - 1;
		}

		// see if the last index can be used.
		if ((lastIndex >= lower) && (lastIndex <= upper)) {
			Element lastHit = (Element) children[lastIndex];
			p0 = lastHit.getStartOffset();
			p1 = lastHit.getEndOffset();
			if ((offset >= p0) && (offset < p1)) {
				return lastIndex;
			}

			// last index wasn't a hit, but it does give useful info about
			// where a hit (if any) would be.
			if (offset < p0) {
				upper = lastIndex;
			} else {
				lower = lastIndex;
			}
		}

		while (lower <= upper) {
			mid = lower + ((upper - lower) / 2);
			Element elem = (Element) children[mid];
			p0 = elem.getStartOffset();
			p1 = elem.getEndOffset();
			if ((offset >= p0) && (offset < p1)) {
				// found the location
				index = mid;
				lastIndex = index;
				return index;
			} else if (offset < p0) {
				upper = mid - 1;
			} else {
				lower = mid + 1;
			}
		}

		// didn't find it, but we indicate the index of where it would belong
		if (offset < p0) {
			index = mid;
		} else {
			index = mid + 1;
		}
		lastIndex = index;
		return index;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getElementCount() {
		return getChildCount();
	}

	public abstract void applyAttributes();

	public TreeNode getParent() {
		return (TreeNode) getParentObject();
	}

	public int getChildCount() {
		Object[] children = getChildren();
		return (children != null ? children.length : 0);
	}

	public Enumeration children() {
		int count = getChildCount();
		Vector ret = new Vector(count);
		Object[] children = getChildren();
		for (int i = 0; i < count; i++) {
			ret.add(children[i]);
		}

		return ret.elements();
	}

	public TreeNode getChildAt(int childIndex) {
		TreeNode ret = null;
		try {
			Object[] children = getChildren();
			ret = (TreeNode) children[childIndex];
		} catch (Exception e) {

		}
		return ret;
	}

	public int getIndex(TreeNode node) {
		int ret = -1;
		int count = getChildCount();
		Object[] children = getChildren();
		for (int i = 0; i < count; i++) {
			if (children[i].equals(node)) {
				ret = i;
				break;
			}
		}

		return ret;
	}

	public Object positionToElement(int pos) {

		Object ret = null;
		Object[] children = getChildren();
		if (children != null) {
			int index = getElementIndex(pos);
			DefaultElementNode child = (DefaultElementNode)children[index];
			int p0 = child.getStartOffset();
			int p1 = child.getEndOffset();
			if ((pos >= p0) && (pos < p1)) {
				if (child.isLeaf()) {
					ret = child;
				} else {
					ret = child.positionToElement(pos);
				}
			}
		}
		
		if (ret == null) {
			int start = getStartOffset();
			int end = getEndOffset();
			if ((pos >= start) && (pos < end)) {
				ret = this;
			}
		}

		return ret;
	}
	
	public abstract int getId();

}
