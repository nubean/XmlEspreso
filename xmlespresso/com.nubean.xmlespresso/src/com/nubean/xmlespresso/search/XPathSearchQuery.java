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

package com.nubean.xmlespresso.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ide.ResourceUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nubean.michutil.LocalizedResources;
import com.nubean.michutil.NameSpaceContextImpl;
import com.nubean.michxml.XMLDocument;
import com.nubean.michxml.XMLDocumentDescriptor;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.michxml.XMLNode;
import com.nubean.michxml.elem.XMLAbstractElement;
import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.editors.XMLEspressoEditor;

public class XPathSearchQuery implements ISearchQuery {

	private String[] xmlFileExtensions = { "xml", "xsl", "xslt", "xsd",
			"xhtml", "xs" };

	public static final String XPATH_MARKER = "com.nubean.xmlespresso.xpathmarker";

	private String query;
	private XPath xPath;
	private XPathSearchResult searchResult;
	private ISearchPageContainer container;

	public XPathSearchQuery(String query, ISearchPageContainer container) {
		this.query = query;
		this.container = container;
		xPath = XPathFactory.newInstance().newXPath();
		String label = LocalizedResources.applicationResources
				.getString("xpath.query") + query;
		this.searchResult = new XPathSearchResult(label, query, this);

		NewSearchUI.activateSearchResultView();
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public String getLabel() {
		return LocalizedResources.applicationResources
				.getString("xpath.search");
	}

	@Override
	public ISearchResult getSearchResult() {
		return searchResult;
	}

	@Override
	public IStatus run(IProgressMonitor arg0) throws OperationCanceledException {

		Status status = null;

		searchResult.removeAll();
		try {
			switch (container.getSelectedScope()) {
			case ISearchPageContainer.SELECTION_SCOPE:
				searchSelectionScope();
				break;
			case ISearchPageContainer.WORKSPACE_SCOPE:
				searchWorkspaceScope();
				break;
			case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
				searchSelectedProjectsScope();
				break;
			case ISearchPageContainer.WORKING_SET_SCOPE:
				searchWorkingSets();
				break;
			}

			status = new Status(Status.OK, XMLEspressoActivator.PLUGIN_ID,
					"Search completed");
		} catch (OperationCanceledException e) {
			status = new Status(Status.CANCEL, XMLEspressoActivator.PLUGIN_ID,
					"Search cancelled");
			throw e;
		} catch (Throwable t) {
			status = new Status(Status.ERROR, XMLEspressoActivator.PLUGIN_ID,
					"Search cancelled", t);
		}

		return status;
	}

	private boolean isXml(IFile file) {
		String ext = file.getFileExtension();

		boolean xml = false;

		for (String s : xmlFileExtensions) {
			if (s.equalsIgnoreCase(ext)) {
				xml = true;
				break;
			}
		}

		return xml;
	}

	private void clearXPathMarkers(IFile file) {
		try {
			IMarker[] markers = file.findMarkers(XPATH_MARKER, false,
					IResource.DEPTH_ZERO);
			int mcount = (markers != null ? markers.length : 0);
			for (int i = 0; i < mcount; i++) {
				String msg = "Deleting XPath marker:" + markers[i].getType();
				XMLEspressoActivator.getDefault().println(msg);
				markers[i].delete();
			}

		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error("XPath Search error:", e);
		}

	}

	private void addXPathMarker(IFile file, int start,
			int end, int line, String text) {
		try {
			IMarker marker = file.createMarker(XPATH_MARKER);
			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, end);
			marker.setAttribute(IMarker.LINE_NUMBER, line);
			marker.setAttribute(IMarker.LOCATION, "Line " + line);
			marker.setAttribute(IMarker.MESSAGE, text);
		} catch (CoreException e) {
			XMLEspressoActivator.getDefault().error("XPath Search error:", e);
		}
	}

