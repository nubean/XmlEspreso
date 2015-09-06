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

package com.nubean.michbase.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.DocFlavor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.tree.TreePath;
import javax.swing.undo.CompoundEdit;

import com.nubean.michbase.DefaultElementNode;
import com.nubean.michbase.DefaultErrorHandler;
import com.nubean.michbase.DefaultStyleContext;
import com.nubean.michbase.DefaultStyledEditorKit;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.EditorConfiguration;
import com.nubean.michbase.ParseProblem;
import com.nubean.michbase.design.TextPaneToolTip;
import com.nubean.michbase.project.Project;
import com.nubean.michutil.BasicPrint;
import com.nubean.michutil.EditUtils;
import com.nubean.michutil.GotoAction;
import com.nubean.michutil.IconLoader;
import com.nubean.michutil.LocalizedResources;
import com.nubean.michutil.Region;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.michide.LineNumbersAnnotatedScrollPane;
import com.nubean.michide.MichiganTextPane;

public abstract class AbstractEditor implements IDEditor {
	public final static Color HIGHLIGHT_COLOR = new Color(14410725);

	protected JTextPane textPane;

	protected EditorConfiguration editorConfig;

	protected DocumentDescriptor docInfo;

	protected Project project;

	protected boolean integrated;

	protected DefaultHighlighter.DefaultHighlightPainter painter;

	protected JTabbedPane viewTabs, workTabs;

	protected boolean showLineNumbers;

	protected UndoableEditHandler undoHandler;

	protected DocumentTracker documentTracker;

	protected LineNumbersAnnotatedScrollPane textPaneLineNumbers;

	protected Vector<UndoableEditListener> undoableEditListeners;
	protected Vector<PropertyChangeListener> propertyChangeListeners;

	protected boolean dirty, open;

	protected Timer parseTimer;

	protected Component sourcePanel;

	protected Object selectTag;

	protected Region findScope;

	protected int findOffset, findLength;

	protected boolean refresh;

	protected JTree docTree;

	public AbstractEditor(Project project, DocumentDescriptor dd,
			boolean integrated) {
		this.undoableEditListeners = new Vector<UndoableEditListener>(2, 2);
		this.propertyChangeListeners = new Vector<PropertyChangeListener>(2, 2);

		this.project = project;
		this.integrated = integrated;
		docInfo = dd;

		if (project != null) {
			editorConfig = (EditorConfiguration) project
					.getProjectConfiguration().getEditorConfiguration(
							docInfo.getMimeType());
		}

		init();
	}

	protected void init() {
		painter = new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHT_COLOR);;

		if (integrated) {
			workTabs = new JTabbedPane(JTabbedPane.TOP);
			viewTabs = new JTabbedPane(JTabbedPane.BOTTOM);
		}

