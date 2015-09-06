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

package com.nubean.xmlespresso.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.nubean.michbase.CommonUtils;
import com.nubean.michxml.XMLDefaultStyleContext;
import com.nubean.michxml.XMLDocument;
import com.nubean.michxml.XMLDocumentDescriptor;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.xmlespresso.XMLEspressoActivator;

public class XMLEspressoDocumentProvider extends FileDocumentProvider {

	private XMLEditorConfiguration editorConfig;

	public XMLEspressoDocumentProvider(XMLEditorConfiguration editorConfig) {
		super();
		this.editorConfig = editorConfig;
	}

	protected IDocument createDocument(Object element) throws CoreException {

		if (element instanceof IEditorInput) {
			IEditorInput input = (IEditorInput) element;

			if (input instanceof IFileEditorInput) {
				return createDocument((IFileEditorInput) input);
			} else {
				if (input instanceof IAdaptable) {
					IFileEditorInput editorInput = (IFileEditorInput) input
							.getAdapter(IFileEditorInput.class);
					if (editorInput != null) {
						return createDocument((IFileEditorInput) input);
					}
				}
			}

		}
		throw new CoreException(new Status(IStatus.ERROR, XMLEspressoActivator.PLUGIN_ID,
				IStatus.ERROR, "Invalid Input", null));
	}

	private IDocument createDocument(IFileEditorInput editorInput)
			throws CoreException {

		XMLDocument xmldoc = new XMLDocument(
				(editorConfig != null ? editorConfig.getStyleContext()
						: new XMLDefaultStyleContext()));

		IFile file = editorInput.getFile();

		File ffile = file.getRawLocation().toFile();
		if (ffile.length() > 0) {
			String encoding = null;

			String xml = file.getPersistentProperty(new QualifiedName(
					XMLEspressoActivator.QUALIFIER,
					XMLEspressoActivator.DOC_DESCRIPTOR_PROPERTY));
			if (xml != null) {
				try {
					XMLDocumentDescriptor docInfo = (XMLDocumentDescriptor) CommonUtils
							.deserialize(xml);
					encoding = docInfo.getEncoding();
				} catch (Exception e) {

				}
			}
			readDocumentFromFile(ffile, xmldoc, encoding);
		}

		return new XMLEspressoDocument(xmldoc);
	}

	private void readDocumentFromFile(File file, XMLDocument doc,
			String encoding) {
		// so just set text
		BufferedReader ir = null;
		try {
			if (doc.getLength() > 0) {
				doc.replace(0, doc.getLength(), "", null);
			}
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
			XMLEspressoActivator.getDefault().error(
					"File read error:" + file.getAbsolutePath(), e);
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

	protected IAnnotationModel createAnnotationModel(Object element)
			throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) element;
			return new XMLMarkerAnnotationModel(input.getFile());
		}

		return super.createAnnotationModel(element);
	}
}
