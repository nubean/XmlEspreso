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

package com.nubean.michxml;

public class TaglibDef {
	public String nsPrefix;

	public String nsURI;

	public String schemaLocation;

	public Object clone() {

		TaglibDef td = new TaglibDef();

		td.nsPrefix = this.nsPrefix;
		td.nsURI = this.nsURI;
		td.schemaLocation = this.schemaLocation;

		return td;

	}

	public boolean equals(Object other) {
		TaglibDef otd = (TaglibDef) other;

		boolean ret = false;

		try {
			ret = otd.nsPrefix.equals(this.nsPrefix)
					&& otd.nsURI.equals(this.nsURI)
					&& otd.schemaLocation.equals(this.schemaLocation);
		} catch (Exception e) {
			ret = super.equals(other);
		}
		return ret;
	}
}
