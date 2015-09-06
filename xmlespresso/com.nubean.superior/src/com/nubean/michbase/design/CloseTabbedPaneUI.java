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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

import com.nubean.michutil.IconLoader;

public class CloseTabbedPaneUI extends BasicTabbedPaneUI {

	private static Color tabselectedbgColor = UIManager
			.getColor("TabbedPane.tabselectedbg");

	private static Color tabselectedfgColor = UIManager
			.getColor("TabbedPane.tabselectedfg");

	private Vector<ActionListener> closeActions;

	public CloseTabbedPaneUI() {
		super();
		closeActions = new Vector<ActionListener>(2, 2);
	}

	public void addCloseAction(ActionListener action) {
		if (!closeActions.contains(action))
			closeActions.add(action);
	}

	public void removeCloseAction(ActionListener action) {
		closeActions.remove(action);
	}

	public void fireCloseAction() {
		int count = closeActions.size();
		ActionEvent ae = new ActionEvent(tabPane, tabPane.getSelectedIndex(),
				"closeTab");
		for (int i = 0; i < count; i++) {
			ActionListener action = (ActionListener)closeActions.elementAt(i);
			action.actionPerformed(ae);
		}
	}

	protected int calculateTabWidth(int tabPlacement, int tabIndex,
			FontMetrics metrics) {
		int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
		return width + 20;
	}

	protected void layoutLabel(int tabPlacement, FontMetrics metrics,
			int tabIndex, String title, Icon icon, Rectangle tabRect,
			Rectangle iconRect, Rectangle textRect, boolean isSelected) {
		super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon,
				tabRect, iconRect, textRect, isSelected);

		textRect.x -= 10;
		iconRect.x -= 10;
	}

	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		g.setColor(!isSelected ? tabPane.getBackgroundAt(tabIndex)
				: tabselectedbgColor);
		switch (tabPlacement) {
		case LEFT:
			g.fillRect(x + 1, y + 1, w - 2, h - 3);
			break;
		case RIGHT:
			g.fillRect(x, y + 1, w - 2, h - 3);
			break;
		case BOTTOM:
			g.fillRect(x + 1, y, w - 3, h - 1);
			break;
		case TOP:
		default:
			g.fillRect(x + 1, y + 1, w - 3, h - 1);
		}
	}

	protected void paintText(Graphics g, int tabPlacement, Font font,
			FontMetrics metrics, int tabIndex, String title,
			Rectangle textRect, boolean isSelected) {

		g.setFont(font);

		View v = getTextViewForTab(tabIndex);
		if (v != null) {
			// html
			v.paint(g, textRect);
		} else {
			// plain text
			int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);

			if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
				if (!isSelected)
					g.setColor(tabPane.getForegroundAt(tabIndex));
				else
					g.setColor(tabselectedfgColor);
				BasicGraphicsUtils
						.drawStringUnderlineCharAt(g, title, mnemIndex,
								textRect.x, textRect.y + metrics.getAscent());

			} else { // tab disabled
				g.setColor(tabPane.getBackgroundAt(tabIndex).brighter());
				BasicGraphicsUtils
						.drawStringUnderlineCharAt(g, title, mnemIndex,
								textRect.x, textRect.y + metrics.getAscent());
				g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title,
						mnemIndex, textRect.x - 1, textRect.y
								+ metrics.getAscent() - 1);

			}
		}

	}

	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
			int tabIndex, Rectangle iconRect, Rectangle textRect) {
		super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
		Rectangle tabRect = rects[tabIndex];
		int selectedIndex = tabPane.getSelectedIndex();
		boolean isSelected = selectedIndex == tabIndex;

		if (isSelected) {
			IconLoader.xiconIcon.paintIcon(tabPane, g, tabRect.x
					+ tabRect.width - 15, textRect.y + 2);
		}
	}
	
	 protected MouseListener createMouseListener() {
		 return new CloseMouseHandler();
	 }

	 public class CloseMouseHandler extends MouseHandler {
	        public void mousePressed(MouseEvent e) {
	        	int selectedIndex = tabPane.getSelectedIndex();
	        	if(selectedIndex >= 0) {
	        		Rectangle tabRect = rects[selectedIndex];
	        		Rectangle r = new Rectangle();
	        		r.x = tabRect.x + tabRect.width - 15;
	        		r.y = tabRect.y;
	        		r.width = 10;
	        		r.height = 10;
	        		
	        		if(r.contains(e.getX(), e.getY())) {
	        			fireCloseAction();
	        			return;
	        		}
	        	}
	        	super.mousePressed(e);
	        }
	    }
}
