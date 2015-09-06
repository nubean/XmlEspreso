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

import javax.swing.undo.UndoableEdit;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class XMLEspressoUndoableOperation implements IUndoableOperation {

	private UndoableEdit edit;
	private Vector contexts;
	
	public XMLEspressoUndoableOperation(UndoableEdit edit) {
		super();
		this.edit = edit;
		contexts = new Vector(2,2);
	}

	public void addContext(IUndoContext context) {
		if(!contexts.contains(context))
			contexts.add(context);
	}

	public boolean canExecute() {
		return false;
	}

	public boolean canRedo() {
		return edit != null && edit.canRedo();
	}

	public boolean canUndo() {
		return edit != null && edit.canUndo();
	}

	public void dispose() {

	}

	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return null;
	}

	public IUndoContext[] getContexts() {
		IUndoContext[] ctxt = new IUndoContext[contexts.size()];
		contexts.toArray(ctxt);
		return ctxt;
	}

	public String getLabel() {
		return edit.getPresentationName();
	}

	public boolean hasContext(IUndoContext context) {
		return contexts.contains(context);
	}

	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		edit.redo();
		return Status.OK_STATUS;
	}

	public void removeContext(IUndoContext context) {
		contexts.remove(context);
	}

	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		edit.undo();
		return Status.OK_STATUS;
	}

}
