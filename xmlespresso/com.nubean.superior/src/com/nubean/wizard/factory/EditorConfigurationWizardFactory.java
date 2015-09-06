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

package com.nubean.wizard.factory;

import java.awt.Component;

import com.nubean.michbase.DefaultEditorConfiguration;
import com.nubean.michbase.EditorConfiguration;
import com.nubean.michbase.wizard.DefaultEditorConfigurationWizard;
import com.nubean.michxml.XMLEditorConfiguration;
import com.nubean.michxml.wizard.XMLEditorConfigurationWizard;

public class EditorConfigurationWizardFactory {

	public static Component newEditorConfiguration(
			EditorConfiguration editorConfig) {
		if (editorConfig instanceof XMLEditorConfiguration)
			return new XMLEditorConfigurationWizard(
					(XMLEditorConfiguration) editorConfig);
		else if (editorConfig instanceof DefaultEditorConfiguration)
			return new DefaultEditorConfigurationWizard(
					(DefaultEditorConfiguration) editorConfig);
		else
			return null;
	}
}
