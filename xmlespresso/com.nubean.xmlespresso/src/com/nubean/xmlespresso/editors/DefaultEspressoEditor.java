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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.nubean.michbase.DefaultElementNode;
import com.nubean.michbase.DefaultErrorHandler;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.Editor;
import com.nubean.michbase.EditorConfiguration;
import com.nubean.michbase.ParseProblem;
import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.doc.DefaultEspressoDocument;
import com.nubean.xmlespresso.doc.DefaultEspressoDocumentProvider;
import com.nubean.xmlespresso.doc.DefaultEspressoLineBackgroundAdapter;
import com.nubean.xmlespresso.doc.DefaultEspressoLineStyleAdapter;
import com.nubean.xmlespresso.doc.DefaultEspressoSourceViewer;
import com.nubean.xmlespresso.doc.DefaultEspressoSourceViewerConfiguration;
import com.nubean.xmlespresso.doc.DefaultEspressoStyledDocument;
import com.nubean.xmlespresso.pages.DefaultDocumentOutlinePage;

public abstract class DefaultEspressoEditor extends TextEditor implements
		Editor {

	protected DefaultEspressoSourceViewer textViewer;

	protected DocumentAction documentAction;

	protected DocumentTracker documentTracker;

	protected DefaultDocumentOutlinePage outline;

	protected DefaultTreeListener defaultTreeListener;

	protected IFile file;

	protected TreeModel treeModel;

	public DefaultEspressoEditor() {
		super();

		setDocumentProvider(new DefaultEspressoDocumentProvider(
				getEditorConfiguration()));
		setSourceViewerConfiguration(new DefaultEspressoSourceViewerConfiguration());

	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		textViewer = new DefaultEspressoSourceViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		textViewer.addTextInputListener(new TextInputListener());

		return textViewer;
	}

	@Override
	public IStatusLineManager getStatusLineManager() {

		IEditorActionBarContributor contributor = getEditorSite()
				.getActionBarContributor();
		if (!(contributor instanceof EditorActionBarContributor))
			return null;

		IActionBars actionBars = ((EditorActionBarContributor) contributor)
				.getActionBars();
		if (actionBars == null)
			return null;

		return actionBars.getStatusLineManager();
	}

	protected void setStatusMessage(String message) {
		getStatusLineManager().setMessage(message);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		if (!(input instanceof IFileEditorInput))
			throw new PartInitException(new Status(IStatus.ERROR,
					XMLEspressoActivator.PLUGIN_ID, IStatus.ERROR,
					XMLEspressoActivator
							.getResourceString("invalid.input.type"), null));

		IFileEditorInput editorInput = (IFileEditorInput) input;
		file = editorInput.getFile();

		if (!file.getLocation().toFile().exists()) {
			throw new PartInitException(new Status(IStatus.ERROR,
					XMLEspressoActivator.PLUGIN_ID, IStatus.ERROR,
					file.getLocation().toOSString()
							+ XMLEspressoActivator
									.getResourceString("invalid.file"), null));
		}

		getStatusLineManager();
		addResourceChangeListeners();
	}

	public abstract DocumentDescriptor getDocumentDescriptor();

	public abstract EditorConfiguration getEditorConfiguration();

	@Override
	public void beginUndoEditSession() {
		if (textViewer != null) {
			IUndoManager undoManager = textViewer.getUndoManager();
			if (undoManager != null)
				undoManager.beginCompoundChange();
		}
	}

	@Override
	public void endUndoEditSession() {
		if (textViewer != null) {
			IUndoManager undoManager = textViewer.getUndoManager();
			if (undoManager != null)
				undoManager.endCompoundChange();
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
	public void setCaretPosition(int offset) {
		try {
			textViewer.getTextWidget().setCaretOffset(offset);
		} catch (Exception e) {
		}
	}

	@Override
	public void setDirty(boolean dirty) {
		// ignored
	}

	protected void showOverviewRulerMessage(MouseEvent e) {
		Control control = (Control) e.widget;
		int line = getOverviewRuler().toDocumentLineNumber(e.y);
		if (line > -1) {
			line++;
			try {
				IMarker[] markers = file.findMarkers(IMarker.PROBLEM, false,
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

	protected void showVerticalRulerMessage(MouseEvent e) {
		Control control = (Control) e.widget;
		int line = getVerticalRuler().toDocumentLineNumber(e.y);
		if (line > -1) {
			line++;
			try {
				IMarker[] markers = file.findMarkers(IMarker.PROBLEM, false,
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

	protected void clearProblemMarkers() {
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

	protected void addProblemMarkers(DefaultErrorHandler eh) {

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

					Map<String, Object> attributes = new HashMap<String, Object>(
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

	protected class DocumentAction extends KeyAdapter {

		public void keyPressed(KeyEvent e) {
			if (file.isReadOnly()) {
				return;
			}

			if (e.stateMask == SWT.CTRL && e.character == ' ') {
				doContentAssist();
			}
		}
	}

	protected void showDocument() {
		parseDocument();
	}

	public abstract void doContentAssist();

	public abstract void parseDocument();

	public abstract Vector<String> getExpectedValues();

	protected void addDocumentActions() {
		if (textViewer != null) {
			StyledText st = textViewer.getTextWidget();
			if (st != null) {
				st.addKeyListener(documentAction = new DocumentAction());
			}
		}
	}

	protected void removeDocumentActions() {
		if (textViewer != null) {
			StyledText st = textViewer.getTextWidget();
			if (st != null) {
				if (documentAction != null) {
					st.removeKeyListener(documentAction);
					documentAction = null;
				}
			}
		}
	}

	protected Element findElementWithNode(DefaultElementNode node) {

		Object root = outline.getRoot();

		Stack<Object> elementStack = new Stack<Object>();
		HashMap<DefaultElementNode, Stack<Integer>> stackMap = new HashMap<DefaultElementNode, Stack<Integer>>(
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
				Stack<Integer> indexStack = (Stack<Integer>) stackMap.get(element);
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

	protected void doInputDocumentChanged(IDocument oldInput, IDocument newInput) {

		if (newInput instanceof DefaultEspressoDocument) {
			DefaultEspressoDocument dedoc = (DefaultEspressoDocument) newInput;
			DefaultEspressoStyledDocument dsdoc = dedoc.getDocument();

			dsdoc.setEditor(DefaultEspressoEditor.this);

			StyledText st = textViewer.getTextWidget();
			st.addLineStyleListener(new DefaultEspressoLineStyleAdapter(dsdoc));
			st.addLineBackgroundListener(new DefaultEspressoLineBackgroundAdapter());

			showDocument();
			dsdoc.addDocumentListener(documentTracker = new DocumentTracker());
			addDocumentActions();
			initRulerAnnotationListeners();
		}
	}

	public void updateSaveStatus() {
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	protected void documentChanged(DocumentEvent e) {
		updateSaveStatus();
		Display display = XMLEspressoActivator.getDefault().getDisplay();
		if (display != null) {
			display.asyncExec(new AsyncParsing());
		}
	}

	private void addResourceChangeListeners() {

		IResourceChangeListener closeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent e) {
				IResource resource = e.getResource();
				final int type = e.getType();
				IProject project = file.getProject();
				if (resource.equals(project)) {
					IWorkbench workbench = XMLEspressoActivator.getDefault()
							.getWorkbench();
					if (workbench != null) {
						IWorkbenchWindow wbw = workbench
								.getActiveWorkbenchWindow();
						if (wbw == null
								&& workbench.getWorkbenchWindowCount() > 0)
							wbw = workbench.getWorkbenchWindows()[0];

						if (wbw != null) {
							final IWorkbenchPage page = wbw.getActivePage();
							if (page != null) {
								workbench.getDisplay().asyncExec(
										new Runnable() {
											public void run() {
												page.closeEditor(
														DefaultEspressoEditor.this,
														type == IResourceChangeEvent.PRE_CLOSE);
											}
										});

							}
						}
					}

					ResourcesPlugin.getWorkspace()
							.removeResourceChangeListener(this);
				}
			}
		};

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				closeListener,
				IResourceChangeEvent.PRE_CLOSE
						| IResourceChangeEvent.PRE_DELETE);

		IResourceChangeListener changeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent e) {

				try {
					switch (e.getType()) {
					case IResourceChangeEvent.POST_CHANGE:
						e.getDelta().accept(new IResourceDeltaVisitor() {
							public boolean visit(IResourceDelta delta)
									throws CoreException {
								switch (delta.getKind()) {
								case IResourceDelta.REMOVED:
									if (file.equals(delta.getResource())) {
										IWorkbench workbench = XMLEspressoActivator
												.getDefault().getWorkbench();
										if (workbench != null) {
											IWorkbenchWindow wbw = workbench
													.getActiveWorkbenchWindow();
											if (wbw == null
													&& workbench
															.getWorkbenchWindowCount() > 0)
												wbw = workbench
														.getWorkbenchWindows()[0];

											if (wbw != null) {
												final IWorkbenchPage page = wbw
														.getActivePage();
												if (page != null) {
													workbench
															.getDisplay()
															.asyncExec(
																	new Runnable() {
																		public void run() {
																			page.closeEditor(
																					DefaultEspressoEditor.this,
																					false);
																		}
																	});

												}
											}
										}
									}
									break;
								}

								return true;
							}
						});
						break;
					}

				} catch (CoreException ex) {
				}
			}
		};

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				changeListener, IResourceChangeEvent.POST_CHANGE);

	}

	protected class TextInputListener implements ITextInputListener {

		public void inputDocumentAboutToBeChanged(IDocument oldInput,
				IDocument newInput) {

		}

		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			doInputDocumentChanged(oldInput, newInput);
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

	protected void initRulerAnnotationListeners() {
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

	protected class DefaultTreeListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {

			if (outline != null) {
				TreePath p = outline.getTreeSelectionPath();

				if (p != null) {
					DefaultElementNode lnode = (DefaultElementNode) p
							.getLastPathComponent();
					if (lnode != null) {
						Element ele = findElementWithNode(lnode);
						if (ele != null) {
							int start = ele.getStartOffset();
							int end = ele.getEndOffset();

							textViewer.setSelectedRange(start, (end - start));
							textViewer.getTextWidget().showSelection();
						}
					}
				}
			}

		}

	}

	protected class AsyncParsing implements Runnable {
		public void run() {
			parseDocument();
		}
	}
}