		this.showLineNumbers = true;
	} //

	protected void setTabs(JTextPane tp, int charactersPerTab) {
		FontMetrics fm = tp.getFontMetrics(tp.getFont());
		int charWidth = fm.charWidth('w');
		int tabWidth = charWidth * charactersPerTab;

		TabStop[] tabs = new TabStop[16];

		for (int j = 0; j < tabs.length; j++) {
			int tab = j + 1;
			tabs[j] = new TabStop(tab * tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = tp.getDocument().getLength();
		tp.getStyledDocument().setParagraphAttributes(0, length, attributes,
				false);
	}

	protected void normalizeNewLine(StringBuffer sb) {
		for (int i = 0; i < sb.length() - 1; i++) {
			char c = sb.charAt(i);
			char n = sb.charAt(i + 1);

			if (c == '\r' && n == '\n') {
				sb.replace(i, i + 1, "");
			}
		}
	}

	protected void readDocumentFromFile(File file, Document doc) {
		// so just set text
		BufferedReader ir = null;
		try {
			ir = new BufferedReader(new FileReader(file));

			char[] buf = new char[128];

			int nread = 0;
			int offset = 0;

			StringBuffer sb = new StringBuffer();
			while ((nread = ir.read(buf, 0, buf.length)) > 0) {
				sb.append(buf, 0, nread);
				normalizeNewLine(sb);
				String str = sb.toString();
				doc.insertString(offset, str, null);
				offset += str.length();
				sb.setLength(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ir != null)
					ir.close();
			} catch (IOException e) {

			}
		}
	}

	protected File getDocumentFile() {
		File file = null;
		try {
			file = new File(docInfo.getPath());
			String fileName = docInfo.getName();
			file = new File(file, fileName);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			file = null;
		}
		return file;
	}

	public void showDocument() {
		createDocument();

		// call method to parse document and apply style attributes
		parseDocument();

		if (docTree != null)
			setProperty(IDEditor.PROP_OUTLINE_VIEW, null, docTree);

		// update save status
		updateSaveStatus();
	}

	protected StyleContext createStyleContext() {
		return new DefaultStyleContext(null);
	}

	protected void createDocument() {
		if (!(textPane.getEditorKit() instanceof DefaultStyledEditorKit)) {

			int charactersPerTab = (project != null
					&& project.getProjectConfiguration() != null ? Integer
					.valueOf(project.getProjectConfiguration().getTabsize())
					: XMLEditorConfiguration.DEFAULT_CHARS_PER_TAB);

			DefaultStyledEditorKit ek = new DefaultStyledEditorKit(
					(editorConfig != null ? editorConfig.getStyleContext()
							: createStyleContext()));

			textPane.setEditorKit(ek);
			setTabs(textPane, charactersPerTab);

			Document doc = textPane.getDocument();
			readDocumentFromFile(getDocumentFile(), doc);

			doc.addDocumentListener(documentTracker = createDocumentTracker());
			doc.addUndoableEditListener(undoHandler = createUndoHandler());

			Integer tabSize = (project != null ? Integer.getInteger(project
					.getProjectConfiguration().getTabsize()) : new Integer(1));
			doc.putProperty(PlainDocument.tabSizeAttribute, tabSize);
		}
	}

	protected DocumentTracker createDocumentTracker() {
		return new DocumentTracker();
	}

	protected UndoableEditHandler createUndoHandler() {
		return new UndoableEditHandler();
	}

	protected abstract void parseDocument();

	protected void setFeature(String name, boolean value) {
		PropertyChangeEvent pe = new PropertyChangeEvent(this, name, null,
				new Boolean(value));
		for (int i = 0; i < propertyChangeListeners.size(); i++) {
			PropertyChangeListener lis = propertyChangeListeners.elementAt(i);
			lis.propertyChange(pe);
		}
	}

	protected void setProperty(String name, Object oldValue, Object newValue) {
		PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue,
				newValue);
		for (int i = 0; i < propertyChangeListeners.size(); i++) {
			PropertyChangeListener lis = (PropertyChangeListener) propertyChangeListeners
					.elementAt(i);
			lis.propertyChange(pe);
		}
	}

	public boolean openDocument() {
		if (open) {
			setProperty(IDEditor.PROP_EDITOR_VIEW, viewTabs, viewTabs);
			return true;
		}

		return setupDocument();
	}

	public void updateSaveStatus() {
		setProperty(IDEditor.PROP_SAVE_STATUS, null, null);
	}

	protected boolean setupDocument() {
		File docFile = getDocumentFile();

		if (project != null) {
			File bakFile = new File(project.getBackupPath() + File.separator
					+ docInfo.getName());
			if (project != null && bakFile.exists()
					&& bakFile.lastModified() > docFile.lastModified()) {
				int option = JOptionPane
						.showConfirmDialog(sourcePanel,
								LocalizedResources.applicationResources
										.getString("open.backup.file"),
								LocalizedResources.applicationResources
										.getString("backup.check"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					dirty = true;
					docFile = bakFile;
				}
			}
		}

		initDocTree();
		initDocViews();

		setFeature("findButton", true);
		setFeature("findMenuItem", true);

		updateSaveStatus();

		return true;
	}

	protected abstract void initDocTree();

	protected void initDocViews() {
		int tabCount = (viewTabs == null ? 0 : viewTabs.getTabCount());
		if (tabCount > 0) {
			for (int i = 0; i < tabCount; i++) {
				viewTabs.remove(0);
			}
		}

		initTextView();

		addDocumentToIde();

		showDocument();
	}

	protected EditorPopup createEditorPopup() {
		return new EditorPopup(docInfo.getName());
	}

	protected void addDocumentAction() {

	}

	protected void caretMoved() {
		Document doc = textPane.getDocument();
		Element root = doc.getDefaultRootElement();
		int where = textPane.getCaretPosition();
		int line = root.getElementIndex(where);
		int col = where - root.getElement(line).getStartOffset();

		setProperty(IDEditor.PROP_LINE_COL, null, new Point(++line, ++col));
	}

	protected void initTextView() {
		textPane = new MichiganTextPane() {
			private static final long serialVersionUID = 8160707566445919845L;

			public void paint(Graphics g) {
				super.paint(g);
				if (textPaneLineNumbers != null) {
					textPaneLineNumbers.repaintLineNumbers();
				}
			}
		};

		if (project != null) {
			textPane
					.setKeymap(com.nubean.michide.KeyMapFactory
							.createKeymap(project.getProjectConfiguration()
									.getKeymap()));
		}

		addDocumentAction();

		if (integrated) {

			textPane.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()
							|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {
						EditorPopup popup = createEditorPopup();
						if (popup != null)
							popup.show(textPane, e.getX(), e.getY());
					}
				}
			});
		}

		textPane.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {

				JTextPane tp = (JTextPane) e.getSource();
				int start = tp.getSelectionStart();
				int end = tp.getSelectionEnd();

				clear();
				if (end > start) {
					setFeature("cutButton", true);
					setFeature("copyButton", true);
					setFeature("cutMenuItem", true);
					setFeature("copyMenuItem", true);
				} else {
					setFeature("cutButton", false);
					setFeature("copyButton", false);
					setFeature("cutMenuItem", false);
					setFeature("copyMenuItem", false);
				}
				caretMoved();
			}
		});

		JScrollPane sp = new JScrollPane(textPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setWheelScrollingEnabled(true);

		textPaneLineNumbers = new LineNumbersAnnotatedScrollPane(textPane, sp);
		sourcePanel = textPaneLineNumbers;
		if (viewTabs != null)
			viewTabs.add(LocalizedResources.applicationResources
					.getString("source"), textPaneLineNumbers);
		textPaneLineNumbers.showLineNumbers(showLineNumbers);
	}

	protected void addErrorView(DefaultErrorHandler errorHandler) {
		if (errorHandler != null) {
			if (workTabs != null) {
				String title = LocalizedResources.applicationResources
						.getString("error.log");

				boolean found = false;
				for (int i = 0; i < workTabs.getTabCount(); i++) {
					if (title.equals(workTabs.getTitleAt(i))) {
						ProblemsView xview = (ProblemsView) workTabs
								.getComponentAt(i);
						xview.setErrorHandler(errorHandler);
						xview.setTextPane(textPane);
						workTabs.setSelectedIndex(i);
						found = true;
						break;
					}
				}

				if (!found) {
					ProblemsView xview = new ProblemsView(errorHandler,
							textPane);
					workTabs.addTab(title, null, xview, null);
				}
			}
		}
	}

	protected void addDocumentToIde() {
		if (open)
			return;

		open = true;

		setProperty(IDEditor.PROP_MESSAGE_VIEW, null, workTabs);
		setProperty(IDEditor.PROP_EDITOR_VIEW, null, viewTabs);
	}

	protected boolean openDoc() {
		if (open)
			return true;

		boolean success = true;

		try {
			success = setupDocument();
		} catch (Exception e) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("document.open.failed"));
			success = false;
		}
		return success;
	}

	public void fireUndoableEditEvent(UndoableEditEvent e) {
		for (int i = 0; i < undoableEditListeners.size(); i++) {
			UndoableEditListener lis = undoableEditListeners.elementAt(i);
			lis.undoableEditHappened(e);
		}
	}

	protected void addHighlight(int offset, int len) {

		Highlighter dh = textPane.getHighlighter();

		try {
			if (textPane != null) {
				textPane.setSelectionStart(offset);
				textPane.setSelectionEnd(offset + len);
			}
			if (selectTag != null)
				dh.removeHighlight(selectTag);
			selectTag = dh.addHighlight(offset, offset + len, painter);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	protected void removeAllHighlights() {
		Highlighter dh = textPane.getHighlighter();
		dh.removeAllHighlights();
		selectTag = null;
	}

	public boolean openDocumentAsText() {
		return openDocument();
	}

	public boolean newDocument() {
		boolean ret = openDocument();
		if (ret) {
			dirty = true;
			updateSaveStatus();
		}
		return ret;
	}

	public int closeDocument() {
		int option = JOptionPane.YES_OPTION;
		if (!open)
			return option;

		if (dirty && !docInfo.getDoNotSave()) {
			option = JOptionPane.showConfirmDialog(sourcePanel, docInfo
					.getName()
					+ LocalizedResources.applicationResources
							.getString("document.save"),
					LocalizedResources.applicationResources.getString("save"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, IconLoader.saveIcon);
			if (option == JOptionPane.YES_OPTION) {
				saveDocument();
			} else if (option == JOptionPane.CANCEL_OPTION) {
				return option;
			}
		}

		if (project != null) {
			File file = new File(project.getBackupPath(), docInfo.getName());
			if (file.exists())
				file.delete();
		}

		removeDocumentFromIde();
		return option;
	}

	protected void removeDocumentFromIde() {
		open = false;
		setProperty(IDEditor.PROP_EDITOR_VIEW, viewTabs, null);
		setProperty(IDEditor.PROP_MESSAGE_VIEW, workTabs, null);

		updateSaveStatus();
		docInfo.setEditor(null);
	}

	public void saveDocumentAs() {
		File defaultPath = new File(docInfo.getPath(), docInfo.getName());
		JFileChooser fileChooser = new JFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("save.as"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("save.document.as"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		} else
			return;

		try {
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			PrintWriter writer = new PrintWriter(osw);
			javax.swing.text.Document doc = textPane.getDocument();
			textPane.getEditorKit().write(writer, doc, 0, doc.getLength());
			writer.close();
		} catch (Exception e) {

			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("save.document.failed"));
		}
	}

	public void saveDocument() {
		if (!dirty)
			return;

		File file = new File(docInfo.getPath());
		try {
			if (!file.exists())
				file.mkdirs();

			file = new File(file, docInfo.getName());
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			PrintWriter writer = new PrintWriter(osw);
			javax.swing.text.Document doc = textPane.getDocument();
			textPane.getEditorKit().write(writer, doc, 0, doc.getLength());
			writer.close();
			if (project != null) {
				file = new File(project.getBackupPath(), docInfo.getName());
				if (file.exists())
					file.delete();
			}

			dirty = false;
			updateSaveStatus();

		} catch (Exception e) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("save.document.failed"));
		}
	}

	public void backupDocument() {
		if (!dirty || project == null)
			return;

		File file = new File(project.getBackupPath());
		try {
			if (!file.exists())
				file.mkdirs();

			file = new File(file, docInfo.getName());
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			PrintWriter writer = new PrintWriter(osw);
			javax.swing.text.Document doc = textPane.getDocument();
			textPane.getEditorKit().write(writer, doc, 0, doc.getLength());
			writer.close();
		} catch (Exception e) {
		}
	}

	public void renameDocument() {
		File defaultPath = new File(docInfo.getPath(), docInfo.getName());
		JFileChooser fileChooser = new JFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("rename"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources.getString("rename"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			defaultPath.renameTo(file);
			docInfo.setPath(file.getParentFile().getAbsolutePath());
			docInfo.setName(file.getName());
		}
	}

	public void cut() {
		textPane.cut();

		setFeature("cutButton", false);
		setFeature("cutMenuItem", false);
		setFeature("copyButton", false);
		setFeature("copyMenuItem", false);

	}

	public void paste() {
		textPane.paste();
	}

	public void copy() {
		textPane.copy();

		setFeature("cutButton", false);
		setFeature("cutMenuItem", false);
		setFeature("copyButton", false);
		setFeature("copyMenuItem", false);

	}

	public int find(String text, boolean matchCase, boolean wrap,
			boolean forward, boolean regex, boolean allscope,
			boolean incremental) {

		int retval = -1;

		if (!regex) {
			if (allscope) {
				retval = findText(text, matchCase, wrap, forward, incremental);
			} else {
				retval = findTextInSelectedLines(text, matchCase, wrap,
						forward, incremental);
			}

		} else {
			if (allscope) {
				retval = findRegularExpression(text, matchCase, wrap, forward,
						incremental);
			} else {
				retval = findRegularExpressionInSelectedLines(text, matchCase,
						wrap, forward, incremental);
			}
		}

		if (retval >= 0) {
			setFeature("findAgainButton", true);
			setFeature("replaceButton", true);
		} else {
			setFeature("findAgainButton", false);
			setFeature("replaceButton", false);
		}
		return retval;

	}

	protected int findText(String text, boolean matchCase, boolean wrap,
			boolean forward, boolean incremental) {

		if (text == null || text.length() == 0)
			return -1;

		int start = (!incremental ? textPane.getCaretPosition() : findOffset);
		javax.swing.text.Document doc = textPane.getDocument();

		int len = doc.getLength();
		if (len == 0 || start > len || start < 0)
			return -1;

		int slen = text.length();
		try {

			if (forward) {
				for (int i = start; i <= len - slen; i++) {
					String tmp = doc.getText(i, slen);
					if (matchCase) {
						if (tmp.equals(text)) {
							addHighlight(i, slen);
							findOffset = i;
							findLength = slen;
							return i;
						}
					} else {
						if (tmp.equalsIgnoreCase(text)) {
							addHighlight(i, slen);
							findOffset = i;
							findLength = slen;
							return i;
						}
					}
				}

				if (wrap) {
					textPane.setCaretPosition(0);
					return findText(text, matchCase, false, forward,
							incremental);
				}
			} else {

				int selStart = textPane.getSelectionStart();
				int selEnd = textPane.getSelectionEnd();

				if (selStart == (start - slen) && selEnd == start) {
					start = start - slen;
				}
				for (int i = start - slen; i >= 0; i--) {
					String tmp = doc.getText(i, slen);
					if (matchCase) {
						if (tmp.equals(text)) {
							addHighlight(i, slen);
							findOffset = i;
							findLength = slen;
							return i;
						}
					} else {
						if (tmp.equalsIgnoreCase(text)) {
							addHighlight(i, slen);
							findOffset = i;
							findLength = slen;
							return i;
						}
					}
				}
				if (wrap) {
					textPane.setCaretPosition(len);
					return findText(text, matchCase, false, forward,
							incremental);
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	protected int findTextInSelectedLines(String text, boolean matchCase,
			boolean wrap, boolean forward, boolean incremental) {

		if (text == null || text.length() == 0)
			return -1;

		int slen = text.length();

		int start = (!incremental ? textPane.getCaretPosition() : findOffset);
		int selStart = textPane.getSelectionStart();
		int selEnd = textPane.getSelectionEnd();

		if (findScope != null) {
			selStart = findScope.offset;
			selEnd = findScope.offset + findScope.length;
		}

		if (selStart == selEnd) {
			return -1;
		}

		if ((start < selStart) || (start >= selEnd)) {
			if (forward)
				start = selStart;
			else
				start = selEnd;
		}

		javax.swing.text.Document doc = textPane.getDocument();
		int len = doc.getLength();
		if (len == 0)
			return -1;

		try {

			if (forward) {
				for (int i = start; i <= selEnd - slen; i++) {
					String tmp = doc.getText(i, slen);
					if (matchCase) {
						if (tmp.equals(text)) {
							findOffset = i;
							findLength = slen;
							return i;
						}
					} else {
						if (tmp.equalsIgnoreCase(text)) {
							addHighlight(i, slen);
							findOffset = i;
							findLength = slen;
							return i;
						}
					}
				}

				if (wrap) {
					textPane.setCaretPosition(selStart);
					return findTextInSelectedLines(text, matchCase, false,
							forward, incremental);
				}
			} else {
				for (int i = start - slen; i >= selStart; i--) {
					String tmp = doc.getText(i, slen);
					if (matchCase) {
						if (tmp.equals(text)) {
							findOffset = i;
							findLength = slen;
							return i;
						}
					} else {
						if (tmp.equalsIgnoreCase(text)) {
							addHighlight(i, slen);
							findOffset = i;
							findLength = slen;
							return i;
						}
					}
				}
				if (wrap) {
					textPane.setCaretPosition(selEnd);
					return findTextInSelectedLines(text, matchCase, false,
							forward, incremental);
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	protected int findRegularExpression(String regex, boolean matchCase,
			boolean wrap, boolean forward, boolean incremental) {

		if (regex == null || regex.length() == 0)
			return -1;

		int start = (!incremental ? textPane.getCaretPosition() : findOffset);
		javax.swing.text.Document doc = textPane.getDocument();

		int len = doc.getLength();
		if (len == 0 || start > len || start < 0)
			return -1;

		try {

			if (forward) {
				String tmp = doc.getText(start, len - start);
				if (matchCase) {
					Pattern p = Pattern.compile(regex);

					Matcher m = p.matcher(tmp);
					if (m.find()) {
						findOffset = start + m.start();
						findLength = m.end() - m.start();
						addHighlight(findOffset, findLength);
						return findOffset;
					}
				} else {
					Pattern p = Pattern.compile(regex.toLowerCase());
					Matcher m = p.matcher(tmp.toLowerCase());
					if (m.find()) {
						findOffset = start + m.start();
						findLength = m.end() - m.start();
						addHighlight(findOffset, findLength);
						return findOffset;
					}
				}

				if (wrap) {
					textPane.setCaretPosition(0);
					return findRegularExpression(regex, matchCase, false,
							forward, incremental);
				}
			} else {

				for (int i = start - 1; i >= 0; i--) {
					String tmp = doc.getText(i, start - i);

					if (matchCase) {
						Pattern p = Pattern.compile(regex);

						Matcher m = p.matcher(tmp);
						if (m.find()) {
							findOffset = i + m.start();
							findLength = m.end() - m.start();
							addHighlight(findOffset, findLength);
							return findOffset;
						}
					} else {
						Pattern p = Pattern.compile(regex.toLowerCase());
						Matcher m = p.matcher(tmp.toLowerCase());
						if (m.find()) {
							findOffset = i + m.start();
							findLength = m.end() - m.start();
							addHighlight(findOffset, findLength);
							return findOffset;
						}
					}

					if (wrap) {
						textPane.setCaretPosition(len);
						return findRegularExpression(regex, matchCase, false,
								forward, incremental);
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	protected int findRegularExpressionInSelectedLines(String regex,
			boolean matchCase, boolean wrap, boolean forward,
			boolean incremental) {

		if (regex == null || regex.length() == 0)
			return -1;

		int start = (!incremental ? textPane.getCaretPosition() : findOffset);
		int selStart = textPane.getSelectionStart();
		int selEnd = textPane.getSelectionEnd();

		if (findScope != null) {
			selStart = findScope.offset;
			selEnd = findScope.offset + findScope.length;
		}

		if (selStart == selEnd) {
			return -1;
		}

		if ((start < selStart) || (start >= selEnd)) {
			if (forward)
				start = selStart;
			else
				start = selEnd;
			wrap = false;
		}

		javax.swing.text.Document doc = textPane.getDocument();

		int len = doc.getLength();
		if (len == 0)
			return -1;

		try {

			if (forward) {
				String tmp = doc.getText(start, selEnd - start);
				if (matchCase) {
					Pattern p = Pattern.compile(regex);

					Matcher m = p.matcher(tmp);
					if (m.find()) {
						findOffset = start + m.start();
						findLength = m.end() - m.start();
						addHighlight(findOffset, findLength);
						return findOffset;
					}
				} else {
					Pattern p = Pattern.compile(regex.toLowerCase());
					Matcher m = p.matcher(tmp.toLowerCase());
					if (m.find()) {
						findOffset = start + m.start();
						findLength = m.end() - m.start();
						addHighlight(findOffset, findLength);
						if (!incremental)
							textPane.setCaretPosition(findOffset + findLength);
						return findOffset;
					}
				}

				if (wrap) {
					textPane.setCaretPosition(selStart);
					return findRegularExpression(regex, matchCase, false,
							forward, incremental);
				}
			} else {

				for (int i = start - 1; i >= 0; i--) {
					String tmp = doc.getText(i, start - i);

					if (matchCase) {
						Pattern p = Pattern.compile(regex);

						Matcher m = p.matcher(tmp);
						if (m.find()) {
							findOffset = i + m.start();
							findLength = m.end() - m.start();
							addHighlight(findOffset, findLength);
							return findOffset;
						}
					} else {
						Pattern p = Pattern.compile(regex.toLowerCase());
						Matcher m = p.matcher(tmp.toLowerCase());
						if (m.find()) {
							findOffset = i + m.start();
							findLength = m.end() - m.start();
							addHighlight(findOffset, findLength);
							return findOffset;
						}
					}

					if (wrap) {
						textPane.setCaretPosition(selEnd);
						return findRegularExpression(regex, matchCase, false,
								forward, incremental);
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void replace(String text) {
		if (findLength == 0) {
			return;
		}
		javax.swing.text.Document doc = textPane.getDocument();
		try {
			doc.remove(findOffset, findLength);
			doc.insertString(findOffset, text, null);
		} catch (javax.swing.text.BadLocationException be) {
			be.printStackTrace();
		}
	}

	public void clear() {
		Highlighter dh = textPane.getHighlighter();
		if (dh != null) {
			if (selectTag != null) {
				dh.removeHighlight(selectTag);
				selectTag = null;
			}
		}
	}

	public void setRefresh(boolean refresh) {
	}

	public boolean getRefresh() {
		return false;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isOpen() {
		return open;
	}

	public Object getEditorView() {
		return viewTabs;
	}

	public Object getMessagesView() {
		return workTabs;
	}

	public Object getOutlineView() {
		return docTree;
	}

	public Object getSourceView() {
		return sourcePanel;
	}

	public Object getDesignView() {
		return null;
	}

	public void print() {
		try {
			saveDocument();
			File file = new File(docInfo.getPath(), docInfo.getName());
			new BasicPrint(file, DocFlavor.INPUT_STREAM.AUTOSENSE);
		} catch (Exception e) {
			if (integrated)
				JOptionPane.showMessageDialog(textPane,
						LocalizedResources.applicationResources
								.getString("printing.error:")
								+ ":\n" + e,
						LocalizedResources.applicationResources
								.getString("printing.error"),
						JOptionPane.ERROR_MESSAGE);
		}
	}

	public void addUndoableEditListener(UndoableEditListener listener) {
		if (listener != null && !undoableEditListeners.contains(listener))
			undoableEditListeners.add(listener);
	}

	public void removeUndoableEditListener(UndoableEditListener listener) {
		if (listener != null && !undoableEditListeners.contains(listener))
			undoableEditListeners.remove(listener);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (listener != null && !propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.add(listener);
		}
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listener != null) {
			propertyChangeListeners.remove(listener);
		}
	}

	public DocumentDescriptor getDocumentDescriptor() {
		return docInfo;
	}

	public boolean isTextMode() {
		return true;
	}

	public void showLineNumbers(boolean show) {
		this.showLineNumbers = show;
		textPaneLineNumbers.showLineNumbers(show);
	}

	public boolean isEditable() {
		return true;
	}

	public void setSelection(int offset, int length) {
		if (textPane != null) {
			textPane.setSelectionStart(offset);
			textPane.setSelectionEnd(offset + length);
		}
	}

	public void setScope(Region region) {
		this.findScope = region;
	}

	public Region getScope() {
		return findScope;
	}

	public Region getLineSelection() {
		if (textPane != null) {
			int start = textPane.getSelectionStart();
			int end = textPane.getSelectionEnd();
			javax.swing.text.Document doc = textPane.getDocument();

			if (end > start) {

				int startline = doc.getDefaultRootElement().getElementIndex(
						start);
				int endline = doc.getDefaultRootElement().getElementIndex(end);

				return new Region(startline, endline - startline + 1);
			}
			int startline = doc.getDefaultRootElement().getElementIndex(
					getCaretPosition());
			return new Region(startline, 1);

		}

		return null;
	}

	public Region getSelection() {
		if (textPane != null) {
			int start = textPane.getSelectionStart();
			int end = textPane.getSelectionEnd();

			if (end > start) {
				return new Region(start, end - start);
			}

			start = getCaretPosition();
			return new Region(start, 0);
		}

		return null;
	}

	public String getSelectionText() {
		if (textPane != null) {
			return textPane.getSelectedText();
		}

		return null;
	}

	public void beginFindReplaceSession() {
		findScope = null;
		findLength = 0;
		findOffset = textPane.getCaretPosition();
	}

	public void endFindReplaceSession() {
		findScope = null;
		findOffset = findLength = 0;
	}

	public void beginUndoEditSession() {
		undoHandler.beginSession();
	}

	public void endUndoEditSession() {
		undoHandler.endSession();
	}

	public int getCaretPosition() {
		int pos = 0;

		if (textPane != null) {
			pos = textPane.getCaretPosition();
		}

		return pos;
	}

	public void setCaretPosition(int offset) {
		try {
			textPane.setCaretPosition(offset);
		} catch (Exception e) {
		}
	}

	public void toggleTextMode() {

	}

	protected CompoundEdit createCompoundEdit() {
		return new CompoundEdit();
	}

	protected void documentChanged(DocumentEvent e) {
		dirty = true;
		updateSaveStatus();
	}

	protected void startParseTimer() {
		if (parseTimer == null) {
			ActionListener timerListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new AsyncParsing());
				}
			};

			parseTimer = new Timer(2000, timerListener);
			parseTimer.setRepeats(false);
			parseTimer.start();
		} else {
			parseTimer.restart();
		}
	}

	protected Point getDialogPopupLocation() {
		Point p = new Point(0, 0);

		if (sourcePanel != null) {
			sourcePanel.getLocation();

			p.x += (sourcePanel.getWidth() / 2);
			p.y += (sourcePanel.getHeight() / 2);
			EditUtils.convertPointToScreen(p, sourcePanel);
		}
		return p;
	}

	protected Element findElementWithNode(DefaultElementNode node) {

		if (docTree.getModel() == null || node == null)
			return null;

		Object root = docTree.getModel().getRoot();

		Stack<Object> elementStack = new Stack<Object>();
		HashMap<Object, Stack<Integer>> stackMap = new HashMap<Object, Stack<Integer>>(
				17, 0.85f);

		elementStack.push(root);

		while (!elementStack.empty()) {
			DefaultElementNode element = (DefaultElementNode) elementStack
					.peek();

			if (element == node)
				return element;

			int count = element.getElementCount();

			if (count == 0) {
				elementStack.pop();
				continue;
			} else {
				Stack<Integer> indexStack = stackMap.get(element);
				if (indexStack == null) {
					indexStack = new Stack<Integer>();
					stackMap.put(element, indexStack);
					elementStack.push(element.getElement(0));
					indexStack.push(new Integer(1));
					continue;
				} else {
					int top = ((Integer) indexStack.pop()).intValue();
					if (top < count) {
						elementStack.push(element.getElement(top));
						indexStack.push(new Integer(top + 1));
						continue;
					} else {
						Object key = elementStack.pop();
						stackMap.remove(key);
						continue;
					}
				}
			}
		}

		return null;
	}

	protected class DocTreeListener implements TreeSelectionListener {

		public DocTreeListener() {

		}

		public void valueChanged(TreeSelectionEvent e) {

			TreePath p = e.getNewLeadSelectionPath();
			if (p != null) {
				DefaultElementNode lnode = (DefaultElementNode) p
						.getLastPathComponent();
				if (lnode != null) {
					Element ele = findElementWithNode(lnode);
					if (ele != null) {
						int start = ele.getStartOffset();
						int end = ele.getEndOffset();

						addHighlight(start, (end - start));
					}
				}
			}
		}
	}

	protected class DocumentTracker implements DocumentListener {

		protected void processEvent(DocumentEvent e) {
			documentChanged(e);
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			processEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			processEvent(e);
		}

	}

	protected class UndoableEditHandler implements UndoableEditListener {

		protected CompoundEdit cedit;

		protected int recursion;

		public void undoableEditHappened(UndoableEditEvent e) {
			if (recursion == 0) {
				fireUndoableEditEvent(e);
				return;
			}

			if (cedit == null) {
				cedit = createCompoundEdit();
			}

			cedit.addEdit(e.getEdit());
		}

		public void endSession() {
			if (--recursion == 0 && cedit != null) {
				Document doc = textPane.getDocument();
				cedit.end();
				UndoableEditEvent ue = new UndoableEditEvent(doc, cedit);
				cedit = null;
				fireUndoableEditEvent(ue);
			}
		}

		public void beginSession() {
			recursion++;
		}
	}

	protected class ProblemsView extends JScrollPane {
		private static final long serialVersionUID = 6587807706632472934L;

		protected DefaultErrorHandler errorHandler;

		protected JTextPane errorTextPane;

		protected JTable table;

		public ProblemsView(DefaultErrorHandler eh, JTextPane tp) {
			super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

			this.errorTextPane = tp;
			this.errorHandler = eh;

			table = new JTable() {
				private static final long serialVersionUID = 3373689087559671579L;

				public JToolTip createToolTip() {
					return TextPaneToolTip.sharedInstance;
				}
			};

			table.setDefaultRenderer(Object.class, new ProblemCellRenderer());
			table.setModel(errorHandler);

			TableColumnModel columnModel = table.getTableHeader()
					.getColumnModel();
			TableColumn column = columnModel.getColumn(0);
			column.setPreferredWidth(20);

			column = columnModel.getColumn(1);
			column.setPreferredWidth(600);

			column = columnModel.getColumn(2);
			column.setPreferredWidth(40);

			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;

							int first = e.getFirstIndex();
							int last = e.getLastIndex();

							int row = -1;

							for (int i = first; i <= last; i++) {
								if (table.isRowSelected(i)) {
									row = i;
									break;
								}
							}
							ParseProblem pp = (row >= 0 ? errorHandler
									.getParseProblem(row) : null);
							if (pp != null && errorTextPane != null) {
								Document doc = errorTextPane.getDocument();
								Element root = doc.getDefaultRootElement();
								if (root != null) {
									Element le = root
											.getElement(pp.getLine() - 1);
									if (le != null) {
										addHighlight(le.getStartOffset(), pp
												.getColumn() - 1);
									}
								}
							}
						}
					});

			setViewportView(table);
		}

		public void setErrorHandler(DefaultErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
			table.setModel(errorHandler);
			TableColumnModel columnModel = table.getTableHeader()
					.getColumnModel();
			TableColumn column = columnModel.getColumn(0);
			column.setPreferredWidth(20);

			column = columnModel.getColumn(1);
			column.setPreferredWidth(600);

			column = columnModel.getColumn(2);
			column.setPreferredWidth(40);
		}

		public void setTextPane(JTextPane tp) {
			this.errorTextPane = tp;
		}

	}

	protected class ProblemCellRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = 7635542141117142343L;

		public ProblemCellRenderer() {
			setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean selected, boolean hasFocus, int row,
				int column) {

			if (column == 0 && value instanceof Integer) {
				switch (((Integer) value).intValue()) {
				case DefaultErrorHandler.ERROR:
				case DefaultErrorHandler.FATAL:
					setIcon(IconLoader.errorIcon);
					break;
				default:
					setIcon(IconLoader.warningIcon);
					break;
				}
				setText("");
			} else {
				setText(value.toString());
				setToolTipText(value.toString());
				setIcon(null);
			}
			return this;

		}

	}

	protected class EditorPopup extends JPopupMenu {

		private static final long serialVersionUID = -8840375277135112194L;

		public EditorPopup(String title) {
			super(title);
			init();
		}

		protected void init() {
			JMenuItem mi = null;

			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("save"), IconLoader.saveIcon);
			mi.setMnemonic('s');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					saveDocument();
				}
			});
			add(mi);

			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("save.as..."), IconLoader.saveAsIcon);
			mi.setMnemonic('a');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					saveDocumentAs();
				}
			});
			add(mi);

			this.addSeparator();

			mi = new JCheckBoxMenuItem(LocalizedResources.applicationResources
					.getString("show.line.numbers"));
			mi.setSelected(showLineNumbers);
			mi.setMnemonic('l');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					showLineNumbers(!showLineNumbers);
				}
			});
			add(mi);

			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("go.to"));

			mi.setMnemonic('g');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GotoAction action = new GotoAction(textPane);
					action.actionPerformed(e);
					setVisible(false);
				}
			});
			add(mi);

			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("select.none"));
			mi.setMnemonic('n');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					removeAllHighlights();
				}
			});
			add(mi);
			this.addSeparator();

			mi = new JMenuItem(LocalizedResources.applicationResources
					.getString("close"), IconLoader.closeIcon);
			mi.setMnemonic('s');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					closeDocument();
				}
			});
			add(mi);

		}

	}

	protected class AsyncParsing implements Runnable {
		public AsyncParsing() {
		}

		public void run() {
			setProperty(IDEditor.PROP_STATUS, null, "");
			if (integrated)
				textPane.setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));
			parseDocument();
			if (integrated)
				textPane.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