	private void searchFile(IFile file) {
		try {
			if (!isXml(file))
				return;

			xPath.reset();
			clearXPathMarkers(file);

			org.w3c.dom.Document domDocument = null;
			XMLDocumentDescriptor docInfo = new XMLDocumentDescriptor();
			docInfo.setName(file.getName());
			docInfo.setPath(file.getParent().getLocation().toString());

			XMLEditorConfiguration editorConfig = new XMLEditorConfiguration();

			XMLEspressoEditor editor = new XMLEspressoEditor(file,
					editorConfig, docInfo, null);
			domDocument = editor.getXml();

			HashMap<String, String> namespace = editor.getXMLModel()
					.getDocumentNamespace();

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

			if (domDocument != null) {
				NodeList nodeList = null;

				try {
					nodeList = (NodeList) xPath.evaluate(query, domDocument,
							XPathConstants.NODESET);
				} catch (Exception e) {
					XMLEspressoActivator.getDefault().error(
							"XPath Search error:", e);
				}

				int nodeCount = (nodeList != null ? nodeList.getLength() : 0);

				if (nodeCount > 0) {

					XMLDocument xmldoc = editor.createNewXMLDocument();

					XMLNode rootNode = xmldoc.getRootElement().getXMLNode();

					for (int i = 0; i < nodeCount; i++) {
						Node node = nodeList.item(i);

						XMLNode matchXMLNode = rootNode
								.findXMLNodeWithNode(node);

						if (matchXMLNode != null) {
							XMLAbstractElement ele = xmldoc
									.findElementWithNode(matchXMLNode);

							if (ele != null) {

								int start = ele.getStartOffset();
								int end = ele.getEndOffset();

								int startline = xmldoc.getDefaultRootElement()
										.getElementIndex(start);
								int length = end - start;
								int excerpt = Math.min(length, 128);
								String text = xmldoc.getText(start, excerpt);
								if (excerpt < length)
									text = text + "...";
								XPathMatch xpathMatch = new XPathMatch(
										new XPathMatchElement(file),
										Match.UNIT_CHARACTER, start, length,
										startline, text);

								searchResult.addMatch(xpathMatch);
								addXPathMarker(file, start, end, startline, text);
							}
						}
					}
				} else {
					try {
						String result = xPath.evaluate(query, domDocument);
						if (result != null && result.trim().length() > 0) {
							String msg = "XPath query: " + query + ": File: "
									+ file.getName() + ": Result: " + result;
							XMLEspressoActivator.getDefault().println(msg);
						}
					} catch (XPathExpressionException e) {
						XMLEspressoActivator.getDefault().error(
								"XPath Search error:", e);
					}
				}
			}
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error("XPath Search error:", e);
		}
	}

	private void searchContainer(IContainer resource) {
		try {
			IResource[] members = resource.members();
			for (IResource member : members) {

				if (member.isHidden() || !member.isAccessible()
						|| member.isDerived())
					continue;

				switch (member.getType()) {
				case IResource.FILE:
					searchFile((IFile) member);
					break;
				case IResource.FOLDER:
					searchContainer((IFolder) member);
					break;
				case IResource.PROJECT:
					searchContainer((IProject) member);
					break;
				default:
					String msg = "Unexpected type:" + member.getType() + ":"
							+ member.getFullPath();
					XMLEspressoActivator.getDefault().println(msg);
					break;
				}

			}
		} catch (CoreException e) {
			XMLEspressoActivator.getDefault().error("XPath Search error:", e);
		}

	}

	private void searchSelectionScope() {
		ISelection selection = container.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() > 0) {

				@SuppressWarnings("rawtypes")
				Iterator it = ss.iterator();
				while (it.hasNext()) {
					Object next = it.next();
					IResource resource = ResourceUtil.getResource(next);
					switch (resource.getType()) {
					case IResource.FILE:
						searchFile((IFile) resource);
						break;
					case IResource.FOLDER:
						searchContainer((IFolder) resource);
						break;
					case IResource.PROJECT:
						searchContainer((IProject) resource);
						break;
					default:
						String msg = "Unexpected type:" + resource.getType()
								+ ":" + resource.getFullPath();
						XMLEspressoActivator.getDefault().println(msg);
					}
				}
			}
		}

	}

	private void searchWorkspaceScope() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();

		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			searchContainer(project);
		}
	}

	private void searchSelectedProjectsScope() {

		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		String[] selectedProjects = container.getSelectedProjectNames();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {

			for (String p : selectedProjects) {
				if (p.equals(project.getName())) {
					searchContainer(project);
					break;
				}
			}
		}

	}

	private void searchWorkingSets() {

		IWorkingSet[] workingSets = container.getSelectedWorkingSets();
		for (IWorkingSet ws : workingSets) {
			IAdaptable[] elements = ws.getElements();
			for (IAdaptable element : elements) {
				if (element instanceof IFile) {
					searchFile((IFile) element);
				} else if (element instanceof IWorkspace) {
					searchWorkspaceScope();
				} else if (element instanceof IContainer) {
					searchContainer((IContainer) element);
				}
			}
		}

	}

}
