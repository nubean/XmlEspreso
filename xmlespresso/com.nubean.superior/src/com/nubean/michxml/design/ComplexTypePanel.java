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

package com.nubean.michxml.design;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.tree.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.design.DefaultTreeCellRenderer;
import com.nubean.michide.HtmlPanel;
import com.nubean.michutil.IconLoader;
import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.SchemaTreeModel;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLTreeModel;
import com.nubean.michxml.XMLUndoableEdit;
import com.nubean.michxml.design.attr.AttributePropertySheet;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

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

public class ComplexTypePanel extends JPanel {

	private static final long serialVersionUID = 895069290679850440L;

	private SchemaTreeModel treeModel;

	private Component apanel;

	private XMLAbstractEditor editor;

	private TreeCellRenderer treeCellRenderer;

	private JTree typeTree, elementTree;

	private SchemaNode typeNode;

	private SchemaPopup spopup;

	private ElementPopup epopup;

	public JTree getTypeTree() {
		return typeTree;
	}

	public JTree getElementTree() {
		return elementTree;
	}

	private Component makeAttributePanel(SchemaNode node) {
		AttributePropertySheet ps = new AttributePropertySheet(node, editor);
		JScrollPane sp = new JScrollPane(ps,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		String elementAttrs = LocalizedResources.applicationResources
				.getString("element.attributes");
		sp.setBorder(new TitledBorder(new EmptyBorder(5, 5, 5, 5), elementAttrs));

		return sp;
	}

	private void showSchemaPopup(MouseEvent e) {
		if (e.isPopupTrigger()
				|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {

			if (!editor.isWellFormed())
				return;

			TreePath selPath = typeTree.getPathForLocation(e.getX(), e.getY());

			if (selPath == null)
				return;

			typeTree.setSelectionPath(selPath);
			SchemaNode node = (SchemaNode) selPath.getLastPathComponent();
			if (!node.getNodeName().equals("element")
					&& !node.getNodeName().equals("any"))
				return;

			SchemaTreeModel stm = (SchemaTreeModel) getTypeTree().getModel();
			XMLTreeModel etm = (XMLTreeModel) getElementTree().getModel();

			String pattern = stm.getPattern();
			RegExp re = new RegExp(pattern);
			Automaton automata = re.toAutomaton();

			pattern = "^" + pattern + "$";
			Pattern compile = Pattern.compile(pattern);

			if (CommonUtils.getInsertBefore(editor.getXMLModel(), compile,
					automata, etm, node, getSelectedRow()) >= 0) {
				spopup.insert.setEnabled(true);
			} else {
				spopup.insert.setEnabled(false);
			}
			spopup.show(typeTree, e.getX(), e.getY());
		}
	}

	private void showElementPopup(MouseEvent e) {
		if (e.isPopupTrigger()
				|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {

			if (!editor.isWellFormed())
				return;

			TreePath selPath = elementTree.getPathForLocation(e.getX(),
					e.getY());
			
			if(selPath == null)
				return;
			
			elementTree.setSelectionPath(selPath);
			XMLNode node = (XMLNode) selPath.getLastPathComponent();
			SchemaTreeModel ctm = (SchemaTreeModel) typeTree.getModel();
			SchemaNode schemaNode = (SchemaNode) ctm.getRoot();
			if (node == editor.getComplexTypeNode()) {
				epopup.remove.setEnabled(false);
				epopup.insertComment.setEnabled(true);
				epopup.insertProc.setEnabled(true);
				if (schemaNode.isMixed()) {
					epopup.insertTextAfter.setEnabled(true);
					epopup.insertTextBefore.setEnabled(false);
				} else {
					epopup.insertTextAfter.setEnabled(false);
					epopup.insertTextBefore.setEnabled(false);
				}
			} else if (node.getParent() == editor.getComplexTypeNode()) {
				epopup.remove.setEnabled(true);
				epopup.insertComment.setEnabled(false);
				epopup.insertProc.setEnabled(false);
				if (schemaNode.isMixed()) {
					epopup.insertTextAfter.setEnabled(true);
					epopup.insertTextBefore.setEnabled(true);
				} else {
					epopup.insertTextAfter.setEnabled(false);
					epopup.insertTextBefore.setEnabled(false);
				}
			} else {
				epopup.remove.setEnabled(false);
				epopup.insertTextAfter.setEnabled(false);
				epopup.insertTextBefore.setEnabled(false);
				epopup.insertComment.setEnabled(false);
				epopup.insertProc.setEnabled(false);
			}
			epopup.show(elementTree, e.getX(), e.getY());
		}
	}

	private void initMainPanel() {
		spopup = new SchemaPopup("Complex Type");
		epopup = new ElementPopup("Elements");

		setLayout(new BorderLayout());

		String documentation = typeNode.getDocumentation();
		HtmlPanel htmlPane = null;
		if (documentation != null)
			htmlPane = new HtmlPanel(documentation, IconLoader.backIcon,
					IconLoader.homeIcon);

		if (htmlPane != null) {
			// Build split-pane view
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					initTypePanel(), htmlPane);
			splitPane.setContinuousLayout(true);
			splitPane.setDividerLocation(200);

			add(splitPane, BorderLayout.CENTER);
		} else {
			add(initTypePanel(), BorderLayout.CENTER);
		}
	}

	private Component initSimpleContent() {
		SchemaNode simpleContent = CommonUtils.getChildByName(typeNode,
				"simpleContent");
		SchemaNode erNode = CommonUtils.getChildByName(simpleContent,
				"extension");
		if (erNode == null) {
			erNode = CommonUtils.getChildByName(simpleContent, "restriction");
		}

		Component apanel = null;

		if (erNode != null)
			apanel = makeAttributePanel(erNode);
		editor.setSimpleTypeNode(editor.getComplexTypeNode());
		editor.setSimpleTypePath(editor.getComplexTypePath());

		if (apanel != null) {
			SimpleTypePanel sp = new SimpleTypePanel(typeNode, editor);
			JSplitPane hsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					sp, apanel);
			hsplitPane.setContinuousLayout(true);
			hsplitPane.setDividerLocation(400);

			return hsplitPane;
		} else {
			return new SimpleTypePanel(typeNode, editor);
		}
	}

	private void initComplexType() {
		apanel = makeAttributePanel(typeNode);
		treeModel = new SchemaTreeModel(typeNode);
	}

	public SchemaNode getTypeNode() {
		return typeNode;
	}

	public void doRemove() {
		editor.beginUndoEditSession();
		TreePath p = elementTree.getSelectionPath();
		if (p != null) {
			XMLNode node = (XMLNode) p.getLastPathComponent();
			int treePosition = editor.getComplexTypeNode()
					.getOutlineChildIndex(node);
			int pos = editor.getComplexTypeNode().removeChild(node);

			// fire tree model event for element tree
			Object[] path = { editor.getComplexTypeNode() };
			int[] childIndices = { treePosition };
			Object[] children = { node };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = (XMLTreeModel) elementTree.getModel();
			xtm.fireTreeNodesRemoved(tme);

			// fire tree model event for xml tree
			TreePath xmlTreePath = editor.getComplexTypePath();
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);
			xtm = (XMLTreeModel) editor.getXMLTreeModel();
			editor.setLocked(true);
			xtm.fireTreeNodesRemoved(tme);
			editor.setLocked(false);

			if (!editor.isTextMode() && !editor.getUndoInProgress()) {
				XMLUndoableEdit ue = new XMLUndoableEdit(editor,
						editor.getComplexTypeNode(), node, pos, null,
						XMLUndoableEdit.NODE_REMOVE);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				editor.fireUndoableEditEvent(ev);
			}

		}
		editor.endUndoEditSession();
	}

