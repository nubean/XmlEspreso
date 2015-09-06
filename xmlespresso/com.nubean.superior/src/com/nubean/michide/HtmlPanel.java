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
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import com.nubean.michutil.IconLoader;
import com.nubean.michutil.LocalizedResources;

/**
 * This class is used to display HTML pages within a panel. This class does not
 * support all HTML tags. In particular this class does not support Applet tag.
 * 
 * @author Ajay Vohra
 */
public class HtmlPanel extends JPanel implements HyperlinkListener {
	private static final long serialVersionUID = -5577630487617657157L;

	protected ImageIcon backImage;

	protected ImageIcon homeImage;

	protected HtmlPane html;

	protected Stack<URL> ustack;

	protected JToolBar toolbar;

	protected JButton back;

	protected JButton home;

	protected URL homeURL;

	protected JLabel status;

	private DefaultHighlighter.DefaultHighlightPainter painter, findPainter;

	private void initialize() {
		try {
			findPainter = new DefaultHighlighter.DefaultHighlightPainter(
					new Color(200, 240, 160));
			setLayout(new BorderLayout());

			if (backImage != null && homeImage != null) {
				createToolbar();
				add(toolbar, BorderLayout.NORTH);
			}

			html = new HtmlPane();
			html.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
			html.setEditable(false);
			html.addHyperlinkListener(this);
			JScrollPane scroller = new JScrollPane(html);
			add(scroller, BorderLayout.CENTER);
			final JPopupMenu popup = new Popup(
					LocalizedResources.applicationResources
							.getString("documentation"));

			html.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()
							|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0))
						popup.show(html, e.getX(), e.getY());
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	public void setText(String text) {
		html.setText(text);
	}

	public void setPage(String page) {
		try {
			if (page != null) {
				homeURL = getURL(page);
				setText("<html><head></head><body><a href='"
						+ homeURL
						+ "'>"
						+ LocalizedResources.applicationResources
								.getString("see.documentation")
						+ "</a></body></html>");
			}
		} catch (Exception e) {
			setText(page);
		}
	}

	private URL getURL(String docurl) throws java.net.MalformedURLException {
		URL ret = null;
		docurl = docurl.trim();

		if (docurl.indexOf(":") > 0) {
			ret = new URL(docurl);
		} else {
			String ref = null;
			int index = -1;
			String url = docurl;
			if ((index = docurl.indexOf("#")) > 0) {
				ref = docurl.substring(index);
				url = docurl.substring(0, index);
			}
			ret = getClass().getClassLoader().getResource(url);
			if (ret != null && ref != null) {
				ret = new URL(ret.toExternalForm() + ref);
			}
		}
		return ret;

	}

	/**
	 * create an HTML Panel with a home page and with buttons for back and home.
	 * 
	 * @param docurl
	 *            home page url
	 * @param backImage
	 *            icon for bakc button
	 * @param homeImage
	 *            icon for home button
	 */
	public HtmlPanel(String docurl, ImageIcon backImage, ImageIcon homeImage) {
		this.backImage = backImage;
		this.homeImage = homeImage;
		String text = null;

		try {
			if (docurl != null) {
				homeURL = getURL(docurl);
				setText("<html><head></head><body><a href='"
						+ homeURL
						+ "'>"
						+ LocalizedResources.applicationResources
								.getString("see.documentation")
						+ "</a></body></html>");
			}
		} catch (Exception e) {
			text = docurl;
		} finally {
			initialize();
			if (text != null)
				setText(text);
		}

	}

	/**
	 * this is public as an implementaiton side effect
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (html.getPage() != null && ustack != null)
				ustack.push(html.getPage());
			linkActivated(e.getURL());

			if (back != null)
				back.setEnabled(true);
		} else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * This reloads the current page
	 */
	public void reload() {
		linkActivated(html.getPage());
	}

	/**
	 * This transfers page to the new url.
	 * 
	 * @param u
	 *            url to go to
	 */
	public void linkActivated(URL u) {
		if (homeURL == null)
			homeURL = u;
		Cursor c = html.getCursor();
		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		html.setCursor(waitCursor);

		SwingUtilities.invokeLater(new PageLoader(u, c));
	}

