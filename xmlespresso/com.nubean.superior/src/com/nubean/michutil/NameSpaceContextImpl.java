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

package com.nubean.michutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

public class NameSpaceContextImpl implements NamespaceContext {
	private HashMap<String, String> prefix2Uri;
	private HashMap<String, String> uri2Prefix;

	public NameSpaceContextImpl() {
		this.prefix2Uri = new HashMap<String, String>();
		this.uri2Prefix = new HashMap<String, String>();
	}

	public void add(String prefix, String uri) {
		prefix2Uri.put(prefix, uri);
		uri2Prefix.put(uri, prefix);
	}
	
	@Override
	public String getNamespaceURI(String prefix) {
		return prefix2Uri.get(prefix);
	}

	@Override
	public java.util.Iterator<String> getPrefixes(String uri) {
		ArrayList<String> prefixes = new ArrayList<String>();

		Set<String> keys = prefix2Uri.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (prefix2Uri.get(key).equals(uri)) {
				prefixes.add(key);
			}
		}

		return prefixes.iterator();
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return uri2Prefix.get(namespaceURI);
	}
}