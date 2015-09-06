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

import java.io.*;
import java.util.*;

import java.util.regex.*;

import javax.swing.event.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.tree.TreePath;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.nubean.michbase.*;
import com.nubean.michutil.*;

import com.nubean.michxml.*;
import com.nubean.michxml.attr.AttributeName;
import com.nubean.michxml.attr.AttributePropertySheetModel;
import com.nubean.michxml.attr.AttributeValue;
import com.nubean.michxml.elem.XMLAbstractElement;
import com.nubean.michxml.elem.XMLDocumentElement;
import com.nubean.michxml.elem.XMLElement;
import com.nubean.michxml.elem.XMLEndTagElement;
import com.nubean.michxml.elem.XMLStartTagElement;
import com.nubean.michxml.elem.XMLWhiteSpaceElement;
import com.nubean.michxml.parser.ParseException;
import com.nubean.michxml.parser.Token;
import com.nubean.michxml.parser.TokenMgrError;
import com.nubean.michxml.parser.XML10Parser;
import com.nubean.michxml.parser.XML10ParserConstants;

import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.actions.XMLFormatAction;
import com.nubean.xmlespresso.actions.XMLRefreshAction;
import com.nubean.xmlespresso.actions.XMLSaveAsPDF;
import com.nubean.xmlespresso.actions.XMLSaveSchemaAs;
import com.nubean.xmlespresso.actions.XMLToggleEditModeAction;
import com.nubean.xmlespresso.actions.XMLValidateDTDAction;
import com.nubean.xmlespresso.actions.XMLValidateSchemaAction;
import com.nubean.xmlespresso.actions.XsltTransformAction;
import com.nubean.xmlespresso.design.*;

import com.nubean.xmlespresso.dialogs.*;
import com.nubean.xmlespresso.doc.*;

