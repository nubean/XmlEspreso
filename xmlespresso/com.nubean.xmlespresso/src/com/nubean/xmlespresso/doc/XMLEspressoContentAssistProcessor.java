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

import java.util.Vector;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.nubean.michxml.XMLAbstractEditor;
import com.nubean.michxml.XMLDocument;
import com.nubean.michxml.elem.XMLAbstractElement;
import com.nubean.michxml.elem.XMLDocumentElement;
import com.nubean.michxml.elem.XMLElement;
import com.nubean.michxml.elem.XMLEndTagElement;
import com.nubean.michxml.elem.XMLStartTagElement;
import com.nubean.michxml.elem.XMLWhiteSpaceElement;
import com.nubean.xmlespresso.XMLEspressoActivator;

public class XMLEspressoContentAssistProcessor implements
		IContentAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(
			ITextViewer textViewer, int offset) {
		ICompletionProposal[] proposals = null;

		try {
			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument xmldoc = xedoc.getXMLDocument();
			XMLAbstractEditor editor = xmldoc.getEditor();

			if (editor.isTextMode() && !editor.isWellFormed()) {
				Vector<Object> expected = editor.getParserContentAssist(offset);
				if (expected != null) {
					proposals = new ICompletionProposal[expected.size()];
					expected.toArray(proposals);
				}
			} else {
				proposals = getContentAssist(textViewer, offset);
			}
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error(
					"XMLEspresso content completion proposals error", e);
		}

		return proposals;

	}

	private ICompletionProposal[] getContentAssist(ITextViewer textViewer,
			int where) {

		ICompletionProposal[] proposals = null;

		try {
			XMLEspressoDocument xedoc = (XMLEspressoDocument) textViewer
					.getDocument();
			XMLDocument document = (XMLDocument) xedoc.getXMLDocument();
			XMLAbstractElement root = document.getRootElement();
			XMLAbstractElement ele = root.positionToElement(where);
			XMLAbstractEditor editor = document.getEditor();

			if (ele == null)
				return proposals;

			Vector<Object> expected = null;

			if (ele instanceof XMLStartTagElement) {
				where = ele.getEndOffset() - 1;

				expected = editor.getAttributeContentAssist(
						((XMLAbstractElement) ele).getXMLNode(), where);

				if (expected != null) {
					proposals = new ICompletionProposal[expected.size()];
					expected.toArray(proposals);
				}

				return proposals;
			}
			
			if (ele.getParentElement() instanceof XMLStartTagElement) {
				where = ele.getParentElement().getEndOffset() - 1;

				expected = editor.getAttributeContentAssist(
						((XMLAbstractElement) ele.getParentElement())
								.getXMLNode(), where);

				if (expected != null) {
					proposals = new ICompletionProposal[expected.size()];
					expected.toArray(proposals);
				}

				return proposals;
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

				parent = (XMLAbstractElement) ele.getParentElement();
			}
			if (parent != null) {
				expected = editor.getElementContentAssist(parent.getXMLNode(),
						ele.getXMLNode(), where);
				if (expected != null) {
					proposals = new ICompletionProposal[expected.size()];
					expected.toArray(proposals);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return proposals;

	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer arg0,
			int arg1) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

}
