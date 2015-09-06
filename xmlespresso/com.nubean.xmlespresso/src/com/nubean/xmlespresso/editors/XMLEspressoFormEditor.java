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

import java.io.File;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.FileEditorInput;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michxml.XMLDocumentDescriptor;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.michxml.XMLModel;
import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.actions.XMLFormatAction;
import com.nubean.xmlespresso.wizards.OpenXMLDocumentWizard;

public class XMLEspressoFormEditor extends FormEditor {

	private static final String EDITOR_CONTEXT_ID = "com.nubean.xmlEspressoEditorContext";
	public static final String FORMAT_ACTION_DEF_ID = "com.nubean.xmlespresso.format";

	private XMLEspressoEditor editor, seditor;

	private DocumentDescriptor docInfo;

	private IFile file;

	public XMLEspressoFormEditor() {
		super();
	}
	
	/**
	 * Instantiate any handlers specific to this view and activate them.
	 */
	private void activateFormatHandler(XMLEspressoEditor editor) {
		// 1 - get the handler service from the view site
		IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);
		// 2 - create the handler instance
		ActionHandler formatHandler = new ActionHandler(new XMLFormatAction(editor));
		
		// 3 - activate this handler instance for the format command
		handlerService.activateHandler(FORMAT_ACTION_DEF_ID, formatHandler);
	}
	
	/**
	 * Activate a context that this view uses. It will be tied to this view
	 * activation events and will be removed when the view is disposed.
	 */
	private void activateContext() {
		IContextService contextService = (IContextService) getSite()
				.getService(IContextService.class);
		contextService.activateContext(EDITOR_CONTEXT_ID);
	}

	protected void addPages() {
		IPreferenceStore ps = XMLEspressoActivator.getDefault()
				.getPreferenceStore();

		String mimeType = docInfo.getMimeType();

		StringBuffer sb = new StringBuffer(XMLEspressoActivator.CONFIG_PREF);
		sb.append(":").append(mimeType);

		String xml = ps.getString(sb.toString());
		XMLEditorConfiguration config = null;
		if (xml != null && xml.trim().length() > 0) {
			config = (XMLEditorConfiguration) CommonUtils.deserialize(xml);
		} else {
			config = new XMLEditorConfiguration();
		}

		createEditorPages((XMLEditorConfiguration) config);
	}

	private void createEditorPages(XMLEditorConfiguration config) {
		activateContext();
		
		Composite parent = getContainer();
		Composite designPage = new Composite(parent, SWT.BORDER);
		designPage.setLayout(new FillLayout(SWT.HORIZONTAL));

		boolean validateSchema = config.getValidateSchema();

		editor = new XMLEspressoEditor(file, config,
				(XMLDocumentDescriptor) docInfo, designPage);
		editor.setStatusLineManager(getStatusLineManager());

		activateFormatHandler(editor);
		
		try {
			addPage(0, editor, getEditorInput());
			addPage(1, designPage);

			setPageText(0, XMLEspressoActivator.getResourceString("source"));
			setPageText(1, XMLEspressoActivator.getResourceString("design"));

			if (validateSchema) {
				XMLModel xmlModel = editor.getXMLModel();
				File sfile = xmlModel.getSchemaFile();
				if (sfile != null && sfile.exists()) {
					try {
						IPath path = new Path(sfile.getAbsolutePath());
						IProject project = file.getProject();
						cleanupAndSync(project);

						StringBuffer sb = new StringBuffer(16);
						sb.append(".").append(sfile.getName());
						IFile schemaFile = project.getFile(sb.toString());

						if (schemaFile.exists()) {
							schemaFile.delete(true, false, null);
						}
						schemaFile.createLink(path, IResource.SHALLOW, null);
						ResourceAttributes ra = new ResourceAttributes();
						ra.setReadOnly(true);
						schemaFile.setResourceAttributes(ra);

						XMLDocumentDescriptor sdocInfo = new XMLDocumentDescriptor();
						sdocInfo.setPath(sfile.getParentFile()
								.getAbsolutePath());
						sdocInfo.setName(sfile.getName());
						String xml = sdocInfo.toXMLString();
						schemaFile
								.setPersistentProperty(
										new QualifiedName(
												XMLEspressoActivator.QUALIFIER,
												XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY),
										xml);

						seditor = new XMLEspressoEditor(schemaFile, config,
								sdocInfo, null);
						seditor.setStatusLineManager(getStatusLineManager());

						addPage(2, seditor, new FileEditorInput(schemaFile));
						setPageText(2, XMLEspressoActivator
								.getResourceString("schema"));
					} catch (CoreException e) {
						XMLEspressoActivator.getDefault().error("create editor pages error", e);
					}
				}

			}
		} catch (PartInitException e) {
			XMLEspressoActivator.getDefault().error("create editor pages error", e);
		}
	}

	private IStatusLineManager getStatusLineManager() {

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

	public void doSave(IProgressMonitor monitor) {
		if (editor != null) {
			editor.doSave(monitor);
		}
	}

	private void cleanupAndSync(IProject project) {
		try {
			project.refreshLocal(IProject.DEPTH_ONE, null);
			IResource[] res = project.members();

			int count = (res != null ? res.length : 0);
			for (int i = 0; i < count; i++) {
				IResource r = (IResource) res[i];
				if (r.getType() == IResource.FILE && r.isLinked()) {
					String name = r.getName();
					if (name.startsWith(".michide") && name.endsWith(".xsd")) {
						File file = r.getRawLocation().toFile();
						if (!file.exists()) {
							r.delete(true, null);
						}
					}
				}
			}
		} catch (CoreException e) {
			
		}

	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		super.init(site, input);
		setInput(input);
		setSite(site);
		setPartName(input.getName());

		if (!(input instanceof IFileEditorInput))
			throw new PartInitException(new Status(IStatus.ERROR,
					XMLEspressoActivator.PLUGIN_ID, IStatus.ERROR, XMLEspressoActivator
							.getResourceString("invalid.input.type"), null));

		IFileEditorInput editorInput = (IFileEditorInput) input;
		file = editorInput.getFile();
		if (!file.getLocation().toFile().exists()) {
			throw new PartInitException(new Status(IStatus.ERROR,
					XMLEspressoActivator.PLUGIN_ID, IStatus.ERROR, file.getLocation()
							.toOSString()
							+ XMLEspressoActivator
									.getResourceString("invalid.file"), null));
		}

		boolean newDocument = false;

		try {
			String xml = file.getPersistentProperty(new QualifiedName(
					XMLEspressoActivator.QUALIFIER,
					XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY));
			File ffile = file.getRawLocation().toFile();

			if (ffile.length() == 0)
				newDocument = true;

			if (xml == null) {
				if (!newDocument) {
					docInfo = new XMLDocumentDescriptor();
					docInfo.setName(file.getName());
					docInfo.setPath(file.getParent().getLocation().toString());
					xml = docInfo.toXMLString();
					file.setPersistentProperty(new QualifiedName(
							XMLEspressoActivator.QUALIFIER,
							XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY), xml);

				} else {
					OpenXMLDocumentWizard wizard = new OpenXMLDocumentWizard();
					wizard.init(XMLEspressoActivator.getDefault().getWorkbench(),
							new StructuredSelection(file));
					WizardDialog dialog = new WizardDialog(XMLEspressoActivator
							.getDefault().getWorkbench()
							.getActiveWorkbenchWindow().getShell(), wizard);
					dialog.open();
					xml = file.getPersistentProperty(new QualifiedName(
							XMLEspressoActivator.QUALIFIER,
							XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY));
				}
				if (xml == null) {
					throw new PartInitException(
							new Status(
									IStatus.ERROR,
									XMLEspressoActivator.PLUGIN_ID,
									IStatus.ERROR,
									file.getLocation().toOSString()
											+ XMLEspressoActivator
													.getResourceString("invalid.document"),
									null));
				}

			}
			docInfo = (DocumentDescriptor) CommonUtils.deserialize(xml);
			if (!docInfo.getName().equals(file.getName())
					|| !docInfo.getPath().equals(
							file.getParent().getLocation().toString())) {
				docInfo.setName(file.getName());
				docInfo.setPath(file.getParent().getLocation().toString());
				xml = docInfo.toXMLString();
				file.setPersistentProperty(new QualifiedName(
						XMLEspressoActivator.QUALIFIER,
						XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY), xml);
			}
		} catch (CoreException e) {
			throw new PartInitException(new Status(IStatus.ERROR,
					XMLEspressoActivator.PLUGIN_ID, IStatus.ERROR, e
							.getLocalizedMessage(), e));
		}

		addResourceChangeListeners();

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
												page
														.closeEditor(
																XMLEspressoFormEditor.this,
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
																			page
																					.closeEditor(
																							XMLEspressoFormEditor.this,
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

	public boolean isDirty() {
		if (editor != null)
			return editor.isDirty();

		return false;
	}

	public void doSaveAs() {
		if (editor != null)
			editor.doSaveAs();
	}

	public boolean isSaveAsAllowed() {
		if (editor != null)
			return editor.isSaveAsAllowed();

		return false;
	}

	public Object getAdapter(Class required) {

		if (editor != null)
			return editor.getAdapter(required);

		return null;
	}

	public XMLEspressoEditor getXMLEditor() {
		return editor;
	}
}