	private void createToolbar() {
		MouseListener ml = new MouseAdapter() {
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
		};

		ustack = new Stack<URL>();
		toolbar = new JToolBar();
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		// toolbar.setBorder(new CompoundBorder(new
		// BevelBorder(BevelBorder.LOWERED),
		// new EmptyBorder(5,5,5,5)));

		toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));
		back = new JButton(backImage);
		back.setContentAreaFilled(false);
		back.setEnabled(false);
		back.setToolTipText(LocalizedResources.applicationResources
				.getString("back"));
		back.setBorderPainted(false);
		back.addMouseListener(ml);
		toolbar.add(back);

		back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				HtmlPanel.this.setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (!ustack.empty()) {
					URL url = (URL) ustack.pop();
					linkActivated(url);
					if (ustack.empty())
						back.setEnabled(false);
				}
				HtmlPanel.this.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});

		home = new JButton(homeImage);
		home.setContentAreaFilled(false);
		home.setToolTipText(LocalizedResources.applicationResources
				.getString("home"));
		home.setBorderPainted(false);
		home.addMouseListener(ml);
		toolbar.add(home);

		home.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (homeURL != null) {
					HtmlPanel.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					linkActivated(homeURL);
					back.setEnabled(false);
					HtmlPanel.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		status = new JLabel();
		toolbar.add(status);
	}

	class PageLoader implements Runnable {

		URL url;

		Cursor cursor;

		PageLoader(URL u, Cursor c) {
			url = u;
			cursor = c;
		}

		public void run() {
			if (url == null) {
				html.setCursor(cursor);
				html.getParent().repaint();

			} else {
				Document doc = html.getDocument();

				try {
					status.setText(LocalizedResources.applicationResources
							.getString("loading")
							+ " " + url + "...");
					html.setPage(url);
				} catch (Throwable e) {
					html.setDocument(doc);
					getToolkit().beep();
				} finally {
					url = null;
					SwingUtilities.invokeLater(this);
				}
			}
		}
	}

	private class Popup extends JPopupMenu {
		private static final long serialVersionUID = -8474631165468328671L;

		private java.util.regex.Matcher matcher;

		private int offset;

		private String text;

		private Object findTag;

		private JMenuItem fami;

		public Popup(String title) {
			super(title);
			init();
		}

		private void find() {
			text = (String) JOptionPane.showInputDialog(HtmlPanel.this,
					LocalizedResources.applicationResources
							.getString("find.text"),
					LocalizedResources.applicationResources.getString("find"),
					JOptionPane.QUESTION_MESSAGE, IconLoader.findIcon, null,
					null);
			if (text == null)
				return;

			javax.swing.text.Document doc = html.getDocument();

			matcher = com.nubean.michutil.EditUtils.getMatcher(doc, text);
			offset = 0;
			findAgain();
		}

		private void findAgain() {
			if (matcher != null && matcher.find(offset)) {
				Highlighter dh = html.getHighlighter();
				try {
					if (findTag != null)
						dh.removeHighlight(findTag);
					int start = matcher.start();
					int end = matcher.end();
					html.setCaretPosition(start);
					findTag = dh.addHighlight(start, end, findPainter);
					fami.setEnabled(true);
					offset = end;
				} catch (BadLocationException e) {
				}
			} else {
				matcher = null;
				fami.setEnabled(false);
			}
		}

		private void removeAllHighlights() {
			Highlighter dh = html.getHighlighter();
			dh.removeAllHighlights();
		}

		private void init() {
			JMenuItem mi = null;
			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("search.find"), IconLoader.findIcon);
			mi.setMnemonic('f');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					find();
					setVisible(false);
				}
			});
			add(mi);

			fami = mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("search.find.again"), IconLoader.findAgainIcon);
			mi.setEnabled(false);
			mi.setMnemonic('a');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					findAgain();
					setVisible(false);
				}
			});
			add(mi);

			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("remove.highlights"));
			mi.setMnemonic('h');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeAllHighlights();
					setVisible(false);
				}
			});
			add(mi);

		}
	}
}
