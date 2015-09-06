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

import com.nubean.xmlespresso.XMLEspressoActivator;
import com.nubean.xmlespresso.editors.DefaultEspressoEditor;

public class DefaultEspressoContentAssistProcessor implements
		IContentAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(
			ITextViewer textViewer, int offset) {
		ICompletionProposal[] proposals = null;

		try {
			DefaultEspressoDocument dedoc = (DefaultEspressoDocument) textViewer
					.getDocument();
			DefaultEspressoStyledDocument document = dedoc.getDocument();
			DefaultEspressoEditor editor = document.getEditor();
			editor.parseDocument();
			Vector<String> expected = editor.getExpectedValues();
			if (expected != null) {
				proposals = new ICompletionProposal[expected.size()];
				expected.toArray(proposals);
			}
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error(
					"Content completion proposals error", e);
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
