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

package com.nubean.michbase;

import javax.swing.table.*;
import org.xml.sax.*;

import com.nubean.michutil.LocalizedResources;

import java.util.ArrayList;

/**
 * <p>
 * Title: Michigan XML Editor
 * </p>
 * <p>
 * Description: This edits an XML document based on an XML schema.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: Nubean LLC
 * </p>
 * 
 * @author Ajay Vohra
 * @version 1.0
 */

public class DefaultErrorHandler extends AbstractTableModel implements ErrorHandler {
	private static final long serialVersionUID = 6169645475349293851L;

	public final static int ERROR = 0, WARNING = 1, FATAL = 2;

	private final static int COLUMN_COUNT = 3;

	private int nerrors, nfatal, nwarnings;

	private ArrayList<ParseProblem> problems;

	/**
	 * @param document
	 *            the document being validated
	 */

	public DefaultErrorHandler() {
		problems = new ArrayList<ParseProblem>();
	}

	public void warning(SAXParseException exception) throws SAXException {
		problems.add(new ParseProblem(WARNING, exception));
		nwarnings++;
	}

	public void error(SAXParseException exception) throws SAXException {
		problems.add(new ParseProblem(ERROR, exception));
		nerrors++;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		problems.add(new ParseProblem(FATAL, exception));
		nfatal++;
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	public Object getValueAt(int row, int col) {
		ParseProblem p = (ParseProblem) problems.get(row);
		Object obj = null;
		switch (col) {
		case 0:
			obj = new Integer(p.getType());
			break;
		case 1:
			obj = p.getDescription();
			break;
		case 2:
			obj = "line " + Integer.toString(p.getLine()) + ", " + "col "
					+ Integer.toString(p.getColumn());
			break;
		}
		return obj;
	}

	public int getRowCount() {
		return problems.size();
	}

	public String getColumnName(int col) {
		String name = null;
		switch (col) {
		case 0:
			name = LocalizedResources.applicationResources
			.getString("severity");
			break;
		case 1:
			name = LocalizedResources.applicationResources
					.getString("description");
			break;
		case 2:
			name = LocalizedResources.applicationResources
					.getString("location");
			break;
		default:
			name = super.getColumnName(col);
			break;
		}
		return name;
	}

	public ParseProblem getParseProblem(int row) {
		if (row < problems.size())
			return (ParseProblem) problems.get(row);
		else
			return null;
	}

}