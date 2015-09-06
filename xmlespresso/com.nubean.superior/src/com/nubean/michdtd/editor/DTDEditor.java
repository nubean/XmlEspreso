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

package com.nubean.michdtd.editor;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.CharArrayReader;
import java.io.File;
import java.util.Collections;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.DefaultErrorHandler;
import com.nubean.michbase.DefaultStyleContext;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.design.DefaultTreeCellRenderer;
import com.nubean.michbase.dialog.DefaultElementChooser;
import com.nubean.michbase.editor.AbstractEditor;
import com.nubean.michbase.project.Project;
import com.nubean.michdtd.parser.*;
import com.nubean.michutil.EditUtils;
import com.nubean.michutil.LocalizedResources;

public class DTDEditor extends AbstractEditor {

	private static final String[] keywords = new String[] { "ALL", "version",
			"ANY", "EMPTY", "ID", "IDREF", "IDREFS", "ENTITY", "ENTITIES",
			"NMTOKEN", "NMTOKENS", "NOTATION", "CDATA", "NDATA", "IGNORE",
			"INCLUDE", "PUBLIC", "SYSTEM" };

	private static final String[] toplevel = new String[] { "<!ELEMENT >",
			"<!ATTLIST >", "<!NOTATION >", "<!ENTITY >", "<?target 'data' ?>",
			"<!-- Comment -->" };

	private ParseException parseException;

	private TokenMgrError tokenMgrError;

	private Token token;

	private ASTdtd dtd;

	private String prefix;

	private String previous;

	public DTDEditor(Project project, DocumentDescriptor dd, boolean integrated) {
		super(project, dd, integrated);
	}

	protected void readDocumentFromFile(File file, Document doc) {
		super.readDocumentFromFile(file, doc);
		if (doc.getLength() == 0) {
			try {
				doc.insertString(0, "<?xml version='1.0' encoding='utf-8' ?>",
						null);
			} catch (BadLocationException e) {

			}
		}
	}

	protected void documentChanged(DocumentEvent e) {
		super.documentChanged(e);
		startParseTimer();
	}

	protected StyleContext createStyleContext() {
		return new DefaultStyleContext(DTDParserTreeConstants.jjtNodeName);
	}

	protected void initDocTree() {
		docTree = new JTree();
		docTree.setCellRenderer(new DefaultTreeCellRenderer());
		docTree.addTreeSelectionListener(new DocTreeListener());
	}

	protected synchronized void parseDocument() {
		DefaultErrorHandler errorHandler = new DefaultErrorHandler();
		Document doc = textPane.getDocument();
		doc.removeDocumentListener(documentTracker);
		doc.removeUndoableEditListener(undoHandler);

		try {
			parseException = null;
			token = null;
			dtd = null;

			String text = doc.getText(0, doc.getLength());
			CharArrayReader cr = new CharArrayReader(text.toCharArray());
			DTDParser parser = new DTDParser(cr);
			parser.setTabSize(1);
			parser.setDocument(doc);
			parser.setErrorHandler(errorHandler);

			dtd = parser.dtd();
			if (dtd != null) {
				dtd.applyAttributes();
				docTree.setModel(new DefaultTreeModel(dtd));

			} else {
				DefaultTreeModel model = new DefaultTreeModel(
						new DefaultMutableTreeNode(
								LocalizedResources.applicationResources
										.getString("outline.not.available")));
				docTree.setModel(model);
			}

			parseException = parser.getParseException();
			tokenMgrError = parser.getTokenMgrError();
			token = parser.token;

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			addErrorView(errorHandler);
		}

		doc.addDocumentListener(documentTracker);
		doc.addUndoableEditListener(undoHandler);
	}

	private Vector<String> stripKeywords(Vector<String> expected) {
		Vector<String> ret = new Vector<String>(8, 8);
		int count = expected.size();
		for (int i = 0; i < count; i++) {
			String value = (String) expected.elementAt(i);
			if (!CommonUtils.contains(keywords, value)) {
				ret.add(value);
			}
		}

		return ret;
	}

