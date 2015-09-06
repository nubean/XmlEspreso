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

package com.nubean.michxml.editor;

import java.util.*;

import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.ParseProblem;
import com.nubean.michbase.DefaultErrorHandler;
import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.XMLBuilder;
import com.nubean.michbase.design.CloseTabbedPaneUI;
import com.nubean.michbase.design.DefaultTreeCellRenderer;
import com.nubean.michbase.design.TextPaneToolTip;
import com.nubean.michbase.dialog.DefaultElementChooser;
import com.nubean.michbase.editor.IDEditor;
import com.nubean.michbase.project.Project;
import com.nubean.michxml.parser.XML10ParserConstants;
import com.nubean.michxml.parser.ParseException;
import com.nubean.michxml.parser.Token;
import com.nubean.michxml.parser.TokenMgrError;
import com.nubean.michide.*;
import com.nubean.michutil.*;
import com.nubean.michxml.Namespace;
import com.nubean.michxml.ProcInstr;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.SchemaTreeModel;
import com.nubean.michxml.TLD2Schema;
import com.nubean.michxml.TaglibDef;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLCompoundEdit;
import com.nubean.michxml.XMLDefaultStyleContext;
import com.nubean.michxml.XMLDocument;
import com.nubean.michxml.XMLDocumentDescriptor;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.michxml.XMLEditorKit;
import com.nubean.michxml.XMLModel;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLSchema;
import com.nubean.michxml.XMLTreeModel;
import com.nubean.michxml.XMLUndoableEdit;
import com.nubean.michxml.elem.*;
import com.nubean.michxml.parser.XML10Parser;
import com.nubean.michxml.design.ComplexTypePanel;
import com.nubean.michxml.design.SimpleTypePanel;
import com.nubean.michxml.design.attr.AttributePropertySheet;
import com.nubean.michxml.dialog.ElementChooser;
import com.nubean.michxml.dialog.ElementInput;
import com.nubean.michxml.dialog.NamespaceInput;
import com.nubean.michxml.dialog.ProcInstrInput;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import org.xml.sax.*;
import java.util.regex.*;

import org.xml.sax.helpers.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import javax.print.*;

import java.io.*;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.*;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.w3c.dom.*;

// Basic GUI components
import javax.swing.*;
import javax.swing.Timer; // GUI support classes
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// For creating a TreeModel

import javax.swing.tree.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

public class XMLEditor implements XMLAbstractEditor, IDEditor {

	private static final String ppxsl = "<xsl:stylesheet version='1.0' "
			+ "xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
			+ "<xsl:output method='xml' indent='yes' />"
			+ "<xsl:template match='*'>" + "<xsl:copy>"
			+ "<xsl:copy-of select='@*' />" + "<xsl:apply-templates />"
			+ "</xsl:copy>" + "</xsl:template>"
			+ "<xsl:template match='comment()|processing-instruction()'>"
			+ "<xsl:copy />" + "</xsl:template>" + "</xsl:stylesheet>";

	private static final String[] keywords = new String[] { "ALL", "NONE",
			"RMD", "version", "ANY", "EMPTY", "ID", "IDREF", "IDREFS",
			"ENTITY", "ENTITIES", "NMTOKEN", "NMTOKENS", "NOTATION", "CDATA",
			"NDATA", "IGNORE", "INTERNAL", "INCLUDE", "PUBLIC", "SYSTEM" };

	private static String MISSING_END_TAG_ERROR = "must be terminated by the matching end-tag";
	private static String COMMENT, PROC_INSTR, CDATA, TAGLIB;

	private int findOffset, findLength;

	private DefaultHighlighter.DefaultHighlightPainter painter;
	private DefaultHighlighter.DefaultHighlightPainter xpainter;

	private Object selectTag;

	private Vector<Object> multipleSelectTags;

	private JTabbedPane workTabs;

	private boolean locked, auto;

	private boolean integrated, undoInProgress;

	private boolean refresh;

	private boolean standalone;

	private boolean dirty;

	private boolean open;

	private DefaultErrorHandler xmlCheckHandler;

	private JTabbedPane viewTabs;

	private JTextPane textPane, schemaPane;

	private TreePath simpleTypePath, complexTypePath;

	private JSplitPane designSplitPane;

	private ComplexTypePanel complexTypePanel;

	private XMLNode selectedTypeNode, complexTypeNode, simpleTypeNode;

	private JTree xmlTree, designTree;
	private DesignTreePopup designTreePopup;

	private XMLDocumentDescriptor docInfo;

	private boolean askForNamespace;

	private DefaultTreeCellRenderer treeCellRenderer;

	private XMLEditorConfiguration editorConfig;

	private Project project;

	private boolean textMode;

	private Component sourcePanel;

	private Vector<String> anyElements;

	private boolean wellFormed;

	private Region findScope;

	private XMLModel xmlModel;

	private boolean showLineNumbers;

	private ParseException parseException;

	private TokenMgrError tokenMgrError;

	private Token token;

	private String prefix, previous;

	private LineNumbersAnnotatedScrollPane textPaneLineNumbers,
			schemaPaneLineNumbers;

	private Vector<UndoableEditListener> undoableEditListeners = new Vector<UndoableEditListener>(
			2, 2);
	private Vector<PropertyChangeListener> propListener = new Vector<PropertyChangeListener>(
			20, 20);

	private Timer parseTimer;

	private UndoableEditHandler undoHandler;

	private XMLTreeListener xmlTreeListener, designTreeListener;

	private String xPathQuery;

	private Transformer transformer;

	public boolean isTextMode() {
		return textMode;
	}

