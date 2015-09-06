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

import javax.swing.ImageIcon;

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

public class IconLoader {

	public static ImageIcon newIcon, projectIcon, xmlIcon, backIcon, homeIcon,
			printIcon;

	public static ImageIcon openProjectIcon, openIcon, saveIcon, saveAsIcon,
			saveAllIcon;

	public static ImageIcon closeProjectIcon, addIcon, removeIcon, closeIcon;

	public static ImageIcon undoIcon, redoIcon, copyIcon, cutIcon, pasteIcon;

	public static ImageIcon findIcon, findAgainIcon, replaceIcon, ruleIcon;

	public static ImageIcon elementIcon, attrIcon, typeIcon, complexTypeIcon;

	public static ImageIcon simpleTypeIcon, textIcon, contentIcon, commentIcon;

	public static ImageIcon dtdfIcon, dtdIcon, adeclIcon, edeclIcon, entityIcon,
			notationIcon, cssIcon, importIcon, leafIcon;

	public static ImageIcon validateSchemaIcon, validateDtdIcon,
			propertiesIcon, helpIcon, errorIcon, warningIcon, xiconIcon;

	public static ImageIcon bannerIcon, beanIcon, procIcon, documentIcon, cssfIcon;

	static {
		ClassLoader classLoader = IconLoader.class.getClassLoader();
		java.net.URL url = null;

		url = classLoader.getResource("images/bean.gif");
		beanIcon = new ImageIcon(url);

		url = classLoader.getResource("images/banner.jpg");
		bannerIcon = new ImageIcon(url);

		url = classLoader.getResource("images/back.gif");
		backIcon = new ImageIcon(url);

		url = classLoader.getResource("images/home.gif");
		homeIcon = new ImageIcon(url);

		url = classLoader.getResource("images/project.gif");
		projectIcon = new ImageIcon(url);

		url = classLoader.getResource("images/add.gif");
		addIcon = new ImageIcon(url);

		url = classLoader.getResource("images/open.gif");
		openIcon = new ImageIcon(url);

		url = classLoader.getResource("images/remove.gif");
		removeIcon = new ImageIcon(url);

		url = classLoader.getResource("images/close.gif");
		closeIcon = new ImageIcon(url);

		url = classLoader.getResource("images/xml.gif");
		xmlIcon = new ImageIcon(url);

		url = classLoader.getResource("images/new.gif");
		newIcon = new ImageIcon(url);

		url = classLoader.getResource("images/find.gif");
		findIcon = new ImageIcon(url);

		url = classLoader.getResource("images/findAgain.gif");
		findAgainIcon = new ImageIcon(url);

		url = classLoader.getResource("images/replace.gif");
		replaceIcon = new ImageIcon(url);

		url = classLoader.getResource("images/undo.gif");
		undoIcon = new ImageIcon(url);

		url = classLoader.getResource("images/redo.gif");
		redoIcon = new ImageIcon(url);

		url = classLoader.getResource("images/copy.gif");
		copyIcon = new ImageIcon(url);

		url = classLoader.getResource("images/cut.gif");
		cutIcon = new ImageIcon(url);

		url = classLoader.getResource("images/closeProject.gif");
		closeProjectIcon = new ImageIcon(url);

		url = classLoader.getResource("images/paste.gif");
		pasteIcon = new ImageIcon(url);

		url = classLoader.getResource("images/openProject.gif");
		openProjectIcon = new ImageIcon(url);
		url = classLoader.getResource("images/save.gif");
		saveIcon = new ImageIcon(url);

		url = classLoader.getResource("images/saveAll.gif");
		saveAllIcon = new ImageIcon(url);

		url = classLoader.getResource("images/saveAs.gif");
		saveAsIcon = new ImageIcon(url);

		url = classLoader.getResource("images/tag.gif");
		elementIcon = new ImageIcon(url);

		url = classLoader.getResource("images/attr.gif");
		attrIcon = new ImageIcon(url);

		url = classLoader.getResource("images/schema.gif");
		typeIcon = new ImageIcon(url);

		url = classLoader.getResource("images/simple.gif");
		simpleTypeIcon = new ImageIcon(url);

		url = classLoader.getResource("images/complex.gif");
		complexTypeIcon = new ImageIcon(url);

		url = classLoader.getResource("images/comment.gif");
		commentIcon = new ImageIcon(url);

		url = classLoader.getResource("images/text.gif");
		textIcon = new ImageIcon(url);

		url = classLoader.getResource("images/content.gif");
		contentIcon = new ImageIcon(url);

		url = classLoader.getResource("images/entity.gif");
		entityIcon = new ImageIcon(url);

		url = classLoader.getResource("images/notation.gif");
		notationIcon = new ImageIcon(url);

		url = classLoader.getResource("images/dtd.gif");
		dtdIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/dtdf.gif");
		dtdfIcon = new ImageIcon(url);

		url = classLoader.getResource("images/edecl.gif");
		edeclIcon = new ImageIcon(url);

		url = classLoader.getResource("images/adecl.gif");
		adeclIcon = new ImageIcon(url);

		url = classLoader.getResource("images/validateSchema.gif");
		validateSchemaIcon = new ImageIcon(url);

		url = classLoader.getResource("images/validateDtd.gif");
		validateDtdIcon = new ImageIcon(url);

		url = classLoader.getResource("images/properties.gif");
		propertiesIcon = new ImageIcon(url);

		url = classLoader.getResource("images/help.gif");
		helpIcon = new ImageIcon(url);

		url = classLoader.getResource("images/print.gif");
		printIcon = new ImageIcon(url);

		url = classLoader.getResource("images/proc.gif");
		procIcon = new ImageIcon(url);

		url = classLoader.getResource("images/document.gif");
		documentIcon = new ImageIcon(url);

		url = classLoader.getResource("images/error.gif");
		errorIcon = new ImageIcon(url);

		url = classLoader.getResource("images/warning.gif");
		warningIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/xicon.gif");
		xiconIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/css.gif");
		cssIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/import.gif");
		importIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/leaf.gif");
		leafIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/cssf.gif");
		cssfIcon = new ImageIcon(url);
		
		url = classLoader.getResource("images/rule.gif");
		ruleIcon = new ImageIcon(url);
		
	}

	public static ImageIcon getIcon(String resource) {
		ClassLoader classLoader = IconLoader.class.getClassLoader();
		java.net.URL url = null;
		url = classLoader.getResource(resource);
		ImageIcon icon = new ImageIcon(url);
		return icon;
	}

}