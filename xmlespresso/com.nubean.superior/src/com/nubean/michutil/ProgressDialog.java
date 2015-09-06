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

import javax.swing.*;
import java.awt.*;

/**
 * <p>
 * Title: Michigan XML Editor
 * </p>
 * <p>
 * Description: This edits an XML document based on an XML schema.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: Nubean LLC
 * </p>
 * 
 * @author Ajay Vohra
 * @version 1.0
 */

public class ProgressDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4757127239182428438L;
	private JLabel msgl, pmsgl;
	private JFrame frame;

	public ProgressDialog(JFrame frame, String title, String msg, String pmsg) {
		super(frame, title, false);
		this.frame = frame;
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel upanel = new JPanel();
		msgl = new JLabel(msg);

		upanel.add(msgl);
		panel.add(upanel, BorderLayout.CENTER);

		JPanel bpanel = new JPanel();
		panel.add(bpanel, BorderLayout.SOUTH);

		pmsgl = new JLabel(pmsg);
		bpanel.add(pmsgl);
		this.getContentPane().add(panel);
	}

	public void setProgressMsg(String pmsg) {
		pmsgl.setText(pmsg);
	}

	public void setVisible(boolean flag) {
		if (flag) {
			Dimension size = frame.getSize();
			Point loc = frame.getLocationOnScreen();
			setLocation(loc.x + size.width / 2 - 200 / 2, loc.y + size.height
					/ 2 - 100 / 2);
			setSize(200, 100);
		}

		super.setVisible(flag);
	}

}