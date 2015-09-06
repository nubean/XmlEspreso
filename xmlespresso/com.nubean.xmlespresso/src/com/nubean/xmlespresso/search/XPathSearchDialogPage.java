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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import com.nubean.michutil.LocalizedResources;

public class XPathSearchDialogPage extends DialogPage implements ISearchPage {
	private Combo queryCombo;
	private ISearchPageContainer container;
	private XPath xPath;
	private Composite control;
	private static ArrayList<String> QUERY_LIST = new ArrayList<String>(8);

	public XPathSearchDialogPage() {
		super(LocalizedResources.applicationResources.getString("xpath.search"));
		xPath = XPathFactory.newInstance().newXPath();
		NewSearchUI.activateSearchResultView();
	}

	@Override
	public void createControl(Composite parent) {
		if (control == null) {
			Group group1 = new Group(parent, SWT.SHADOW_NONE);
			group1.setText(LocalizedResources.applicationResources
					.getString("xpath.query"));
			group1.setLayout(new FillLayout(SWT.HORIZONTAL));
			queryCombo = new Combo(group1, SWT.DROP_DOWN);
			queryCombo
					.setItems(QUERY_LIST.toArray(new String[QUERY_LIST.size()]));
			control = group1;
		}
		setControl(control);
	}

	@Override
	public boolean performAction() {

		String query = queryCombo.getText();
		xPath.reset();

		try {
			xPath.compile(query);
		} catch (XPathExpressionException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			String msg = sw.toString();
			msg = msg.substring(0, msg.indexOf('\n'));
			MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
			mb.setMessage(msg);
			mb.open();
			return false;
		}

		if (!QUERY_LIST.contains(query)) {
			QUERY_LIST.add(0, query);
		}

		XPathSearchQuery searchQuery = new XPathSearchQuery(query, container);

		NewSearchUI.runQueryInForeground(container.getRunnableContext(), searchQuery);

		return true;
	}

	@Override
	public void setContainer(ISearchPageContainer c) {
		this.container = c;
	}

}
