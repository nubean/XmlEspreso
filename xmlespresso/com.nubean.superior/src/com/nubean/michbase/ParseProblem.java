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

import org.xml.sax.SAXParseException;

public class ParseProblem implements Comparable<ParseProblem> {

	private SAXParseException exception;
	private int type;

	public ParseProblem(int type, SAXParseException ex) {
		this.exception = ex;
		this.type = type;
	}

	public int compareTo(ParseProblem p) throws ClassCastException {
		int ret = 0;
		if (exception.getLineNumber() > p.exception.getLineNumber()) {
			ret = 1;
		} else if (exception.getLineNumber() < p.exception.getLineNumber()) {
			ret = -1;
		} else if (exception.getLineNumber() == p.exception.getLineNumber()) {
			if (exception.getColumnNumber() > p.exception.getColumnNumber()) {
				ret = 1;
			} else if (exception.getColumnNumber() < p.exception
					.getColumnNumber()) {
				ret = -1;
			} else {
				ret = 0;
			}
		}
		return ret;
	}

	public String getDescription() {
		return exception.getMessage();
	}

	public int getType() {
		return type;
	}

	public int getLine() {
		return exception.getLineNumber();
	}

	public int getColumn() {
		return exception.getColumnNumber();
	}

}