import com.nubean.xmlespresso.pages.XMLDocumentOutlinePage;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class XMLEspressoEditor extends TextEditor implements XMLAbstractEditor {

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

	private static final String ANY_ELEMENT = CommonUtils.getNameWithNamespace(
			"any", "##any");

	private static final String[] filter = { "*.xslt", "*.xsl" };

	private static final String TOGGLE_EDIT_MODE = "ToggleEditMode";

	private static final String FORMAT = "Format";

	private static final String REFRESH = "Refresh";

	private static final String VALIDATE_USING_DTD = "ValidateUsingDTD";

	private static final String VALIDATE_USING_SCHEMA = "ValidateUsingSchema";

	private static final String TRANSFORM_USING_XSLT = "TransformUsingXslt";

	private static final String CONTENT_ASSIST = "ContentAssist";

	private static final String SAVE_AS_PDF = "SaveAsPDF";

	private static final String SAVE_SCHEMA_AS = "SaveSchemaAs";

	private static final String ANY_ELEMENTS = "any-elements";

	private static final String XML_EDITOR_CATEGORY = "XMLEditor";

	static {
		setLocale();
	};

	private boolean locked, auto;

	private boolean refresh;

	private boolean undoInProgress;

	private boolean standalone;

	private XMLEspressoSourceViewer textViewer;

	private TreePath simpleTypePath, complexTypePath;

	private Composite designPanel, designPage;

	private ComplexTypeDesign complexTypeDesign;

	private XMLNode typeNode, complexTypeNode, simpleTypeNode;

	private XMLTreeModel xmlTreeModel;

	private XMLDocumentDescriptor docInfo;

	private boolean askForNamespace;

	private XMLEditorConfiguration editorConfig;

	private boolean textMode;

	private boolean wellFormed;

	private XMLModel xmlModel;

	private XMLDocumentOutlinePage outline;

	private IStatusLineManager statusLineManager;

	private IDialogSettings dialogSettings;

	private ParseException parseException;

	private TokenMgrError tokenMgrError;

	private Token token;

	private String prefix, previous;

	private StatusLineContributionItem statusLine;

	private DefaultErrorHandler xmlCheckHandler;

	private IFile file;

	private XmlTreeListener xmlTreeListener;
	private DesignTreeListener designTreeListener;
	private DefaultTreeViewer designTreeViewer;

	private DOMImplementationLS domImplementation;
	private Transformer transformer;

	private Menu addChildPopup;
	private boolean pendingAsyncParsing;

	public XMLEspressoEditor(IFile file, XMLEditorConfiguration editorConfig,
			XMLDocumentDescriptor docInfo, Composite designPage) {
		this.file = file;
		this.editorConfig = editorConfig;
		this.docInfo = docInfo;
		this.designPage = designPage;

		init();
	}

	public ITextViewer getTextViewer() {
		return textViewer;
	}

	@Override
	public boolean isTextMode() {
		// always return true
		// so the document always thinks we are in text mode
		// We will impose XMLMode rules in verify listener.
		return true;
	}

	private void clearProblemMarkers() {
		try {
			IMarker[] markers = file.findMarkers(IMarker.PROBLEM, false,
					IResource.DEPTH_ZERO);
			int mcount = (markers != null ? markers.length : 0);
			for (int i = 0; i < mcount; i++) {
				markers[i].delete();
			}

		} catch (Exception e) {

		}

	}

	private void addProblemMarkers(DefaultErrorHandler eh) {

		try {
			clearProblemMarkers();

			int count = (eh != null ? eh.getRowCount() : 0);

			if (count > 0) {
				for (int i = 0; i < count; i++) {
					ParseProblem pp = eh.getParseProblem(i);
					IMarker marker = file.createMarker(IMarker.PROBLEM);
					int severity = IMarker.SEVERITY_INFO;

					switch (pp.getType()) {
					case DefaultErrorHandler.ERROR:
					case DefaultErrorHandler.FATAL:
						severity = IMarker.SEVERITY_ERROR;
						break;
					case DefaultErrorHandler.WARNING:
						severity = IMarker.SEVERITY_WARNING;
						break;
					default:
						severity = IMarker.SEVERITY_INFO;
						break;
					}

					Map<String, Comparable<?>> attributes = new HashMap<String, Comparable<?>>(
							9);
					attributes.put(IMarker.SEVERITY, new Integer(severity));
					attributes.put(IMarker.MESSAGE, pp.getDescription());
					attributes.put(IMarker.LINE_NUMBER,
							new Integer(pp.getLine()));

					marker.setAttributes(attributes);
				}

			}
		} catch (Exception e) {
		}
	}

	private void clearTaskMarkers() {
		try {
			IMarker[] markers = file.findMarkers(IMarker.TASK, false,
					IResource.DEPTH_ZERO);
			int mcount = (markers != null ? markers.length : 0);
			for (int i = 0; i < mcount; i++) {
				markers[i].delete();
			}

		} catch (Exception e) {

		}

	}

	private void addTaskMarkers(DefaultErrorHandler eh) {

		try {
			clearTaskMarkers();

			int count = (eh != null ? eh.getRowCount() : 0);

			if (count > 0) {
				for (int i = 0; i < count; i++) {
					ParseProblem pp = eh.getParseProblem(i);
					IMarker marker = file.createMarker(IMarker.TASK);
					int severity = IMarker.PRIORITY_NORMAL;

					switch (pp.getType()) {
					case DefaultErrorHandler.ERROR:
					case DefaultErrorHandler.FATAL:
						severity = IMarker.PRIORITY_HIGH;
						break;
					case DefaultErrorHandler.WARNING:
						severity = IMarker.PRIORITY_LOW;
						break;
					default:
						severity = IMarker.PRIORITY_NORMAL;
						break;
					}

					Map<String, Comparable<?>> attributes = new HashMap<String, Comparable<?>>(
							9);
					attributes.put(IMarker.PRIORITY, new Integer(severity));
					attributes.put(IMarker.MESSAGE, pp.getDescription());
					attributes.put(IMarker.LINE_NUMBER,
							new Integer(pp.getLine()));

					marker.setAttributes(attributes);
				}

			}
		} catch (Exception e) {
		}
	}

	public void validateUsingDtd() {
		if (!wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("invalid.document"));
			return;
		}

		try {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("validate.dtd"));
			saveDocument();

			DefaultErrorHandler errorHandler = new DefaultErrorHandler();
			xmlModel.validateUsingDtd(errorHandler);
			addTaskMarkers(errorHandler);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("validation.complete"));
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("validation.complete.error"),
							e);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("validation.complete.error"));
		}
	}

	public void validateUsingSchema() {
		if (!wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("invalid.document"));
			return;
		}

		try {
			setStatusMessage(LocalizedResources.applicationResources
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

			addTaskMarkers(errorHandler);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("validation.complete"));
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("validation.complete.error"),
							e);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("validation.complete.error"));
		}
	}

	private static void setLocale() {
		COMMENT = LocalizedResources.applicationResources.getString("comment");
		PROC_INSTR = LocalizedResources.applicationResources
				.getString("processing.instruction");
		CDATA = LocalizedResources.applicationResources.getString("cdata");
		TAGLIB = LocalizedResources.applicationResources
				.getString("insert.taglib");
	}

	private File getXsltFile() {
		FileDialog openDialog = new FileDialog(XMLEspressoActivator
				.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getShell(), SWT.OPEN);
		openDialog.setText(XMLEspressoActivator
				.getResourceString("select.xslt.file"));
		openDialog.setFilterExtensions(filter);
		String path = openDialog.open();
		File file = null;
		if (path != null)
			file = new File(path);
		return file;
	}

	private File getTransformOutputFile() {
		FileDialog openDialog = new FileDialog(XMLEspressoActivator
				.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getShell(), SWT.SAVE);
		openDialog.setText(XMLEspressoActivator
				.getResourceString("select.output.file"));
		String path = openDialog.open();
		File file = null;
		if (path != null)
			file = new File(path);
		return file;
	}

	public void transform() {
		try {
			File xslt = getXsltFile();
			if (xslt == null)
				return;

			TransformerFactory tffactory = TransformerFactory.newInstance();
			Transformer xsltTransformer = tffactory.newTransformer();

			File ofile = getTransformOutputFile();
			if (ofile == null)
				return;

			FileOutputStream os = new FileOutputStream(ofile);

			StreamSource source = new StreamSource(file.getLocation().toFile());
			StreamResult result = new StreamResult(os);

			StreamSource xsltSource = new StreamSource(xslt);
			xsltTransformer = tffactory.newTransformer(xsltSource);
			xsltTransformer.transform(source, result);
			os.close();
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error("Transform:", e);
		}
	}

	@Override
	public int getCaretPosition() {
		int pos = 0;

		if (textViewer != null && textViewer.getTextWidget() != null) {
			pos = textViewer.getTextWidget().getCaretOffset();
		}

		return pos;
	}

	@Override
	public void setDirty(boolean dirty) {
		// ignored
	}

	public void setTreeSelectionPath(TreePath path) {
		setSelectionPath(path);

		if (designTreeViewer != null) {
			designTreeViewer.expandToLevel(path.getLastPathComponent(),
					path.getPathCount());
			designTreeViewer.setTreeSelectionPath(path);
		}

		if (outline != null) {
			outline.setTreeSelectionPath(path);
		}

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

	@Override
	public void setCaretPosition(int offset) {
		try {
			textViewer.getTextWidget().setCaretOffset(offset);
		} catch (Exception e) {
		}
	}

	@Override
	public String getEncoding() {
		if (docInfo != null)
			return docInfo.getEncoding();

		return "utf-8";
	}

	private DefaultErrorHandler getXMLCheckHandler(boolean errorHandler) {
		return (errorHandler ? new DefaultErrorHandler() : null);
	}

	private void selectRootElement() {
		try {
			XMLTreeModel xtm = (XMLTreeModel) getXMLTreeModel();
			if (xtm != null) {
				XMLNode rootNode = (XMLNode) xtm.getRoot();
				XMLNode rootElement = xtm.getRootElement();
				if (rootElement != null) {
					Object[] path = { rootNode, rootElement };
					TreePath rootPath = new TreePath(path);
					setTreeSelectionPath(rootPath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void selectElementAtCursor(XMLDocument document) {

		try {
			int where = textViewer.getTextWidget().getCaretOffset();
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
				TreePath path = getXMLTreeModel().getPathToRoot(
						parent.getXMLNode());
				setTreeSelectionPath(path);
			} else {
				selectRootElement();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupDocument() {
		File docFile = getDocumentFile();
		if (wellFormed) {
			xmlModel.parseXmlPI(docFile);
			xmlModel.initMaps();
			refreshXMLTreeModel();
			initCharacterCodes();
		}

		updateSaveStatus();
	}

	private void parse() {
		try {
			parseDocument();
			parseSchema();
			setupDocument();
		} catch (Exception ex) {
			XMLEspressoActivator.getDefault().error("Parse document error", ex);
			wellFormed = false;
		}
	}

	private void parseSchema() {
		try {
			xmlModel.parseSchema(null);
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error("Parse schema error", e);
		}
	}

	private void parseDocument() throws Exception {
		xmlCheckHandler = getXMLCheckHandler(true);
		wellFormed = false;
		try {
			xmlModel.parseDocument(xmlCheckHandler);
			wellFormed = true;
		} catch (SAXParseException se) {
			XMLEspressoActivator.getDefault().error("Parse document error", se);
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error("Parse document error", e);
		} finally {
			xmlModel.setParsed(true);
		}
	}

	public void removeNode(XMLNode parent, XMLNode node) {
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = (xtm != null ? xtm.getPathToRoot(parent) : null);
		if (path == null)
			return;
		auto = true;
		setTreeSelectionPath(path);
		if (parent == complexTypeNode) {
			if (node != null) {
				int treePosition = complexTypeNode.getOutlineChildIndex(node);
				complexTypeNode.removeChild(node);
				int[] childIndices = { treePosition };
				Object[] children = { node };
				TreePath xmlTreePath = complexTypePath;
				TreeModelEvent tme = new TreeModelEvent(this, xmlTreePath,
						childIndices, children);
				xtm = getXMLTreeModel();
				locked = true;
				xtm.fireTreeNodesRemoved(tme);
				locked = false;

			}
		} else {
			int treePosition = complexTypeNode.getOutlineChildIndex(node);
			parent.removeChild(node);
			int[] childIndices = { treePosition };
			Object[] children = { node };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);

			locked = true;
			xtm.fireTreeNodesRemoved(tme);
			locked = false;

		}
		auto = false;
	}

	public void insertNode(XMLNode parent, XMLNode newNode, int pos) {
		auto = true;
		beginUndoEditSession();
		parent.insertBefore(newNode, pos);
		pos = parent.index(newNode);
		int treePosition = parent.getOutlineChildIndex(newNode);

		// fire tree model event for element tree
		Object[] path = { parent };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;
		if (complexTypeDesign != null
				&& complexTypeDesign.getElementTreeModel() != null) {

			xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
			xtm.fireTreeNodesInserted(tme);
		}

		// fire tree model event for xml tree
		xtm = getXMLTreeModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);
		locked = true;
		xtm.fireTreeNodesInserted(tme);
		locked = false;

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

		// fire tree model event for element tree
		Object[] path = { node };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;

		if (complexTypeDesign != null
				&& complexTypeDesign.getElementTreeModel() != null) {
			xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
			xtm.fireTreeNodesInserted(tme);
		}
		// fire tree model event for xml tree
		xtm = getXMLTreeModel();
		TreePath xmlTreePath = xtm.getPathToRoot(node);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		locked = true;
		xtm.fireTreeNodesInserted(tme);
		locked = false;

		endUndoEditSession();
		auto = false;
	}

	public void insertProcInstr(XMLNode node, XMLNode insertBefore) {
		auto = true;

		ProcInstr pi = XMLEspressoProcInstrInput.showDialog(getEditorSite()
				.getShell());

		if (pi == null || pi.target == null || pi.target.trim().length() == 0
				|| pi.data == null || pi.data.trim().length() == 0)
			return;

		beginUndoEditSession();
		org.w3c.dom.ProcessingInstruction pinode = xmlModel.getDocument()
				.createProcessingInstruction(pi.target, pi.data);
		XMLNode newNode = new XMLNode(pinode, false);

		insertNewNodeBeforeNode(node, newNode, insertBefore);

		int treePosition = node.getOutlineChildIndex(newNode);

		// fire tree model event for element tree
		Object[] path = { node };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;
		if (complexTypeDesign != null
				&& complexTypeDesign.getElementTreeModel() != null) {
			xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
			xtm.fireTreeNodesInserted(tme);
		}

		// fire tree model event for xml tree
		xtm = getXMLTreeModel();
		TreePath xmlTreePath = xtm.getPathToRoot(node);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		locked = true;
		xtm.fireTreeNodesInserted(tme);
		locked = false;

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
					if (complexTypeDesign != null
							&& complexTypeDesign.getSchemaTreeModel() != null) {
						XMLNode newNode = new XMLNode(node, false);
						newNode.setParent(parent);
						SchemaNode snode = CommonUtils.getSchemaNode(xmlModel,
								newNode, standalone);
						SchemaTreeModel stm = (SchemaTreeModel) complexTypeDesign
								.getSchemaTreeModel();
						XMLTreeModel etm = (XMLTreeModel) complexTypeDesign
								.getElementTreeModel();
						int selRow = complexTypeDesign.getSelectedRow();

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

	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);

		try {
			IFileEditorInput editorInput = (IFileEditorInput) getEditorInput();
			IFile file = editorInput.getFile();
			file.refreshLocal(IResource.DEPTH_ZERO, monitor);

			docInfo.setTimestamp(file.getLocalTimeStamp());
			StringWriter sw = new StringWriter(512);
			docInfo.printXml(new PrintWriter(sw), "");
			file.setPersistentProperty(new QualifiedName(
					XMLEspressoActivator.QUALIFIER,
					XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY), sw
					.getBuffer().toString());

			firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (CoreException e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("save.document.failed"),
							e);
			setStatusMessage("Save failed");
		}
	}

	public boolean canInsertTextInNode(XMLNode parent) {
		boolean ret = false;

		int saveCaretOffset = textViewer.getTextWidget().getCaretOffset();

		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);

		if (parent == this.simpleTypeNode) {
			ret = true;
		} else if (parent == complexTypeNode) {
			SchemaTreeModel ctm = (SchemaTreeModel) complexTypeDesign
					.getSchemaTreeModel();
			SchemaNode schemaNode = (SchemaNode) ctm.getRoot();
			if ((schemaNode.getNodeByName("simpleContent") != null)
					|| schemaNode.isMixed()) {
				ret = true;
			}
		}

		textViewer.getTextWidget().setCaretOffset(saveCaretOffset);
		return ret;
	}

	public Vector<Object> getParserContentAssist(int where) {
		prefix = null;

		String text = null;
		XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
				.getDocument();
		XMLDocument xmldoc = xedoc.getXMLDocument();
		try {
			text = xmldoc.getText(0, xmldoc.getLength());
		} catch (BadLocationException e) {

		}

		if (text != null) {
			CharArrayReader cr = new CharArrayReader(text.toCharArray());
			XML10Parser parser = new XML10Parser(cr);

			parseException = null;
			tokenMgrError = null;
			token = null;

			try {
				parser.Document(xmldoc, null);
			} catch (ParseException e) {

			}
			parseException = parser.getParseException();
			tokenMgrError = parser.getTokenMgrError();
			token = parser.token;

		}

		Vector<String> expected = getExpectedValues(textViewer);

		int count = (expected != null ? expected.size() : 0);
		Vector<Object> proposals = new Vector<Object>(count);
		for (int i = 0; i < count; i++) {
			String replacementText = expected.elementAt(i);
			XMLCompletionProposal completionProposal = new XMLCompletionProposal(
					replacementText, where, 0, replacementText.length(), null,
					replacementText, null,
					XMLEspressoActivator.getResourceString("parsing.error"));
			proposals.add(completionProposal);
		}
		return proposals;
	}

	public Vector<Object> getElementContentAssist(XMLNode node, XMLNode before,
			int where) {
		Vector<Object> proposals = null;

		Vector<SchemaNode> isn = null;

		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(node);
		setTreeSelectionPath(path);

		if (node == complexTypeNode && complexTypeDesign != null
				&& complexTypeDesign.getSchemaTreeModel() != null) {

			XMLTreeModel etm = (XMLTreeModel) complexTypeDesign
					.getElementTreeModel();
			if (before != null) {
				TreePath epath = etm.getPathToRoot(before);
				complexTypeDesign.setElementTreeSelectionPath(epath);
			} else {
				complexTypeDesign.setElementTreeSelectionPath(null);
			}
			SchemaTreeModel stm = (SchemaTreeModel) complexTypeDesign
					.getSchemaTreeModel();
			int selRow = complexTypeDesign.getSelectedRow();
			isn = CommonUtils.getInsertableElements(xmlModel, stm, etm, selRow);
		}

		int count = (isn != null ? isn.size() : 0);
		proposals = new Vector<Object>(count);

		for (int i = 0; i < count; i++) {

			SchemaNode schemaNode = isn.elementAt(i);
			String tag = schemaNode.toString().split(" ")[0];
			Namespace ns = getTypeNodeNS(schemaNode);

			StringBuilder startTag = new StringBuilder();
			startTag.append("<");
			if (ns != null && ns.prefix != null) {
				startTag.append(ns.prefix).append(":");
			}
			startTag.append(tag).append(">");

			StringBuilder endTag = new StringBuilder();
			endTag.append("</");

			if (ns != null && ns.prefix != null) {
				endTag.append(ns.prefix).append(":");
			}

			endTag.append(tag).append(">");

			StringBuilder sb = new StringBuilder(1024);
			serialize(sb, schemaNode);

			XMLCompletionProposal completionProposal = new XMLCompletionProposal(
					startTag.toString() + endTag.toString(), where, 0,
					startTag.length(), null, schemaNode.toString(), null,
					prettyPrint(sb.toString()));
			proposals.add(completionProposal);
		}

		proposals.add(new XMLCompletionProposal("<!-- -->", where, 0, 4, null,
				"XML comment", null, "Insert XML comment"));
		return proposals;
	}

	private void serialize(StringBuilder sb, SchemaNode schemaNode) {
		if (domImplementation != null) {
			LSSerializer lsWriter = domImplementation.createLSSerializer();
			sb.append(lsWriter.writeToString(schemaNode.getDomNode()));

			int count = schemaNode.childCount();
			for (int i = 0; i < count; i++)
				serialize(sb, schemaNode.child(i));

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void insertSubElement(XMLNode node, XMLNode before) {
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(node);
		setTreeSelectionPath(path);

		auto = true;
		Vector isn = null;
		if (node == complexTypeNode && complexTypeDesign != null
				&& complexTypeDesign.getSchemaTreeModel() != null) {

			XMLTreeModel etm = (XMLTreeModel) complexTypeDesign
					.getElementTreeModel();
			if (before != null) {
				TreePath epath = etm.getPathToRoot(before);
				complexTypeDesign.setElementTreeSelectionPath(epath);
			} else {
				complexTypeDesign.setElementTreeSelectionPath(null);
			}
			SchemaTreeModel stm = (SchemaTreeModel) complexTypeDesign
					.getSchemaTreeModel();
			int selRow = complexTypeDesign.getSelectedRow();
			isn = CommonUtils.getInsertableElements(xmlModel, stm, etm, selRow);

			SchemaNode proxyAnyNode = null;

			if (xmlModel.getSchema().isJSP()) {
				for (int i = 0; i < isn.size(); i++) {
					SchemaNode sn = (SchemaNode) isn.elementAt(i);

					if (sn.toString().equals(ANY_ELEMENT)) {
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

			Object choice = XMLEspressoElementChooser.showDialog(textViewer
					.getTextWidget().getShell(), isn, askForNamespace);

			if (choice != null) {
				if (choice instanceof SchemaNode) {
					SchemaNode csn = (SchemaNode) choice;
					if (csn.getTld() != null) {
						this.askForNamespace = false;
						path = stm.getPathToRoot(proxyAnyNode);
						proxyAnyNode.setProxyNode(csn);
						complexTypeDesign.setSchemaTreeSelectionPath(path);
						complexTypeDesign.doInsert();
					} else {
						this.askForNamespace = csn.getEditNamespace();
						path = stm.getPathToRoot(csn);
						complexTypeDesign.setSchemaTreeSelectionPath(path);
						complexTypeDesign.doInsert();
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
			isn = new Vector(4, 4);

			if (canInsertTextInNode(node))
				isn.add(CDATA);

			isn.add(COMMENT);
			isn.add(PROC_INSTR);
			Object choice = XMLEspressoElementChooser.showDialog(textViewer
					.getTextWidget().getShell(), isn, askForNamespace);

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

	private void insertTaglib() {
		// get tld file
		FileDialog fileChooser = new FileDialog(getEditorSite().getShell(),
				SWT.OPEN);
		fileChooser.setText(LocalizedResources.applicationResources
				.getString("open.tld"));
		String filePath = fileChooser.open();

		if (filePath == null)
			return;

		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}

		// ask for name space and prefix
		String[] p = { "" };
		String[] u = { "" };

		Namespace ns = XMLEspressoNamespaceInput.showDialog(getEditorSite()
				.getShell(), p, u);

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
			XMLEspressoActivator.getDefault().error("Insert taglib error", e);
			setStatusMessage("Insert taglib error");
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
			textNode.setNodeValue(value);
			Object[] path = { simpleTypeNode };

			XMLNode changedNode = simpleTypeNode.findTextNode();
			changedNode.setParsed(false);
			int treePosition = simpleTypeNode.getOutlineChildIndex(changedNode);
			int[] childIndices = { treePosition };
			Object[] children = { changedNode };
			XMLTreeModel xtm = getXMLTreeModel();
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			xtm.fireTreeNodesChanged(tme);

		} else {
			org.w3c.dom.Node newTextNode = xmlModel.getDocument()
					.createTextNode(value);
			XMLNode newNode = new XMLNode(newTextNode, false);
			simpleTypeNode.appendChild(newNode);
			int treePosition = simpleTypeNode.getOutlineChildIndex(newNode);

			// fire tree model event for element tree
			Object[] path = { simpleTypeNode };
			int[] childIndices = { treePosition };
			Object[] children = { newNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = null;
			if (complexTypeDesign.getElementTreeModel() != null) {
				xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
				xtm.fireTreeNodesInserted(tme);
			}

			// fire tree model event for xml tree
			TreePath xmlTreePath = simpleTypePath;

			xtm = getXMLTreeModel();
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

			locked = true;
			xtm.fireTreeNodesInserted(tme);
			locked = false;

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
			cdataNode.setNodeValue(value);
			Object[] path = { simpleTypeNode };

			XMLNode changedNode = simpleTypeNode.findCDATANode();
			changedNode.setParsed(false);
			int treePosition = simpleTypeNode.getOutlineChildIndex(changedNode);
			int[] childIndices = { treePosition };
			Object[] children = { changedNode };
			XMLTreeModel xtm = getXMLTreeModel();
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			xtm.fireTreeNodesChanged(tme);

		} else {
			org.w3c.dom.Node newCDATANode = xmlModel.getDocument()
					.createCDATASection(value);
			XMLNode newNode = new XMLNode(newCDATANode, false);
			simpleTypeNode.appendChild(newNode);
			int treePosition = simpleTypeNode.getOutlineChildIndex(newNode);

			// fire tree model event for element tree
			Object[] path = { simpleTypeNode };
			int[] childIndices = { treePosition };
			Object[] children = { newNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = null;
			if (complexTypeDesign.getElementTreeModel() != null) {
				xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
				xtm.fireTreeNodesInserted(tme);
			}

			// fire tree model event for xml tree
			TreePath xmlTreePath = simpleTypePath;

			xtm = getXMLTreeModel();
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

			locked = true;
			xtm.fireTreeNodesInserted(tme);
			locked = false;

		}
		endUndoEditSession();
		auto = false;
	}

	public Vector<Object> getAttributeContentAssist(XMLNode parent, int where) {
		Vector<Object> proposals = null;
		Vector<AttributeName> anames = null;
		Vector<AttributeValue> avalues = null;

		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);
		if (parent == typeNode && complexTypeDesign != null) {

			SchemaNode typeNode = complexTypeDesign.getSchemaNode();

			AttributePropertySheetModel model = new AttributePropertySheetModel(
					typeNode, this);

			int count = model.getRowCount();
			anames = new Vector<AttributeName>(count);
			avalues = new Vector<AttributeValue>(count);

			for (int i = 0; i < count; i++) {
				anames.add((AttributeName) model.getValueAt(i, 0));
				AttributeValue cvalue = (AttributeValue) model.getValueAt(i, 1);
				avalues.add(cvalue);
			}

			proposals = new Vector<Object>(anames.size());
			for (int i = 0; i < anames.size(); i++) {
				AttributeValue attrValue = avalues.elementAt(i);

				if (attrValue.getValue() != null)
					continue;

				StringBuilder infoBuilder = new StringBuilder();
				AttributeName attrName = anames.elementAt(i);

				serialize(infoBuilder, typeNode);
				String replacementString = " " + attrName.getValue() + "=\"\" ";

				XMLCompletionProposal completionProposal = new XMLCompletionProposal(
						replacementString, where, 0,
						replacementString.length() - 2, null,
						attrName.getValue(), null, infoBuilder.toString());
				proposals.add(completionProposal);
			}
		}

		return proposals;
	}

	public void insertTextNode(String value, XMLNode parent, XMLNode before) {
		auto = true;
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);
		if (parent == simpleTypeNode) {
			insertTextInSimpleType(value);
		} else if (parent == complexTypeNode) {
			SchemaTreeModel ctm = (SchemaTreeModel) complexTypeDesign
					.getSchemaTreeModel();
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
		auto = true;
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);
		if (parent == simpleTypeNode) {
			insertCDATAInSimpleType(value);
		} else if (parent == complexTypeNode) {
			SchemaTreeModel ctm = (SchemaTreeModel) complexTypeDesign
					.getSchemaTreeModel();
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
		int treePosition = parent.getOutlineChildIndex(newNode);

		// fire tree model event for element tree
		Object[] path = { parent };
		int[] childIndices = { treePosition };
		Object[] children = { newNode };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		XMLTreeModel xtm = null;

		if (complexTypeDesign != null
				&& complexTypeDesign.getElementTreeModel() != null) {
			xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
			xtm.fireTreeNodesInserted(tme);
		}

		// fire tree model event for xml tree
		xtm = getXMLTreeModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

		setLocked(true);
		xtm.fireTreeNodesInserted(tme);
		setLocked(false);
		if (!init) {
			TreePath newPath = xmlTreePath.pathByAddingChild(newNode);
			setTreeSelectionPath(newPath);

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
		return xmlTreeModel;
	}

	public XMLModel getXMLModel() {
		return xmlModel;
	}

	public void endUndoEditSession() {
		if (textViewer != null) {
			IUndoManager undoManager = textViewer.getUndoManager();
			if (undoManager != null)
				undoManager.endCompoundChange();
		}
	}

	public void beginUndoEditSession() {
		if (textViewer != null) {
			IUndoManager undoManager = textViewer.getUndoManager();
			if (undoManager != null)
				undoManager.beginCompoundChange();
		}
	}

	public void showDocument() {
		XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
				.getDocument();
		XMLDocument xmldoc = (XMLDocument) xedoc.getXMLDocument();
		if (wellFormed) {
			XMLTreeModel xmltm = getXMLTreeModel();
			parseDocumentElement((XMLNode) xmltm.getRoot(), xmldoc);
			xmltm.removeTreeModelListener(xmldoc);
			xmltm.addTreeModelListener(xmldoc);
		} else {
			parseDocumentElement(null, xmldoc);
		}

		if (wellFormed) {
			if (!refresh) {
				selectRootElement();
			} else {
				selectElementAtCursor(xmldoc);
			}
			setStatusMessage(LocalizedResources.applicationResources
					.getString("document.insync"));
		} else {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("document.not.insync"));
		}
		refresh = false;

		updateSaveStatus();
	}

	public void updateSaveStatus() {
		firePropertyChange(IEditorPart.PROP_DIRTY);
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
		int treePosition = parent.getOutlineChildIndex(node);

		int[] childIndices = { treePosition };
		Object[] children = { node };

		if (parent == complexTypeNode) {
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			if (complexTypeDesign.getElementTreeModel() != null) {
				XMLTreeModel xtm = (XMLTreeModel) complexTypeDesign
						.getElementTreeModel();
				xtm.fireTreeNodesChanged(tme);
			}
		}

		// fire tree model event for xml tree
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath xmlTreePath = xtm.getPathToRoot(parent);
		TreeModelEvent tme = new TreeModelEvent(this, xmlTreePath,
				childIndices, children);
		xtm.fireTreeNodesChanged(tme);

	}

	private void initCharacterCodes() {
		try {
			XMLTreeModel xtm = getXMLTreeModel();
			xmlModel.initDocCharacterCodes((XMLNode) xtm.getRoot());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Namespace getNamespaceInput() {
		Namespace ns = null;
		Vector<String> pv = xmlModel.getNSPrefix();
		Vector<String> uv = xmlModel.getNSUri();

		String[] pa = new String[pv.size()];
		String[] ua = new String[uv.size()];

		pv.toArray(pa);
		uv.toArray(ua);

		ns = XMLEspressoNamespaceInput.showDialog(textViewer.getTextWidget()
				.getShell(), pa, ua);

		if (ns == null) {
			String prefix = getPrefix(typeNode.toString());
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

		SchemaTreeModel stm = (SchemaTreeModel) complexTypeDesign
				.getSchemaTreeModel();
		XMLTreeModel etm = (XMLTreeModel) complexTypeDesign
				.getElementTreeModel();

		int selRow = complexTypeDesign.getSelectedRow();
		String pattern = stm.getPattern();
		RegExp re = new RegExp(pattern);
		Automaton automata = re.toAutomaton();

		pattern = "^" + pattern + "$";
		Pattern compile = Pattern.compile(pattern);

		int pos = CommonUtils.getInsertBefore(xmlModel, compile, automata, etm,
				typeNode, selRow);
		if (pos == -1)
			return null;
		String elementName = CommonUtils.getUnqualifiedElementName(typeNode
				.toString());
		boolean anyNamespace = false;
		if (elementName.equals("any")) {
			if (typeNode.getProxyNode() == null) {
				String[] ea = dialogSettings.getArray(ANY_ELEMENTS);
				if (ea == null) {
					ea = new String[0];
				}

				String ename = (String) XMLEspressoElementInput.showDialog(
						getEditorSite().getShell(), ea);

				if (ename != null && ename.trim().length() > 0) {
					elementName = ename;

					if (!contains(ename, ea)) {
						String[] na = new String[ea.length + 1];
						System.arraycopy(ea, 0, na, 1, ea.length);
						na[0] = ename;
						dialogSettings.put(ANY_ELEMENTS, na);
					}
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

		setStatusMessage(LocalizedResources.applicationResources
				.getString("document.insync"));
		return newNode;
	}

	private boolean contains(String key, String[] array) {
		int count = (array != null && key != null ? array.length : 0);
		boolean ret = false;
		for (int i = 0; i < count; i++) {
			if (array[i].equals(key)) {
				ret = true;
				break;
			}
		}
		return ret;
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
		return 0;
	}

	public void saveDocumentAs() {
		FileDialog fileChooser = new FileDialog(getEditorSite().getShell(),
				SWT.SAVE);
		fileChooser.setText(LocalizedResources.applicationResources
				.getString("save.as"));

		String filePath = fileChooser.open();

		if (filePath == null)
			return;

		File file = new File(filePath);

		try {
			parseEncoding();
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os,
					docInfo.getEncoding());
			PrintWriter writer = new PrintWriter(osw);

			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument doc = xedoc.getXMLDocument();

			writer.print(doc.getText(0, doc.getLength()));
			writer.close();
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("save.document.failed"),
							e);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("save.document.failed"));
		}
	}

	public void saveDocumentAsPdf() {
		if (!wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("invalid.document"));
			return;
		}

		FileDialog fileChooser = new FileDialog(getEditorSite().getShell(),
				SWT.SAVE);
		fileChooser.setText(LocalizedResources.applicationResources
				.getString("save.as.pdf"));
		String filePath = fileChooser.open();

		if (filePath == null)
			return;

		File file = new File(filePath);
		try {
			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument document = xedoc.getXMLDocument();
			new PDFConverter(document, file);
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("save.document.as.pdf.failed"),
							e);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("save.document.as.pdf.failed"));
		}
	}

	public void saveSchemaAs() {

		if (!wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("invalid.document"));
			return;
		}

		FileDialog fileChooser = new FileDialog(getEditorSite().getShell(),
				SWT.SAVE);
		fileChooser.setText(LocalizedResources.applicationResources
				.getString("save.schema.as"));
		String filePath = fileChooser.open();

		if (filePath == null)
			return;

		File file = new File(filePath);
		try {
			InputStream input = new FileInputStream(xmlModel.getSchemaFile());
			CommonUtils.copyToFile(input, file);
			input.close();
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("save.schema.failed"),
							e);
			setStatusMessage(LocalizedResources.applicationResources
					.getString("save.schema.failed"));
		}
	}

	public void saveDocument() {
		doSave(null);
	}

	private void init() {

		statusLine = new StatusLineContributionItem(XML_EDITOR_CATEGORY, true,
				40);
		setStatusField(statusLine, XML_EDITOR_CATEGORY);
		dialogSettings = XMLEspressoActivator.getDefault().getDialogSettings();
		setDocumentProvider(new XMLEspressoDocumentProvider(editorConfig));
		setSourceViewerConfiguration(new XMLEspressoSourceViewerConfiguration());

		setAction(TOGGLE_EDIT_MODE, new XMLToggleEditModeAction(this));

		IAction faction = new XMLFormatAction(this);
		faction.setActionDefinitionId(XMLEspressoFormEditor.FORMAT_ACTION_DEF_ID);
		setAction(FORMAT, faction);

		setAction(REFRESH, new XMLRefreshAction(this));

		IAction action = new ContentAssistAction(XMLEspressoActivator
				.getDefault().getResourceBundle(), "content.assist.", this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction(CONTENT_ASSIST, action);
		markAsStateDependentAction(CONTENT_ASSIST, true);

		setAction(VALIDATE_USING_DTD, new XMLValidateDTDAction(this));
		setAction(VALIDATE_USING_SCHEMA, new XMLValidateSchemaAction(this));

		setAction(TRANSFORM_USING_XSLT, new XsltTransformAction(this));

		setAction(SAVE_AS_PDF, new XMLSaveAsPDF(this));
		setAction(SAVE_SCHEMA_AS, new XMLSaveSchemaAs(this));

		xmlModel = new XMLModel(editorConfig, docInfo);

		File file = getDocumentFile();

		if (file.length() == 0) {
			newDocument();
		} else {
			parse();
		}

		try {
			domImplementation = (DOMImplementationLS) DOMImplementationRegistry
					.newInstance().getDOMImplementation("LS 3.0");
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error("DOMImplmentationLS error",
					e);
		}

		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();

			StringReader stylesheet = new StringReader(ppxsl);
			StreamSource xslSource = new StreamSource(stylesheet);
			transformer = tfactory.newTransformer(xslSource);

		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error(
					"Transformer factory error", e);
		}

	} // init

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		addAction(menu, CONTENT_ASSIST);

		if (textMode) {
			addAction(menu, FORMAT);
			addAction(menu, REFRESH);
		}

		if (wellFormed) {
			addAction(menu, VALIDATE_USING_DTD);
			addAction(menu, VALIDATE_USING_SCHEMA);
			addAction(menu, SAVE_AS_PDF);
			addAction(menu, TRANSFORM_USING_XSLT);
		}

		if (xmlModel != null && xmlModel.getSchemaFile() != null) {
			addAction(menu, SAVE_SCHEMA_AS);
		}

		super.editorContextMenuAboutToShow(menu);
	}

	@Override
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		if (wellFormed) {
			addAction(menu, TOGGLE_EDIT_MODE);
		}

		super.rulerContextMenuAboutToShow(menu);
	}

	private void showOverviewRulerMessage(MouseEvent e) {
		Control control = (Control) e.widget;
		int line = getOverviewRuler().toDocumentLineNumber(e.y);
		if (line > -1) {
			line++;
			try {
				IMarker[] markers = file.findMarkers(
						(!wellFormed ? IMarker.PROBLEM : IMarker.TASK), false,
						IResource.DEPTH_ZERO);
				int mcount = (markers != null ? markers.length : 0);
				boolean found = false;
				for (int i = 0; i < mcount; i++) {
					IMarker marker = markers[i];
					int mline = MarkerUtilities.getLineNumber(marker);
					if (mline == line) {
						control.setToolTipText(MarkerUtilities
								.getMessage(marker));
						found = true;
						break;
					}
				}

				if (!found) {
					control.setToolTipText(null);
				}

			} catch (Exception ex) {

			}
		} else {
			control.setToolTipText(null);
		}
	}

	private void showVerticalRulerMessage(MouseEvent e) {
		Control control = (Control) e.widget;
		int line = getVerticalRuler().toDocumentLineNumber(e.y);
		if (line > -1) {
			line++;
			try {
				IMarker[] markers = file.findMarkers(
						(!wellFormed ? IMarker.PROBLEM : IMarker.TASK), false,
						IResource.DEPTH_ZERO);
				int mcount = (markers != null ? markers.length : 0);
				boolean found = false;
				for (int i = 0; i < mcount; i++) {
					IMarker marker = markers[i];
					int mline = MarkerUtilities.getLineNumber(marker);
					if (mline == line) {
						control.setToolTipText(MarkerUtilities
								.getMessage(marker));
						found = true;
						break;
					}
				}

				if (!found) {
					control.setToolTipText(null);
				}

			} catch (Exception ex) {

			}
		} else {
			control.setToolTipText(null);
		}
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		textViewer = new XMLEspressoSourceViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		textViewer.addTextInputListener(new TextInputListener());

		createDesignViewer();

		return textViewer;
	}

	private void parseEncoding() {
		try {
			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument doc = xedoc.getXMLDocument();
			int offset = -1;
			for (int i = 0; i < doc.getLength(); i++) {
				if (doc.getText(i, 1).charAt(0) == '\n') {
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
			XMLEspressoActivator.getDefault().error("Parse encoding error", e);
		}
	}

	public void format() {
		if (!wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("invalid.document"));
			return;
		}

		XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
				.getDocument();
		XMLDocument xmldoc = xedoc.getXMLDocument();

		try {
			String text = xmldoc.getText(0, xmldoc.getLength());
			xmldoc.replace(0, xmldoc.getLength(), prettyPrint(text));
			refresh();
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("format"),
							e);
		}
	}

	public void refresh() {
		if (!wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("invalid.document"));
			return;
		}

		xmlCheckHandler = getXMLCheckHandler(true);
		XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
				.getDocument();
		XMLDocument doc = xedoc.getXMLDocument();
		try {
			if (xmlTreeModel != null)
				xmlTreeModel.removeTreeModelListener(doc);
			removeXmlTreeListener();
			removeDesignTreeListener();
			xmlModel.refresh(doc.getText(0, doc.getLength()), xmlCheckHandler,
					null);
			refreshXMLTreeModel();
			initCharacterCodes();

			this.refresh = true;
			wellFormed = true;

			showDocument();
			addXmlTreeListener();
			addDesignTreeListener();
		} catch (Exception e) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("refresh"),
							e);
		} finally {
			addProblemMarkers(xmlCheckHandler);
		}
	}

	private void refreshXMLTreeModel() {
		if (xmlModel != null) {
			xmlTreeModel = new XMLTreeModel(xmlModel.getDocument(),
					xmlModel.isParsed());
			if (outline != null) {
				outline.setInput(xmlTreeModel);
			}

			if (designTreeViewer != null) {
				designTreeViewer.setInput(xmlTreeModel);
			}
		}
	}

	public synchronized boolean isWellFormed() {
		if (!wellFormed) {
			xmlCheckHandler = getXMLCheckHandler(true);
			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument doc = xedoc.getXMLDocument();
			try {
				if (xmlTreeModel != null)
					xmlTreeModel.removeTreeModelListener(doc);
				removeXmlTreeListener();
				removeDesignTreeListener();
				xmlModel.refreshDocument(doc.getText(0, doc.getLength()),
						xmlCheckHandler, null);
				refreshXMLTreeModel();
				initCharacterCodes();

				this.refresh = true;
				wellFormed = true;

				showDocument();
				addXmlTreeListener();
				addDesignTreeListener();
			} catch (Exception e) {
			} finally {
				addProblemMarkers(xmlCheckHandler);
			}
		}

		if (wellFormed) {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("document.insync"));
		} else {
			setStatusMessage(LocalizedResources.applicationResources
					.getString("document.not.insync"));
		}
		return wellFormed;
	}

	public void toggleTextMode() {

		if (!wellFormed) {
			// if we are in text mode, but not in sync,
			// we can not toggle out of text mode to xml mode.
			setStatusMessage(LocalizedResources.applicationResources
					.getString("document.not.insync"));
			return;
		}

		textMode = !textMode;
		setStatusMessage(null);
	}

	public boolean initDocument() throws Exception {
		boolean success = xmlModel.initDocument();
		if (success) {
			refreshXMLTreeModel();
			initCharacterCodes();
		}
		return success;
	}

	public boolean newDocument() {

		try {
			parseSchema();
			wellFormed = initDocument();
		} catch (Exception ex) {
			XMLEspressoActivator
					.getDefault()
					.error(LocalizedResources.applicationResources
							.getString("new.document.error"),
							ex);
			wellFormed = false;
		}
		return wellFormed;
	}

	private void createDesignViewer() {

		if (designPage != null) {
			SashForm sashForm = new SashForm(designPage, SWT.VERTICAL
					| SWT.SMOOTH);

			Group group1 = new Group(sashForm, SWT.BORDER
					| SWT.SHADOW_ETCHED_IN);
			group1.setText(LocalizedResources.applicationResources
					.getString("design.tree"));
			group1.setLayout(new FillLayout(SWT.HORIZONTAL));

			designTreeViewer = new XMLTreeViewer(group1, SWT.BORDER
					| SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
			designTreeViewer.setInput(xmlTreeModel);
			designTreeViewer.getTree().addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					if (e.button == 3)
						showAddChildPopup(e);
				}
			});
			addChildPopup = getAddChildPopup(designTreeViewer.getControl());
			designTreeListener = new DesignTreeListener();
			designTreeViewer.getTree().addSelectionListener(designTreeListener);

			designPanel = new Composite(sashForm, SWT.BORDER);
			designPanel.setLayout(new FillLayout(SWT.HORIZONTAL));

			complexTypeDesign = new ComplexTypeDesign(this, designPanel);
			sashForm.setWeights(new int[] { 1, 1 });
		}
	}

	private void showAddChildPopup(MouseEvent e) {

		if (!isWellFormed())
			return;

		Point loc = new Point(e.x, e.y);
		TreeItem selItem = designTreeViewer.getTree().getItem(loc);
		XMLNode node = (XMLNode) selItem.getData();

		TreePath p = designTreeViewer.getTreeSelectionPath();
		setSelectionPath(p);

		if (node == complexTypeNode) {
			addChildPopup.setVisible(true);
		}

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

			if (lnode == typeNode)
				return;

			int nodeType = lnode.getDomNode().getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				SchemaNode schemaNode = CommonUtils.getTypeNode(xmlModel,
						lnode, standalone);
				String typeNodeName = schemaNode.getNodeName();

				if (typeNodeName.equals("complexType")
						&& !schemaNode.isSimpleContent()) {
					complexTypePath = p;
					typeNode = complexTypeNode = lnode;
				} else if (schemaNode.isSimpleContent()) {
					typeNode = complexTypeNode = simpleTypeNode = lnode;
					complexTypePath = simpleTypePath = p;
				} else {
					typeNode = simpleTypeNode = lnode;
					simpleTypePath = p;
				}
				complexTypeDesign.setSchemaNode(schemaNode);
			}

			if (designPage != null) {
				designPage.layout();
				designPage.update();
			}
		}
	}

	public void setUndoInProgress(boolean flag) {
		undoInProgress = flag;
	}

	public boolean getUndoInProgress() {
		return undoInProgress;
	}

	private class DesignTreeListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			if (!wellFormed) {
				setStatusMessage(LocalizedResources.applicationResources
						.getString("document.not.insync"));
				return;
			}

			if (designTreeViewer != null) {
				TreePath p = designTreeViewer.getTreeSelectionPath();
				setSelectionPath(p);
			}

		}

	}

	private class XmlTreeListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			if (!wellFormed) {
				setStatusMessage(LocalizedResources.applicationResources
						.getString("document.not.insync"));
				return;
			}

			if (outline != null) {
				TreePath p = outline.getTreeSelectionPath();
				setSelectionPath(p);

				if (!locked) {
					if (p != null) {
						setSelectionPath(p);
						XMLNode lnode = (XMLNode) p.getLastPathComponent();
						if (lnode != null) {
							XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
									.getDocument();

							XMLDocument xmldoc = (XMLDocument) xedoc
									.getXMLDocument();
							Element ele = xmldoc.findElementWithNode(lnode);
							if (ele != null) {
								int start = ele.getStartOffset();
								int end = ele.getEndOffset();

								textViewer.setSelectedRange(start,
										(end - start));
								textViewer.getTextWidget().showSelection();
							}
						}
					}
				}
			}

		}

	}

	private void setStatusMessage(String message) {
		StringBuffer sb = new StringBuffer();
		if (textMode) {
			sb.append(XMLEspressoActivator.getResourceString("text.mode"));
		} else {
			sb.append(XMLEspressoActivator.getResourceString("xml.mode"));
		}
		if (message != null) {
			sb.append(": ").append(message);
		}
		getStatusLineManager().setMessage(sb.toString());
	}

	public void setStatusLineManager(IStatusLineManager statusLineManager) {
		this.statusLineManager = statusLineManager;
	}

	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}

	public void setAttribute(String attr, String text) {
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
			int treePosition = complexTypeNode.getOutlineChildIndex(newNode);
			int[] childIndices = { treePosition };
			Object[] children = { newNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = null;
			if (complexTypeDesign.getElementTreeModel() != null) {
				xtm = (XMLTreeModel) complexTypeDesign.getElementTreeModel();
				xtm.fireTreeNodesInserted(tme);
			}

			// fire tree model event for xml tree
			xtm = getXMLTreeModel();
			TreePath xmlTreePath = complexTypePath;
			tme = new TreeModelEvent(this, xmlTreePath, childIndices, children);

			locked = true;
			xtm.fireTreeNodesInserted(tme);
			locked = false;

		} else {
			Object[] path = { complexTypeNode };

			XMLNode changedNode = complexTypeNode.findAttribute(attrNode
					.getName());
			changedNode.setParsed(false);
			int treePosition = complexTypeNode
					.getOutlineChildIndex(changedNode);
			int[] childIndices = { treePosition };
			attrNode.setNodeValue(text);
			Object[] children = { changedNode };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			XMLTreeModel xtm = getXMLTreeModel();
			locked = true;
			xtm.fireTreeNodesChanged(tme);
			locked = false;
		}
		wellFormed = true;
	}

	public void fireUndoableEditEvent(UndoableEditEvent e) {
		// ignore
	}

	public DocumentDescriptor getDocumentDescriptor() {
		return docInfo;
	}

	private void removeXmlTreeListener() {
		if (outline != null) {
			outline.removeSelectionListener(xmlTreeListener);
		}
	}

	private void addXmlTreeListener() {
		if (outline != null) {
			outline.addSelectionListener(xmlTreeListener);
		}
	}

	private void removeDesignTreeListener() {
		if (designTreeViewer != null) {
			designTreeViewer.getTree().removeSelectionListener(
					designTreeListener);
		}
	}

	private void addDesignTreeListener() {
		if (designTreeViewer != null) {
			designTreeViewer.getTree().addSelectionListener(designTreeListener);
		}
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			xmlTreeListener = new XmlTreeListener();
			outline = new XMLDocumentOutlinePage(getXMLTreeModel(),
					xmlTreeListener);
			return outline;
		}

		return super.getAdapter(required);
	}

	public XMLDocument getXMLDocument() {
		XMLDocument xmlDocument = null;

		if (textViewer != null) {
			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			if (xedoc != null)
				xmlDocument = (XMLDocument) xedoc.getXMLDocument();
		}

		return xmlDocument;
	}

	public XMLDocument createNewXMLDocument() {
		XMLDocument xmldoc = new XMLDocument(this, new XMLDefaultStyleContext());

		try {
			File file = getDocumentFile();
			readDocumentFromFile(file, xmldoc, getEncoding());
			String text = xmldoc.getText(0, xmldoc.getLength());
			CharArrayReader cr = new CharArrayReader(text.toCharArray());
			XML10Parser parser = new XML10Parser(cr);

			XMLNode rootNode = (XMLNode) getXMLTreeModel().getRoot();
			XMLDocumentElement documentElement = parser.Document(xmldoc,
					rootNode);
			// documentElement.applyAttributes();
			xmldoc.setRootElement(documentElement);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return xmldoc;
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

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	private XMLDocumentElement parseDocumentElement(XMLNode rootNode,
			XMLDocument xmldoc) {

		XMLDocumentElement documentElement = null;

		if (rootNode != null && !rootNode.isParsed()) {
			documentElement = new XMLDocumentElement(rootNode, xmldoc);
		} else {
			try {
				String text = xmldoc.getText(0, xmldoc.getLength());
				CharArrayReader cr = new CharArrayReader(text.toCharArray());
				XML10Parser parser = new XML10Parser(cr);

				documentElement = parser.Document(xmldoc, rootNode);
				documentElement.applyAttributes();
				textViewer.getTextWidget().redraw();
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

	private Vector<String> getExpectedValues(ITextViewer tp) {
		prefix = null;

		Vector<String> expected = new Vector<String>(8, 8);
		int where = tp.getTextWidget().getCaretOffset();
		XMLEspressoDocument xedoc = (XMLEspressoDocument) tp.getDocument();
		XMLDocument doc = xedoc.getXMLDocument();
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

	@Override
	public void showAttributes(XMLNode parent) {
		XMLTreeModel xtm = getXMLTreeModel();
		TreePath path = xtm.getPathToRoot(parent);
		setTreeSelectionPath(path);
		if (parent == typeNode && complexTypeDesign != null) {

			SchemaNode typeNode = complexTypeDesign.getSchemaNode();

			AttributePropertySheetModel model = new AttributePropertySheetModel(
					typeNode, this);

			AttributeDialog attrDialog = new AttributeDialog(getEditorSite()
					.getShell(), this, model);
			attrDialog.setBlockOnOpen(false);

			attrDialog.open();

		}
	}

	private void doContentAssist(ITextViewer tp) {
		try {
			XMLEspressoDocument xedoc = (XMLEspressoDocument) tp.getDocument();
			XMLDocument xmldoc = xedoc.getXMLDocument();

			int where = tp.getTextWidget().getCaretOffset();
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
			if (expected.size() > 1) {
				Collections.sort(expected);

				int index = 0;
				if (previous != null) {
					index = expected.indexOf(previous);
					if (index < 0)
						index = 0;
				}

				choice = (String) XMLEspressoElementChooser.showDialog(
						textViewer.getTextWidget().getShell(), expected,
						askForNamespace);
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
				xmldoc.insertString(where, choice.toString(), null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void doContentAssist() {
		try {

			if (textMode && !isWellFormed()) {
				doContentAssist(textViewer);
				return;
			}

			int where = textViewer.getTextWidget().getCaretOffset();

			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument document = (XMLDocument) xedoc.getXMLDocument();
			XMLAbstractElement root = document.getRootElement();
			XMLAbstractElement ele = root.positionToElement(where);

			if (ele == null)
				return;

			if (ele instanceof XMLStartTagElement
					|| (ele.getParentElement() instanceof XMLStartTagElement)) {
				if (ele instanceof XMLStartTagElement)
					showAttributes(ele.getXMLNode());
				else
					showAttributes(((XMLAbstractElement) ele.getParentElement())
							.getXMLNode());
				return;
			}

			boolean endtag = false;

			if ((ele instanceof XMLWhiteSpaceElement)
					&& (ele.getParentElement() != null && (ele
							.getParentElement() instanceof XMLEndTagElement))) {
				endtag = true;
			}

			XMLAbstractElement parent = (endtag ? (XMLAbstractElement) ele
					.getParentElement().getParentElement().getParentElement()
					: (XMLAbstractElement) ele.getParentElement());

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

	private void addChild() {
		Vector<SchemaNode> isn = null;

		complexTypeDesign.setElementTreeSelectionPath(null);
		isn = CommonUtils.getInsertableElements(getXMLModel(),
				complexTypeDesign.getSchemaTreeModel(),
				complexTypeDesign.getElementTreeModel(),
				complexTypeDesign.getSelectedRow());

		Collections.sort(isn);

		boolean askForNamespace = false;

		Object choice = XMLEspressoElementChooser.showDialog(complexTypeDesign
				.getSchemaTreeViewer().getTree().getShell(), isn,
				askForNamespace);

		if (choice != null) {
			if (choice instanceof SchemaNode) {
				SchemaNode csn = (SchemaNode) choice;

				askForNamespace = csn.getEditNamespace();
				TreePath path = complexTypeDesign.getSchemaTreeModel()
						.getPathToRoot(csn);
				complexTypeDesign.setSchemaTreeSelectionPath(path);
				complexTypeDesign.doInsert();
			}
		}
	}

	private class AddChildAction extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			addChild();
		}
	}

	private Menu getAddChildPopup(Control parent) {
		Menu menu = new Menu(parent);
		MenuItem mi = new MenuItem(menu, SWT.CASCADE, 0);
		mi.setText(LocalizedResources.applicationResources
				.getString("add.child"));

		mi.addSelectionListener(new AddChildAction());
		return menu;
	}

	private class DocumentTracker implements DocumentListener {
		private void documentChanged() {

			updateSaveStatus();

			if (textMode && !auto && !locked) {
				wellFormed = false;
				typeNode = null;

				if (!pendingAsyncParsing) {
					pendingAsyncParsing = true;
					Display display = XMLEspressoActivator.getDefault()
							.getDisplay();
					if (display != null) {
						display.asyncExec(new AsyncParsing());
					}
				}

			} else if (wellFormed && !locked && typeNode != null) {
				XMLTreeModel xtm = getXMLTreeModel();
				if (xtm != null) {
					TreePath path = xtm.getPathToRoot(typeNode);
					typeNode = null;
					setTreeSelectionPath(path);
				}
			}

		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			documentChanged();
		}

		public void removeUpdate(DocumentEvent e) {
			documentChanged();
		}

	}

	private class VerifyAction implements VerifyListener {
		public void verifyText(VerifyEvent e) {

			if (file.isReadOnly()) {
				e.doit = false;
				return;
			}

			if (textMode) {
				e.doit = true;
				return;
			}
			int where = e.start;
			int len = e.end - where;

			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument xmldoc = (XMLDocument) xedoc.getXMLDocument();
			try {
				XMLAbstractElement documentElement = xmldoc.getRootElement();
				if (documentElement != null) {
					XMLAbstractElement ele = documentElement
							.positionToElement(where);
					e.doit = (ele != null && ele.isEditable() && ele
							.getEndOffset() >= where + len);
				}
			} catch (Exception ex) {
				e.doit = false;
			}
		}
	}

	private class TextInputListener implements ITextInputListener {

		private VerifyAction verifyAction;

		private void addDocumentActions() {
			if (textViewer != null) {
				StyledText st = textViewer.getTextWidget();
				if (st != null) {
					st.addVerifyListener(verifyAction = new VerifyAction());
				}
			}
		}

		private void removeDocumentActions() {
			if (textViewer != null) {
				StyledText st = textViewer.getTextWidget();
				if (st != null) {
					if (verifyAction != null) {
						st.removeVerifyListener(verifyAction);
						verifyAction = null;
					}

				}
			}
		}

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput,
				IDocument newInput) {

		}

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {

			if (oldInput != null && oldInput instanceof XMLEspressoDocument) {
				removeDocumentActions();
				xmlModel = null;
				xmlTreeModel = null;
			}

			if (newInput instanceof XMLEspressoDocument) {
				XMLEspressoDocument xedoc = (XMLEspressoDocument) newInput;
				XMLDocument doc = xedoc.getXMLDocument();

				doc.setEditor(XMLEspressoEditor.this);

				StyledText st = textViewer.getTextWidget();
				st.addLineStyleListener(new DefaultEspressoLineStyleAdapter(doc));
				st.addLineBackgroundListener(new DefaultEspressoLineBackgroundAdapter());

				textMode = true;
				showDocument();
				doc.addDocumentListener(new DocumentTracker());
				addDocumentActions();
				initRulerAnnotationListeners();
				addProblemMarkers(xmlCheckHandler);
			}

		}
	}

	private void initRulerAnnotationListeners() {
		try {
			getOverviewRuler().getControl().addMouseMoveListener(
					new MouseMoveListener() {
						public void mouseMove(MouseEvent event) {
							showOverviewRulerMessage(event);
						}
					});

			getVerticalRuler().getControl().addMouseMoveListener(
					new MouseMoveListener() {
						public void mouseMove(MouseEvent event) {
							showVerticalRulerMessage(event);
						}
					});
		} catch (Exception e) {

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

	private void normalizeNewLine(StringBuffer sb) {
		for (int i = 0; i < sb.length() - 1; i++) {
			char c = sb.charAt(i);
			char n = sb.charAt(i + 1);

			if (c == '\r' && n == '\n') {
				sb.replace(i, i + 1, "");
			}
		}
	}

	private class AsyncParsing implements Runnable {
		public synchronized void run() {
			if (!wellFormed) {
				isWellFormed();
				pendingAsyncParsing = false;
			}
		}
	}

}