	private void addDocumentAction() {
		textPane.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
						InputEvent.CTRL_MASK), "documentAction");
		DocumentAction documentAction = new DocumentAction();
		textPane.getActionMap().put("documentAction", documentAction);
	}

	private void addSchemaAction() {
		schemaPane.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
						InputEvent.CTRL_MASK), "schemaAction");
		SchemaAction schemaAction = new SchemaAction();
		schemaPane.getActionMap().put("schemaAction", schemaAction);
	}

	private void addFormatAction() {
		textPane.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK
						| InputEvent.SHIFT_MASK), "formatAction");

		Action formatAction = new AbstractAction() {
			private static final long serialVersionUID = -2448658190253189626L;

			public void actionPerformed(ActionEvent e) {
				format();
			}
		};
		textPane.getActionMap().put("formatAction", formatAction);
	}

	private static void setLocale() {
		COMMENT = LocalizedResources.applicationResources.getString("comment");
		PROC_INSTR = LocalizedResources.applicationResources
				.getString("processing.instruction");
		CDATA = LocalizedResources.applicationResources.getString("cdata");
		TAGLIB = LocalizedResources.applicationResources
				.getString("insert.taglib");
	}

	public boolean isOpen() {
		return open;
	}

	public Object getSourceView() {
		return textPaneLineNumbers;
	}

	public Object getDesignView() {
		return designSplitPane;
	}

	public Object getEditorView() {
		return viewTabs;
	}

	public Object getMessagesView() {
		return workTabs;
	}

	public void clear() {

		Highlighter dh = getActiveTextPane().getHighlighter();
		if (dh != null) {
			if (selectTag != null) {
				dh.removeHighlight(selectTag);
				selectTag = null;
			}

		}
	}

	public void clearXPath() {

		Highlighter dh = getActiveTextPane().getHighlighter();
		if (dh != null) {
			if (multipleSelectTags != null) {
				int count = multipleSelectTags.size();
				for (int i = 0; i < count; i++) {
					dh.removeHighlight(multipleSelectTags.elementAt(i));
				}
				multipleSelectTags.setSize(0);
			}

		}
	}

	public boolean isEditable() {
		return (textPane != null ? textPane.isEditable() : false);
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

	public int getCaretPosition() {
		int pos = 0;

		if (textPane != null) {
			pos = textPane.getCaretPosition();
		}

		return pos;
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

	public Object getOutlineView() {
		return xmlTree;
	}

	public XMLEditor(Project project, DocumentDescriptor dd, boolean integrated) {
		this.project = project;
		this.integrated = integrated;
		docInfo = (XMLDocumentDescriptor) dd;

		treeCellRenderer = new DefaultTreeCellRenderer();
		if (project != null) {
			editorConfig = (XMLEditorConfiguration) project
					.getProjectConfiguration().getEditorConfiguration(
							docInfo.getMimeType());
		}

		init();
	}

	public void showLineNumbers(boolean show) {
		this.showLineNumbers = show;
		textPaneLineNumbers.showLineNumbers(show);
		if (schemaPaneLineNumbers != null)
			schemaPaneLineNumbers.showLineNumbers(show);
	}

	public XMLEditor(XMLEditorConfiguration editorConfig,
			XMLDocumentDescriptor dd, boolean integrated) {
		this.integrated = integrated;
		docInfo = dd;

		treeCellRenderer = new DefaultTreeCellRenderer();
		this.editorConfig = editorConfig;

		init();
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
		if (listener != null && !propListener.contains(listener)) {
			propListener.add(listener);
			setProperty(IDEditor.PROP_TEXT_MODE, Boolean.valueOf(textMode),
					Boolean.valueOf(textMode));
		}
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listener != null) {
			propListener.remove(listener);
		}
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean getRefresh() {
		return refresh;
	}

	public void setTreeSelectionPath(TreePath path) {
		setSelectionPath(path);
		setLocked(true);
		if (xmlTree != null)
			xmlTree.setSelectionPath(path);
		if (designTree != null)
			designTree.setSelectionPath(path);
		setLocked(false);
	}

	public int getTagStyle() {
		return (editorConfig != null ? editorConfig.getTagStyle()
				: XMLAbstractEditor.TAG_INDENT);
	}

	public String getDocType() {
		StringBuffer sb = new StringBuffer();
		if (docInfo.getDtdPublicId() != null
				&& docInfo.getDtdPublicId().trim().length() > 0)
			sb.append(" PUBLIC '").append(docInfo.getDtdPublicId())
					.append("' ");
		if (docInfo.getDtdLocation() != null
				&& (docInfo.getDtdLocation().trim().length() > 0)
				&& (docInfo.getDtdLocation().trim().startsWith("http://"))) {
			if (docInfo.getDtdPublicId() == null
					|| docInfo.getDtdPublicId().trim().length() == 0)
				sb.append(" SYSTEM '");
			else
				sb.append(" '");
			sb.append(docInfo.getDtdLocation()).append("' ");
		}
		return sb.toString();
	}

	public String getXmlProcInstr() {
		return xmlModel.getXmlProcInstr();
	}

	public void setCaretPosition(int offset) {
		try {
			textPane.setCaretPosition(offset);
		} catch (Exception e) {
		}
	}

	public String getEncoding() {
		return docInfo.getEncoding();
	}

	protected void caretMoved() {
		Document doc = textPane.getDocument();
		Element root = doc.getDefaultRootElement();
		int where = textPane.getCaretPosition();
		int line = root.getElementIndex(where);
		int col = where - root.getElement(line).getStartOffset();

		setProperty(IDEditor.PROP_LINE_COL, null, new Point(++line, ++col));
	}

	public void removeNode(XMLNode parent, XMLNode node) {
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = (xtm != null ? xtm.getPathToRoot(parent) : null);
		if (path == null)
			return;

		setTreeSelectionPath(path);
		auto = true;
		if (parent == complexTypeNode) {
			if (node != null) {
				int index = parent.index(node);
				if (complexTypePanel.getElementTree() != null) {
					XMLTreeModel etm = (XMLTreeModel) complexTypePanel
							.getElementTree().getModel();
					TreePath epath = etm.getPathToRoot(node);
					complexTypePanel.getElementTree().setSelectionPath(epath);
					complexTypePanel.doRemove();
				} else {
					int treePosition = complexTypeNode
							.getOutlineChildIndex(node);
					int[] childIndices = { treePosition };
					Object[] children = { node };
					TreePath xmlTreePath = complexTypePath;
					TreeModelEvent tme = new TreeModelEvent(this, xmlTreePath,
							childIndices, children);
					xtm = (XMLTreeModel) xmlTree.getModel();
					locked = true;
					xtm.fireTreeNodesRemoved(tme);
					locked = false;

					if (!textMode && !undoInProgress) {
						XMLUndoableEdit ue = new XMLUndoableEdit(this, parent,
								node, index, null, XMLUndoableEdit.NODE_REMOVE);
						UndoableEditEvent ev = new UndoableEditEvent(this, ue);
						this.fireUndoableEditEvent(ev);
					}
				}
			}
		} else {
			int index = parent.index(node);
			int treePosition = parent.getOutlineChildIndex(node);
			parent.removeChild(node);
			int[] childIndices = { treePosition };
			Object[] children = { node };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);

			locked = true;
			xtm.fireTreeNodesRemoved(tme);
			locked = false;

			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(this, parent, node,
						index, null, XMLUndoableEdit.NODE_REMOVE);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				this.fireUndoableEditEvent(ev);
			}
		}
		auto = false;
	}

	public void insertNode(XMLNode parent, XMLNode newNode, int pos) {
		auto = true;
		beginUndoEditSession();
		parent.insertBefore(newNode, pos);
		int treePosition = parent.getOutlineChildIndex(newNode);
		pos = parent.index(newNode);

		// fire tree model event for element tree
		Object[] path = { parent };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;
		if (complexTypePanel != null
				&& complexTypePanel.getElementTree() != null) {
			xtm = (XMLTreeModel) complexTypePanel.getElementTree().getModel();
			xtm.fireTreeNodesInserted(tme);
		}

		// fire tree model event for xml tree
		xtm = (XMLTreeModel) xmlTree.getModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);
		locked = true;
		xtm.fireTreeNodesInserted(tme);
		locked = false;
		if (!textMode && !undoInProgress) {
			XMLUndoableEdit ue = new XMLUndoableEdit(this, parent, newNode,
					pos, null, XMLUndoableEdit.NODE_INSERT);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			this.fireUndoableEditEvent(ev);
		}
		endUndoEditSession();
		auto = false;
	}

	private void insertNewNodeBeforeNode(XMLNode node, XMLNode newNode,
			XMLNode insertBefore) {
		auto = true;
		if (insertBefore != null) {
			try {
				node.insertBefore(newNode, insertBefore);
			} catch (org.w3c.dom.DOMException e) {
				node.appendChild(newNode);
			}
		} else
			node.appendChild(newNode);
		auto = false;
	}

	public void insertComment(XMLNode node, XMLNode insertBefore) {
		auto = true;
		beginUndoEditSession();
		Comment comment = xmlModel.getDocument().createComment(COMMENT);
		XMLNode newNode = new XMLNode(comment, false);

		insertNewNodeBeforeNode(node, newNode, insertBefore);
		int treePosition = node.getOutlineChildIndex(newNode);
		int pos = node.index(newNode);

		// fire tree model event for element tree
		Object[] path = { node };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;

		if (complexTypePanel != null
				&& complexTypePanel.getElementTree() != null) {
			xtm = (XMLTreeModel) complexTypePanel.getElementTree().getModel();
			xtm.fireTreeNodesInserted(tme);
		}
		// fire tree model event for xml tree
		xtm = (XMLTreeModel) xmlTree.getModel();
		TreePath xmlTreePath = xtm.getPathToRoot(node);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		locked = true;
		xtm.fireTreeNodesInserted(tme);
		locked = false;

		if (!textMode && !undoInProgress) {
			XMLUndoableEdit ue = new XMLUndoableEdit(this, node, newNode, pos,
					null, XMLUndoableEdit.NODE_INSERT);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			this.fireUndoableEditEvent(ev);
		}
		endUndoEditSession();
		auto = false;
	}

	private Point getSourcePopupLocation() {
		Point p = sourcePanel.getLocation();

		p.x += (sourcePanel.getWidth() / 2);
		p.y += (sourcePanel.getHeight() / 2);
		EditUtils.convertPointToScreen(p, sourcePanel);
		return p;
	}

	private Point getDesignPopupLocation() {
		Point p = designTree.getLocation();

		p.x += (designTree.getWidth() / 2);
		p.y += (designTree.getHeight() / 2);
		EditUtils.convertPointToScreen(p, designTree);
		return p;
	}

	public void insertProcInstr(XMLNode node, XMLNode insertBefore) {
		auto = true;

		Point loc = textPane.getCaret().getMagicCaretPosition();
		Point save = null;
		if (loc == null) {
			loc = this.getSourcePopupLocation();
		} else {
			save = new Point(loc.x, loc.y);

			EditUtils.convertPointToScreen(loc, textPane);
		}
		ProcInstr pi = ProcInstrInput.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("insert.processing.instruction"), loc);
		if (save != null)
			textPane.getCaret().setMagicCaretPosition(save);

		if (pi == null || pi.target == null || pi.target.trim().length() == 0
				|| pi.data == null || pi.data.trim().length() == 0)
			return;

		beginUndoEditSession();
		org.w3c.dom.ProcessingInstruction pinode = xmlModel.getDocument()
				.createProcessingInstruction(pi.target, pi.data);
		XMLNode newNode = new XMLNode(pinode, false);

		insertNewNodeBeforeNode(node, newNode, insertBefore);

		int pos = node.index(newNode);
		int treePosition = node.getOutlineChildIndex(newNode);

		// fire tree model event for element tree
		Object[] path = { node };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;
		if (complexTypePanel != null
				&& complexTypePanel.getElementTree() != null) {
			xtm = (XMLTreeModel) complexTypePanel.getElementTree().getModel();
			xtm.fireTreeNodesInserted(tme);
		}

		// fire tree model event for xml tree
		xtm = (XMLTreeModel) xmlTree.getModel();
		TreePath xmlTreePath = xtm.getPathToRoot(node);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		locked = true;
		xtm.fireTreeNodesInserted(tme);
		locked = false;

		if (!textMode && !undoInProgress) {
			XMLUndoableEdit ue = new XMLUndoableEdit(this, node, newNode, pos,
					null, XMLUndoableEdit.NODE_INSERT);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			this.fireUndoableEditEvent(ev);
		}
		endUndoEditSession();
		auto = false;
	}

	public void insertFragment(String fragment, XMLNode parent, XMLNode before) {

		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);
		auto = true;
		if (parent == complexTypeNode) {
			int pos = (before != null ? parent.index(before) : parent
					.getChildCount());
			Node node = xmlModel.parseFragment(parent.getDomNode(), fragment);

			if (node != null) {
				switch (node.getNodeType()) {
				case Node.COMMENT_NODE:
				case Node.PROCESSING_INSTRUCTION_NODE:
					insertNode(parent, new XMLNode(node, false), pos);
					break;
				default:
					if (complexTypePanel != null
							&& complexTypePanel.getTypeTree() != null) {
						XMLNode newNode = new XMLNode(node, false);
						newNode.setParent(parent);
						SchemaNode snode = CommonUtils.getSchemaNode(xmlModel,
								newNode, standalone);
						SchemaTreeModel stm = (SchemaTreeModel) complexTypePanel
								.getTypeTree().getModel();
						XMLTreeModel etm = (XMLTreeModel) complexTypePanel
								.getElementTree().getModel();
						int selRow = complexTypePanel.getSelectedRow();

						String pattern = stm.getPattern();
						RegExp re = new RegExp(pattern);
						Automaton automata = re.toAutomaton();

						pattern = "^" + pattern + "$";
						Pattern compile = Pattern.compile(pattern);

						if (snode != null
								&& ((pos = CommonUtils.getInsertBefore(
										xmlModel, compile, automata, etm,
										snode, selRow)) >= 0)) {
							newNode.setCharCode(snode.getCharCode());
							insertNode(parent, newNode, pos);
						}
					}
					break;
				}
			}
		}
		auto = false;
	}

	public boolean canInsertTextInNode(XMLNode parent) {
		boolean ret = false;

		int save = (!integrated ? textPane.getCaretPosition() : 0);

		XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);

		if (parent == this.simpleTypeNode) {
			ret = true;
		} else if (parent == complexTypeNode) {
			SchemaTreeModel ctm = (SchemaTreeModel) complexTypePanel
					.getTypeTree().getModel();
			SchemaNode schemaNode = (SchemaNode) ctm.getRoot();
			if ((schemaNode.getNodeByName("simpleContent") != null)
					|| schemaNode.isMixed()) {
				ret = true;
			}
		}

		if (!integrated) {
			textPane.grabFocus();
			textPane.setCaretPosition(save);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void insertSubElement(XMLNode node, XMLNode before) {
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(node);
		setTreeSelectionPath(path);

		auto = true;
		Vector isn = null;
		if (node == complexTypeNode && complexTypePanel != null
				&& complexTypePanel.getTypeTree() != null) {
			XMLTreeModel etm = (XMLTreeModel) complexTypePanel.getElementTree()
					.getModel();
			if (before != null) {
				TreePath epath = etm.getPathToRoot(before);
				complexTypePanel.getElementTree().setSelectionPath(epath);
			} else {
				complexTypePanel.getElementTree().setSelectionPath(null);
			}
			SchemaTreeModel stm = (SchemaTreeModel) complexTypePanel
					.getTypeTree().getModel();
			int selRow = complexTypePanel.getSelectedRow();
			isn = CommonUtils.getInsertableElements(xmlModel, stm, etm, selRow);

			SchemaNode proxyAnyNode = null;

			if (xmlModel.getSchema().isJSP()) {
				for (int i = 0; i < isn.size(); i++) {
					SchemaNode sn = (SchemaNode) isn.elementAt(i);

					if (sn.toString().equals(CommonUtils.ANY_ELEMENT)) {
						proxyAnyNode = sn;
						isn.addAll(xmlModel.getSchema().getTaglibSchemaNodes());
						break;
					}
				}
				Collections.sort(isn);
				if (node.getDomNode() == xmlModel.getDocument()
						.getDocumentElement())
					isn.add(TAGLIB);
			} else
				Collections.sort(isn);

			if (canInsertTextInNode(node))
				isn.add(CDATA);

			isn.add(COMMENT);
			isn.add(PROC_INSTR);

			Point loc = textPane.getCaret().getMagicCaretPosition();
			Point save = null;
			if (loc == null) {
				loc = this.getSourcePopupLocation();
			} else {
				save = new Point(loc.x, loc.y);

				EditUtils.convertPointToScreen(loc, textPane);
			}

			Object choice = ElementChooser.showDialog(textPane, node
					.getDomNode().getNodeName(), isn, loc, askForNamespace);

			if (save != null)
				textPane.getCaret().setMagicCaretPosition(save);
			if (choice != null) {
				if (choice instanceof SchemaNode) {
					SchemaNode csn = (SchemaNode) choice;
					if (csn.getTld() != null) {
						this.askForNamespace = false;
						path = stm.getPathToRoot(proxyAnyNode);
						proxyAnyNode.setProxyNode(csn);
						complexTypePanel.getTypeTree().setSelectionPath(path);
						complexTypePanel.doInsert();
					} else {
						this.askForNamespace = csn.getEditNamespace();
						path = stm.getPathToRoot(csn);
						complexTypePanel.getTypeTree().setSelectionPath(path);
						complexTypePanel.doInsert();
					}
				} else {
					String option = (String) choice;
					if (option.equals(COMMENT))
						insertComment(node, before);
					else if (option.equals(PROC_INSTR))
						insertProcInstr(node, before);
					else if (option.equals(CDATA)) {
						insertCDATANode(" ", node, before);
					} else if (option.equals(TAGLIB)) {
						insertTaglib();
					}
				}
			}
		} else {
			isn = new Vector<String>(4, 4);
			Point loc = textPane.getCaret().getMagicCaretPosition();
			Point save = null;
			if (loc == null) {
				loc = getSourcePopupLocation();
			} else {
				save = new Point(loc.x, loc.y);

				EditUtils.convertPointToScreen(loc, textPane);
			}
			if (canInsertTextInNode(node))
				isn.add(CDATA);

			isn.add(COMMENT);
			isn.add(PROC_INSTR);
			Object choice = ElementChooser.showDialog(textPane,
					LocalizedResources.applicationResources
							.getString("choose.element"), isn, loc,
					askForNamespace);
			if (save != null)
				textPane.getCaret().setMagicCaretPosition(save);
			if (choice != null) {
				String option = (String) choice;
				if (option.equals(COMMENT))
					insertComment(node, before);
				else if (option.equals(LocalizedResources.applicationResources
						.getString("processing.instruction")))
					insertProcInstr(node, before);
				else if (option.equals(CDATA))
					insertCDATANode(" ", node, before);
			}
		}
		auto = false;
	}

	private void showDesignTreePopup(MouseEvent e) {
		if (e.isPopupTrigger()
				|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {

			if (!isWellFormed())
				return;

			TreePath selPath = designTree
					.getPathForLocation(e.getX(), e.getY());
			if (selPath != null) {
				designTree.setSelectionPath(selPath);
				XMLNode node = (XMLNode) selPath.getLastPathComponent();
				designTreePopup.getAddChild().setEnabled(
						node == complexTypeNode && node != simpleTypeNode);
				designTreePopup.show(designTree, e.getX(), e.getY());
			}
		}
	}

	private void addChild() {
		auto = true;
		Vector<SchemaNode> isn = null;
		if (complexTypePanel != null && complexTypePanel.getTypeTree() != null) {
			XMLTreeModel etm = (XMLTreeModel) complexTypePanel.getElementTree()
					.getModel();
			complexTypePanel.getElementTree().setSelectionPath(null);
			SchemaTreeModel stm = (SchemaTreeModel) complexTypePanel
					.getTypeTree().getModel();
			int selRow = complexTypePanel.getSelectedRow();
			isn = CommonUtils.getInsertableElements(xmlModel, stm, etm, selRow);

			if (isn.size() > 0) {
				Collections.sort(isn);

				Object choice = ElementChooser.showDialog(textPane,
						complexTypeNode.getDomNode().getNodeName(), isn,
						getDesignPopupLocation(), askForNamespace);

				if (choice != null) {
					if (choice instanceof SchemaNode) {
						SchemaNode csn = (SchemaNode) choice;

						this.askForNamespace = csn.getEditNamespace();
						TreePath path = stm.getPathToRoot(csn);
						complexTypePanel.getTypeTree().setSelectionPath(path);
						complexTypePanel.doInsert();
					}
				}
			} else {
				setProperty(IDEditor.PROP_STATUS, null,
						LocalizedResources.applicationResources
								.getString("no.children.allowed"));
			}
		}
		auto = false;
	}

	private void insertTaglib() {
		// get tld file
		File defaultPath = new File(docInfo.getPath(), docInfo.getName());
		JFileChooser fileChooser = new JFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("open.tld"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("open.taglib"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		} else
			return;

		// ask for name space and prefix
		String[] p = { "" };
		String[] u = { "" };
		Point loc = textPane.getCaret().getMagicCaretPosition();
		Point save = null;
		if (loc == null) {
			loc = this.getSourcePopupLocation();
		} else {
			save = new Point(loc.x, loc.y);

			EditUtils.convertPointToScreen(loc, textPane);
		}

		Namespace ns = NamespaceInput.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("taglib.namespace.input"), p, u, loc);
		if (save != null)
			textPane.getCaret().setMagicCaretPosition(save);

		if (ns == null || ns.uri == null || ns.uri.trim().length() == 0
				|| ns.prefix == null || ns.prefix.length() == 0
				|| xmlModel.getSchema().isTaglibImported(ns.uri))
			return;
		// convert tld to schema file
		try {
			File tldschema = TLD2Schema.getSchemaFromTLD(file, ns.uri);

			// import schema for tld file
			Vector<TaglibDef> v = new Vector<TaglibDef>();
			TaglibDef td = new TaglibDef();
			td.nsPrefix = ns.prefix;
			td.nsURI = ns.uri;
			td.schemaLocation = "file:///" + tldschema.getCanonicalPath();
			v.add(td);

			// import tag lib
			xmlModel.getSchema().importTaglibs(v);

			// add namespace and prefix attribute to document
			setAttribute(CommonUtils.XMLNS_PREFIX + ns.prefix, ns.uri);

			// add taglib to docinfo, but we store the tld path, not the
			// converted
			// schema path, 'cause schema is a temp file
			td.schemaLocation = file.getCanonicalPath();
			docInfo.addTaglib(td);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void insertTextInSimpleType(String value) {
		if (value == null)
			return;

		auto = true;
		beginUndoEditSession();
		value = CommonUtils.escape(value);
		Node textNode = simpleTypeNode.getTextNode();
		if (textNode != null) {
			String oldValue = textNode.getNodeValue();
			textNode.setNodeValue(value);
			Object[] path = { simpleTypeNode };

			XMLNode changedNode = simpleTypeNode.findTextNode();
			changedNode.setParsed(false);
			int pos = simpleTypeNode.index(changedNode);
			int treePosition = simpleTypeNode.getOutlineChildIndex(changedNode);
			int[] childIndices = { treePosition };
			Object[] children = { changedNode };
			XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			xtm.fireTreeNodesChanged(tme);

			if (complexTypePanel.getElementTree() != null)
				complexTypePanel.getElementTree().treeDidChange();
			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(this, simpleTypeNode,
						changedNode, pos, oldValue, XMLUndoableEdit.NODE_CHANGE);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				this.fireUndoableEditEvent(ev);
			}

		} else {

			org.w3c.dom.Node newTextNode = xmlModel.getDocument()
					.createTextNode(value);
			XMLNode newNode = new XMLNode(newTextNode, false);
			simpleTypeNode.appendChild(newNode);
			int pos = simpleTypeNode.index(newNode);
			int treePosition = simpleTypeNode.getOutlineChildIndex(newNode);

			// fire tree model event for element tree
			Object[] path = { simpleTypeNode };
			int[] childIndices = { treePosition };
			Object[] children = { newNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = null;
			if (complexTypePanel.getElementTree() != null) {
				xtm = (XMLTreeModel) complexTypePanel.getElementTree()
						.getModel();
				xtm.fireTreeNodesInserted(tme);
			}

			// fire tree model event for xml tree
			TreePath xmlTreePath = simpleTypePath;

			xtm = (XMLTreeModel) xmlTree.getModel();
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

			locked = true;
			xtm.fireTreeNodesInserted(tme);
			locked = false;

			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(this, simpleTypeNode,
						newNode, pos, null, XMLUndoableEdit.NODE_INSERT);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				this.fireUndoableEditEvent(ev);
			}

		}
		endUndoEditSession();
		auto = false;
	}

	public void insertCDATAInSimpleType(String value) {
		if (value == null)
			return;

		auto = true;
		beginUndoEditSession();
		Node cdataNode = simpleTypeNode.getCDATANode();
		if (cdataNode != null) {
			String oldValue = cdataNode.getNodeValue();
			cdataNode.setNodeValue(value);
			Object[] path = { simpleTypeNode };

			XMLNode changedNode = simpleTypeNode.findCDATANode();
			changedNode.setParsed(false);
			int treePosition = simpleTypeNode.getOutlineChildIndex(changedNode);
			int pos = simpleTypeNode.index(changedNode);
			int[] childIndices = { treePosition };
			Object[] children = { changedNode };
			XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			xtm.fireTreeNodesChanged(tme);

			if (complexTypePanel.getElementTree() != null)
				complexTypePanel.getElementTree().treeDidChange();
			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(this, simpleTypeNode,
						changedNode, pos, oldValue, XMLUndoableEdit.NODE_CHANGE);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				this.fireUndoableEditEvent(ev);
			}

		} else {
			org.w3c.dom.Node newCDATANode = xmlModel.getDocument()
					.createCDATASection(value);
			XMLNode newNode = new XMLNode(newCDATANode, false);
			simpleTypeNode.appendChild(newNode);
			int pos = simpleTypeNode.index(newNode);
			int treePosition = simpleTypeNode.getOutlineChildIndex(newNode);

			// fire tree model event for element tree
			Object[] path = { simpleTypeNode };
			int[] childIndices = { treePosition };
			Object[] children = { newNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = null;
			if (complexTypePanel.getElementTree() != null) {
				xtm = (XMLTreeModel) complexTypePanel.getElementTree()
						.getModel();
				xtm.fireTreeNodesInserted(tme);
			}

			// fire tree model event for xml tree
			TreePath xmlTreePath = simpleTypePath;

			xtm = (XMLTreeModel) xmlTree.getModel();
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

			locked = true;
			xtm.fireTreeNodesInserted(tme);
			locked = false;

			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(this, simpleTypeNode,
						newNode, pos, null, XMLUndoableEdit.NODE_INSERT);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				this.fireUndoableEditEvent(ev);
			}

		}
		endUndoEditSession();
		auto = false;
	}

	public void showAttributes(XMLNode parent) {
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);
		if (parent == selectedTypeNode && complexTypePanel != null) {

			SchemaNode typeNode = complexTypePanel.getTypeNode();
			Point loc = textPane.getCaret().getMagicCaretPosition();
			Point save = null;
			if (loc == null) {
				loc = this.getSourcePopupLocation();
			} else {
				save = new Point(loc.x, loc.y);
				EditUtils.convertPointToScreen(loc, textPane);
			}

			AttributePropertySheet.showDialog(sourcePanel,
					LocalizedResources.applicationResources
							.getString("edit.attributes"), typeNode, this, loc);
			if (save != null)
				textPane.getCaret().setMagicCaretPosition(save);

		}
	}

	public void insertTextNode(String value, XMLNode parent, XMLNode before) {

		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);

		auto = true;

		if (parent == simpleTypeNode) {
			insertTextInSimpleType(value);
		} else if (parent == complexTypeNode) {
			SchemaTreeModel ctm = (SchemaTreeModel) complexTypePanel
					.getTypeTree().getModel();
			SchemaNode schemaNode = (SchemaNode) ctm.getRoot();
			if (value.trim().length() == 0
					|| (schemaNode.getNodeByName("simpleContent") != null)
					|| schemaNode.isMixed()) {
				value = CommonUtils.escape(value);
				org.w3c.dom.Node newTextNode = xmlModel.getDocument()
						.createTextNode(value);
				XMLNode newNode = new XMLNode(newTextNode, false);
				complexTypeNode.insertBefore(newNode, before);
				updateTreeModel(complexTypeNode, newNode, false);
			}
		}
		auto = false;
	}

	public void insertCDATANode(String value, XMLNode parent, XMLNode before) {

		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);

		auto = true;
		if (parent == simpleTypeNode) {
			insertCDATAInSimpleType(value);
		} else if (parent == complexTypeNode) {
			SchemaTreeModel ctm = (SchemaTreeModel) complexTypePanel
					.getTypeTree().getModel();
			SchemaNode schemaNode = (SchemaNode) ctm.getRoot();
			if (value.trim().length() == 0
					|| (schemaNode.getNodeByName("simpleContent") != null)
					|| schemaNode.isMixed()) {
				org.w3c.dom.Node newCDATANode = xmlModel.getDocument()
						.createCDATASection(value);
				XMLNode newNode = new XMLNode(newCDATANode, false);
				complexTypeNode.insertBefore(newNode, before);
				updateTreeModel(complexTypeNode, newNode, false);
			}
		}
		auto = false;
	}

	private void updateTreeModel(XMLNode parent, XMLNode newNode, boolean init) {
		int pos = parent.index(newNode);
		int treePosition = parent.getOutlineChildIndex(newNode);

		// fire tree model event for element tree
		Object[] path = { parent };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;

		if (complexTypePanel != null
				&& complexTypePanel.getElementTree() != null) {
			xtm = (XMLTreeModel) complexTypePanel.getElementTree().getModel();
			xtm.fireTreeNodesInserted(tme);
		}

		// fire tree model event for xml tree
		xtm = (XMLTreeModel) xmlTree.getModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		setLocked(true);
		xtm.fireTreeNodesInserted(tme);
		setLocked(false);
		if (!init) {
			TreePath newPath = xmlTreePath.pathByAddingChild(newNode);
			setTreeSelectionPath(newPath);
		}
		if (!textMode && !undoInProgress) {
			XMLUndoableEdit ue = new XMLUndoableEdit(this, parent, newNode,
					pos, null, XMLUndoableEdit.NODE_INSERT);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			fireUndoableEditEvent(ev);
		}
	}

	public boolean isEmpty(XMLNode node) {
		SchemaNode typeNode = CommonUtils.getTypeNode(xmlModel, node,
				standalone);
		if (typeNode == null)
			return false;
		else
			return typeNode.isEmpty();
	}

	public XMLTreeModel getXMLTreeModel() {
		return (xmlTree != null ? (XMLTreeModel) xmlTree.getModel() : null);
	}

	public XMLModel getXMLModel() {
		return xmlModel;
	}

	private void normalizeNewLine(StringBuffer sb) {
		for (int i = 0; i < sb.length() - 1; i++) {
			char c = sb.charAt(i);
			char n = sb.charAt(i + 1);

			if (c == '\r' && n == '\n') {
				sb.replace(i, i + 1, "");
			}
		}
	}

	private void readDocumentFromFile(File file, XMLDocument doc,
			String encoding) {
		// so just set text
		BufferedReader ir = null;
		try {
			if (encoding != null)
				ir = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), encoding));
			else
				ir = new BufferedReader(new FileReader(file));

			char[] buf = new char[128];

			int nread = 0;
			int offset = 0;

			StringBuffer sb = new StringBuffer();
			while ((nread = ir.read(buf, 0, buf.length)) > 0) {
				sb.append(buf, 0, nread);
				normalizeNewLine(sb);
				String str = sb.toString();
				doc.insertContent(offset, str, null);
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

	private XMLDocumentElement getRootElement(XMLNode rootNode,
			XMLDocument xmldoc) {

		XMLDocumentElement documentElement = null;

		if (rootNode != null && !rootNode.isParsed()) {
			if (textMode)
				xmldoc.removeUndoableEditListener(undoHandler);
			documentElement = new XMLDocumentElement(rootNode, xmldoc);
			if (textMode)
				xmldoc.addUndoableEditListener(undoHandler);
		} else {
			try {
				String text = xmldoc.getText(0, xmldoc.getLength());
				CharArrayReader cr = new CharArrayReader(text.toCharArray());
				XML10Parser parser = new XML10Parser(cr);
				parser.setTabSize(1);

				documentElement = parser.Document(xmldoc, rootNode);
				if (!undoInProgress) {
					if (textMode)
						xmldoc.removeUndoableEditListener(undoHandler);
					documentElement.applyAttributes();
					if (textMode)
						xmldoc.addUndoableEditListener(undoHandler);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		xmldoc.setRootElement(documentElement);

		return documentElement;
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

	private Vector<String> getExpectedValues(JTextPane tp) {
		prefix = null;

		Vector<String> expected = new Vector<String>(8, 8);
		int where = tp.getCaretPosition();
		Document doc = tp.getDocument();
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
						if (!expected.contains(str)) {
							if (str.equals("</")) {
								if (xmlCheckHandler.getRowCount() == 1) {

									ParseProblem pp = xmlCheckHandler
											.getParseProblem(0);
									String description = pp.getDescription();
									int index = description
											.indexOf(MISSING_END_TAG_ERROR);
									if (index > 0) {
										int startIndex = description.indexOf(
												"</", index);
										int endIndex = description.indexOf('>',
												startIndex) + 1;

										if (endIndex > startIndex) {
											str = description.substring(
													startIndex, endIndex);
											expected.setSize(0);
											expected.add(str);
											return expected;
										}
									}
								}
							} else {
								expected.add(str);
							}
						}
						if (kind == XML10ParserConstants.Name)
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
						int count = XML10ParserConstants.tokenImage.length;
						for (int i = 0; i < count; i++) {
							String str = XML10ParserConstants.tokenImage[i];
							str = getTokenLiteral(str);
							if (str.startsWith(prefix)) {
								expected.add(str);
							}
						}
					}
				} catch (BadLocationException e) {

				}
			}
		}

		return expected;
	}

	private void doContentAssist(JTextPane tp) {
		try {

			int where = tp.getCaretPosition();
			XMLDocument xmldoc = (XMLDocument) tp.getDocument();
			String text = xmldoc.getText(0, xmldoc.getLength());
			CharArrayReader cr = new CharArrayReader(text.toCharArray());
			XML10Parser parser = new XML10Parser(cr);

			parseException = null;
			tokenMgrError = null;
			token = null;

			parser.Document(xmldoc, null);

			parseException = parser.getParseException();
			tokenMgrError = parser.getTokenMgrError();
			token = parser.token;

			Vector<String> expected = getExpectedValues(tp);
			String choice = null;
			Point save = null;
			if (expected.size() > 1) {
				Collections.sort(expected);

				Point loc = tp.getCaret().getMagicCaretPosition();

				if (loc == null) {
					loc = getSourcePopupLocation();
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
					tp.getCaret().setMagicCaretPosition(save);
				if (prefix != null)
					choice = choice.substring(prefix.length());
				if (choice.equals("Space")) {
					choice = " ";
				}
				tp.getDocument().insertString(where, choice.toString(), null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setTabs(JTextPane tp, int charactersPerTab) {
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

	public synchronized void showDocument() {

		if (!(textPane.getEditorKit() instanceof XMLEditorKit)) {

			int charactersPerTab = (project != null
					&& project.getProjectConfiguration() != null ? Integer
					.valueOf(project.getProjectConfiguration().getTabsize())
					: XMLEditorConfiguration.DEFAULT_CHARS_PER_TAB);

			XMLEditorKit ek = new XMLEditorKit(this,
					(editorConfig != null ? editorConfig.getStyleContext()
							: new XMLDefaultStyleContext()));

			textPane.setEditorKit(ek);
			setTabs(textPane, charactersPerTab);

			XMLDocument xmldoc = (XMLDocument) textPane.getDocument();

			readDocumentFromFile(getDocumentFile(), xmldoc,
					docInfo.getEncoding());

			if (wellFormed) {
				XMLTreeModel xmltm = getXMLTreeModel();
				getRootElement((XMLNode) xmltm.getRoot(), xmldoc);
				xmltm.addTreeModelListener(xmldoc);
			} else {
				getRootElement(null, xmldoc);
			}

			xmldoc.addDocumentListener(new DocumentTracker());
		} else if (refresh) {

			XMLDocument xmldoc = (XMLDocument) textPane.getDocument();
			if (wellFormed) {
				XMLTreeModel xmltm = getXMLTreeModel();
				getRootElement((XMLNode) xmltm.getRoot(), xmldoc);
				xmltm.addTreeModelListener(xmldoc);
			} else {
				getRootElement(null, xmldoc);
			}

		}

		if (wellFormed && xmlTree != null) {
			setProperty(IDEditor.PROP_OUTLINE_VIEW, null, xmlTree);

			if (xmlTree.getModel() instanceof XMLTreeModel) {
				if (!refresh) {
					selectRootElement();
				} else {
					selectElementAtCursor();
				}
			}
		} else {
			setProperty(IDEditor.PROP_OUTLINE_VIEW, null, null);
		}
		refresh = false;

		updateSaveStatus();
		setProperty(IDEditor.PROP_TEXT_MODE, Boolean.valueOf(textMode),
				Boolean.valueOf(textMode));

	}

	private void selectRootElement() {
		try {
			if (xmlTree != null) {
				XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
				if (xtm != null) {
					XMLNode rootNode = (XMLNode) xtm.getRoot();
					XMLNode rootElement = xtm.getRootElement();
					if (rootElement != null) {
						Object[] path = { rootNode, rootElement };
						TreePath rootPath = new TreePath(path);
						setTreeSelectionPath(rootPath);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void selectElementAtCursor() {
		try {
			XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
			int where = textPane.getCaretPosition();

			XMLDocument document = (XMLDocument) textPane.getDocument();
			XMLAbstractElement root = document.getRootElement();
			XMLAbstractElement ele = (root != null ? root
					.positionToElement(where) : null);

			if (ele == null) {
				selectRootElement();
				return;
			}

			boolean endtag = false;

			if ((ele instanceof XMLWhiteSpaceElement)
					&& (ele.getParentElement() != null
							&& ele.getParentElement().getParentElement() != null && (ele
							.getParentElement() instanceof XMLEndTagElement))) {
				endtag = true;
			}

			XMLAbstractElement parent = (endtag ? (XMLAbstractElement) ele
					.getParentElement().getParentElement().getParentElement()
					: (XMLAbstractElement) ele.getParentElement());

			while (parent != null && !(parent instanceof XMLElement)) {
				ele = parent;
				parent = (XMLAbstractElement) ele.getParentElement();
			}

			if (parent != null) {
				TreePath path = xtm.getPathToRoot(parent.getXMLNode());
				setTreeSelectionPath(path);
			} else {
				selectRootElement();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setFeature(String name, boolean value) {
		PropertyChangeEvent pe = new PropertyChangeEvent(this, name, null,
				new Boolean(value));
		for (int i = 0; i < propListener.size(); i++) {
			PropertyChangeListener lis = (PropertyChangeListener) propListener
					.elementAt(i);
			lis.propertyChange(pe);
		}
	}

	private void setProperty(String name, Object oldValue, Object newValue) {
		PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue,
				newValue);
		for (int i = 0; i < propListener.size(); i++) {
			PropertyChangeListener lis = (PropertyChangeListener) propListener
					.elementAt(i);
			lis.propertyChange(pe);
		}
	}

	public void updateSaveStatus() {
		setProperty(IDEditor.PROP_SAVE_STATUS, null, null);
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean getLocked() {
		return locked;
	}

	public void nodeChanged(XMLNode node, String oldValue) {
		// fire tree model event for element tree
		XMLNode parent = (XMLNode) node.getParent();
		Object[] path = { parent };
		int pos = parent.getIndex(node);
		int treePosition = parent.getOutlineChildIndex(node);
		int[] childIndices = { treePosition };
		Object[] children = { node };

		if (parent == complexTypeNode) {
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			if (complexTypePanel.getElementTree() != null) {
				XMLTreeModel xtm = (XMLTreeModel) complexTypePanel
						.getElementTree().getModel();
				xtm.fireTreeNodesChanged(tme);
			}
		}

		// fire tree model event for xml tree
		XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		TreeModelEvent tme = new TreeModelEvent(this, xmlTreePath,
				childIndices, children);
		xtm.fireTreeNodesChanged(tme);
		if (!textMode && !undoInProgress) {
			XMLUndoableEdit ue = new XMLUndoableEdit(this, parent, node, pos,
					oldValue, XMLUndoableEdit.NODE_CHANGE);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			this.fireUndoableEditEvent(ev);
		}

	}

	private void initCharacterCodes() {
		try {
			XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
			xmlModel.initDocCharacterCodes((XMLNode) xtm.getRoot());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Namespace getNamespaceInput() {
		Namespace ns = null;
		Point loc = textPane.getCaret().getMagicCaretPosition();
		Point save = null;
		if (loc == null) {
			loc = this.getSourcePopupLocation();
		} else {
			save = new Point(loc.x, loc.y);

			EditUtils.convertPointToScreen(loc, textPane);
		}

		ns = NamespaceInput.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("namespace.input"), xmlModel.getNSPrefix()
						.toArray(), xmlModel.getNSUri().toArray(), loc);

		if (save != null)
			textPane.getCaret().setMagicCaretPosition(save);

		if (ns == null) {
			String prefix = getPrefix(selectedTypeNode.toString());
			String uri = xmlModel.getSchema().getUri(prefix);
			if (uri != null) {
				ns = new Namespace(prefix, uri);
			}
		}

		if (ns != null && ns.prefix != null && ns.uri != null) {
			if (xmlModel.getNSPrefix().contains(ns.prefix)) {
				xmlModel.getNSPrefix().remove(ns.prefix);
			}
			xmlModel.getNSPrefix().insertElementAt(ns.prefix, 0);

			if (xmlModel.getNSUri().contains(ns.uri)) {
				xmlModel.getNSUri().remove(ns.uri);
			}
			xmlModel.getNSUri().insertElementAt(ns.uri, 0);
		}
		return ns;
	}

	public XMLNode insertElementOfType(SchemaNode typeNode) {

		if (typeNode.getProxyNode() != null) {
			SchemaNode snode = typeNode;
			typeNode = typeNode.getProxyNode();
			snode.setProxyNode(null);
		}

		SchemaTreeModel stm = (SchemaTreeModel) complexTypePanel.getTypeTree()
				.getModel();
		XMLTreeModel etm = (XMLTreeModel) complexTypePanel.getElementTree()
				.getModel();

		int selRow = complexTypePanel.getSelectedRow();
		String pattern = stm.getPattern();
		RegExp re = new RegExp(pattern);
		Automaton automata = re.toAutomaton();

		pattern = "^" + pattern + "$";
		Pattern compile = Pattern.compile(pattern);

		int pos = CommonUtils.getInsertBefore(xmlModel, compile, automata, etm,
				typeNode, selRow);

		// System.out.println("Insert before:" + pos + "; slected row:"+
		// selRow);
		if (pos == -1)
			return null;
		String elementName = CommonUtils.getUnqualifiedElementName(typeNode
				.toString());
		boolean anyNamespace = false;
		if (elementName.equals("any")) {
			if (typeNode.getProxyNode() == null) {
				Point loc = textPane.getCaret().getMagicCaretPosition();
				Point save = null;
				if (loc == null) {
					loc = this.getSourcePopupLocation();
				} else {
					save = new Point(loc.x, loc.y);

					EditUtils.convertPointToScreen(loc, textPane);
				}
				String ename = (String) ElementInput.showDialog(sourcePanel,
						LocalizedResources.applicationResources
								.getString("any.element"), loc, anyElements
								.toArray());
				if (save != null)
					textPane.getCaret().setMagicCaretPosition(save);

				if (ename != null && ename.trim().length() > 0) {
					elementName = ename;
					anyElements.add(ename);
				} else
					return null;

				anyNamespace = typeNode.isAnyNamespace();
			}
		}

		Namespace ns = null;

		if (typeNode.getTld() == null) {
			if (askForNamespace || anyNamespace) {
				ns = this.getNamespaceInput();
			} else {
				ns = this.getTypeNodeNS(typeNode);
			}
		} else {
			TaglibDef td = typeNode.getTld();

			ns = new Namespace(td.nsPrefix, td.nsURI);
		}

		org.w3c.dom.Element element = null;
		if (ns != null && ns.prefix != null && ns.prefix.trim().length() > 0
				&& ns.uri != null && ns.uri.trim().length() > 0) {

			try {
				element = xmlModel.getDocument().createElementNS(ns.uri,
						ns.prefix + ":" + elementName);
				String parentNsURI = complexTypeNode.getDomNode()
						.getNamespaceURI();
				if (parentNsURI == null || !parentNsURI.equals(ns.uri)) {
					org.w3c.dom.Attr attr = xmlModel.getDocument()
							.createAttributeNS(
									XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
									CommonUtils.XMLNS_PREFIX + ns.prefix);
					attr.setValue(ns.uri);
					element.setAttributeNodeNS(attr);
				}
			} catch (Exception e) {
				element = xmlModel.getDocument().createElement(elementName);
			}
		} else
			element = xmlModel.getDocument().createElement(elementName);

		XMLNode newNode = new XMLNode(element, false);
		if (CommonUtils.getUnqualifiedElementName(typeNode.toString()).equals(
				"any")) {
			typeNode = CommonUtils.getSchemaNode(xmlModel, newNode, standalone);
		}
		newNode.setCharCode(typeNode.getCharCode());
		complexTypeNode.insertBefore(newNode, pos);
		newNode.setSchemaNode(typeNode);
		wellFormed = true;
		setProperty(IDEditor.PROP_STATUS, null,
				LocalizedResources.applicationResources
						.getString("document.insync"));
		return newNode;
	}

	private String getPrefix(String name) {
		String prefix = null;
		try {
			prefix = name.substring(0, name.indexOf(":"));
		} catch (Exception e) {
		}
		return prefix;
	}

	private Namespace getTypeNodeNS(SchemaNode typeNode) {
		String prefix = null;

		if (typeNode.getTagName() != null) {
			prefix = getPrefix(typeNode.getTagName());
		} else if (typeNode.getRefName() != null) {
			prefix = getPrefix(typeNode.getRefName());
		}
		String uri = xmlModel.getSchema().getUri(prefix);

		Namespace ns = null;

		Node pnode = complexTypeNode.getDomNode();
		String parentNsURI = pnode.getNamespaceURI();

		if (prefix != null && uri != null) {
			ns = new Namespace(prefix, uri);
		} else if (parentNsURI != null
				&& typeNode.getSchema().getTargetNameSpace() != null
				&& typeNode.getSchema().getTargetNameSpace()
						.equals(parentNsURI)) {
			uri = parentNsURI;
			prefix = getPrefix(pnode.getNodeName());
			ns = new Namespace(prefix, uri);
		} else if (docInfo.getUseSchemaTargetNamespace()
				&& typeNode.getSchema().getTargetNameSpace() != null) {
			if (typeNode.getSchema().getTargetNameSpace()
					.equals(docInfo.getNSUri())) {
				ns = new Namespace(docInfo.getNSPrefix(), docInfo.getNSUri());
			} else {
				ns = this.getNamespaceInput();
			}
		}
		return ns;
	}

	public int closeDocument() {
		int option = JOptionPane.YES_OPTION;
		if (!open)
			return option;

		if (dirty && !docInfo.getDoNotSave()) {
			option = JOptionPane.showConfirmDialog(
					sourcePanel,
					docInfo.getName()
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

	private void removeDocumentFromIde() {
		open = false;
		setProperty(IDEditor.PROP_EDITOR_VIEW, viewTabs, null);
		setProperty(IDEditor.PROP_MESSAGE_VIEW, workTabs, null);
		setProperty(IDEditor.PROP_OUTLINE_VIEW, xmlTree, null);

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
			parseEncoding();
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os,
					docInfo.getEncoding());
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

	public void saveDocumentAsPdf() {
		if (textMode && !wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("invalid.document"));
			return;
		}

		File defaultPath = new File(docInfo.getPath(), docInfo.getName());
		JFileChooser fileChooser = new JFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("save.as.pdf"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("save.document.as.pdf"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		} else
			return;

		try {
			new PDFConverter((AbstractDocument) textPane.getDocument(), file);
		} catch (Exception e) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("save.document.as.pdf.failed"));
		}
	}

	public void saveSchemaAs() {
		File defaultPath = new File(docInfo.getPath(), docInfo.getName());
		JFileChooser fileChooser = new JFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("save.schema.as"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("save.schema.as"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		} else
			return;

		try {
			InputStream input = null;
			if (schemaPane == null) {
				input = new FileInputStream(xmlModel.getSchemaFile());
			} else {
				Document doc = schemaPane.getDocument();
				if (doc != null) {
					String text = doc.getText(0, doc.getLength());
					input = new ByteArrayInputStream(text.getBytes());
				}
			}
			CommonUtils.copyToFile(input, file);
			input.close();
		} catch (Exception e) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("save.schema.failed"));
		}
	}

	public void saveDocument() {
		if (!dirty)
			return;

		isWellFormed();

		File file = new File(docInfo.getPath());
		try {
			if (!file.exists())
				file.mkdirs();

			file = new File(file, docInfo.getName());
			parseEncoding();
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os,
					docInfo.getEncoding());
			PrintWriter writer = new PrintWriter(osw);
			javax.swing.text.Document doc = textPane.getDocument();
			textPane.getEditorKit().write(writer, doc, 0, doc.getLength());
			writer.close();
			docInfo.setTimestamp(file.lastModified());
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
			parseEncoding();
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os,
					docInfo.getEncoding());
			PrintWriter writer = new PrintWriter(osw);
			javax.swing.text.Document doc = textPane.getDocument();
			textPane.getEditorKit().write(writer, doc, 0, doc.getLength());
			writer.close();
		} catch (Exception e) {
		}
	}

	private File getDocumentFile() {
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

	private void removeWorkTab(String title) {
		setProperty(IDEditor.PROP_REMOVE_MESSAGE_TITLE, title, null);
	}

	public void validateUsingDtd() {
		if (textMode && !wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("invalid.document"));
			return;
		}

		try {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("validate.dtd"));
			saveDocument();

			DefaultErrorHandler errorHandler = new DefaultErrorHandler();
			ProblemsView pview = new ProblemsView(errorHandler, textPane);
			xmlModel.validateUsingDtd(errorHandler);
			removeWorkTab(LocalizedResources.applicationResources
					.getString("validation.dtd"));
			if (workTabs != null)
				workTabs.add(LocalizedResources.applicationResources
						.getString("validation.dtd"), pview);
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("validation.complete"));
		} catch (Exception e) {
			e.printStackTrace();
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("validation.complete.error"));
		}
	}

	public void validateUsingSchema() {
		if (textMode && !wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("invalid.document"));
			return;
		}

		try {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("validate.schema"));
			saveDocument();
			File file = getDocumentFile();
			XMLReader parser = XMLReaderFactory.createXMLReader();

			DefaultErrorHandler errorHandler = new DefaultErrorHandler();

			parser.setErrorHandler(errorHandler);
			parser.setFeature("http://xml.org/sax/features/namespaces", true);
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature(
					"http://apache.org/xml/features/validation/schema", true);

			parser.parse(file.getAbsolutePath());
			removeWorkTab(LocalizedResources.applicationResources
					.getString("validation.schema"));

			ProblemsView eview = new ProblemsView(errorHandler, textPane);

			if (workTabs != null) {
				workTabs.add(LocalizedResources.applicationResources
						.getString("validation.schema"), eview);
				workTabs.setSelectedComponent(eview);
			}
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("validation.complete"));
		} catch (Exception e) {
			e.printStackTrace();
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("validation.complete.error"));
		}
	}

	private void init() {
		setLocale();
		painter = new DefaultHighlighter.DefaultHighlightPainter(
				HIGHLIGHT_COLOR);
		xpainter = new DefaultHighlighter.DefaultHighlightPainter(
				HIGHLIGHT_COLOR);

		if (integrated) {
			workTabs = new JTabbedPane(JTabbedPane.TOP);
			workTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			CloseTabbedPaneUI ui = new CloseTabbedPaneUI();
			ui.addCloseAction(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JTabbedPane tp = (JTabbedPane) e.getSource();
					int index = e.getID();

					tp.remove(index);
				}
			});
			workTabs.setUI(ui);
			viewTabs = new JTabbedPane(JTabbedPane.BOTTOM);
		}

		anyElements = new Vector<String>(5, 5);
		this.showLineNumbers = true;
		this.textMode = true;

		xmlModel = new XMLModel(editorConfig, docInfo);
		undoHandler = new UndoableEditHandler();

		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();

			StringReader stylesheet = new StringReader(ppxsl);
			StreamSource xslSource = new StreamSource(stylesheet);
			transformer = tfactory.newTransformer(xslSource);

		} catch (Exception e) {

		}
	} // init

	private void initTextView() {
		textPane = new MichiganTextPane() {
			private static final long serialVersionUID = 3431401057201923230L;

			public void paint(Graphics g) {
				super.paint(g);
				if (textPaneLineNumbers != null) {
					textPaneLineNumbers.repaintLineNumbers();
				}
			}
		};

		if (project != null) {
			textPane.setKeymap(com.nubean.michide.KeyMapFactory
					.createKeymap(project.getProjectConfiguration().getKeymap()));
		}

		addDocumentAction();
		addFormatAction();

		addXMLCheckView(xmlCheckHandler);

		if (integrated) {

			textPane.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()
							|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {
						EditorPopup popup = new EditorPopup(docInfo.getName());
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
			viewTabs.add(
					LocalizedResources.applicationResources.getString("source"),
					textPaneLineNumbers);
		textPaneLineNumbers.showLineNumbers(showLineNumbers);
	}

	private void initDocTree() {
		try {
			// Set up the tree
			xmlTree = new JTree();
			xmlTree.setCellRenderer(treeCellRenderer);

			designTree = new JTree();
			designTreePopup = new DesignTreePopup("Design Tree");
			designTree.setCellRenderer(treeCellRenderer);

			if (wellFormed) {
				XMLTreeModel treeModel = new XMLTreeModel(
						xmlModel.getDocument(), xmlModel.isParsed());
				xmlTree.setModel(treeModel);
				xmlTree.addTreeSelectionListener(xmlTreeListener = new XMLTreeListener());

				designTree.setModel(treeModel);
				designTree
						.addTreeSelectionListener(designTreeListener = new XMLTreeListener());

				designTree.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						showDesignTreePopup(e);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JTextPane getActiveTextPane() {
		JTextPane activeTextPane = textPane;

		if (viewTabs != null) {
			if (viewTabs.getSelectedIndex() == 2) {
				activeTextPane = schemaPane;
			}
		}

		return activeTextPane;
	}

	private void initDocViews() {
		int tabCount = (viewTabs == null ? 0 : viewTabs.getTabCount());
		if (tabCount > 0) {
			for (int i = 0; i < tabCount; i++) {
				viewTabs.remove(0);
			}
		}
		makeDesignPanel();

		initTextView();

		if (viewTabs != null) {
			viewTabs.add(
					LocalizedResources.applicationResources.getString("design"),
					designSplitPane);
			if (validateSchema()) {

				JScrollPane sp = new JScrollPane(schemaPane,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				sp.setWheelScrollingEnabled(true);

				schemaPaneLineNumbers = new LineNumbersAnnotatedScrollPane(
						schemaPane, sp);
				viewTabs.add(LocalizedResources.applicationResources
						.getString("schema"), schemaPaneLineNumbers);
				schemaPaneLineNumbers.showLineNumbers(showLineNumbers);

			}
		}
		addDocumentToIde();

		showDocument();

	}

	private void addDocumentToIde() {
		if (open)
			return;

		open = true;

		setProperty(IDEditor.PROP_MESSAGE_VIEW, null, workTabs);
		setProperty(IDEditor.PROP_EDITOR_VIEW, null, viewTabs);
	}

	public void copy() {
		getActiveTextPane().copy();

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

	private int findText(String text, boolean matchCase, boolean wrap,
			boolean forward, boolean incremental) {

		if (text == null || text.length() == 0)
			return -1;

		JTextPane findTextPane = getActiveTextPane();

		int start = (!incremental ? findTextPane.getCaretPosition()
				: findOffset);
		javax.swing.text.Document doc = findTextPane.getDocument();

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
					findTextPane.setCaretPosition(0);
					return findText(text, matchCase, false, forward,
							incremental);
				}
			} else {

				int selStart = findTextPane.getSelectionStart();
				int selEnd = findTextPane.getSelectionEnd();

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
					findTextPane.setCaretPosition(len);
					return findText(text, matchCase, false, forward,
							incremental);
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int findTextInSelectedLines(String text, boolean matchCase,
			boolean wrap, boolean forward, boolean incremental) {

		if (text == null || text.length() == 0)
			return -1;

		JTextPane findTextPane = getActiveTextPane();

		int slen = text.length();

		int start = (!incremental ? findTextPane.getCaretPosition()
				: findOffset);
		int selStart = findTextPane.getSelectionStart();
		int selEnd = findTextPane.getSelectionEnd();

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

		javax.swing.text.Document doc = findTextPane.getDocument();
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
					findTextPane.setCaretPosition(selStart);
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
					findTextPane.setCaretPosition(selEnd);
					return findTextInSelectedLines(text, matchCase, false,
							forward, incremental);
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int findRegularExpression(String regex, boolean matchCase,
			boolean wrap, boolean forward, boolean incremental) {

		if (regex == null || regex.length() == 0)
			return -1;

		JTextPane findTextPane = getActiveTextPane();

		int start = (!incremental ? findTextPane.getCaretPosition()
				: findOffset);
		javax.swing.text.Document doc = findTextPane.getDocument();

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
					findTextPane.setCaretPosition(0);
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
						findTextPane.setCaretPosition(len);
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

	private int findRegularExpressionInSelectedLines(String regex,
			boolean matchCase, boolean wrap, boolean forward,
			boolean incremental) {

		if (regex == null || regex.length() == 0)
			return -1;
		JTextPane findTextPane = getActiveTextPane();

		int start = (!incremental ? findTextPane.getCaretPosition()
				: findOffset);
		int selStart = findTextPane.getSelectionStart();
		int selEnd = findTextPane.getSelectionEnd();

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

		javax.swing.text.Document doc = findTextPane.getDocument();

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
							findTextPane.setCaretPosition(findOffset
									+ findLength);
						return findOffset;
					}
				}

				if (wrap) {
					findTextPane.setCaretPosition(selStart);
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
						findTextPane.setCaretPosition(selEnd);
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
		JTextPane findTextPane = getActiveTextPane();
		javax.swing.text.Document doc = findTextPane.getDocument();
		try {
			doc.remove(findOffset, findLength);
			doc.insertString(findOffset, text, null);
		} catch (javax.swing.text.BadLocationException be) {
			be.printStackTrace();
		}
	}

	private void addHighlight(int offset, int len) {

		JTextPane findTextPane = getActiveTextPane();
		Highlighter dh = findTextPane.getHighlighter();

		try {
			if (findTextPane != null) {
				findTextPane.setSelectionStart(offset);
				findTextPane.setSelectionEnd(offset + len);
			}
			if (selectTag != null)
				dh.removeHighlight(selectTag);
			selectTag = dh.addHighlight(offset, offset + len, painter);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	private void addMultipleHighlight(int offset, int len) {

		JTextPane activeTextPane = getActiveTextPane();
		Highlighter dh = activeTextPane.getHighlighter();

		try {
			if (activeTextPane != null) {
				activeTextPane.setSelectionStart(offset);
				activeTextPane.setSelectionEnd(offset + len);
			}
			if (multipleSelectTags == null) {
				multipleSelectTags = new Vector<Object>(10, 4);
			}
			multipleSelectTags.add(dh.addHighlight(offset, offset + len,
					xpainter));

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	public void cut() {
		getActiveTextPane().cut();

		setFeature("cutButton", false);
		setFeature("cutMenuItem", false);
		setFeature("copyButton", false);
		setFeature("copyMenuItem", false);

	}

	public void paste() {
		getActiveTextPane().paste();
	}

	private void parseEncoding() {
		try {
			javax.swing.text.Document doc = textPane.getDocument();
			int offset = -1;
			int dlen = doc.getLength();
			for (int i = 0; i < dlen; i++) {
				if ((i == dlen - 1) || doc.getText(i, 1).charAt(0) == '\n') {
					offset = i;
					break;
				}
			}
			String line = doc.getText(0, offset);
			int index = 0;
			if ((index = line.indexOf("encoding")) != -1) {
				index += "encoding".length();
				while (line.charAt(index) != '=')
					index++;
				index++;
				StringTokenizer st = new StringTokenizer(line.substring(index),
						"'\"");
				String encoding = st.nextToken().trim();
				String[] enc = CommonUtils.encodings;
				boolean found = false;
				for (int i = 0; i < enc.length; i++) {
					if (encoding.equalsIgnoreCase(enc[i])) {
						docInfo.setEncoding(encoding);
						found = true;
						break;
					}
				}
				if (!found) {
					docInfo.setEncoding("UTF-8");
				}
			} else {
				docInfo.setEncoding("UTF-8");
			}
		} catch (Exception e) {
			docInfo.setEncoding("UTF-8");
			e.printStackTrace();
		}
	}

	private String prettyPrint(String xml) {
		String ret = xml;

		try {
			if (transformer != null) {
				StringReader sr = new StringReader(xml);
				StreamSource xmlSource = new StreamSource(sr);

				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult(sw);
				transformer.transform(xmlSource, result);

				ret = sw.toString();
			}
		} catch (Exception e) {

		}

		return ret;

	}

	public synchronized void format() {

		if (textMode && !wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("invalid.document"));
			return;
		}

		try {
			XMLDocument xmldoc = (XMLDocument) textPane.getDocument();
			String text = xmldoc.getText(0, xmldoc.getLength());
			xmldoc.replace(0, xmldoc.getLength(), prettyPrint(text));
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean isWellFormed() {

		if (!wellFormed) {

			try {
				xmlTree.removeTreeSelectionListener(xmlTreeListener);
				designTree.removeTreeSelectionListener(designTreeListener);
				xmlCheckHandler = (DefaultErrorHandler) getXMLCheckHandler(true);

				XMLDocument xmldoc = (XMLDocument) textPane.getDocument();
				xmlModel.refreshDocument(xmldoc.getText(0, xmldoc.getLength()),
						xmlCheckHandler, null);

				XMLTreeModel xtm = new XMLTreeModel(xmlModel.getDocument(),
						xmlModel.isParsed());
				xmlTree.setModel(xtm);
				designTree.setModel(xtm);
				initCharacterCodes();

				this.refresh = true;
				wellFormed = true;

				xmlTree.addTreeSelectionListener(xmlTreeListener);
				designTree.addTreeSelectionListener(designTreeListener);
				showDocument();
			} catch (SAXParseException se) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				addXMLCheckView(xmlCheckHandler);
			}
		}

		if (wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("document.insync"));
		} else {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("document.not.insync"));
		}
		return wellFormed;
	}

	private synchronized void refresh() {

		Document doc = textPane.getDocument();

		DefaultErrorHandler xmlErrorHandler = null;
		try {
			xmlTree.removeTreeSelectionListener(xmlTreeListener);
			designTree.removeTreeSelectionListener(designTreeListener);
			xmlCheckHandler = (DefaultErrorHandler) getXMLCheckHandler(true);
			xmlErrorHandler = getSchemaErrorHandler();
			xmlModel.refresh(doc.getText(0, doc.getLength()), xmlCheckHandler,
					xmlErrorHandler);

			XMLTreeModel xtm = new XMLTreeModel(xmlModel.getDocument(),
					xmlModel.isParsed());

			xmlTree.setModel(xtm);
			designTree.setModel(xtm);
			initCharacterCodes();

			this.refresh = true;
			wellFormed = true;

			showDocument();
			xmlTree.addTreeSelectionListener(xmlTreeListener);
			designTree.addTreeSelectionListener(designTreeListener);
		} catch (SAXParseException se) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			addXMLCheckView(xmlCheckHandler);
			addSchemaErrorView(xmlErrorHandler);
		}

		if (wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("document.insync"));

		} else {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("document.not.insync"));

		}
	}

	private void addXMLCheckView(DefaultErrorHandler errorHandler) {
		if (errorHandler != null) {
			if (workTabs != null) {
				String title = LocalizedResources.applicationResources
						.getString("xml.check");

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

	private DefaultErrorHandler getXMLCheckHandler(boolean errorHandler) {
		return (errorHandler ? new DefaultErrorHandler() : null);
	}

	private void parseDocument() throws Exception {
		xmlCheckHandler = getXMLCheckHandler(true);
		try {
			xmlModel.parseDocument(xmlCheckHandler);
			wellFormed = true;
		} catch (SAXParseException se) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			xmlModel.setParsed(true);
			addXMLCheckView(xmlCheckHandler);
			if (wellFormed) {
				setProperty(IDEditor.PROP_STATUS, null,
						LocalizedResources.applicationResources
								.getString("document.insync"));

			} else {
				setProperty(IDEditor.PROP_STATUS, null,
						LocalizedResources.applicationResources
								.getString("document.not.insync"));
			}
		}
	}

	public boolean openDocument() {
		if (open) {
			setProperty(IDEditor.PROP_EDITOR_VIEW, viewTabs, viewTabs);
			return true;
		}

		return openDoc();
	}

	public void toggleTextMode() {

		if (textMode && !wellFormed) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("invalid.document"));
			return;
		}

		textMode = !textMode;
		wellFormed = true;

		Document doc = textPane.getDocument();
		if (textMode)
			doc.addUndoableEditListener(undoHandler);
		else
			doc.removeUndoableEditListener(undoHandler);
		setProperty(IDEditor.PROP_TEXT_MODE, Boolean.valueOf(!textMode),
				Boolean.valueOf(textMode));
	}

	public boolean openDocumentAsText() {
		if (open) {
			return true;
		}
		textMode = true;
		setProperty(IDEditor.PROP_TEXT_MODE, Boolean.valueOf(!textMode),
				Boolean.valueOf(textMode));
		return openDoc();
	}

	private void addSchemaErrorView(DefaultErrorHandler errorHandler) {

		if (errorHandler != null && workTabs != null) {
			String title = LocalizedResources.applicationResources
					.getString("schema");

			boolean found = false;
			for (int i = 0; i < workTabs.getTabCount(); i++) {
				if (title.equals(workTabs.getTitleAt(i))) {
					ProblemsView sview = (ProblemsView) workTabs
							.getComponentAt(i);
					sview.setErrorHandler(errorHandler);
					sview.setTextPane(schemaPane);
					workTabs.setSelectedIndex(i);
					found = true;
					break;
				}
			}

			if (!found) {
				ProblemsView sview = new ProblemsView(errorHandler, schemaPane);
				workTabs.addTab(title, null, sview, null);
			}
		}
	}

	private DefaultErrorHandler getSchemaErrorHandler() {
		return (validateSchema() ? new DefaultErrorHandler() : null);
	}

	private boolean validateSchema() {
		return editorConfig != null && editorConfig.getValidateSchema();
	}

	private void buildSchemaTextView() throws Exception {
		schemaPane = new MichiganTextPane() {
			private static final long serialVersionUID = 3561756358498714783L;

			public void paint(Graphics g) {
				super.paint(g);
				if (schemaPaneLineNumbers != null) {
					schemaPaneLineNumbers.repaintLineNumbers();
				}
			}
		};

		int charactersPerTab = (project != null
				&& project.getProjectConfiguration() != null ? Integer
				.valueOf(project.getProjectConfiguration().getTabsize())
				: XMLEditorConfiguration.DEFAULT_CHARS_PER_TAB);

		XMLEditorKit ek = new XMLEditorKit(null,
				(editorConfig != null ? editorConfig.getStyleContext()
						: new XMLDefaultStyleContext()));

		schemaPane.setEditorKit(ek);
		setTabs(schemaPane, charactersPerTab);

		XMLDocument xmldoc = (XMLDocument) schemaPane.getDocument();

		readDocumentFromFile(xmlModel.getSchemaFile(), xmldoc, null);

		String text = xmldoc.getText(0, xmldoc.getLength());
		CharArrayReader cr = new CharArrayReader(text.toCharArray());
		XML10Parser parser = new XML10Parser(cr);
		parser.setErrorHandler(null);

		XMLDocumentElement documentElement = parser.Document(xmldoc, null);

		documentElement.applyAttributes();
		addSchemaAction();
		xmldoc.addDocumentListener(new SchemaTracker());
		xmldoc.addUndoableEditListener(undoHandler);
	}

	private void parseSchema() {
		DefaultErrorHandler errorHandler = null;
		try {

			if (wellFormed) {
				errorHandler = getSchemaErrorHandler();
				xmlModel.parseSchema(errorHandler);
				if (validateSchema()) {
					buildSchemaTextView();
				}
			}
		} catch (Exception e) {
		} finally {
			addSchemaErrorView(errorHandler);
		}
	}

	private boolean parse() {
		boolean success = false;
		try {

			parseDocument();
			parseSchema();
			success = setupDocument();
		} catch (Exception ex) {
			wellFormed = false;
			ex.printStackTrace();
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("xml.check.failed.text"));
		}
		return success;
	}

	public boolean initDocument() throws Exception {
		boolean success = xmlModel.initDocument();
		if (success) {
			initDocTree();
			initCharacterCodes();
			initDocViews();

			setFeature("findButton", true);
			setFeature("findMenuItem", true);

			dirty = true;
			updateSaveStatus();
		}
		return success;
	}

	public boolean newDocument() {
		if (open) {
			// it is possible this document was created but we did not yet save
			// and this document is being opened again.
			setProperty(IDEditor.PROP_EDITOR_VIEW, viewTabs, viewTabs);
			return true;
		}

		boolean success = false;

		try {
			wellFormed = true;
			parseSchema();
			success = initDocument();
		} catch (Exception ex) {
			ex.printStackTrace();
			wellFormed = false;
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("new.document.error"));
		}
		return success;
	}

	private boolean setupDocument() {
		File docFile = getDocumentFile();
		boolean success = false;

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

		if (wellFormed) {
			xmlModel.parseXmlPI(docFile);
			xmlModel.initMaps();
		}
		initDocTree();

		if (wellFormed) {
			initCharacterCodes();
		}
		initDocViews();

		setFeature("findButton", true);
		setFeature("findMenuItem", true);

		updateSaveStatus();

		success = true;

		if (wellFormed)
			getXMLTreeModel().setParsed(false);
		return success;
	}

	private boolean openDoc() {
		if (open)
			return true;

		boolean success = true;

		try {
			success = parse();
		} catch (Exception e) {
			setProperty(IDEditor.PROP_STATUS, null,
					LocalizedResources.applicationResources
							.getString("document.open.failed"));
			success = false;
		}
		return success;
	}

	private void makeDesignPanel() {
		designSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		designTree.expandRow(2);
		JScrollPane sp = new JScrollPane(designTree);
		String designTree = LocalizedResources.applicationResources
				.getString("design.tree");
		sp.setBorder(new TitledBorder(new EmptyBorder(5, 5, 5, 5), designTree));
		sp.setMinimumSize(new Dimension(200, 200));
		designSplitPane.setTopComponent(sp);
		designSplitPane.setDividerLocation(0.5f);
		designSplitPane.setContinuousLayout(true);
	}

	public XMLSchema getSchema() {
		return xmlModel.getSchema();
	}

	public String getStyleSheet() {
		return docInfo.getStyleSheet();
	}

	public XMLNode getComplexTypeNode() {
		return complexTypeNode;
	}

	public boolean isDocumentParsed() {
		return xmlModel.isParsed();
	}

	public org.w3c.dom.Document getXml() {
		return xmlModel.getDocument();
	}

	public TreePath getComplexTypePath() {
		return complexTypePath;
	}

	public TreePath getSimpleTypePath() {
		return simpleTypePath;
	}

	public XMLNode getSimpleTypeNode() {
		return simpleTypeNode;
	}

	public void setComplexTypePath(TreePath path) {
		this.complexTypePath = path;
	}

	public void setSimpleTypePath(TreePath path) {
		this.simpleTypePath = path;
	}

	public void setComplexTypeNode(XMLNode node) {
		this.complexTypeNode = node;
	}

	public void setSimpleTypeNode(XMLNode node) {
		this.simpleTypeNode = node;
	}

	private void setSelectionPath(TreePath p) {

		if (p != null && wellFormed) {
			XMLNode lnode = (XMLNode) p.getLastPathComponent();
			if (lnode == selectedTypeNode)
				return;
			int nodeType = lnode.getDomNode().getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				SchemaNode type = CommonUtils.getTypeNode(xmlModel, lnode,
						standalone);
				String typeNodeName = type.getNodeName();

				if (typeNodeName.equals("complexType")
						&& !type.isSimpleContent()) {
					complexTypePath = p;
					selectedTypeNode = complexTypeNode = lnode;
					complexTypePanel = new ComplexTypePanel(type, this);
					designSplitPane.setBottomComponent(complexTypePanel);
					designSplitPane.setDividerLocation(0.5f);
				} else if (type.isSimpleContent()) {
					selectedTypeNode = complexTypeNode = simpleTypeNode = lnode;
					complexTypePath = simpleTypePath = p;
					complexTypePanel = new ComplexTypePanel(type, this);
					designSplitPane.setBottomComponent(complexTypePanel);
					designSplitPane.setDividerLocation(0.5f);
				} else {
					selectedTypeNode = simpleTypeNode = lnode;
					simpleTypePath = p;
					SimpleTypePanel simpleTypePanel = new SimpleTypePanel(type,
							this);
					designSplitPane.setBottomComponent(simpleTypePanel);
					designSplitPane.setDividerLocation(0.5f);
				}
			} else if (nodeType == Node.DOCUMENT_NODE
					|| nodeType == Node.DOCUMENT_TYPE_NODE) {
				selectedTypeNode = lnode;
				simpleTypeNode = null;
				complexTypeNode = null;
				JLabel ndl = new JLabel(
						LocalizedResources.applicationResources
								.getString("no.design.support.label"),
						null, JLabel.CENTER);
				designSplitPane.setBottomComponent(ndl);
				designSplitPane.setDividerLocation(0.5f);
			}

		}
	}

	public void setUndoInProgress(boolean flag) {

		if (textMode && !flag) {
			wellFormed = false;
			isWellFormed();
		}

		undoInProgress = flag;
	}

	public boolean getUndoInProgress() {
		return undoInProgress;
	}

	private class XMLTreeListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			if (textMode && (!wellFormed || undoInProgress)) {
				return;
			}
			if (!locked) {
				TreePath p = e.getNewLeadSelectionPath();
				if (p != null) {
					setSelectionPath(p);
					XMLNode lnode = (XMLNode) p.getLastPathComponent();
					if (lnode != null) {
						XMLDocument xmldoc = (XMLDocument) textPane
								.getDocument();
						Element ele = xmldoc.findElementWithNode(lnode);
						if (ele != null) {
							int start = ele.getStartOffset();
							int end = ele.getEndOffset();

							addHighlight(start, (end - start));
						}
					}
				}
			}
		}
	}

	public void setAttribute(String attr, String text) {
		undoHandler.beginSession();
		Attr attrNode = complexTypeNode.getAttributeNode(attr);

		if (text == null)
			text = "";

		if (attrNode == null) {
			if (attr.startsWith(CommonUtils.XMLNS_PREFIX))
				attrNode = xmlModel.getDocument().createAttributeNS(
						XMLConstants.XMLNS_ATTRIBUTE_NS_URI, attr);
			else
				attrNode = xmlModel.getDocument().createAttribute(attr);
			XMLNode newNode = complexTypeNode.setAttribute(attrNode);
			attrNode.setNodeValue(text);

			// fire tree model event for element tree
			Object[] path = { complexTypeNode };
			int pos = complexTypeNode.index(newNode);
			int treePosition = complexTypeNode.getOutlineChildIndex(newNode);
			int[] childIndices = { treePosition };
			Object[] children = { newNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = null;
			if (complexTypePanel.getElementTree() != null) {
				xtm = (XMLTreeModel) complexTypePanel.getElementTree()
						.getModel();
				xtm.fireTreeNodesInserted(tme);
			}

			// fire tree model event for xml tree
			xtm = (XMLTreeModel) xmlTree.getModel();
			TreePath xmlTreePath = complexTypePath;
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

			locked = true;
			xtm.fireTreeNodesInserted(tme);
			locked = false;

			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(XMLEditor.this,
						complexTypeNode, newNode, pos, null,
						XMLUndoableEdit.NODE_INSERT);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				fireUndoableEditEvent(ev);
			}

		} else {
			Object[] path = { complexTypeNode };

			XMLNode changedNode = complexTypeNode.findAttribute(attrNode
					.getName());
			changedNode.setParsed(false);
			int pos = complexTypeNode.index(changedNode);
			int treePosition = complexTypeNode
					.getOutlineChildIndex(changedNode);
			int[] childIndices = { treePosition };
			String oldValue = attrNode.getNodeValue();
			attrNode.setNodeValue(text);
			Object[] children = { changedNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = (XMLTreeModel) xmlTree.getModel();
			locked = true;
			xtm.fireTreeNodesChanged(tme);
			locked = false;
			if (!textMode && !undoInProgress) {
				XMLUndoableEdit ue = new XMLUndoableEdit(XMLEditor.this,
						complexTypeNode, changedNode, pos, oldValue,
						XMLUndoableEdit.NODE_CHANGE);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				fireUndoableEditEvent(ev);
			}
		}
		undoHandler.endSession();
		wellFormed = true;
	}

	public void fireUndoableEditEvent(UndoableEditEvent e) {
		for (int i = 0; i < undoableEditListeners.size(); i++) {
			UndoableEditListener lis = (UndoableEditListener) undoableEditListeners
					.elementAt(i);
			lis.undoableEditHappened(e);
		}
	}

	public void print() {
		try {
			saveDocument();
			File file = new File(docInfo.getPath(), docInfo.getName());
			new BasicPrint(file, DocFlavor.INPUT_STREAM.AUTOSENSE);
		} catch (Exception e) {
			if (integrated)
				JOptionPane.showMessageDialog(
						textPane,
						LocalizedResources.applicationResources
								.getString("printing.error:") + ":\n" + e,
						LocalizedResources.applicationResources
								.getString("printing.error"),
						JOptionPane.ERROR_MESSAGE);
		}
	}

	public DocumentDescriptor getDocumentDescriptor() {
		return docInfo;
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

	private File getTransformOutputFile() {
		File defaultPath = new File(docInfo.getPath(), "myFile");
		JFileChooser fileChooser = new JFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("save.transform.output"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources
						.getString("save.transform.output"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		return file;
	}

	private File getXsltFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("open.xslt.file"));

		int ok = fileChooser.showDialog(sourcePanel,
				LocalizedResources.applicationResources.getString("xslt.file"));
		File file = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		return file;
	}

	private void transform() {
		try {
			if (textMode && !wellFormed) {
				setProperty(IDEditor.PROP_STATUS, null,
						LocalizedResources.applicationResources
								.getString("invalid.document"));
				return;
			}

			File xslt = getXsltFile();
			if (xslt == null)
				return;

			TransformerFactory tffactory = TransformerFactory.newInstance();
			Transformer transformer = tffactory.newTransformer();

			File ofile = getTransformOutputFile();
			if (ofile == null)
				return;

			FileOutputStream os = new FileOutputStream(ofile);

			DOMSource source = new DOMSource(xmlModel.getDocument());
			StreamResult result = new StreamResult(os);

			StreamSource xsltSource = new StreamSource(xslt);
			transformer = tffactory.newTransformer(xsltSource);
			transformer.transform(source, result);
			os.close();
		} catch (Exception e) {
			if (integrated)
				JOptionPane.showMessageDialog(
						sourcePanel,
						LocalizedResources.applicationResources
								.getString("xslt.failed") + ":\n" + e,
						LocalizedResources.applicationResources
								.getString("xslt.transform"),
						JOptionPane.ERROR_MESSAGE);
		}
	}

	private void removeAllHighlights() {
		Highlighter dh = textPane.getHighlighter();
		dh.removeAllHighlights();
		selectTag = null;
	}

	public void endUndoEditSession() {
		if (textMode)
			undoHandler.endSession();
	}

	public void beginUndoEditSession() {
		if (textMode)
			undoHandler.beginSession();
	}

	private class EditorPopup extends JPopupMenu {
		private static final long serialVersionUID = 7474514977981356951L;

		public EditorPopup(String title) {
			super(title);
			init();
		}

		private void init() {
			JMenuItem mi = null;

			mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("save"),
					IconLoader.saveIcon);
			mi.setMnemonic('s');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					saveDocument();
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("save.as..."),
					IconLoader.saveAsIcon);
			mi.setMnemonic('a');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					saveDocumentAs();
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("save.as.pdf"),
					IconLoader.saveAsIcon);
			mi.setEnabled(wellFormed);
			mi.setMnemonic('p');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					saveDocumentAsPdf();
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("save.schema.as"),
					IconLoader.saveAsIcon);
			mi.setMnemonic('m');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					saveSchemaAs();
				}
			});
			mi.setEnabled(xmlModel.getSchemaFile() != null);
			add(mi);

			this.addSeparator();

			mi = new JCheckBoxMenuItem(
					LocalizedResources.applicationResources
							.getString("toggle.text.mode"));
			mi.setMnemonic('x');
			mi.setEnabled(wellFormed);
			mi.setSelected(textMode);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					toggleTextMode();
				}
			});
			add(mi);

			mi = new JCheckBoxMenuItem(
					LocalizedResources.applicationResources
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

			mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("go.to"));

			mi.setMnemonic('g');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GotoAction action = new GotoAction(textPane);
					action.actionPerformed(e);
					setVisible(false);
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("xpath.search"));
			mi.setMnemonic('x');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					XPathAction action = new XPathAction();
					action.actionPerformed(e);
					setVisible(false);
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
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

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("refresh"));
			mi.setEnabled(wellFormed);
			mi.setMnemonic('f');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (integrated)
							textPane.setCursor(Cursor
									.getPredefinedCursor(Cursor.WAIT_CURSOR));
						refresh();
						if (integrated)
							textPane.setCursor(Cursor
									.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					} catch (Exception ex) {
						setProperty(IDEditor.PROP_STATUS, null,
								LocalizedResources.applicationResources
										.getString("parse.failed"));

					}
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("format"));
			mi.setMnemonic('r');
			mi.setEnabled(wellFormed);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					try {
						setVisible(false);
						if (integrated)
							textPane.setCursor(Cursor
									.getPredefinedCursor(Cursor.WAIT_CURSOR));
						format();
						if (integrated)
							textPane.setCursor(Cursor
									.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					} catch (Exception ex) {
						if (integrated) {
							JOptionPane.showMessageDialog(
									sourcePanel,
									LocalizedResources.applicationResources
											.getString("format.failed")
											+ ":\n"
											+ e,
									LocalizedResources.applicationResources
											.getString("format.error"),
									JOptionPane.ERROR_MESSAGE);
						} else
							setProperty(IDEditor.PROP_STATUS, null,
									LocalizedResources.applicationResources
											.getString("format.failed"));
					}
				}
			});
			add(mi);

			this.addSeparator();

			add(buildValidateMenu());

			this.addSeparator();
			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("xsl.transform"));
			mi.setEnabled(wellFormed);
			mi.setMnemonic('f');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					transform();
				}
			});
			add(mi);
			this.addSeparator();

			mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("close"),
					IconLoader.closeIcon);
			mi.setMnemonic('s');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					closeDocument();
				}
			});
			add(mi);

		}

		private JMenu buildValidateMenu() {
			JMenu menu = new JMenu(
					LocalizedResources.applicationResources
							.getString("validate"),
					false);
			menu.setMnemonic('v');
			menu.setEnabled(wellFormed);

			JMenuItem mi = null;

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("validate.dtd"),
					IconLoader.validateDtdIcon);
			mi.setMnemonic('d');
			mi.setEnabled(wellFormed);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (integrated)
						textPane.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
					validateUsingDtd();
					if (integrated)
						textPane.setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					setVisible(false);
				}
			});

			menu.add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("validate.using.schema"),
					IconLoader.validateSchemaIcon);
			mi.setMnemonic('s');
			mi.setEnabled(wellFormed);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					if (integrated)
						textPane.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
					validateUsingSchema();
					if (integrated)
						textPane.setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				}
			});

			menu.add(mi);

			return menu;
		}
	}

	private class SchemaAction extends AbstractAction {
		private static final long serialVersionUID = -6494372780489422037L;

		public void actionPerformed(ActionEvent e) {
			if (schemaPane != null) {
				doContentAssist(schemaPane);
			}
		}
	}

	private class DocumentAction extends AbstractAction {

		private static final long serialVersionUID = 7982872404124626528L;

		public void actionPerformed(ActionEvent e) {
			try {
				if (textMode && !isWellFormed()) {
					doContentAssist(textPane);
					return;
				}

				int where = textPane.getCaretPosition();

				XMLDocument document = (XMLDocument) textPane.getDocument();
				XMLAbstractElement root = document.getRootElement();
				XMLAbstractElement ele = root.positionToElement(where);

				if (ele == null)
					return;

				if (ele instanceof XMLStartTagElement
						|| (ele.getParentElement() instanceof XMLStartTagElement)) {
					if (ele instanceof XMLStartTagElement)
						showAttributes(ele.getXMLNode());
					else
						showAttributes(((XMLAbstractElement) ele
								.getParentElement()).getXMLNode());
					return;
				}

				boolean endtag = false;

				if ((ele instanceof XMLWhiteSpaceElement)
						&& (ele.getParentElement() != null && (ele
								.getParentElement() instanceof XMLEndTagElement))) {
					endtag = true;
				}

				XMLAbstractElement parent = (endtag ? (XMLAbstractElement) ele
						.getParentElement().getParentElement()
						.getParentElement() : (XMLAbstractElement) ele
						.getParentElement());

				while (parent != null && !(parent instanceof XMLElement)
						&& !(parent instanceof XMLDocumentElement)) {

					ele = parent;
					if (ele instanceof XMLStartTagElement) {
						showAttributes(ele.getXMLNode());
						return;
					}

					parent = (XMLAbstractElement) ele.getParentElement();
				}
				if (parent != null)
					insertSubElement(parent.getXMLNode(), ele.getXMLNode());

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	private class ProblemsView extends JScrollPane {
		private static final long serialVersionUID = -1000641405211269349L;

		private DefaultErrorHandler errorHandler;

		private JTextPane errorTextPane;

		private JTable table;

		public ProblemsView(DefaultErrorHandler eh, JTextPane tp) {
			super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

			this.errorTextPane = tp;
			this.errorHandler = eh;

			table = new JTable() {
				private static final long serialVersionUID = -4453468391303526703L;

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
								if (errorTextPane == textPane)
									viewTabs.setSelectedComponent(textPaneLineNumbers);
								else
									viewTabs.setSelectedComponent(schemaPaneLineNumbers);
								Document doc = errorTextPane.getDocument();
								Element root = doc.getDefaultRootElement();
								if (root != null) {
									Element le = root.getElement(pp.getLine() - 1);
									if (le != null) {
										addHighlight(le.getStartOffset(),
												pp.getColumn() - 1);
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

	private class ProblemCellRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = -5320979226167306642L;

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

	private class UndoableEditHandler implements UndoableEditListener {

		private XMLCompoundEdit cedit;

		private int recursion;

		public void undoableEditHappened(UndoableEditEvent e) {
			if (recursion == 0) {
				fireUndoableEditEvent(e);
				return;
			}

			if (cedit == null) {
				cedit = new XMLCompoundEdit(XMLEditor.this);
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

	private class DocumentTracker implements DocumentListener {

		private void documentChanged(DocumentEvent e) {

			dirty = true;
			updateSaveStatus();
			if (textMode && !auto && !locked && !undoInProgress) {
				wellFormed = false;
				selectedTypeNode = null;
				if (xmlTree != null && xmlTree.getModel() != null) {
					xmlTree.removeTreeSelectionListener(xmlTreeListener);
				}

				if (designTree != null && designTree.getModel() != null) {
					designTree.removeTreeSelectionListener(designTreeListener);
				}

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

			if (!undoInProgress && wellFormed && !locked
					&& selectedTypeNode != null) {
				XMLTreeModel xtm = getXMLTreeModel();
				if (xtm != null) {
					TreePath path = xtm.getPathToRoot(selectedTypeNode);
					selectedTypeNode = null;
					setTreeSelectionPath(path);
				}
			}

		}

		private void processEvent(DocumentEvent e) {
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

	private class SchemaTracker implements DocumentListener {

		private void documentChanged(DocumentEvent e) {

			ActionListener timerListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new SchemaParser());
				}
			};

			Timer t = new Timer(2000, timerListener);
			t.setRepeats(false);
			t.start();

		}

		private void processEvent(DocumentEvent e) {
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

	private class AsyncParsing implements Runnable {
		public void run() {
			if (!wellFormed) {
				setProperty(IDEditor.PROP_STATUS, null, "");
				if (integrated)
					textPane.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
				isWellFormed();
				if (integrated)
					textPane.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	private class SchemaParser implements Runnable {
		public void run() {
			DefaultErrorHandler errorHandler = null;
			try {
				XMLDocument xmldoc = (XMLDocument) schemaPane.getDocument();

				String text = xmldoc.getText(0, xmldoc.getLength());
				CharArrayReader cr = new CharArrayReader(text.toCharArray());
				errorHandler = new DefaultErrorHandler();

				synchronized (XMLBuilder.class) {
					DocumentBuilder builder = XMLBuilder.validatingBuilder;
					builder.setErrorHandler(errorHandler);
					builder.parse(new InputSource(cr));
				}

				cr = new CharArrayReader(text.toCharArray());
				XML10Parser parser = new XML10Parser(cr);
				parser.setErrorHandler(null);

				XMLDocumentElement documentElement = parser.Document(xmldoc,
						null);

				documentElement.applyAttributes();
			} catch (Exception e) {

			} finally {
				addSchemaErrorView(errorHandler);
			}
		}
	}

	private class XPathAction extends AbstractAction {
		private static final long serialVersionUID = -7519202242576605789L;
		private XPath xPath;

		public XPathAction() {
			xPath = XPathFactory.newInstance().newXPath();

			HashMap<String, String> namespace = xmlModel.getDocumentNamespace();

			Set<String> keys = namespace.keySet();
			if (keys.size() > 0) {
				NameSpaceContextImpl namespaceContext = new NameSpaceContextImpl();
				Iterator<String> it = keys.iterator();

				while (it.hasNext()) {
					String uri = it.next();
					String prefix = namespace.get(uri);
					namespaceContext.add(prefix, uri);
				}

				xPath.setNamespaceContext(namespaceContext);
			}
		}

		private Point getPopupLocation() {

			Point p = new Point(100, 200);

			EditUtils.convertPointToScreen(p, textPane);

			return p;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				xPathQuery = GenericInput.showDialog(textPane,
						LocalizedResources.applicationResources
								.getString("xpath.search"),
						LocalizedResources.applicationResources
								.getString("xpath"), null, xPathQuery,
						getPopupLocation());
				if (xPathQuery != null) {
					xpathSearch(xPathQuery);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private void xpathSearch(String query) {
			try {
				clearXPath();

				xPath.reset();
				setProperty(
						IDEditor.PROP_STATUS,
						null,
						LocalizedResources.applicationResources
								.getString("xpath.query") + query);

				NodeList nodeList = null;

				try {
					nodeList = (NodeList) xPath.evaluate(query,
							xmlModel.getDocument(), XPathConstants.NODESET);
				} catch (Exception e) {

				}

				int nodeCount = (nodeList != null ? nodeList.getLength() : 0);
				if (nodeCount > 0) {
					XMLNode rootNode = getXMLTreeModel().getRootElement();

					for (int i = 0; i < nodeCount; i++) {
						Node node = nodeList.item(i);

						XMLNode match = rootNode.findXMLNodeWithNode(node);
						if (match != null) {
							XMLDocument xmldoc = (XMLDocument) textPane
									.getDocument();
							Element ele = xmldoc.findElementWithNode(match);
							if (ele != null) {
								int start = ele.getStartOffset();
								int end = ele.getEndOffset();

								addMultipleHighlight(start, (end - start));
							}
						}
					}

					JOptionPane.showMessageDialog(
							textPane,
							query
									+ ":\n"
									+ nodeCount
									+ " "
									+ LocalizedResources.applicationResources
											.getString("xpath.nodes"),
							LocalizedResources.applicationResources
									.getString("xpath.result"),
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					String result = xPath.evaluate(query,
							xmlModel.getDocument());
					if (result != null && result.trim().length() > 0) {
						setProperty(
								IDEditor.PROP_STATUS,
								null,
								LocalizedResources.applicationResources
										.getString("xpath.result") + result);

						JOptionPane.showMessageDialog(textPane, query + ":\n"
								+ result,
								LocalizedResources.applicationResources
										.getString("xpath.result"),
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane
								.showMessageDialog(
										textPane,
										query
												+ ":\n"
												+ LocalizedResources.applicationResources
														.getString("xpath.result.empty"),
										LocalizedResources.applicationResources
												.getString("xpath.result"),
										JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);

				String msg = sw.toString();
				msg = msg.substring(0, msg.indexOf('\n'));
				JOptionPane.showMessageDialog(
						textPane,
						LocalizedResources.applicationResources
								.getString("xpath.error") + ":\n" + msg,
						LocalizedResources.applicationResources
								.getString("xpath.error"),
						JOptionPane.ERROR_MESSAGE);

				try {
					xPathQuery = GenericInput.showDialog(textPane,
							LocalizedResources.applicationResources
									.getString("xpath.search"),
							LocalizedResources.applicationResources
									.getString("xpath"), null, xPathQuery,
							getPopupLocation());
					if (xPathQuery != null) {
						xpathSearch(xPathQuery);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public Vector<Object> getAttributeContentAssist(XMLNode parent,
			int documentOffset) {
		return null;
	}

	public Vector<Object> getElementContentAssist(XMLNode node, XMLNode before,
			int documentOffset) {
		return null;
	}

	public Vector<Object> getParserContentAssist(int documentOffset) {
		return null;
	}

	private class DesignTreePopup extends JPopupMenu {
		private static final long serialVersionUID = 1316799585900389214L;
		private JMenuItem addChild;

		public DesignTreePopup(String title) {
			super(title);
			init();
		}

		private void init() {
			JMenuItem mi = null;
			setAddChild(mi = new JMenuItem(new AddChildAction(
					LocalizedResources.applicationResources
							.getString("add.child"))));
			mi.setMnemonic('a');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);
		}

		public void setAddChild(JMenuItem addChild) {
			this.addChild = addChild;
		}

		public JMenuItem getAddChild() {
			return addChild;
		}
	}

	private class AddChildAction extends AbstractAction {
		private static final long serialVersionUID = 7525974290695236103L;

		public AddChildAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			addChild();
		}

	}

}