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

package com.nubean.xmlespresso.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.part.*;

import com.nubean.xmlespresso.*;
import com.nubean.michxml.*;
import com.nubean.michbase.*;

public class NewXMLDocumentWizard extends Wizard implements INewWizard {

	private IStructuredSelection selection;

	private NewXMLFileCreationWizardPage newFilePage;

	private CatalogsWizardPage catalogsPage;

	private CustomDocumentWizardPage customPage;

	private static final String NEW_XML_DOCUMENT = XMLEspressoActivator
			.getResourceString("newXMLDocument");

	private static final String NEW_FILE_PAGE = XMLEspressoActivator
			.getResourceString("newFilePage");

	private static final String CATALOG_PAGE = XMLEspressoActivator
			.getResourceString("catalogPage");

	private static final String CUSTOM_PAGE = XMLEspressoActivator
			.getResourceString("customPage");

	private XMLDocumentDescriptor docInfo;

	public NewXMLDocumentWizard() {
		super();
	}

	public XMLDocumentDescriptor getDocumentDescriptor() {
		return docInfo;
	}

	@Override
	public boolean performFinish() {
		IFile newFile = newFilePage.createNewFile();

		docInfo = new XMLDocumentDescriptor();
		docInfo.setName(newFile.getName());
		docInfo.setPath(newFile.getParent().getLocation().toString());

		if (customPage != null) {
			String schema = customPage.getSchema();
			if (schema != null && !schema.startsWith("http://")
					&& !schema.startsWith("HTTP://")
					&& !schema.startsWith("FILE://")
					&& !schema.startsWith("file://")) {
				schema = "file:///" + schema;
			}
			docInfo.setSchemaLocation(schema);

			String dtd = customPage.getDTD();
			if (dtd != null && !dtd.startsWith("http://")
					&& !dtd.startsWith("HTTP://") && !dtd.startsWith("FILE://")
					&& !dtd.startsWith("file://")) {
				dtd = "file:///" + dtd;
			}
			docInfo.setDtdLocation(dtd);

			docInfo.setRootElement(customPage.getRoot());
			docInfo.setDtdPublicId(customPage.getPublicID());
			docInfo.setEncoding(customPage.getEncoding());
			docInfo.setUseSchemaTargetNamespace(catalogsPage
					.getUseDefulatNamespace());

			if (!docInfo.getUseSchemaTargetNamespace()) {
				docInfo.setNSPrefix(customPage.getPrefix());
				docInfo.setNSUri(customPage.getURI());
			}

		} else if (catalogsPage != null) {
			CatalogEntry entry = catalogsPage.getEntry();

			docInfo.setMimeType(entry.getMimeType());
			docInfo.setSchemaLocation(entry.getProperty("schema"));
			docInfo.setDtdLocation(entry.getProperty("dtd"));
			docInfo.setRootElement(entry.getProperty("root"));
			docInfo.setNSPrefix(entry.getProperty("targetPrefix"));
			docInfo.setNSUri(entry.getProperty("targetNamespace"));
			docInfo.setExt(entry.getProperty("ext"));
			docInfo.setDtdPublicId(entry.getProperty("public"));
			docInfo.setEncoding(catalogsPage.getEncoding());

			docInfo.setUseSchemaTargetNamespace(catalogsPage
					.getUseDefulatNamespace());
			if (!docInfo.getUseSchemaTargetNamespace()) {
				docInfo.setNSPrefix(catalogsPage.getPrefix());
				docInfo.setNSUri(catalogsPage.getURI());
			}
		}

		try {
			newFile.setPersistentProperty(new QualifiedName(
					XMLEspressoActivator.QUALIFIER,
					XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY), docInfo
					.toXMLString());
			FileEditorInput input = new FileEditorInput(newFile);
			String id = "com.nubean.xmlespresso.xmleditor";
			XMLEspressoActivator.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().openEditor(
							input, id);
		} catch (Exception e) {

		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(NEW_XML_DOCUMENT);
	}

	@Override
	public void addPages() {
		newFilePage = new NewXMLFileCreationWizardPage(NEW_FILE_PAGE, selection);
		addPage(newFilePage);

		catalogsPage = new CatalogsWizardPage(CATALOG_PAGE);
		addPage(catalogsPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = null;

		if (page instanceof NewXMLFileCreationWizardPage) {

			if (newFilePage.isCatalog()) {
				nextPage = catalogsPage;
				if (customPage != null)
					customPage.setPageComplete(true);
			} else if (newFilePage.isCustom()) {

				if (customPage == null) {
					customPage = new CustomDocumentWizardPage(CUSTOM_PAGE);
					addPage(customPage);
				}
				nextPage = customPage;
				if (catalogsPage != null)
					catalogsPage.setPageComplete(true);
			}

		}
		return nextPage;
	}

}
