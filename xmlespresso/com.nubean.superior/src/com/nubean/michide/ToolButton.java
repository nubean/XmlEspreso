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
import java.awt.event.*;
import javax.swing.*;

public class ToolButton extends JButton implements MouseListener {
	private static final long serialVersionUID = -1062224161026125840L;
	private static final Insets margin = new Insets(1, 1, 1, 1);

	public ToolButton(Icon icon) {
		super(icon);
		init();
	}

	public ToolButton(String label) {
		super(label);
		init();
	}

	public ToolButton(String label, Icon icon) {
		super(icon);
		setToolTipText(label);
		init();
	}

	private void init() {
		setBorderPainted(false);
		setContentAreaFilled(false);
		setMargin(margin);
		addMouseListener(this);

	}

	public void mouseEntered(MouseEvent e) {
		JButton b = (JButton) e.getSource();

		b.setBorderPainted(true);
		b.repaint();
	}

	public void mouseExited(MouseEvent e) {
		JButton b = (JButton) e.getSource();

		b.setBorderPainted(false);
		b.repaint();
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {

	}
}
