/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
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

package com.nubean.css.parser;

import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import com.nubean.michbase.DefaultElementNode;
import com.nubean.michutil.IconLoader;

public class SimpleNode extends DefaultElementNode implements Node {

	protected Node parent;
	protected Node[] children;
	protected int id;
	protected Object value;
	protected CSSParser parser;
	protected Token first_token, last_token;

	public SimpleNode(int i) {
		id = i;
	}

	public SimpleNode(CSSParser p, int i) {
		this(i);
		parser = p;
	}

	public Token getLastToken() {
		return last_token;
	}

	public Token getFirstToken() {
		return first_token;
	}

	public void setFirstToken(Token t) {
		if (t != null && t.image != null) {
			first_token = t;
			Document doc = getDocument();
			Element root = doc.getDefaultRootElement();
			Element line = root.getElement(t.beginLine - 1);
			startOffset = line.getStartOffset() + t.beginColumn - 1;

			line = root.getElement(t.endLine - 1);
			endOffset = line.getStartOffset() + t.endColumn;
		}
	}

	public void setLastToken(Token t) {
		if (t != null && t.image != null) {
			last_token = t;
			Document doc = getDocument();
			Element root = doc.getDefaultRootElement();
			Element line = root.getElement(t.endLine - 1);
			endOffset = line.getStartOffset() + t.endColumn;
		}
	}

	public Object getParentObject() {
		return parent;
	}

	public Document getDocument() {
		return parser.getDocument();
	}

	public Object[] getChildren() {
		return children;
	}

	public int getId() {
		return id;
	}

	public boolean getAllowsChildren() {
		boolean ret = false;
		switch (id) {
		case CSSParserTreeConstants.JJTCSS:
			ret = true;
			break;
		}
		return ret;
	}

	public String getName() {
		return CSSParserTreeConstants.jjtNodeName[id];
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) {
		parent = n;
	}

	public Node jjtGetParent() {
		return parent;
	}

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	public void jjtSetValue(Object value) {
		this.value = value;
	}

	public Object jjtGetValue() {
		return value;
	}

	/** Accept the visitor. **/
	public Object jjtAccept(CSSParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	/** Accept the visitor. **/
	public Object childrenAccept(CSSParserVisitor visitor, Object data) {
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				children[i].jjtAccept(visitor, data);
			}
		}
		return data;
	}

	/*
	 * You can override these two methods in subclasses of SimpleNode to
	 * customize the way the node appears when the tree is dumped. If your
	 * output uses more than one line you should override toString(String),
	 * otherwise overriding toString() is probably all you need to do.
	 */

	public String toString() {
		String ret = "";
		StringBuffer sb = new StringBuffer();
		if (id != CSSParserTreeConstants.JJTCSS) {
			Token cur = first_token;
			do {
				if (cur != null && cur.image != null)
					sb.append(cur.image);
			} while (cur != null && cur != last_token
					&& ((cur = cur.next) != null));

			sb.append(" ");
		} else {
			sb.append(getName());
		}

		ret = sb.toString();
		return ret;
	}

	public String toString(String prefix) {
		return prefix + toString();
	}

	/*
	 * Override this method if you want to customize how the node dumps out its
	 * children.
	 */

	public void dump(String prefix) {
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode) children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}

	public ImageIcon getIcon() {
		ImageIcon icon = null;
		switch (id) {
		case CSSParserTreeConstants.JJTRULESET:
			icon = IconLoader.ruleIcon;
			break;
		case CSSParserTreeConstants.JJTCSS:
			icon = IconLoader.cssIcon;
			break;
		case CSSParserTreeConstants.JJTIMPORTS:
			icon = IconLoader.importIcon;
			break;
		case CSSParserTreeConstants.JJTDECLARATION:
			icon = IconLoader.adeclIcon;
			break;
		case CSSParserTreeConstants.JJTSELECTOR:
			icon = IconLoader.entityIcon;
			break;
		default:
			if (isLeaf())
				icon = IconLoader.leafIcon;
			else
				icon = IconLoader.typeIcon;
			break;
		}
		return icon;
	}

	public void applyAttributes() {
		int count = getChildCount();
		Object[] children = getChildren();
		StyledDocument sd = (StyledDocument) getDocument();
		int startOffset = getStartOffset();
		int length = getEndOffset() - startOffset;
		AttributeSet attrs = getAttributes();
		sd.setCharacterAttributes(startOffset, length, attrs, true);
		for (int i = 0; i < count; i++) {
			SimpleNode sn = (SimpleNode) children[i];
			sn.applyAttributes();
		}
	}
}

/*
 * JavaCC - OriginalChecksum=67ba39f9b14f9e4835a024aad746fb43 (do not edit this
 * line)
 */
