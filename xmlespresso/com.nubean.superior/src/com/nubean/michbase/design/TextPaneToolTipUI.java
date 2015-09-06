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

package com.nubean.michbase.design;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicToolTipUI;


public class TextPaneToolTipUI extends BasicToolTipUI {

	public static TextPaneToolTipUI sharedInstance = new TextPaneToolTipUI();

	public TextPaneToolTipUI() {
		super();
	}

	public void paint(Graphics g, JComponent c) {

		String tipText = ((JToolTip) c).getTipText();
		if (tipText == null) {
			tipText = "";
		}
		JTextPane tp = new JTextPane();
		tp.setText(tipText);

		Dimension size = c.getSize();

		Rectangle paintTextR = new Rectangle(0, 0, size.width, size.height);

		SwingUtilities.paintComponent(g, tp, c, paintTextR);

	}

	public Dimension getPreferredSize(JComponent c) {
		return c.getPreferredSize();
	}

	public Dimension getMinimumSize(JComponent c) {
		return c.getMinimumSize();
	}

	public Dimension getMaximumSize(JComponent c) {
		return c.getMaximumSize();
	}
}
