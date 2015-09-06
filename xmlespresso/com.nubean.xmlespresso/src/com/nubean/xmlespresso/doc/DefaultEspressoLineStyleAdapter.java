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

package com.nubean.xmlespresso.doc;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;

import com.nubean.xmlespresso.XMLEspressoActivator;

public class DefaultEspressoLineStyleAdapter implements LineStyleListener {

	private Document doc;

	public DefaultEspressoLineStyleAdapter(Document doc) {
		this.doc = doc;
	}

	private StyleRange getStyleRange(Element ele) {
		AttributeSet as = ele.getAttributes();
		Color fg = StyleConstants.getForeground(as);
		Color bg = StyleConstants.getBackground(as);

		if (fg.equals(bg)) {
			if (fg.equals(Color.BLACK))
				bg = Color.WHITE;
			else
				bg = Color.BLACK;
		}
		boolean bold = StyleConstants.isBold(as);
		boolean italic = StyleConstants.isItalic(as);
		boolean strikeout = StyleConstants.isStrikeThrough(as);
		boolean underline = StyleConstants.isUnderline(as);

		StyleRange sr = new StyleRange();
		sr.start = ele.getStartOffset();
		sr.length = ele.getEndOffset() - ele.getStartOffset();

		RGB rgb = new RGB(fg.getRed(), fg.getGreen(), fg.getBlue());
		XMLEspressoActivator plugin = XMLEspressoActivator.getDefault();
		sr.foreground = plugin.getColor(rgb);

		rgb = new RGB(bg.getRed(), bg.getGreen(), bg.getBlue());
		sr.background = plugin.getColor(rgb);
		sr.fontStyle = SWT.NORMAL;

		if (bold)
			sr.fontStyle = SWT.BOLD;
		if (italic)
			sr.fontStyle |= SWT.ITALIC;

		sr.strikeout = strikeout;
		sr.underline = underline;

		return sr;
	}

	private void getStyleRange(ArrayList styles, Element ele) {

		if (!ele.isLeaf()) {
			int nele = ele.getElementCount();
			for (int i = 0; i < nele; i++) {
				Element e = ele.getElement(i);
				getStyleRange(styles, e);
			}
		} else {
			styles.add(getStyleRange(ele));
		}
	}

	public void lineGetStyle(LineStyleEvent event) {

		try {
			ArrayList styles = new ArrayList();

			int offset = event.lineOffset;
			String text = event.lineText;
			int length = text.length();

			Element root = doc.getDefaultRootElement();

			int startline = root.getElementIndex(offset);
			int endline = root.getElementIndex(offset + length);

			for (int i = startline; i <= endline; i++) {
				Element ele = root.getElement(i);
				getStyleRange(styles, ele);
			}

			event.styles = new StyleRange[styles.size()];
			styles.toArray(event.styles);
		} catch (Exception e) {
			// ignore
		}

	}

}
