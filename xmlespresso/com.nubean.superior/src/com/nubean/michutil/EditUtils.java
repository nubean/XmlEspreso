/*
 * EditUtils.java
 *
 * Created on July 30, 2002, 10:17 PM
 */

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
import java.util.regex.*;
import javax.swing.text.*;
import javax.swing.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;

/**
 *
 * @author  Ajay Vohra
 */
public class EditUtils {
        
    public static Matcher getMatcher(javax.swing.text.Document doc, String regex)
    {
         
        if(regex == null)
            return null;

        Pattern pattern = Pattern.compile(regex);
        Matcher retval = null;
        try {
            int len = doc.getLength() - 1;
            String input = doc.getText(0, len);
                
            retval = pattern.matcher(input);
        } catch(javax.swing.text.BadLocationException e){
        }
        return retval;
    }
    
  public static  void setStyle(JTextPane textPane, Style style)
  {
    String fontFamily = StyleConstants.getFontFamily(style);
    int fontSize = StyleConstants.getFontSize(style);
    int fontStyle = 0;
    fontStyle |= (StyleConstants.isItalic(style) ? Font.ITALIC : 0);
    fontStyle |= (StyleConstants.isBold(style) ? Font.BOLD : 0);
    Font font = new Font(fontFamily, fontStyle, fontSize);
    textPane.setFont(font);
    
    textPane.setBackground(StyleConstants.getBackground(style));
    textPane.setForeground(StyleConstants.getForeground(style));
  }
 
  public static void convertPointToScreen(Point p, Component c) {
	  SwingUtilities.convertPointToScreen(p, c);
	 
	  Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	  if (p.x < 0 || p.x > dim.width) {
		  p.x = dim.width/2;
	  }
	  
	  if(p.y < 0 || p.y > dim.height) {
		  p.y = dim.height/2;
	  }
	  
  }
}
