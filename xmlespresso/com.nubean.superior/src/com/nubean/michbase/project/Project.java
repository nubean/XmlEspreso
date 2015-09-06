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

import javax.swing.*;
import java.io.*;
import org.w3c.dom.*;
import javax.swing.tree.*;

import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.EditorConfiguration;
import com.nubean.michbase.XMLBuilder;
import com.nubean.michbase.editor.IDEditor;
import com.nubean.michide.BackupTask;
import java.lang.reflect.*;

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

public class Project implements TreeNode, com.nubean.michutil.Iconable {
	public Project() {
	}

	public Project(boolean init) {
		if (init) {
			children = new Vector(10, 10);
			dirty = true;
			projectConfiguration = new ProjectConfiguration();
		}
	}

	public void backupAll() throws IOException {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			DocumentDescriptor dd = (DocumentDescriptor) getChildAt(i);
			if (dd.getEditor() != null)
				((IDEditor)dd.getEditor()).backupDocument();
		}
		backupProject();
	}

	public void saveAll() throws IOException {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			DocumentDescriptor dd = (DocumentDescriptor) getChildAt(i);
			if (dd.getEditor() != null)
				((IDEditor)dd.getEditor()).saveDocument();
		}
		saveProject();
	}

	public void saveProject() throws IOException {
		File file = new File(this.getProjectPath());

		if (!file.exists())
			file.mkdirs();

		file = new File(file, this.getName());
		FileOutputStream os = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(os, this.getEncoding());
		PrintWriter writer = new PrintWriter(osw);
		this.printXml(writer, "");
		writer.close();

		file = new File(getBackupPath(), getName());
		if (file.exists())
			file.delete();

		dirty = false;
	}

	public void backupProject() throws IOException {
		File file = new File(this.getBackupPath());

		if (!file.exists())
			file.mkdirs();

		file = new File(file, this.getName());
		FileOutputStream os = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(os, this.getEncoding());
		PrintWriter writer = new PrintWriter(osw);
		this.printXml(writer, "");
		writer.close();
	}

	public void configureProject(ProjectConfiguration props) {
		if (props != null) {
			setDirty(true);
			this.projectConfiguration = props;

			if (backupTimer != null)
				backupTimer.cancel();

			if (props.isBackup()) {
				backupTimer = new java.util.Timer(true);
				long delay = props.getBackupInterval() * 60 * 1000;
				backupTimer.scheduleAtFixedRate(new BackupTask(this), delay,
						delay);
			}

			EditorConfiguration xeditorConfig = props
					.getEditorConfiguration("text/xml");
			if (xeditorConfig != null && xeditorConfig.isSettingsChanged()) {
				Properties systemSettings = System.getProperties();

				String proxyHost = xeditorConfig.getProxyHost();
				if (proxyHost != null && proxyHost.trim().length() > 0) {
					systemSettings.put("http.proxyHost", proxyHost);
					systemSettings.put("proxyHost", proxyHost);
				} else {
					systemSettings.remove("http.proxyHost");
					systemSettings.remove("proxyHost");
				}

				String proxyPort = xeditorConfig.getProxyPort();
				if (proxyPort != null && proxyPort.trim().length() > 0) {
					systemSettings.put("http.proxyPort", proxyPort);
					systemSettings.put("proxyPort", proxyPort);
				} else {
					systemSettings.remove("http.proxyPort");
					systemSettings.remove("proxyPort");
				}
			}
		}
	}

	public ImageIcon getIcon() {
		return com.nubean.michutil.IconLoader.projectIcon;
	}

	public Enumeration children() {
		return children.elements();
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return false;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		return null;
	}

	public TreeNode getChildAt(int index) {
		return (TreeNode) children.elementAt(index);
	}

	public int getChildCount() {
		return children.size();
	}

	public String toString() {
		return name;
	}

	public void setDirty(boolean flag) {
		dirty = flag;
	}

	public boolean isDirty() {
		if (dirty)
			return true;

		return isAnyDocumentDirty();
	}

	public boolean isAnyDocumentDirty() {
		for (int i = 0; i < children.size(); i++) {
			DocumentDescriptor child = (DocumentDescriptor) children
					.elementAt(i);
			if (child.isDirty()) {
				return true;
			}
		}

		return false;
	}

	public void addDoc(DocumentDescriptor doc) {
		if (doc == null)
			return;

		dirty = true;
		if (!children.contains(doc)) {
			boolean inserted = false;
			int count = (children != null ? children.size() : 0);

			for (int i = 0; i < count; i++) {
				DocumentDescriptor child = (DocumentDescriptor) children
						.elementAt(i);
				if (doc.getName().compareTo(child.getName()) >= 0)
					continue;
				else {
					children.insertElementAt(doc, i);
					inserted = true;
					break;
				}
			}
			if (!inserted)
				children.add(doc);

		}
		doc.setParent(this);
	}

	public void removeDoc(DocumentDescriptor doc) {
		dirty = true;
		children.remove(doc);
		doc.setParent(null);
	}

	public void addDocument(DocumentDescriptor doc) {
		if (doc == null)
			return;

		dirty = true;
		if (!children.contains(doc)) {
			boolean inserted = false;
			int count = (children != null ? children.size() : 0);

			for (int i = 0; i < count; i++) {
				DocumentDescriptor child = (DocumentDescriptor) children
						.elementAt(i);
				if (doc.getName().compareTo(child.getName()) >= 0)
					continue;
				else {
					children.insertElementAt(doc, i);
					inserted = true;
					break;
				}
			}
			if (!inserted)
				children.add(doc);

		}
		doc.setParent(this);
	}

	public void removeDocument(DocumentDescriptor doc) {
		dirty = true;
		children.remove(doc);
		doc.setParent(null);
	}

	public void readDocument(InputStream input)
			throws javax.xml.parsers.ParserConfigurationException,
			java.io.IOException, org.xml.sax.SAXException {
		org.w3c.dom.Document doc = null;

		synchronized (XMLBuilder.class) {
			XMLBuilder.nonValidatingBuilder.setErrorHandler(null);
			doc = (org.w3c.dom.Document) XMLBuilder.nonValidatingBuilder
					.parse(input);
		}
		Element element = doc.getDocumentElement();

		name = element.getAttribute("name");
		projectPath = element.getAttribute("projectPath");
		workingPath = element.getAttribute("workingPath");
		outputPath = element.getAttribute("outputPath");
		encoding = element.getAttribute("encoding");
		backupPath = element.getAttribute("backupPath");
		author = element.getAttribute("author");
		title = element.getAttribute("title");
		copyright = element.getAttribute("copyright");
		version = element.getAttribute("version");

		Node descNode = CommonUtils.getChildByName(element, "description");
		if (descNode != null)
			description = CommonUtils.getContent(descNode);

		NodeList nodeList = element
				.getElementsByTagName("projectConfiguration");
		int count = (nodeList != null ? nodeList.getLength() : 0);

		for (int i = 0; i < count; i++) {
			projectConfiguration = new ProjectConfiguration();
			Element ele = (Element) nodeList.item(i);
			projectConfiguration.readElement(ele);
		}

		children = new Vector(count);
		nodeList = element.getElementsByTagName("documentDescriptor");
		count = (nodeList != null ? nodeList.getLength() : 0);
		children = new Vector(count);
		for (int i = 0; i < count; i++) {
			try {
				Element ele = (Element) nodeList.item(i);
				String classname = ele.getAttribute("class");
				Class klass = Class.forName(classname);
				Class[] params = { org.w3c.dom.Element.class };
				Method rem = klass.getMethod("readElement", params);
				Constructor init = klass.getConstructor(new Class[]{});
				DocumentDescriptor dd = (DocumentDescriptor) init
						.newInstance(new Object[]{});
				Object[] args = { ele };
				rem.invoke(dd, args);
				dd.setParent(this);
				children.add(dd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void printXml(PrintWriter pw, String indent) {
		pw.println("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		pw.println();
		pw.println("<project");
		indent += "\t";

		if (name != null) {
			pw.print(indent);
			pw.print("name='");
			pw.print(name);
			pw.println("'");
		}

		if (projectPath != null) {
			pw.print(indent);
			pw.print("projectPath='");
			pw.print(projectPath);
			pw.println("'");
		}

		if (workingPath != null) {
			pw.print(indent);
			pw.print("workingPath='");
			pw.print(workingPath);
			pw.println("'");
		}

		if (outputPath != null) {
			pw.print(indent);
			pw.print("outputPath='");
			pw.print(outputPath);
			pw.println("'");
		}
		if (backupPath != null) {
			pw.print(indent);
			pw.print("backupPath='");
			pw.print(backupPath);
			pw.println("'");
		}

		if (encoding != null) {
			pw.print(indent);
			pw.print("encoding='");
			pw.print(encoding);
			pw.println("'");
		}
		if (author != null) {
			pw.print(indent);
			pw.print("author='");
			pw.print(author);
			pw.println("'");
		}

		if (copyright != null) {
			pw.print(indent);
			pw.print("copyright='");
			pw.print(copyright);
			pw.println("'");
		}

		if (copyright != null) {
			pw.print(indent);
			pw.print("version='");
			pw.print(copyright);
			pw.println("'");
		}

		if (title != null) {
			pw.print(indent);
			pw.print("title='");
			pw.print(title);
			pw.print("'");
		}

		pw.println(">");

		if (description != null) {
			pw.print(indent);
			pw.print("<description>");
			pw.print(description);
			pw.println("</description>");
		}

		if (projectConfiguration != null) {
			projectConfiguration.printXml(pw, indent);
		}
		int count = children.size();
		for (int i = 0; i < count; i++) {
			DocumentDescriptor doc = (DocumentDescriptor) children.elementAt(i);
			doc.printXml(pw, indent);
		}

		pw.println("</project>");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getBackupPath() {
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	public String getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(String workingPath) {
		this.workingPath = workingPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public ProjectConfiguration getProjectConfiguration() {
		return projectConfiguration;
	}

	public String name;

	public String title;

	public String copyright;

	public String author;

	public String description;

	public String version;

	public String encoding;

	public String projectPath;

	public String workingPath;

	public String outputPath;

	public String backupPath;

	public ProjectConfiguration projectConfiguration;

	public transient java.util.Timer backupTimer;

	private transient boolean dirty;

	private transient Vector children;
}