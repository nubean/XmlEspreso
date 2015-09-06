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

package com.nubean.xmlespresso.editors;

import java.io.CharArrayReader;
import java.util.Collections;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.DefaultErrorHandler;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.EditorConfiguration;
import com.nubean.michdtd.DTDEditorConfiguration;
import com.nubean.michdtd.parser.ASTDeclSep;
import com.nubean.michdtd.parser.ASTdtd;
import com.nubean.michdtd.parser.DTDParser;
import com.nubean.michdtd.parser.DTDParserConstants;
import com.nubean.michdtd.parser.ParseException;
import com.nubean.michdtd.parser.Token;
import com.nubean.michdtd.parser.TokenMgrError;
import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.dialogs.DefaultEspressoElementChooser;
import com.nubean.xmlespresso.doc.DefaultEspressoDocument;
import com.nubean.xmlespresso.doc.DefaultEspressoStyledDocument;
import com.nubean.xmlespresso.pages.DTDDocumentOutlinePage;

public class DTDEspressoEditor extends DefaultEspressoEditor {

	private static final String MIME_TYPE = "text/dtd";

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

	public DTDEspressoEditor() {
		super();
	}

	public DocumentDescriptor getDocumentDescriptor() {
		return null;
	}

	public EditorConfiguration getEditorConfiguration() {
		IPreferenceStore ps = XMLEspressoActivator.getDefault()
				.getPreferenceStore();

		StringBuffer sb = new StringBuffer(XMLEspressoActivator.CONFIG_PREF);
		sb.append(":").append(MIME_TYPE);

		String xml = ps.getString(sb.toString());

		EditorConfiguration config = null;
		if (xml != null && xml.trim().length() > 0) {
			config = (EditorConfiguration) CommonUtils.deserialize(xml);
		} else {
			config = new DTDEditorConfiguration();
		}

		return config;

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

	@Override
	public Vector<String> getExpectedValues() {
		prefix = null;

		Vector<String> expected = new Vector<String>(8, 8);
		int where = getCaretPosition();
		
		DefaultEspressoDocument dedoc = (DefaultEspressoDocument) textViewer
				.getDocument();
		DefaultEspressoStyledDocument doc = dedoc.getDocument();
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

	@Override
	public void doContentAssist() {
		try {

			int where = getCaretPosition();
			parseDocument();

			Vector<String> expected = getExpectedValues();
			String choice = null;
			if (expected.size() > 1) {
				Collections.sort(expected);

				int index = 0;
				if (previous != null) {
					index = expected.indexOf(previous);
					if (index < 0)
						index = 0;
				}

				choice = (String) DefaultEspressoElementChooser.showDialog(
						textViewer.getTextWidget().getShell(), expected);
			}

			if (expected.size() == 1)
				choice = (String) expected.elementAt(0);

			if (choice != null) {
				previous = choice;
				if (prefix != null)
					choice = choice.substring(prefix.length());
				if (choice.equals("Space")) {
					choice = " ";
				}
				textViewer.getDocument().replace(where, 0, choice.toString());
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void parseDocument() {
		DefaultErrorHandler errorHandler = new DefaultErrorHandler();
		DefaultEspressoDocument dedoc = (DefaultEspressoDocument) textViewer
				.getDocument();
		DefaultEspressoStyledDocument dsdoc = dedoc.getDocument();

		dsdoc.removeDocumentListener(documentTracker);

		try {
			parseException = null;
			token = null;
			dtd = null;

			String text = dsdoc.getText(0, dsdoc.getLength());
			CharArrayReader cr = new CharArrayReader(text.toCharArray());
			DTDParser parser = new DTDParser(cr);
			parser.setTabSize(1);
			parser.setDocument(dsdoc);
			parser.setErrorHandler(errorHandler);

			dtd = parser.dtd();
			if (dtd != null) {
				dtd.applyAttributes();
				treeModel = new DefaultTreeModel(dtd);
				if (outline != null)
					outline.setInput(treeModel);
			} else {
				if (outline != null)
					outline.setInput(null);
			}

			parseException = parser.getParseException();
			tokenMgrError = parser.getTokenMgrError();
			token = parser.token;

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			addProblemMarkers(errorHandler);
		}

		dsdoc.addDocumentListener(documentTracker);
	}

	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			defaultTreeListener = new DefaultTreeListener();
			outline = new DTDDocumentOutlinePage(treeModel, defaultTreeListener);
			return outline;
		}

		return super.getAdapter(required);
	}

	@Override
	protected void doInputDocumentChanged(IDocument oldInput, IDocument newInput) {

		if (newInput instanceof DefaultEspressoDocument) {
			DefaultEspressoDocument dedoc = (DefaultEspressoDocument) newInput;
			DefaultEspressoStyledDocument dsdoc = dedoc.getDocument();

			if (dsdoc.getLength() == 0) {
				try {
					dsdoc.insertString(0,
							"<?xml version='1.0' encoding='utf-8' ?>", null);
				} catch (BadLocationException e) {

				}
			}
		}
		super.doInputDocumentChanged(oldInput, newInput);
	}
}
