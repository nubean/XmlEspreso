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

package com.nubean.michide;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import com.nubean.michbase.Catalog;
import com.nubean.michbase.CatalogEntry;
import com.nubean.michbase.Catalogs;
import com.nubean.michutil.IconLoader;
import com.nubean.michutil.LocalizedResources;

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

public class CatalogsWizard extends JPanel {
	private static final long serialVersionUID = 3581093089989016966L;

	private Catalogs catalogs;

	private CatalogEntry catalogEntry;

	JButton okButton, cancelButton;

	public CatalogEntry getCatalogEntry() {
		return catalogEntry;
	}

	public CatalogsWizard(Catalogs catalogs) {
		try {
			this.catalogs = catalogs;
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {
		setLayout(new BorderLayout());

		JTabbedPane stepPane = new JTabbedPane();
		JPanel buttonPanel = new JPanel();
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.add(buttonPanel, BorderLayout.CENTER);
		Box box = Box.createHorizontalBox();
		box.add(new JLabel(LocalizedResources.applicationResources
				.getString("ready")));
		box.add(Box.createHorizontalGlue());

		lowerPanel.add(box, BorderLayout.SOUTH);

		okButton = new JButton(LocalizedResources.applicationResources
				.getString("ok"));
		buttonPanel.add(okButton);

		cancelButton = new JButton(LocalizedResources.applicationResources
				.getString("cancel"));
		buttonPanel.add(cancelButton);

		buttonPanel.add(buildHelpButton());
		Vector<Catalog> catalogv = catalogs.getCatalogs();
		Collections.sort(catalogv);
		Iterator<Catalog> it = catalogv.iterator();
		while (it.hasNext()) {
			Catalog catalog = it.next();
			String title = catalog.getTitle();

			stepPane.add(title, makeCatalogView(catalog));
		}
		add(stepPane, BorderLayout.CENTER);
		add(lowerPanel, BorderLayout.SOUTH);
		highlightCustom(stepPane);
	}

	private void highlightCustom(JTabbedPane stepPane) {
		int count = stepPane.getTabCount();

		String custom = LocalizedResources.applicationResources
				.getString("custom");
		for (int i = 0; i < count; i++) {
			String title = stepPane.getTitleAt(i);

			if (title.equalsIgnoreCase(custom)) {
				stepPane.setSelectedIndex(i);
				stepPane.setForegroundAt(i, Color.BLUE.brighter());
				break;
			}
		}
	}

	private JSplitPane makeCatalogView(Catalog catalog) {
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(150);

		Vector<CatalogEntry> entries = catalog.getEntries();
		Collections.sort(entries);
		JList list = new JList(entries);
		final HtmlPanel desc = new HtmlPanel(null, IconLoader.backIcon,
				IconLoader.homeIcon);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList list = (JList) e.getSource();
				catalogEntry = (CatalogEntry) list.getSelectedValue();
				String docs = catalogEntry.getProperty("documentation");
				if (docs != null) {
					desc.setPage(docs.trim());
				}
			}
		});
		JScrollPane sp = new JScrollPane(list);
		sp.setBorder(new TitledBorder("Select an entry:"));

		splitPane.setLeftComponent(sp);
		splitPane.setRightComponent(desc);
		return splitPane;
	}

	private JButton buildHelpButton() {
		JButton helpButton = null;
		HelpSet hs = null;
		try {
			URL hsUrl = HelpSet.findHelpSet(null, "javahelp/XMLEditorHelp.hs");
			hs = new HelpSet(null, hsUrl);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		HelpBroker hb = hs.createHelpBroker();
		helpButton = new JButton("Help");
		CSH.setHelpIDString(helpButton, "CreatingANewDocument");
		helpButton.addActionListener(new CSH.DisplayHelpFromSource(hb));
		return helpButton;
	}

	public static CatalogEntry showDialog(Component component,
			Catalogs catalogs, String title, Point p) {
		final CatalogsWizard pane = new CatalogsWizard(catalogs);
		CatalogsTracker finish = new CatalogsTracker(pane);
		CatalogsCancelTracker cancel = new CatalogsCancelTracker(pane);
		JDialog dialog = createDialog(component, title, true, pane, finish,
				cancel);
		dialog.addWindowListener(new CatalogsDialog.Closer());
		dialog.addComponentListener(new CatalogsDialog.DisposeOnClose());
		dialog.setLocation(p);
		dialog.setVisible(true); // blocks until user brings dialog down...

		return finish.getCatalogEntry();
	}

	public static JDialog createDialog(Component c, String title,
			boolean modal, CatalogsWizard chooserPane,
			ActionListener okListener, ActionListener cancelListener)

	{

		return new CatalogsDialog(c, title, modal, chooserPane, okListener,
				cancelListener);

	}
}

class CatalogsDialog extends JDialog {
	private static final long serialVersionUID = 2813299845273952868L;

	public CatalogsDialog(Component c, String title, boolean modal,
			CatalogsWizard chooserPane, ActionListener okListener,
			ActionListener cancelListener)

	{
		super(JOptionPane.getFrameForComponent(c), title, modal);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(chooserPane, BorderLayout.CENTER);

		// The following few lines are used to register esc to close the dialog
		ActionListener cancelKeyAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((AbstractButton) e.getSource()).doClick();
			}
		};
		KeyStroke cancelKeyStroke = KeyStroke
				.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		chooserPane.cancelButton.registerKeyboardAction(cancelKeyAction,
				cancelKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		chooserPane.cancelButton.addActionListener(cancelListener);
		chooserPane.okButton.addActionListener(okListener);

		pack();
		setSize(new Dimension(650, 400));
	}

	static class Closer extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter {
		public void componentHidden(ComponentEvent e) {
			Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class CatalogsTracker implements ActionListener {
	CatalogsWizard tool;

	CatalogEntry entry;

	public CatalogsTracker(CatalogsWizard c) {
		tool = c;
	}

	private void close() {
		Component parent = tool.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		entry = tool.getCatalogEntry();
		close();
	}

	public CatalogEntry getCatalogEntry() {
		return entry;
	}
}

class CatalogsCancelTracker implements ActionListener {
	CatalogsWizard input;

	public CatalogsCancelTracker(CatalogsWizard c) {
		input = c;
	}

	private void close() {
		Component parent = input.getParent();
		while (!(parent instanceof JDialog) && parent.getParent() != null) {

			parent = parent.getParent();
		}
		parent.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		close();
	}
}