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

package com.nubean.michbase.project;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import com.nubean.michbase.EditorConfiguration;
import com.nubean.michbase.factory.EditorConfigurationFactory;

import org.w3c.dom.*;

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

public class ProjectConfiguration implements Cloneable {

	private boolean backup = true;

	private int backupInterval = 5;

	private transient boolean settingsChanged;

	private Hashtable<String, EditorConfiguration> editorConfigurations;

	private String keymap, tabsize;

	public ProjectConfiguration() {
		editorConfigurations = new Hashtable<String, EditorConfiguration>(7, 0.85f);
		this.keymap = "Default";
		this.tabsize = "1";
	}

	public int getBackupInterval() {
		return backupInterval;
	}

	public Hashtable<String, EditorConfiguration> getEditorConfigurations() {
		return editorConfigurations;
	}

	public void setBackupInterval(int backupInterval) {
		this.backupInterval = backupInterval;
		settingsChanged = true;
	}

	public boolean isBackup() {
		return backup;
	}

	public void setBackup(boolean backup) {
		this.backup = backup;
		settingsChanged = true;
	}

	public String getKeymap() {
		return keymap;
	}

	public void setKeymap(String keymap) {
		this.keymap = keymap;
	}

	public String getTabsize() {
		return tabsize;
	}

	public void setTabsize(String tabsize) {
		this.tabsize = tabsize;
	}
	
	public void setSettingsChanged(boolean settingsChanged) {
		this.settingsChanged = settingsChanged;
		Enumeration enums = this.editorConfigurations.elements();
		while (enums.hasMoreElements()) {
			EditorConfiguration value = (EditorConfiguration) enums
					.nextElement();

			value.setSettingsChanged(settingsChanged);
		}
	}

	public boolean isSettingsChanged() {
		if (settingsChanged)
			return true;

		Enumeration enums = this.editorConfigurations.elements();
		while (enums.hasMoreElements()) {
			EditorConfiguration value = (EditorConfiguration) enums
					.nextElement();

			if (value.isSettingsChanged())
				return true;
		}
		return false;
	}

	public Object clone() {
		ProjectConfiguration copy = new ProjectConfiguration();
		copy.backup = this.backup;
		copy.tabsize = this.tabsize;
		copy.backupInterval = this.backupInterval;
		Enumeration<EditorConfiguration> enums = this.editorConfigurations.elements();

		while (enums.hasMoreElements()) {
			EditorConfiguration value = (EditorConfiguration) enums
					.nextElement();

			copy.editorConfigurations.put(value.getMimeType(), (EditorConfiguration)value.clone());
		}
		return copy;
	}

	public EditorConfiguration getEditorConfiguration(String mimeType) {
		EditorConfiguration ec = (EditorConfiguration) editorConfigurations
				.get(mimeType);
		if (ec == null) {
			ec = EditorConfigurationFactory.newEditorConfiguration(mimeType);
			if (ec != null) {
				this.setEditorConfiguration(mimeType, ec);
			}
		}
		return ec;
	}

	public void setEditorConfiguration(String mimeType,
			EditorConfiguration config) {
		this.editorConfigurations.put(mimeType, config);
	}

	public void readElement(org.w3c.dom.Element element) {
		String value = null;
		value = element.getAttribute("backup");
		if (value.length() > 0)
			backup = Boolean.getBoolean(value);

		keymap = element.getAttribute("keymap");
		if (this.keymap.trim().length() == 0)
			keymap = "Default";
		
		tabsize = element.getAttribute("tabsize");
		if (this.tabsize.trim().length() == 0)
			tabsize = "1";

		value = element.getAttribute("backupInterval");
		try {
			if (value.length() > 0)
				backupInterval = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			backupInterval = 5;
		}

		NodeList nodeList = element.getElementsByTagName("editorConfiguration");
		int count = (nodeList != null ? nodeList.getLength() : 0);
		for (int i = 0; i < count; i++) {
			try {
				Element ele = (Element) nodeList.item(i);
				String classname = ele.getAttribute("class");
				Class klass = Class.forName(classname);
				Class[] params = { org.w3c.dom.Element.class };
				Method rem = klass.getMethod("readElement", params);
				Constructor init = klass.getConstructor(new Class[]{});
				EditorConfiguration ec = (EditorConfiguration) init
						.newInstance(new Object[]{});
				Object[] args = { ele };
				rem.invoke(ec, args);
				editorConfigurations.put(ec.getMimeType(), ec);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void printXml(PrintWriter pw, String indent) {
		String save = indent;
		pw.print(indent);
		pw.println("<projectConfiguration");

		indent += "\t";

		pw.print(indent);
		pw.print("keymap='");
		pw.print(keymap);
		pw.println("'");

		pw.print(indent);
		pw.print("tabsize='");
		pw.print(tabsize);
		pw.println("'");
		
		pw.print(indent);
		pw.print("backup='");
		pw.print((new Boolean(backup)).toString());
		pw.println("'");

		pw.print(indent);
		pw.print("backupInterval='");
		pw.print(Integer.toString(backupInterval));
		pw.println("'");

		pw.print(save);
		pw.println(">");

		Enumeration enums = editorConfigurations.elements();
		while (enums.hasMoreElements()) {
			EditorConfiguration config = (EditorConfiguration) enums
					.nextElement();
			config.printXml(pw, indent);
		}

		pw.print(save);
		pw.println("</projectConfiguration>");
	}
}