	private String getTokenLiteral(String str) {
		StringBuffer sb = new StringBuffer(str.substring(1, str.length() - 1));
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == '\\') {
				sb.replace(i, i + 1, "");
			}
		}

		return sb.toString();
	}

	protected Vector<String> getExpectedValues() {
		prefix = null;

		Vector<String> expected = new Vector<String>(8, 8);
		int where = textPane.getCaretPosition();
		Document doc = textPane.getDocument();
		Element root = doc.getDefaultRootElement();

		if (parseException != null) {
			Element line = root
					.getElement(parseException.currentToken.next.beginLine - 1);
			int offset = line.getStartOffset()
					+ parseException.currentToken.next.beginColumn;
			if (offset >= where
					&& parseException.expectedTokenSequences != null) {
				boolean foundName = false;
				for (int i = 0; i < parseException.expectedTokenSequences.length; i++) {
					for (int j = 0; j < parseException.expectedTokenSequences[i].length; j++) {
						int kind = parseException.expectedTokenSequences[i][j];
						String str = parseException.tokenImage[kind];
						str = getTokenLiteral(str);
						if (!expected.contains(str))
							expected.add(str);
						if (kind == DTDParserConstants.Name)
							foundName = true;
					}
				}
				if (foundName) {
					expected = stripKeywords(expected);
				}
			}
		} else if (tokenMgrError != null) {

			Element line = root.getElement(token.endLine - 1);
			int offset = line.getStartOffset() + token.endColumn;

			while (offset < where) {
				Token t = token.next;
				if (t != null) {
					token = t;
					line = root.getElement(token.endLine - 1);
					offset = line.getStartOffset() + token.endColumn;
				} else {
					break;
				}
			}

			if (where > offset) {
				try {
					line = root.getElement(token.beginLine - 1);
					offset = line.getStartOffset() + token.beginColumn;
					prefix = doc.getText(offset, (where - offset));
					if (prefix != null) {
						prefix = prefix.trim();
						int count = DTDParserConstants.tokenImage.length;
						for (int i = 0; i < count; i++) {
							String str = DTDParserConstants.tokenImage[i];
							str = getTokenLiteral(str);
							if (str.startsWith(prefix)) {
								expected.add(str);
							}
						}
					}
				} catch (BadLocationException e) {

				}
			}
		} else if (dtd != null) {
			Object ele = dtd.positionToElement(where);
			if (ele == null || ele instanceof ASTDeclSep) {
				for (int i = 0; i < toplevel.length; i++) {
					expected.add(toplevel[i]);
				}
			}
		}

		return expected;
	}

	protected void addDocumentAction() {
		textPane.getInputMap()
				.put(
						KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
								InputEvent.CTRL_MASK), "documentAction");
		DocumentAction documentAction = new DocumentAction();
		textPane.getActionMap().put("documentAction", documentAction);
	}

	private void doContentAssist() {
		try {

			int where = textPane.getCaretPosition();
			parseDocument();

			Vector<String> expected = getExpectedValues();
			String choice = null;
			Point save = null;
			if (expected.size() > 1) {
				Collections.sort(expected);

				Point loc = textPane.getCaret().getMagicCaretPosition();

				if (loc == null) {
					loc = getDialogPopupLocation();
				} else {
					save = new Point(loc.x, loc.y);
					EditUtils.convertPointToScreen(loc, textPane);
				}

				int index = 0;
				if (previous != null) {
					index = expected.indexOf(previous);
					if (index < 0)
						index = 0;
				}

				choice = (String) DefaultElementChooser.showDialog(textPane,
						null, expected, loc, index);
			}

			if (expected.size() == 1)
				choice = (String) expected.elementAt(0);

			if (choice != null) {
				previous = choice;
				if (save != null)
					textPane.getCaret().setMagicCaretPosition(save);
				if (prefix != null)
					choice = choice.substring(prefix.length());
				if (choice.equals("Space")) {
					choice = " ";
				}
				textPane.getDocument().insertString(where, choice.toString(),
						null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class DocumentAction extends AbstractAction {

		private static final long serialVersionUID = 8034400119494664383L;

		public void actionPerformed(ActionEvent e) {
			doContentAssist();
		}
	}
}
