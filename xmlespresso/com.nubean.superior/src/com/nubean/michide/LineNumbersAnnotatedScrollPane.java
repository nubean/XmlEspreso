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

package com.nubean.michide;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class LineNumbersAnnotatedScrollPane extends JPanel implements
		SourceView {
	private static final long serialVersionUID = -4376013296623257208L;

	private JTextPane textPane;

	private JScrollPane scrollPane;

	private LineNumberPanel lineNumberPanel;

	private int rightBorder;

	public LineNumbersAnnotatedScrollPane(JTextPane textPane,
			JScrollPane scrollPane) {
		this.textPane = textPane;
		this.scrollPane = scrollPane;
		lineNumberPanel = new LineNumberPanel();
		BorderLayout bl = new BorderLayout();
		setLayout(bl);

		add(lineNumberPanel, BorderLayout.WEST);
		add(scrollPane, BorderLayout.CENTER);

	}

	public Object getTextView() {
		return textPane;
	}

	public Object getScrollView() {
		return scrollPane;
	}

	public void showLineNumbers(boolean show) {
		lineNumberPanel.setVisible(show);
	}

	public void repaintLineNumbers() {
		lineNumberPanel.repaint();
	}

	private class LineNumberPanel extends JPanel {

		private static final long serialVersionUID = 5374339485358444360L;
		private HashMap<String, Font> fontMap;

		public LineNumberPanel() {
			super();
			setBackground(Color.WHITE);
			fontMap = new HashMap<String, Font>(13, 0.87f);
		}

		private Font getFont(String ff, int style, int size) {
			String key = ff + "-" + style + "-" + size;
			Font font = (Font) fontMap.get(key);
			if (font == null) {
				font = new Font(ff, style, size);
				fontMap.put(key, font);
			}
			return font;
		}

		public void paint(Graphics g) {
			super.paint(g);

			// We need to properly convert the points to match the viewport
			// Read docs for viewport

			JViewport viewport = scrollPane.getViewport();
			Point orig = viewport.getViewPosition();
			Dimension size = viewport.getExtentSize();

			// starting pos in document
			int start = textPane.viewToModel(orig);

			// end pos in doc
			int end = textPane.viewToModel(new Point(orig.x
					+ textPane.getWidth(), orig.y + size.height));

			// translate offsets to lines
			Document doc = textPane.getDocument();
			Element root = doc.getDefaultRootElement();
			int startline = root.getElementIndex(start);
			int endline = root.getElementIndex(end);

			int rb = ((endline / 100)) + 10;
			if (rb != rightBorder) {
				rightBorder = rb;
				setBorder(new EmptyBorder(0, 2, 0, rightBorder));
			}

			Element ele = root.getElement(startline);
			AttributeSet as = ele.getAttributes();

			String fontFamily = StyleConstants.getFontFamily(as);
			int fontSize = StyleConstants.getFontSize(as);
			int fontStyle = (StyleConstants.isItalic(as) ? Font.BOLD
					: Font.PLAIN);
			fontStyle |= (StyleConstants.isBold(as) ? Font.ITALIC : 0);
			Font font = getFont(fontFamily, fontStyle, fontSize);
			Graphics2D g2D = (Graphics2D) textPane.getGraphics();
			
			
			try {
				g.setColor(Color.GRAY);
				String str = doc.getText(ele.getStartOffset(), ele
						.getEndOffset()
						- ele.getStartOffset());
				if(str == null) {
					str = "Quick brown fox jumped over the fence";
				}
				LineMetrics lineMetrics = font.getLineMetrics(str, g2D
						.getFontRenderContext());
				int ystart = (int)(lineMetrics.getHeight());
				
				for (int line = startline; line <= endline; line++) {
					ele = root.getElement(line);
					Rectangle rect = textPane.modelToView(ele.getStartOffset());
					
					as = ele.getAttributes();

					fontFamily = StyleConstants.getFontFamily(as);
					fontSize = StyleConstants.getFontSize(as);
					fontStyle = (StyleConstants.isItalic(as) ? Font.BOLD
							: Font.PLAIN);
					fontStyle |= (StyleConstants.isBold(as) ? Font.ITALIC : 0);
					font = getFont(fontFamily, fontStyle, fontSize);
					g.setFont(font);

					int y = (rect.y + ystart);
					y -= (orig.y);
					
					g.drawString(Integer.toString(line + 1), 0, y);

				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

		}
	}
}
