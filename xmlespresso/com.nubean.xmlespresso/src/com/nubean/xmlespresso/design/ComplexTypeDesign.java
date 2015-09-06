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

package com.nubean.xmlespresso.design;

import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.tree.TreePath;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michxml.InsertPosition;
import com.nubean.michxml.SchemaNode;
import com.nubean.michxml.SchemaTreeModel;
import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.XMLTreeModel;
import com.nubean.michxml.XMLUndoableEdit;
import com.nubean.michbase.CommonUtils;
import com.nubean.michxml.attr.AttributePropertySheetModel;
import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.dialogs.XMLEspressoElementChooser;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class ComplexTypeDesign {
	private SchemaTreeModel schemaTreeModel;

	private XMLTreeModel elementTreeModel;

	private XMLAbstractEditor editor;

	private DefaultTreeViewer elementTreeViewer;

	private SchemaTreeViewer schemaTreeViewer;

	private SchemaNode schemaNode, simpleContentAttributeNode;

	private Menu schemaPopup, elementTreePopup;

	private MenuItem insert, remove, insertTextBefore, insertTextAfter,
			insertComment, insertProc;

	private TableViewer tableViewer;

	private AttributePropertySheetModel attributeModel;
	private Composite simpleTypePanel, complexTypePanel, controlPanel;
	private StackLayout stackLayout;
	private SimpleTypeDesign simpleTypeDeisgn;

	private static String[] columnProperties = {
			XMLEspressoActivator.getResourceString("name"),
			XMLEspressoActivator.getResourceString("value") };

	public DefaultTreeViewer getElementTreeViewer() {
		return elementTreeViewer;
	}

	public XMLTreeModel getElementTreeModel() {
		return elementTreeModel;
	}

	public SchemaTreeViewer getSchemaTreeViewer() {
		return schemaTreeViewer;
	}

	public SchemaTreeModel getSchemaTreeModel() {
		return schemaTreeModel;
	}

	public int getSelectedRow() {
		int retval = -1;
		if (elementTreeViewer != null) {
			TreePath path = elementTreeViewer.getSelectionPath();

			if (path != null) {
				XMLNode node = (XMLNode) path.getLastPathComponent();
				XMLTreeModel etm = (XMLTreeModel) elementTreeModel;
				Vector<XMLNode> terms = etm.getElements();
				for (int i = 0; i < terms.size(); i++) {
					if (terms.elementAt(i) == node) {
						retval = i;
						break;
					}
				}
			}
		}
		return retval;
	}

	private Table createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		Table table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn name = new TableColumn(table, SWT.CENTER, 0);
		name.setWidth(100);
		name.setText(columnProperties[0]);

		TableColumn value = new TableColumn(table, SWT.LEFT, 1);
		value.setWidth(100);
		value.setText(columnProperties[1]);

		return table;
	}

	private void createAttributeModel() {
		if (schemaNode.isSimpleContent()) {
			if (simpleContentAttributeNode != null) {
				attributeModel = new AttributePropertySheetModel(
						simpleContentAttributeNode, editor);
			} else {
				attributeModel = null;
			}
		} else {
			attributeModel = new AttributePropertySheetModel(schemaNode, editor);
		}
	}

	private void createAttributePanel(Composite parent) {
		Table table = createTable(parent);
		tableViewer = new TableViewer(table);

		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(new AttributeTableContentProvider());
		tableViewer.setLabelProvider(new AttributeTableLabelProvider());

		tableViewer.setColumnProperties(columnProperties);
		tableViewer
				.setCellModifier(new AttributeCellModifier(columnProperties));
		CellEditor[] editors = new CellEditor[columnProperties.length];
		for (int i = 0; i < editors.length; i++) {
			editors[i] = new AttributeCellEditor(table);
			editors[i].addListener(new CellEditorListener(editors[i], editor));
		}
		tableViewer.setCellEditors(editors);
	}

	public void setSchemaNode(SchemaNode schemaNode) {
		this.schemaNode = schemaNode;
		createSchemaTreeModel();
		schemaTreeViewer.setInput(schemaTreeModel);
		schemaTreeViewer.expandAll();

		String typeNodeName = schemaNode.getNodeName();

		if (typeNodeName.equals("complexType")) {
			if (!schemaNode.isSimpleContent()) {
				createElementTreeModel();
				elementTreeViewer.setInput(elementTreeModel);
				elementTreeViewer.expandAll();
				createAttributeModel();
				tableViewer.setInput(attributeModel);
				stackLayout.topControl = complexTypePanel;
			} else {
				findSimpleContentAttributeNode();
				createAttributeModel();
				tableViewer.setInput(attributeModel);
				simpleTypeDeisgn.setSchemaNode(schemaNode);
				stackLayout.topControl = simpleTypePanel;
			}
		} else {
			simpleTypeDeisgn.setSchemaNode(schemaNode);
			stackLayout.topControl = simpleTypePanel;
		}

		controlPanel.layout();
	}

	public void setElementTreeSelectionPath(TreePath path) {
		if (elementTreeViewer != null)
			elementTreeViewer.setSelectionPath(path);
	}

	public void setSchemaTreeSelectionPath(TreePath path) {
		if (schemaTreeViewer != null)
			schemaTreeViewer.setSelectionPath(path);
	}

	private void showSchemaPopup(MouseEvent e) {

		if (!editor.isWellFormed())
			return;

		Point loc = new Point(e.x, e.y);
		TreeItem selItem = schemaTreeViewer.getTree().getItem(loc);

		SchemaNode node = (SchemaNode) selItem.getData();

		schemaTreeViewer.setSelectionPath(schemaTreeModel.getPathToRoot(node));

		if (!node.getNodeName().equals("element")
				&& !node.getNodeName().equals("any")) {
			return;
		}

		SchemaTreeModel stm = (SchemaTreeModel) getSchemaTreeModel();
		XMLTreeModel etm = (XMLTreeModel) getElementTreeModel();

		String pattern = stm.getPattern();
		RegExp re = new RegExp(pattern);
		Automaton automata = re.toAutomaton();

		pattern = "^" + pattern + "$";
		Pattern compile = Pattern.compile(pattern);

		if (CommonUtils.getInsertBefore(editor.getXMLModel(), compile,
				automata, etm, node, getSelectedRow()) >= 0) {
			insert.setEnabled(true);
		} else {
			insert.setEnabled(false);
		}
		schemaPopup.setVisible(true);

	}

	private void showElementPopup(MouseEvent e) {

		if (!editor.isWellFormed())
			return;

		Point loc = new Point(e.x, e.y);
		TreeItem selItem = elementTreeViewer.getTree().getItem(loc);

		if(selItem == null)
			return;
		
		XMLNode node = (XMLNode) selItem.getData();
		elementTreeViewer
				.setSelectionPath(elementTreeModel.getPathToRoot(node));

		SchemaTreeModel ctm = getSchemaTreeModel();
		SchemaNode schemaNode = (SchemaNode) ctm.getRoot();
		if (node == editor.getComplexTypeNode()) {
			remove.setEnabled(false);
			insertComment.setEnabled(true);
			insertProc.setEnabled(true);

			if (schemaNode.isMixed()) {
				insertTextAfter.setEnabled(true);
				insertTextBefore.setEnabled(false);
			} else {
				insertTextAfter.setEnabled(false);
				insertTextBefore.setEnabled(false);
			}
		} else if (node.getParent() == editor.getComplexTypeNode()) {
			remove.setEnabled(true);
			insertComment.setEnabled(false);
			insertProc.setEnabled(false);
			if (schemaNode.isMixed()) {
				insertTextAfter.setEnabled(true);
				insertTextBefore.setEnabled(true);
			} else {
				insertTextAfter.setEnabled(false);
				insertTextBefore.setEnabled(false);
			}
		} else {
			remove.setEnabled(false);
			insertComment.setEnabled(false);
			insertProc.setEnabled(false);
			insertTextAfter.setEnabled(false);
			insertTextBefore.setEnabled(false);
		}
		elementTreePopup.setVisible(true);
	}

	private void findSimpleContentAttributeNode() {
		simpleContentAttributeNode = null;
		SchemaNode simpleContent = CommonUtils.getChildByName(schemaNode,
				"simpleContent");
		simpleContentAttributeNode = CommonUtils.getChildByName(simpleContent,
				"extension");
		if (simpleContentAttributeNode == null) {
			simpleContentAttributeNode = CommonUtils.getChildByName(
					simpleContent, "restriction");
		}
	}

	private void createSchemaTreeModel() {
		schemaTreeModel = new SchemaTreeModel(schemaNode);
	}

	private void createElementTreeModel() {
		elementTreeModel = new XMLTreeModel(editor.getComplexTypeNode());
	}

	public SchemaNode getSchemaNode() {
		return schemaNode;
	}

	public void doRemove() {
		editor.beginUndoEditSession();
		TreePath selPath = elementTreeViewer.getSelectionPath();
		if (selPath != null) {
			XMLNode node = (XMLNode) selPath.getLastPathComponent();

			int treePosition = editor.getComplexTypeNode()
					.getOutlineChildIndex(node);
			int pos = editor.getComplexTypeNode().removeChild(node);

			// fire tree model event for element tree
			Object[] path = { editor.getComplexTypeNode() };
			int[] childIndices = { treePosition };
			Object[] children = { node };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = getElementTreeModel();
			xtm.fireTreeNodesRemoved(tme);

			// fire tree model event for xml tree
			TreePath xmlTreePath = editor.getComplexTypePath();
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);
			xtm = (XMLTreeModel) editor.getXMLTreeModel();
			editor.setLocked(true);
			xtm.fireTreeNodesRemoved(tme);
			editor.setLocked(false);

			if (!editor.getUndoInProgress()) {
				XMLUndoableEdit ue = new XMLUndoableEdit(editor,
						editor.getComplexTypeNode(), node, pos, null,
						XMLUndoableEdit.NODE_REMOVE);
				UndoableEditEvent ev = new UndoableEditEvent(this, ue);
				editor.fireUndoableEditEvent(ev);
			}
		}
		editor.endUndoEditSession();
	}

	private void createControl(Composite parent) {

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);

		// Set up the type tree
		Group group1 = new Group(sash, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
		group1.setText(LocalizedResources.applicationResources
				.getString("element.schema"));
		group1.setLayout(new FillLayout(SWT.HORIZONTAL));

		schemaTreeViewer = new SchemaTreeViewer(group1, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL | SWT.H_SCROLL);
		schemaPopup = getSchemaPopup(schemaTreeViewer.getControl());
		schemaTreeViewer.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 3)
					showSchemaPopup(e);
			}
		});

		Group group2 = new Group(sash, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
		group2.setText(LocalizedResources.applicationResources
				.getString("element.tree"));
		group2.setLayout(new FillLayout(SWT.HORIZONTAL));

		controlPanel = new Composite(group2, SWT.BORDER);
		controlPanel.setLayout(stackLayout = new StackLayout());

		complexTypePanel = new Composite(controlPanel, SWT.BORDER);
		complexTypePanel.setLayout(new FillLayout(SWT.HORIZONTAL));

		// Set up the element tree
		elementTreeViewer = new XMLTreeViewer(complexTypePanel, SWT.BORDER
				| SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		elementTreePopup = getElementPopup(elementTreeViewer.getControl());

		// set up simple type design
		simpleTypePanel = new Composite(controlPanel, SWT.BORDER);
		simpleTypePanel.setLayout(new FillLayout(SWT.HORIZONTAL));

		simpleTypeDeisgn = new SimpleTypeDesign(editor, simpleTypePanel);

		elementTreeViewer.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 3)
					showElementPopup(e);
			}
		});

		elementTreeViewer.getTree().addSelectionListener(
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (e.item instanceof TreeItem) {
							TreeItem item = (TreeItem) e.item;
							XMLNode node = (XMLNode) item.getData();
							elementTreeViewer.setSelectionPath(elementTreeModel
									.getPathToRoot(node));
						}
					}
				});

		Group group3 = new Group(sash, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
		group3.setText(LocalizedResources.applicationResources
				.getString("element.attributes"));
		group3.setLayout(new FillLayout(SWT.HORIZONTAL));

		createAttributePanel(group3);

		sash.setWeights(new int[] { 1, 1, 1 });
	}

	public ComplexTypeDesign(XMLAbstractEditor editor, Composite parent) {
		this.editor = editor;
		createControl(parent);
	}

	public void doInsert() {
		TreePath path = schemaTreeViewer.getSelectionPath();
		if (path != null) {
			SchemaNode node = (SchemaNode) path.getLastPathComponent();
			if (!node.getNodeName().equals("element")
					&& !node.getNodeName().equals("any"))
				return;

			XMLNode newNode = editor.insertElementOfType(node);
			if (newNode != null)
				updateTree(editor.getComplexTypeNode(), newNode);
		}
	}

	public void addChild() {
		Vector<SchemaNode> isn = null;

		setElementTreeSelectionPath(null);
		isn = CommonUtils.getInsertableElements(editor.getXMLModel(),
				schemaTreeModel, elementTreeModel, getSelectedRow());

		Collections.sort(isn);

		boolean askForNamespace = false;

		Object choice = XMLEspressoElementChooser.showDialog(
				getSchemaTreeViewer().getTree().getShell(), isn,
				askForNamespace);

		if (choice != null) {
			if (choice instanceof SchemaNode) {
				SchemaNode csn = (SchemaNode) choice;

				askForNamespace = csn.getEditNamespace();
				TreePath path = schemaTreeModel.getPathToRoot(csn);
				setSchemaTreeSelectionPath(path);
				doInsert();
			}
		}
	}

	public int getInsertPosition() {
		int retval = -1;
		TreePath path = elementTreeViewer.getSelectionPath();
		if (path != null) {
			XMLNode node = (XMLNode) path.getLastPathComponent();

			Vector<XMLNode> terms = elementTreeModel.getElements();
			for (int i = 0; i < terms.size(); i++) {
				if (terms.elementAt(i) == node) {
					retval = i;
					break;
				}
			}
		}
		return retval;
	}

	private class InsertAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			doInsert();
		}
	}

	private class RemoveAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			doRemove();
		}
	}

	private synchronized void updateTree(XMLNode parent, XMLNode newNode) {
		editor.beginUndoEditSession();
		int treePosition = parent.getOutlineChildIndex(newNode);
		int pos = parent.index(newNode);

		// fire tree model event for element tree
		Object[] path = { parent };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = getElementTreeModel();

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

		if (!editor.getUndoInProgress()) {
			XMLUndoableEdit ue = new XMLUndoableEdit(editor, parent, newNode,
					pos, null, XMLUndoableEdit.NODE_INSERT);
			UndoableEditEvent ev = new UndoableEditEvent(this, ue);
			editor.fireUndoableEditEvent(ev);
		}
		editor.endUndoEditSession();
	}

	private class InsertCommentAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			TreeItem[] selItems = elementTreeViewer.getTree().getSelection();

			XMLTreeModel etm = getElementTreeModel();
			if (selItems != null && selItems.length > 0) {
				XMLNode node = (XMLNode) selItems[0].getData();
				XMLNode root = (XMLNode) etm.getRoot();

				editor.insertComment(root, node);
			}
		}
	}

	private class InsertProcInstrAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			TreeItem[] selItems = elementTreeViewer.getTree().getSelection();

			XMLTreeModel etm = getElementTreeModel();
			if (selItems != null && selItems.length > 0) {
				XMLNode node = (XMLNode) selItems[0].getData();
				XMLNode root = (XMLNode) etm.getRoot();

				editor.insertProcInstr(root, node);
			}
		}
	}

	private class InsertTextBeforeAction extends SelectionAdapter {

		private Control parent;

		public InsertTextBeforeAction(Control parent) {
			this.parent = parent;
		}

		public void widgetSelected(SelectionEvent e) {
			TreeItem[] selItems = elementTreeViewer.getTree().getSelection();

			XMLTreeModel etm = getElementTreeModel();
			if (selItems != null && selItems.length > 0) {
				XMLNode node = (XMLNode) selItems[0].getData();
				XMLNode root = (XMLNode) etm.getRoot();

				InputDialog textInput = new InputDialog(parent.getShell(),
						LocalizedResources.applicationResources
								.getString("text.input"),
						LocalizedResources.applicationResources
								.getString("text.input"), "", null);
				textInput.setBlockOnOpen(true);

				textInput.open();
				String value = textInput.getValue();

				if (value != null) {
					if (e.stateMask == SWT.ALT) {
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

	private class InsertTextAfterAction extends SelectionAdapter {

		private Control parent;

		public InsertTextAfterAction(Control parent) {
			this.parent = parent;
		}

		public void widgetSelected(SelectionEvent e) {
			TreeItem[] selItems = elementTreeViewer.getTree().getSelection();

			XMLTreeModel etm = getElementTreeModel();
			if (selItems != null && selItems.length > 0) {
				XMLNode node = (XMLNode) selItems[0].getData();
				XMLNode root = (XMLNode) etm.getRoot();

				InputDialog textInput = new InputDialog(parent.getShell(),
						LocalizedResources.applicationResources
								.getString("text.input"),
						LocalizedResources.applicationResources
								.getString("text.input"), "", null);
				textInput.setBlockOnOpen(true);

				textInput.open();
				String value = textInput.getValue();

				if (value != null) {
					if (e.stateMask == SWT.ALT) {
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

	private Menu getSchemaPopup(Control parent) {
		Menu menu = new Menu(parent);
		insert = new MenuItem(menu, SWT.CASCADE, 0);
		insert.setText(LocalizedResources.applicationResources
				.getString("insert"));

		insert.setEnabled(false);
		insert.addSelectionListener(new InsertAction());
		return menu;
	}

	private Menu getElementPopup(Control parent) {
		Menu menu = new Menu(parent);
		MenuItem mi = null;
		insertTextBefore = mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(LocalizedResources.applicationResources
				.getString("insert.text.before"));
		mi.addSelectionListener(new InsertTextBeforeAction(parent));
		mi.setAccelerator('b');
		mi.setEnabled(false);

		insertTextAfter = mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(LocalizedResources.applicationResources
				.getString("insert.text.after"));
		mi.addSelectionListener(new InsertTextAfterAction(parent));
		mi.setAccelerator('a');
		mi.setEnabled(false);

		insertComment = mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(LocalizedResources.applicationResources
				.getString("insert.comment"));
		mi.addSelectionListener(new InsertCommentAction());
		mi.setAccelerator('c');
		mi.setEnabled(false);

		insertProc = mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(LocalizedResources.applicationResources
				.getString("insert.processing.instruction"));
		mi.addSelectionListener(new InsertProcInstrAction());
		mi.setAccelerator('p');
		mi.setEnabled(false);

		mi = new MenuItem(menu, SWT.SEPARATOR);

		mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(LocalizedResources.applicationResources
				.getString("select.none"));
		mi.setAccelerator('s');
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				elementTreeViewer.setSelectionPath(null);
			}
		});

		mi = new MenuItem(menu, SWT.SEPARATOR);

		remove = mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(LocalizedResources.applicationResources.getString("remove"));
		mi.addSelectionListener(new RemoveAction());
		mi.setAccelerator('s');
		mi.setEnabled(false);

		return menu;
	}

}
