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

package com.nubean.michutil;

import java.awt.Color;

import com.itextpdf.text.*;
import java.io.*;
import javax.swing.text.*;

public class PDFConverter {
	private com.itextpdf.text.Document pdf;
	private javax.swing.text.AbstractDocument doc;

	public PDFConverter(AbstractDocument doc, File pdfFile) {
		pdf = new com.itextpdf.text.Document();
		this.doc = doc;
		try {
			pdf.open();

			javax.swing.text.Element root = doc.getDefaultRootElement();
			int count = root.getElementCount();
			for (int i = 0; i < count; i++) {
				javax.swing.text.Element ele = root.getElement(i);
				if (ele.isLeaf()) {
					pdf.add(getChunk(ele));
				} else {
					pdf.add(getParagraph(ele));
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if (pdf != null)
				pdf.close();
		}
	}

	private Paragraph getParagraph(javax.swing.text.Element pe)
			throws javax.swing.text.BadLocationException {
		int count = pe.getElementCount();
		Paragraph para = new Paragraph();
		for (int i = 0; i < count; i++) {
			javax.swing.text.Element ele = pe.getElement(i);
			if (ele.isLeaf()) {
				para.add(getChunk(ele));
			} else {
				return getParagraph(ele);
			}
		}
		return para;
	}

	private Chunk getChunk(javax.swing.text.Element ele)
			throws javax.swing.text.BadLocationException {
		int start = ele.getStartOffset();
		int end = ele.getEndOffset();
		String text = doc.getText(start, end - start);
		AttributeSet set = ele.getAttributes();
		String fontFamily = StyleConstants.getFontFamily(set);
		int fontSize = StyleConstants.getFontSize(set);
		int style = Font.NORMAL;
		if (StyleConstants.isBold(set))
			style = Font.BOLD;
		if (StyleConstants.isItalic(set))
			style |= Font.ITALIC;
		Color fontColor = StyleConstants.getForeground(set);

		BaseColor baseColor = new BaseColor(fontColor);
		Chunk chunk = new Chunk(text, FontFactory.getFont(fontFamily, fontSize,
				style, baseColor));
		return chunk;
	}
}
