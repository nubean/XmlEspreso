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

package com.nubean.xmlespresso;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nubean.michbase.Catalogs;
import com.nubean.michbase.CommonUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class XMLEspressoActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.nubean.xmlespresso";

	public static final String EDITOR_ID = "com.nubean.xmlespresso.xmleditor";

	// The shared instance
	private static XMLEspressoActivator plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private Catalogs catalogs;

	public final static String DOC_DESCRIPTOR_PROPERTY = "com.nubean.xmlespresso.doc.descriptor.property";

	public final static String EDITOR_PROPERTY = "com.nubean.xmlespresso.editor.property";

	public final static String CONFIG_PREF = "com.nubean.xmlespresso.config.pref";

	public static final String QUALIFIER = "";

	private final static String VERSION = "7.0.0";

	private Hashtable<RGB, Color> colors;

	/**
	 * The constructor
	 */
	public XMLEspressoActivator() {
		colors = new Hashtable<RGB, Color>();

		resourceBundle = ResourceBundle
				.getBundle("com.nubean.xmlespresso.XMLEspressoPluginResources");
		this.readCatalogs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

		Enumeration<Color> en = colors.elements();
		while (en.hasMoreElements()) {
			Color color = (Color) en.nextElement();
			color.dispose();
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static XMLEspressoActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = XMLEspressoActivator.getDefault()
				.getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	private void readCatalogs() {
		try {
			catalogs = new Catalogs();
			File wp = new File(System.getProperty("user.home") + File.separator
					+ ".xmle4j" + File.separator + "config");
			if (!wp.exists())
				wp.mkdirs();

			File cf = new File(wp, "catalogs" + VERSION + ".xml");
			if (!cf.exists()) {
				java.net.URL url = getClass().getClassLoader().getResource(
						"config/catalogs.xml");
				InputStream input = url.openConnection().getInputStream();
				CommonUtils.copyToFile(input, cf);
			}

			catalogs.readDocument(new FileInputStream(cf));
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error(
					"XMLEspresso read catalogs error:", e);
		}
	}

	public static Catalogs getCatalogs() {
		return XMLEspressoActivator.getDefault().catalogs;
	}

	public Color getColor(RGB rgb) {
		Color color = (Color) colors.get(rgb);
		if (color == null) {
			Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
			color = new Color(shell.getDisplay(), rgb);
			colors.put(rgb, color);
		}
		return color;
	}

	public Display getDisplay() {
		try {
			Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
			return shell.getDisplay();
		} catch (Exception e) {
			XMLEspressoActivator.getDefault().error(
					"XMLEspresso get display error:", e);
		}
		return null;
	}

	public void error(String message, Exception e) {
		IStatus status = new Status(IStatus.ERROR, getDefault().getBundle()
				.getSymbolicName(), IStatus.ERROR, message, e);
		getLog().log(status);
	}

	public void warning(String message, Exception e) {
		IStatus status = new Status(IStatus.WARNING, getDefault().getBundle()
				.getSymbolicName(), IStatus.WARNING, message, e);
		getLog().log(status);
	}

	public void info(String message, Exception e) {
		IStatus status = new Status(IStatus.INFO, getDefault().getBundle()
				.getSymbolicName(), IStatus.INFO, message, e);
		getLog().log(status);
	}

	private MessageConsole getConsole() {
		String name = PLUGIN_ID;
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public void println(String msg) {
		MessageConsole myConsole = getConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(msg);
	}

	public static IEditorPart openEditor(IFile file) {
		IEditorPart editorPart = null;
		IWorkbenchPage page = getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		try {
			editorPart = page.openEditor(new FileEditorInput(file), EDITOR_ID);
		} catch (PartInitException e) {
			XMLEspressoActivator.getDefault().error(
					"XMLEspresso open editor error:", e);
		}

		return editorPart;

	}
}
