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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michutil.*;

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

public class DefaultTreeCellRenderer extends JLabel implements TreeCellRenderer {
	private static final long serialVersionUID = 3879159953841073206L;
	private final static int TRIM_SIZE = 128;

	public DefaultTreeCellRenderer() {
		setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		if (selected) {
			setOpaque(true);
		} else {
			setOpaque(false);
		}
		String text = value.toString();
		
		if(text.trim().length() == 0) {
			text = "";
		} else if (text.length() > TRIM_SIZE) {
			text = text.substring(0, TRIM_SIZE) + "...";
		}
		
		setText(CommonUtils.addEscapes(text));

		if (value instanceof Iconable) {
			Iconable iconable = (Iconable) value;
			Icon icon = iconable.getIcon();
			if (icon != null)
				setIcon(iconable.getIcon());
		}
		return this;

	}

	
}