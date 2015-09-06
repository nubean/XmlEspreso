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

package com.nubean.michbase.editor;

import java.beans.PropertyChangeListener;

import javax.swing.event.UndoableEditListener;

import com.nubean.michbase.Editor;
import com.nubean.michutil.Region;

public interface IDEditor extends Editor {
	public final String PROP_DIRTY = "prop_dirty";

	public final String PROP_SAVE_STATUS = "prop_save_status";

	public final String PROP_EDITOR_VIEW = "prop_editor_view";

	public final String PROP_OUTLINE_VIEW = "prop_outline_view";

	public final String PROP_MESSAGE_VIEW = "prop_message_view";

	public final String PROP_STATUS = "prop_status";

	public final String PROP_REMOVE_MESSAGE_TITLE = "prop_remove_message_title";

	public final String PROP_MESSAGE = "prop_message";

	public final String PROP_ERROR = "prop_error";

	public final String PROP_TEXT_MODE = "prop_text_mode";

	public final String PROP_LINE_COL = "prop_line_col";

	public void saveDocument();

	public void backupDocument();

	public void showDocument();

	public boolean openDocument();

	public boolean openDocumentAsText();

	public boolean newDocument();

	public int closeDocument();

	public void saveDocumentAs();

	public void renameDocument();

	public void cut();

	public void copy();

	public void paste();

	public int find(String text, boolean matchCase, boolean wrap,
			boolean forward, boolean regex, boolean allscope,
			boolean incremental);

	public void replace(String text);

	public void clear();

	public void setRefresh(boolean refresh);

	public boolean getRefresh();

	public boolean isOpen();

	public Object getEditorView();

	public Object getMessagesView();

	public Object getOutlineView();

	public Object getSourceView();

	public Object getDesignView();

	public void print();

	public void showLineNumbers(boolean show);

	public boolean isEditable();

	public void setSelection(int offset, int length);

	public void setScope(Region region);

	public Region getScope();

	public Region getLineSelection();

	public Region getSelection();

	public String getSelectionText();

	public void beginFindReplaceSession();

	public void endFindReplaceSession();

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public void addUndoableEditListener(UndoableEditListener listener);

	public void removeUndoableEditListener(UndoableEditListener listener);

}
