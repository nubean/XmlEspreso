/*
 * MichiganLauncher.java
 *
 * Created on March 2, 2003, 5:07 PM
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

package com.nubean.michlic;

import com.nubean.michide.*;

import javax.swing.*;
import java.io.*;

/**
 * 
 * @author Ajay Vohra
 */
public class MichiganLauncher {


	public static void main(String args[]) {
		try {
			String home = System.getProperty("user.home");
			File homeDir = new File(home);
			File xmle = new File(homeDir, ".xmle4j");
			if (!xmle.exists())
				xmle.mkdir();
			
			MichiganIDE ide = new MichiganIDE();
			if (args != null && args.length > 0) {
				File file = new File(args[0]);
				if (file.exists() && file.length() > 0) {
					ide.openDocumentFile(file);
				}
			}
		} catch (Exception e) {
			JTextPane tp = new JTextPane();
			tp.setEditable(false);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			tp.setText(sw.getBuffer().toString());
			JScrollPane sp = new JScrollPane(tp);
			JOptionPane.showMessageDialog(new JFrame(), sp, e.getMessage(),
					JOptionPane.ERROR_MESSAGE);
		}
	} // main
}
