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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nubean.michbase.*;

import java.util.*;

public class CatalogsTreeContentProvider implements ITreeContentProvider {

	private Catalogs catalogs;

	@Override
	public Object[] getChildren(Object parentElement) {

		try {
			if (parentElement instanceof Catalog) {
				Catalog catalog = (Catalog) parentElement;
				return catalog.getEntries().toArray();
			} else if (parentElement instanceof Catalogs) {
				Catalogs catalogs = (Catalogs) parentElement;
				return catalogs.getCatalogs().toArray();
			}
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		try {
			if (element instanceof CatalogEntry) {
				Vector catalogs = this.catalogs.getCatalogs();
				for (int i = 0; i < catalogs.size(); i++) {
					Catalog catalog = (Catalog) catalogs.elementAt(i);
					if (catalog.getEntries().contains(element))
						return catalog;
				}
			} else if (element instanceof Catalog) {
				return this.catalogs;
			}
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		try {
			if (element instanceof CatalogEntry) {
				return false;
			} else if (element instanceof Catalog) {
				return (((Catalog) element).getEntries().size() > 0);
			} else if (element instanceof Catalogs) {
				return (((Catalogs) element).getCatalogs().size() > 0);
			}
		} catch (Exception e) {

		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			catalogs = (Catalogs) inputElement;
			Vector v = catalogs.getCatalogs();

			for (int i = 0; i < v.size(); i++) {
				Catalog catalog = (Catalog) v.elementAt(i);
				filter(catalog);
				if (catalog.getEntries().size() == 0) {
					v.remove(catalog);
				}
			}

			return v.toArray();
		} catch (Exception e) {

		}
		return null;

	}

	private void filter(Catalog catalog) {
		Vector ces = catalog.getEntries();
		for (int i = 0; i < ces.size(); i++) {
			CatalogEntry ce = (CatalogEntry) ces.elementAt(i);
			if (!ce.getMimeType().equalsIgnoreCase("text/xml")) {
				ces.remove(ce);
			}
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
