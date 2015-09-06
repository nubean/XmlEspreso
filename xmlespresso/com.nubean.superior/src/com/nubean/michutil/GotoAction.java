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

import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

/**
 * <p>Title: Michigan XML Editor</p>
 * <p>Description: This edits an XML document based on an XML schema.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Nubean LLC</p>
 * @author Ajay Vohra
 * @version 1.0
 */

/**
 * This action must be added to a JTextPane only. It is used to go to a user
 * entered line and column number.
 */
public class GotoAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7519202242576605789L;
	private JTextComponent tp;

	private Point getPopupLocation() {

		Point p = new Point(100,200);
		
		EditUtils.convertPointToScreen(p, tp);
		
		return p;
	}

	public GotoAction(JTextComponent tp) {
		this.tp = tp;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String lc = GenericInput.showDialog(tp,
					LocalizedResources.applicationResources
							.getString("goto.line.column"),
					LocalizedResources.applicationResources
							.getString("enter.line.column"), null, null,
					getPopupLocation());
			if (lc != null) {
				StringTokenizer st = new StringTokenizer(lc, " ,-");
				int line = Integer.parseInt(st.nextToken());
				int col = Integer.parseInt(st.nextToken());
				gotoLineCol(tp, line, col);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void gotoLineCol(JTextComponent tp, int line, int col) {
		try {
			javax.swing.text.Document doc = tp.getDocument();
			Element root = doc.getDefaultRootElement();
			Element le = root.getElement(Math.max(line - 1, 0));

			if (le != null) {
				int offset = le.getStartOffset();
				offset += (Math.max(col - 1, 0));

				tp.setCaretPosition(offset);
			}
		} catch (Exception e) {
			tp.setCaretPosition(0);
		}
	}
}