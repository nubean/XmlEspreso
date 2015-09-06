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

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;

public class XMLEspressoUndoManager implements IUndoManager,
		IUndoManagerExtension, UndoableEditListener {

	private ObjectUndoContext undoContext;

	private IOperationHistory history;

	public XMLEspressoUndoManager(int undoLevel) {
		undoContext = new ObjectUndoContext(this);
		history = OperationHistoryFactory.getOperationHistory();
		setMaximalUndoLevel(undoLevel);
	}

	public void connect(ITextViewer viewer) {
	}

	public void undoableEditHappened(UndoableEditEvent e) {
		IUndoableOperation op = new XMLEspressoUndoableOperation(e.getEdit());
		op.addContext(undoContext);
		history.add(op);
	}

	public void disconnect() {
	}

	public void beginCompoundChange() {
	}

	public void endCompoundChange() {

	}

	public void reset() {
		if (history != null && undoContext != null)
			history.dispose(undoContext, true, true, false);
	}

	public void setMaximalUndoLevel(int undoLevel) {
		undoLevel = Math.max(0, undoLevel);
		history.setLimit(undoContext, undoLevel);
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#redoable()
	 */
	public boolean redoable() {
		return history.canRedo(undoContext);
	}

	/*
	 * @see org.eclipse.jface.text.IUndoManager#undoable()
	 */
	public boolean undoable() {
		return history.canUndo(undoContext);
	}

	public void undo() {

		if (undoable()) {
			try {
				history.undo(undoContext, null, null);
			} catch (ExecutionException ex) {
			}
		}
	}

	public void redo() {

		if (redoable()) {
			try {
				history.redo(undoContext, null, null);
			} catch (ExecutionException ex) {
			}
		}

	}

	public IUndoContext getUndoContext() {
		return undoContext;
	}

}
