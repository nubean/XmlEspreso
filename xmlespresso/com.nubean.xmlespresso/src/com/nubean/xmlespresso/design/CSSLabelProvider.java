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

package com.nubean.xmlespresso.design;

import java.net.URL;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.nubean.css.parser.CSSParserTreeConstants;
import com.nubean.css.parser.SimpleNode;
import com.nubean.michutil.IconLoader;

public class CSSLabelProvider extends LabelProvider {

	private Display display;
	
	public CSSLabelProvider(Display display) {
		super();
		this.display = display;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns <code>null</code>.
	 * Subclasses may override.
	 */
	@Override
	public Image getImage(Object element) {
		
		ClassLoader classLoader = IconLoader.class.getClassLoader();
		URL url = null;
		Image image = null;
		if (element instanceof SimpleNode) {
			SimpleNode node = (SimpleNode)element;
			switch(node.getId()) {
			case CSSParserTreeConstants.JJTRULESET:
				url = classLoader.getResource("images/rule.gif");
				break;
			case CSSParserTreeConstants.JJTCSS:
				url = classLoader.getResource("images/css.gif");
				break;
			case CSSParserTreeConstants.JJTIMPORTS:
				url = classLoader.getResource("images/import.gif");
				break;
			case CSSParserTreeConstants.JJTDECLARATION:
				url = classLoader.getResource("images/adecl.gif");
				break;
			case CSSParserTreeConstants.JJTSELECTOR:
				url = classLoader.getResource("images/entity.gif");
				break;
			default:
				if (node.isLeaf())
					url = classLoader.getResource("images/leaf.gif");
				else
					url = classLoader.getResource("images/schema.gif");
				break;
			}
		}
		image = loadImage(url);
		return image;
	}

	private Image loadImage(URL url) {
		
		try {
			return new Image(display, url.openStream());
		} catch(Exception e){
			
		}
		
		return null;
	}
}