	private Component initTypePanel() {
		if (typeNode.isSimpleContent()) {
			return initSimpleContent();
		}

		initComplexType();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// Set up the type tree
		typeTree = new JTree(treeModel);
		typeTree.setCellRenderer(treeCellRenderer);
		typeTree.makeVisible(treeModel.getPathToFirstLeaf());

		typeTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showSchemaPopup(e);
			}
		});

		// Build left-side view
		JScrollPane typeTreeView = new JScrollPane(typeTree);
		String elementSchema = LocalizedResources.applicationResources
				.getString("element.schema");
		typeTreeView.setBorder(new TitledBorder(new EmptyBorder(5, 5, 5, 5),
				elementSchema));

		// Set up the element tree
		XMLTreeModel treeModel2 = new XMLTreeModel(editor.getComplexTypeNode());
		elementTree = new JTree(treeModel2);
		elementTree.setCellRenderer(treeCellRenderer);
		elementTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showElementPopup(e);
			}
		});

		// Build right-side view
		JScrollPane elementTreeView = new JScrollPane(elementTree);
		String elementTree = LocalizedResources.applicationResources
				.getString("element.tree");
		elementTreeView.setBorder(new TitledBorder(new EmptyBorder(5, 5, 5, 5),
				elementTree));

		// Build split-pane view
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				typeTreeView, elementTreeView);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(200);

		panel.add(splitPane, BorderLayout.CENTER);

		// Build split-pane view
		if (apanel != null) {
			JSplitPane vsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					panel, apanel);
			vsplitPane.setContinuousLayout(true);
			vsplitPane.setDividerLocation(400);

			return vsplitPane;
		} else {
			return panel;
		}
	}

	public ComplexTypePanel(SchemaNode node, XMLAbstractEditor editor) {
		this.editor = editor;
		this.treeCellRenderer = new DefaultTreeCellRenderer();
		this.typeNode = node;
		initMainPanel();
	}

	private Point getDialogPopupLocation() {
		Rectangle bounds = getBounds();
		Point loc = new Point();
		loc.x = bounds.x + bounds.width / 2;
		loc.y = bounds.y + bounds.height / 2;
		return loc;
	}

	public void doInsert() {
		TreePath p = typeTree.getSelectionPath();
		if (p != null) {
			SchemaNode node = (SchemaNode) p.getLastPathComponent();
			if (!node.getNodeName().equals("element")
					&& !node.getNodeName().equals("any"))
				return;

			XMLNode newNode = editor.insertElementOfType(node);
			if (newNode != null)
				updateTree(editor.getComplexTypeNode(), newNode);
		}
	}

	public int getSelectedRow() {
		int retval = -1;
		TreePath path = elementTree.getSelectionPath();

		if (path != null) {
			XMLNode node = (XMLNode) path.getLastPathComponent();
			XMLTreeModel etm = (XMLTreeModel) elementTree.getModel();
			Vector<XMLNode> terms = etm.getElements();
			for (int i = 0; i < terms.size(); i++) {
				if (terms.elementAt(i) == node) {
					retval = i;
					break;
				}
			}
		}
		return retval;
	}

	private class InsertAction extends AbstractAction {
		private static final long serialVersionUID = 4644759425773148047L;

		public InsertAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			doInsert();
		}
	}

	private class RemoveAction extends AbstractAction {
		private static final long serialVersionUID = 4986831140572533317L;

		public RemoveAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			doRemove();
		}
	}

	private synchronized void updateTree(XMLNode parent, XMLNode newNode) {
		editor.beginUndoEditSession();
		int treePos = parent.getOutlineChildIndex(newNode);
		int pos = parent.index(newNode);

		// System.out.println("update tree:treepos:" + treePos + ":pos:" + pos
		// + ":parent:" + parent + ":new node:" + newNode);
		// fire tree model event for element tree
		Object[] path = { parent };
		int[] childIndices = { treePos };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = (XMLTreeModel) elementTree.getModel();

		xtm.fireTreeNodesInserted(tme);

		// fire tree model event for xml tree
		xtm = (XMLTreeModel) editor.getXMLTreeModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		editor.setLocked(true);
		xtm.fireTreeNodesInserted(tme);
		editor.setLocked(false);

		TreePath newPath = xmlTreePath.pathByAddingChild(newNode);
		editor.setTreeSelectionPath(newPath);
		elementTree.makeVisible(xtm.getPathToFirstLeaf());

		if (!editor.isTextMode() && !editor.getUndoInProgress()) {
			XMLUndoableEdit ue = new XMLUndoableEdit(editor, parent, newNode,
					pos, null, XMLUndoableEdit.NODE_INSERT);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			editor.fireUndoableEditEvent(ev);
		}
		editor.endUndoEditSession();
	}

	private class InsertCommentAction extends AbstractAction {
		private static final long serialVersionUID = 6477218158081470904L;

		public InsertCommentAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = elementTree.getSelectionPath();
			XMLTreeModel etm = (XMLTreeModel) elementTree.getModel();
			if (p != null) {
				XMLNode node = (XMLNode) p.getLastPathComponent();
				XMLNode root = (XMLNode) etm.getRoot();

				editor.insertComment(root, node);
			}
		}
	}

	private class InsertProcInstrAction extends AbstractAction {
		private static final long serialVersionUID = -1892243581868443929L;

		public InsertProcInstrAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = elementTree.getSelectionPath();
			XMLTreeModel etm = (XMLTreeModel) elementTree.getModel();
			if (p != null) {
				XMLNode node = (XMLNode) p.getLastPathComponent();
				XMLNode root = (XMLNode) etm.getRoot();

				editor.insertProcInstr(root, node);
			}
		}
	}

	private class InsertTextBeforeAction extends AbstractAction {
		private static final long serialVersionUID = 8891549293244278696L;

		public InsertTextBeforeAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = elementTree.getSelectionPath();
			XMLTreeModel etm = (XMLTreeModel) elementTree.getModel();
			if (p != null) {
				XMLNode node = (XMLNode) p.getLastPathComponent();
				XMLNode root = (XMLNode) etm.getRoot();

				String value = TextInput.showDialog(ComplexTypePanel.this,
						LocalizedResources.applicationResources
								.getString("text.input"),
						getDialogPopupLocation());
				if (value != null) {
					if (e.getModifiers() == ActionEvent.ALT_MASK) {
						org.w3c.dom.Node newCdataNode = editor.getXml()
								.createCDATASection(value);
						XMLNode newNode = new XMLNode(newCdataNode, false);
						root.insertBefore(newNode, node);
						updateTree(editor.getComplexTypeNode(), newNode);
					} else {
						value = CommonUtils.escape(value);
						org.w3c.dom.Node newTextNode = editor.getXml()
								.createTextNode(value);
						XMLNode newNode = new XMLNode(newTextNode, false);
						root.insertBefore(newNode, node);
						updateTree(editor.getComplexTypeNode(), newNode);
					}
				}
			}
		}
	}

	private class InsertTextAfterAction extends AbstractAction {
		private static final long serialVersionUID = -8760551556298432690L;

		public InsertTextAfterAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = elementTree.getSelectionPath();
			XMLTreeModel etm = (XMLTreeModel) elementTree.getModel();
			if (p != null) {
				XMLNode node = (XMLNode) p.getLastPathComponent();
				XMLNode root = (XMLNode) etm.getRoot();

				String value = TextInput.showDialog(ComplexTypePanel.this,
						LocalizedResources.applicationResources
								.getString("text.input"),
						getDialogPopupLocation());
				if (value != null) {
					if (e.getModifiers() == ActionEvent.ALT_MASK) {
						org.w3c.dom.Node newCdataNode = editor.getXml()
								.createCDATASection(value);
						XMLNode newNode = new XMLNode(newCdataNode, false);
						int pos = root.index(node);
						root.insertBefore(newNode, pos + 1);
						updateTree(editor.getComplexTypeNode(), newNode);
					} else {
						value = CommonUtils.escape(value);
						org.w3c.dom.Node newTextNode = editor.getXml()
								.createTextNode(value);
						XMLNode newNode = new XMLNode(newTextNode, false);
						int pos = root.index(node);
						root.insertBefore(newNode, pos + 1);
						updateTree(editor.getComplexTypeNode(), newNode);
					}
				}
			}
		}
	}

	private class SchemaPopup extends JPopupMenu {
		private static final long serialVersionUID = -5680175502954538439L;
		public JMenuItem insert;

		public SchemaPopup(String title) {
			super(title);
			init();
		}

		private void init() {
			JMenuItem mi = null;
			insert = mi = new JMenuItem(
					new InsertAction(
							LocalizedResources.applicationResources
									.getString("insert")));
			mi.setMnemonic('i');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);
		}
	}

	private class ElementPopup extends JPopupMenu {
		private static final long serialVersionUID = 1199576011638830093L;
		public JMenuItem remove, insertTextAfter, insertTextBefore,
				insertComment, insertProc;

		public ElementPopup(String title) {
			super(title);
			init();
		}

		private void init() {
			JMenuItem mi = null;
			insertTextBefore = mi = new JMenuItem(new InsertTextBeforeAction(
					LocalizedResources.applicationResources
							.getString("insert.text.before")));
			mi.setMnemonic('b');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);

			insertTextAfter = mi = new JMenuItem(new InsertTextAfterAction(
					LocalizedResources.applicationResources
							.getString("insert.text.after")));
			mi.setMnemonic('a');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);

			insertComment = mi = new JMenuItem(new InsertCommentAction(
					LocalizedResources.applicationResources
							.getString("insert.comment")));
			mi.setMnemonic('c');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);

			insertProc = mi = new JMenuItem(new InsertProcInstrAction(
					LocalizedResources.applicationResources
							.getString("insert.processing.instruction")));
			mi.setMnemonic('p');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);

			this.addSeparator();
			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("select.none"));
			mi.setMnemonic('s');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					elementTree.setSelectionPath(null);
				}
			});
			add(mi);

			this.addSeparator();
			remove = mi = new JMenuItem(
					new RemoveAction(
							LocalizedResources.applicationResources
									.getString("remove")));
			mi.setMnemonic('r');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			add(mi);
		}
	}

}
