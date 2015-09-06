/*
 * HtmlMain.java
 *
 * Created on September 12, 2002, 10:26 AM
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

package com.nubean.michide;

import javax.swing.*;

import com.nubean.michutil.LocalizedResources;

import java.awt.*;

public class HtmlMain {
	public static HtmlPane ep;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public HtmlMain() {
		super();
	}

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame(LocalizedResources.applicationResources
				.getString("html.browser"));
		ep = new HtmlPane();
		ep.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
		JScrollPane sp = new JScrollPane(ep);
		frame.getContentPane().setLayout(new BorderLayout());
		// sp.getViewport().setBackingStoreEnabled(true);
		frame.getContentPane().add(sp, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		final JTextField tf = new JTextField(30);
		panel.add(tf);
		JButton go = new JButton(LocalizedResources.applicationResources
				.getString("go"));
		panel.add(go);
		go.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String url = tf.getText();
				if (url != null && url.trim().length() > 0) {
					try {
						ep.setPage(url);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		frame.setSize(700, 600);
		frame.setVisible(true);
	}

}
