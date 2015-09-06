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

import java.io.*;
import java.util.*;
import javax.help.*;
import java.net.URL;
import java.beans.*;

// Basic GUI components
import javax.swing.*;
// GUI support classes
import java.awt.*;
import java.awt.event.*;
import java.text.*;

// For creating a TreeModel

import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import com.nubean.editor.factory.EditorFactory;
import com.nubean.michbase.CatalogEntry;
import com.nubean.michbase.Catalogs;
import com.nubean.michbase.DocumentDescriptor;
import com.nubean.michbase.CommonUtils;
import com.nubean.michbase.design.CloseTabbedPaneUI;
import com.nubean.michbase.design.DefaultTreeCellRenderer;
import com.nubean.michbase.editor.IDEditor;
import com.nubean.michbase.factory.DocumentDescriptorFactory;
import com.nubean.michbase.project.Project;
import com.nubean.michbase.project.ProjectConfigurationWizard;
import com.nubean.michbase.project.ProjectSave;
import com.nubean.michbase.project.ProjectWizard;
import com.nubean.michutil.*;
import com.nubean.wizard.factory.DocumentWizardFactory;

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

public class MichiganIDE extends JFrame implements AbstractIDE {

	private static final long serialVersionUID = 480731984922264749L;

	private final static int windowHeight = 750, fileTabsWidth = 850;

	private final static JTree outline = new JTree(new DefaultMutableTreeNode(
			LocalizedResources.applicationResources
					.getString("outline.not.available")));

	private final static String VERSION = "8.0.0";

	private final static int leftWidth = 150;

	private final static int rightWidth = 1000;

	private MessageFormat formatter;

	private MichiganUndoManager undoManager;

	private Hashtable<String, Object> controls;

	private Project project;

	private DocumentDescriptor document;

	private JMenuBar menubar;

	private JToolBar toolbar;

	private int windowWidth = leftWidth + rightWidth;

	private DocumentPopup documentPopup;

	private JLabel status, status2, status3;

	private JProgressBar progress;

	private JScrollPane treePane, projectPane;

	private JTabbedPane fileTabs, workTabs;

	private JTree projectTree;

	private Hashtable<Object, DocumentDescriptor> documents;

	private TreeCellRenderer treeCellRenderer;

	private File projectsDir, projectsBakDir;

	private Catalogs catalogs;

	private UndoableEditHandler undoHandler;

	private PropertyChangeHandler propChangeHandler;

	private File lastFileOpened;

	private FindAndReplaceDialog findAndReplacDialog;

	public MichiganIDE() {
		// Set up a GUI framework
		super("NuBean XMLEspresso " + VERSION);
		setIconImage(IconLoader.beanIcon.getImage());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		documents = new Hashtable<Object, DocumentDescriptor>(17, 0.85f);
		controls = new Hashtable<String, Object>(51, 0.85f);
		undoHandler = new UndoableEditHandler();
		propChangeHandler = new PropertyChangeHandler();
		init();
		setLocationRelativeTo(null);
	}

	public boolean getFeature(String feature) {
		Object obj = controls.get(feature);
		if (obj != null) {
			try {
				Class<? extends Object> klass = obj.getClass();
				java.lang.reflect.Method method = klass.getMethod("isEnabled",
						new Class[] {});
				Object retval = method.invoke(obj, new Object[] {});
				return ((Boolean) retval).booleanValue();
			} catch (Exception e) {

			}
		}
		return false;
	}

	public void setFeature(String feature, boolean enable) {
		Object obj = controls.get(feature);
		if (obj != null) {
			try {
				Class<? extends Object> klass = obj.getClass();
				Class<?>[] params = { boolean.class };
				java.lang.reflect.Method method = klass.getMethod("setEnabled",
						params);
				Object[] args = { new Boolean(enable) };
				method.invoke(obj, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Object getProperty(String property) {
		return controls.get(property);
	}

	public void setProperty(String property, Object value) {
		controls.put(property, value);
	}

	private JMenu buildHelpMenu() {
		HelpSet hs = null;
		JMenu menu = null;
		try {
			URL hsUrl = HelpSet.findHelpSet(null, "javahelp/XMLEditorHelp.hs");
			hs = new HelpSet(null, hsUrl);

			HelpBroker hb = hs.createHelpBroker();
			menu = new JMenu("Help", false);
			controls.put("helpMenu", menu);
			menu.setMnemonic('h');
			JMenuItem mi = null;
			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("help.sets"),
					IconLoader.helpIcon);
			mi.addActionListener(new CSH.DisplayHelpFromSource(hb));
			mi.setEnabled(true);
			menu.add(mi);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return menu;
	}

	private void readCatalogs() throws Exception {
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

		catalogs = new Catalogs();
		catalogs.readDocument(new FileInputStream(cf));
	}

	private void clearStatus() {
		status.setText("");
		status.repaint();
		status2.setText("");
		status2.repaint();
		status3.setText("");
		status3.repaint();
	}

	private void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = windowWidth + 10;
		int h = windowHeight + 20;

		try {
			readCatalogs();
		} catch (Exception us) {
			us.printStackTrace();
		}
		UIPreferences.initUIDefaults();
		undoManager = new MichiganUndoManager();

		treeCellRenderer = new DefaultTreeCellRenderer();
		documentPopup = new DocumentPopup(
				LocalizedResources.applicationResources.getString("document"));
		getContentPane().setLayout(new BorderLayout());

		findAndReplacDialog = FindAndReplaceDialog.createDialog(this,
				LocalizedResources.applicationResources
						.getString("findReplace.dialog"), false);

		menubar = buildMenuBar();
		controls.put("menuBar", menubar);
		this.setJMenuBar(menubar);

		toolbar = buildToolbar();
		controls.put("toolBar", toolbar);

		fileTabs = new JTabbedPane(JTabbedPane.TOP);
		fileTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		CloseTabbedPaneUI ui = new CloseTabbedPaneUI();
		ui.addCloseAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDocument();
			}
		});

		fileTabs.setUI(ui);
		fileTabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Component sel = fileTabs.getSelectedComponent();

				if (sel == null) {
					treePane.setViewportView(outline);
					return;
				}

				DocumentDescriptor dd = documents.get(sel);
				if (dd == null) {
					treePane.setViewportView(outline);
					return;
				}

				clearStatus();

				document = dd;
				IDEditor cured = (IDEditor) document.getEditor();
				if (cured != null) {
					treePane.setViewportView(outline);
					cured.showDocument();
					int index = fileTabs.getSelectedIndex();
					if (workTabs != null && index < workTabs.getTabCount()) {
						workTabs.setSelectedIndex(index);
					}
				}
				findAndReplacDialog.setEditor(cured);

				if (projectTree != null) {
					Object[] objs = { project, dd };
					TreePath path = new TreePath(objs);
					projectTree.setSelectionPath(path);
				}
			}
		});
		controls.put("fileTabs", fileTabs);

		projectPane = new JScrollPane(new JPanel());
		projectPane.setBorder(new javax.swing.border.TitledBorder(
				LocalizedResources.applicationResources.getString("project")));

		treePane = new JScrollPane(new JPanel());

		treePane.setBorder(new javax.swing.border.TitledBorder(
				LocalizedResources.applicationResources
						.getString("document.tree")));
		controls.put("treePane", treePane);

		JSplitPane hsp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTabs,
				treePane);

		hsp2.setContinuousLayout(true);
		hsp2.setDividerLocation(fileTabsWidth);
		JSplitPane hsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				projectPane, hsp2);

		hsp.setContinuousLayout(true);
		hsp.setDividerLocation(leftWidth);

		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().setBackground(Color.white);

		workTabs = new JTabbedPane();
		workTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		controls.put("workTabs", workTabs);

		workTabs.setTabPlacement(JTabbedPane.BOTTOM);
		JSplitPane msp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, hsp,
				workTabs);
		msp.setContinuousLayout(true);
		msp.setDividerLocation(600);

		getContentPane().add(msp, BorderLayout.CENTER);
		Box statusBar = Box.createHorizontalBox();
		status = new JLabel(
				LocalizedResources.applicationResources.getString("ready"));
		status2 = new JLabel();
		status3 = new JLabel();
		progress = new JProgressBar(0, 100);

		controls.put("status", status);
		controls.put("progress", progress);
		statusBar.add(status);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(status2);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(status3);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(progress);

		controls.put("frame", this);

		getContentPane().add(statusBar, BorderLayout.SOUTH);
		pack();

		setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
		setSize(w, h);

		setVisible(true);
	} // init

	public void updateSaveStatus() {
		if (document != null) {
			document.setDoNotSave(false);
			boolean dirty = document.isDirty();

			setFeature("saveDocumentMenuItem", dirty);
			setFeature("saveDocumentAsMenuItem", true);

			setFeature("saveButton", dirty);
			setFeature("saveAsButton", true);
			document.setDirty(dirty);

		} else {
			setFeature("saveDocumentMenuItem", false);
			setFeature("saveDocumentAsMenuItem", false);
			setFeature("saveButton", false);
			setFeature("saveAsButton", false);
		}
		if (project != null) {
			boolean dirty = project.isDirty();

			setFeature("saveAllMenuItem", dirty);
			setFeature("saveDocumentsMenuItem", dirty);
			setFeature("saveProjectMenuItem", dirty);
			setFeature("saveAllButton", dirty);
		} else {
			setFeature("saveAllMenuItem", false);
			setFeature("saveDocumentsMenuItem", false);
			setFeature("saveProjectMenuItem", false);
			setFeature("saveAllButton", false);
		}

	}

	private int closeProject() {
		try {
			if (project != null) {

				int option = closeAllDocuments(project);
				if (option == JOptionPane.CANCEL_OPTION)
					return option;

				if (project.isDirty()) {
					option = JOptionPane.showConfirmDialog(
							this,
							project.name
									+ LocalizedResources.applicationResources
											.getString("project.save"),
							LocalizedResources.applicationResources
									.getString("save.project"),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, IconLoader.saveIcon);
					if (option == JOptionPane.YES_OPTION) {
						project.saveProject();
					} else if (option == JOptionPane.CANCEL_OPTION) {
						return JOptionPane.CANCEL_OPTION;
					}
				}

				if (project.backupTimer != null)
					project.backupTimer.cancel();
				File file = new File(projectsBakDir, project.name);
				if (file.exists())
					file.delete();

				fileTabs.removeAll();
				projectPane.setViewportView(null);
				treePane.setViewportView(outline);

				setFeature("closeProjectMenuItem", false);
				setFeature("closeProjectButton", false);
				setFeature("addDocumentMenuItem", false);
				setFeature("newDocumentMenuItem", false);
				setFeature("addButton", false);
				setFeature("removeButton", false);
				setFeature("removeDocumentMenuItem", false);
				setFeature("closeDocumentMenuItem", false);
				setFeature("projectPropertiesMenuItem", false);
				setFeature("projectPropertiesButton", false);
				setFeature("closeButton", false);
				updateSaveStatus();
				project = null;
			}
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			status.setText(LocalizedResources.applicationResources
					.getString("project.close.failed"));
		}
		return JOptionPane.YES_OPTION;
	}

	private void showDocumentPopup(MouseEvent e) {
		if (e.isPopupTrigger()
				|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {
			TreePath selPath = projectTree.getPathForLocation(e.getX(),
					e.getY());

			if (selPath == null)
				return;

			projectTree.setSelectionPath(selPath);
			Object obj = selPath.getLastPathComponent();
			if (obj != null && obj instanceof DocumentDescriptor) {
				document = (DocumentDescriptor) obj;

				IDEditor ie = (IDEditor) document.getEditor();
				if (ie == null) {
					document.setEditor(EditorFactory.createNewEditor(project,
							document, true));
					ie = (IDEditor) document.getEditor();
					ie.addUndoableEditListener(undoHandler);
					ie.addPropertyChangeListener(propChangeHandler);
				}

				if (ie.isOpen()) {
					documentPopup.open.setEnabled(false);
					documentPopup.close.setEnabled(true);
					ie.showDocument();
				} else {
					documentPopup.open.setEnabled(true);
					documentPopup.close.setEnabled(false);
				}
				if (ie.isDirty()) {
					documentPopup.save.setEnabled(true);
				} else {
					documentPopup.save.setEnabled(false);
				}
				documentPopup.show(projectTree, e.getX(), e.getY());
			}
		}
	}

	private void openProject(Project proj) {

		if (proj != null) {
			ProjectTreeModel model = new ProjectTreeModel(proj);
			projectTree = new JTree(model) {
				private static final long serialVersionUID = -6623410348834159855L;
				private StringBuffer sb = new StringBuffer(256);

				public String getToolTipText(MouseEvent e) {
					TreePath selPath = projectTree.getPathForLocation(e.getX(),
							e.getY());
					String ret = null;
					if (selPath != null) {
						Object obj = selPath.getLastPathComponent();

						if (obj != null) {
							if (obj instanceof DocumentDescriptor) {
								DocumentDescriptor d = (DocumentDescriptor) obj;
								sb.append(d.getPath());
								sb.append(File.separatorChar);
								sb.append(d.getName());
								ret = sb.toString();
								sb.setLength(0);
							} else if (obj instanceof Project) {
								Project p = (Project) obj;
								sb.append(p.getProjectPath());
								sb.append(File.separatorChar);
								sb.append(p.getName());
								ret = sb.toString();
								sb.setLength(0);
							}
						}
					}
					return ret;
				}
			};

			ToolTipManager.sharedInstance().registerComponent(projectTree);

			projectTree.setCellRenderer(treeCellRenderer);
			MouseListener ml = new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()
							|| ((e.getModifiers() & MouseEvent.BUTTON3_MASK) > 0)) {
						showDocumentPopup(e);
						return;
					}
					TreePath selPath = projectTree.getPathForLocation(e.getX(),
							e.getY());
					if (selPath == null)
						return;

					Object obj = selPath.getLastPathComponent();
					if (!(obj instanceof DocumentDescriptor)) {
						setFeature("removeDocumentMenuItem", false);
						setFeature("removeButton", false);

						setFeature("closeDocumentMenuItem", false);
						setFeature("closeButton", false);
						return;
					}
					DocumentDescriptor info = (DocumentDescriptor) obj;
					if (e.getClickCount() == 2) {
						MichiganIDE.this.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						if (info.getEditor() == null)
							info.setEditor(EditorFactory.createNewEditor(
									project, info, true));
						document = info;
						IDEditor ie = (IDEditor) document.getEditor();
						ie.addUndoableEditListener(undoHandler);
						ie.addPropertyChangeListener(propChangeHandler);
						File docFile = new File(document.getPath(),
								document.getName());
						if (docFile.exists() && docFile.length() > 0)
							ie.openDocument();
						else
							ie.newDocument();

						MichiganIDE.this.setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			};
			projectTree.addMouseListener(ml);
			setFeature("removeButton", true);
			setFeature("removeDocumentMenuItem", true);

			projectPane.setViewportView(projectTree);
			project = proj;

			setFeature("closeProjectMenuItem", true);
			setFeature("closeProjectButton", true);
			setFeature("addDocumentMenuItem", true);
			setFeature("newDocumentMenuItem", true);
			setFeature("addButton", true);
			setFeature("projectPropertiesMenuItem", true);
			setFeature("projectPropertiesButton", true);

			if (proj.getProjectConfiguration().isBackup()) {
				proj.backupTimer = new java.util.Timer(true);
				long delay = proj.getProjectConfiguration().getBackupInterval() * 60 * 1000;
				proj.backupTimer.scheduleAtFixedRate(new BackupTask(proj),
						delay, delay);
			}

			File file = new File(proj.getOutputPath());
			if (!file.exists())
				file.mkdirs();

			file = new File(proj.getBackupPath());
			if (!file.exists())
				file.mkdirs();
		}
	}

	private boolean closeOpenProject() {
		if (project != null) {
			int option = JOptionPane.showConfirmDialog(this,
					LocalizedResources.applicationResources
							.getString("close.project?"),
					LocalizedResources.applicationResources
							.getString("close.project"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, IconLoader.closeProjectIcon);
			if (option != JOptionPane.YES_OPTION)
				return false;
			else
				closeProject();
		}
		return true;
	}

	private void createNewProject() {
		if (!closeOpenProject())
			return;
		Project info = ProjectWizard.showDialog(this,
				LocalizedResources.applicationResources
						.getString("project.wizard"), getPopupLocation());

		if (info == null)
			return;

		info.setName(info.getName() + ".xml");

		File file = new File(info.projectPath);

		file = new File(file, info.getName());

		if (file.exists()) {
			int option = JOptionPane.showConfirmDialog(
					this,
					info.getName()
							+ LocalizedResources.applicationResources
									.getString("project.replace"),
					LocalizedResources.applicationResources
							.getString("replace.project"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, IconLoader.replaceIcon);
			if (option != JOptionPane.YES_OPTION)
				return;
		}
		if (info != null) {
			openProject(info);
		}
		setFeature("saveProjectMenuItem", true);
		setFeature("closeProjectMenuItem", true);
	}

	private void createNewDocument() {
		if (project == null) {
			status.setText(LocalizedResources.applicationResources
					.getString("create.open.project"));
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		CatalogEntry catalogEntry = CatalogsWizard.showDialog(this, catalogs,
				LocalizedResources.applicationResources.getString("catalogs"),
				this.getPopupLocation());
		if (catalogEntry == null)
			return;

		DocumentDescriptor info = DocumentWizardFactory.createDocumentWizard(
				this, project, catalogEntry, this.getPopupLocation());

		if (info == null)
			return;

		if (info.getExt() == null || info.getExt().trim().length() == 0) {
			String ext = catalogEntry.getProperty("ext");
			if (ext != null) {
				info.setExt(ext);
			}
		}

		if (info.getName().indexOf(".") == -1)
			info.setName(info.getName() + info.getExt());

		if (project != null && info != null) {
			File docFile = new File(info.getPath(), info.getName());

			if (project != null && docFile.exists()) {
				if (!replaceDocument(info))
					return;
			}

			try {
				if (!docFile.getParentFile().exists())
					docFile.getParentFile().mkdirs();

				docFile.createNewFile();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(
						this,
						LocalizedResources.applicationResources
								.getString("file.create.failed")
								+ docFile.getAbsolutePath() + ":\n" + e,
						LocalizedResources.applicationResources
								.getString("file.create.error"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			project.addDoc(info);
			info.setEditor(EditorFactory.createNewEditor(project, info, true));
			progress.setIndeterminate(true);
			MichiganIDE.this.setCursor(Cursor
					.getPredefinedCursor(Cursor.WAIT_CURSOR));
			IDEditor ie = (IDEditor) info.getEditor();
			ie.addUndoableEditListener(undoHandler);
			ie.addPropertyChangeListener(propChangeHandler);
			boolean success = ie.newDocument();
			MichiganIDE.this.setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			progress.setIndeterminate(false);
			if (!success) {
				project.removeDoc(info);
				status.setText(LocalizedResources.applicationResources
						.getString("new.document.failed"));
				Toolkit.getDefaultToolkit().beep();

				return;
			}
			document = info;

		}

		project.addDoc(info);
		int pos = project.getIndex(info);

		// fire tree model event for project tree
		Object[] path = { project };
		int[] childIndices = { pos };
		Object[] children = { info };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		ProjectTreeModel ptm = (ProjectTreeModel) projectTree.getModel();
		ptm.fireTreeNodesInserted(tme);

	}

	private File getDocumentFile(DocumentDescriptor info) {
		File file = null;
		try {
			file = new File(info.getPath());
			String fileName = null;
			if (info.getName().indexOf(".") == -1)
				fileName = info.getName() + "." + info.getExt();
			else
				fileName = info.getName();
			file = new File(file, fileName);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			file = null;
		}
		return file;
	}

	private int closeDocument() {
		if (document != null)
			return closeDocument(document);
		else
			return JOptionPane.YES_OPTION;
	}

	private int closeDocument(DocumentDescriptor info) {
		int option = JOptionPane.YES_OPTION;
		IDEditor ie = (IDEditor) info.getEditor();
		if (ie == null || !ie.isOpen())
			return option;

		if (info.isDirty() && !info.getDoNotSave()) {
			option = JOptionPane.showConfirmDialog(
					this,
					info.getName()
							+ " "
							+ LocalizedResources.applicationResources
									.getString("document.save"),
					LocalizedResources.applicationResources
							.getString("save.document"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, IconLoader.saveIcon);
			if (option == JOptionPane.YES_OPTION) {
				ie.saveDocument();
			} else if (option == JOptionPane.NO_OPTION) {
				info.setDoNotSave(true);
			}
			if (option == JOptionPane.CANCEL_OPTION) {
				return option;
			}
		}

		documents.remove(ie.getEditorView());
		document = null;
		ie.closeDocument();

		if (fileTabs.getTabCount() == 0) {
			this.setFeature("closeAllDocumentsMenuItem", false);
		} else {
			document = documents.get(fileTabs
					.getSelectedComponent());
		}

		JMenuItem saveDocumentMenuItem = (JMenuItem) this
				.getProperty("saveDocumentMenuItem");
		saveDocumentMenuItem.setText(LocalizedResources.applicationResources
				.getString("save.document"));

		return option;
	}

	private int closeAllDocuments(Project proj) {
		if (proj != null) {
			int option = saveDocuments();
			if (option == JOptionPane.CANCEL_OPTION)
				return option;

		}

		if (documents != null) {
			Enumeration<DocumentDescriptor> enm = documents.elements();
			while (enm.hasMoreElements()) {
				DocumentDescriptor info = enm
						.nextElement();
				int option = closeDocument(info);
				if (option == JOptionPane.CANCEL_OPTION)
					return option;
			}

			this.setFeature("closeDocumentMenuItem", false);
			this.setFeature("closeButton", false);
			this.setFeature("removeDocumentMenuItem", false);
			this.setFeature("removeButton", false);

			if (fileTabs.getTabCount() == 0)
				this.setFeature("closeAllDocumentsMenuItem", false);
		}
		return JOptionPane.YES_OPTION;
	}

	private void openProject(File file) {
		try {
			Project info = new Project();
			File bakFile = new File(projectsBakDir, file.getName());
			if (bakFile.exists()
					&& bakFile.lastModified() > file.lastModified()) {
				int option = JOptionPane
						.showConfirmDialog(this,
								LocalizedResources.applicationResources
										.getString("open.backup"),
								LocalizedResources.applicationResources
										.getString("backup.check"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					info.setDirty(true);
					file = bakFile;
				}
			}

			info.readDocument(new FileInputStream(file));
			openProject(info);
		} catch (Exception e) {
			e.printStackTrace();
			status.setText(LocalizedResources.applicationResources
					.getString("open.project.failed"));
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private void openProject() {
		if (!closeOpenProject())
			return;

		if (projectsDir == null || !projectsDir.exists()) {
			String home = System.getProperty("user.home");
			String projDir = home + File.separator + ".xmle4j" + File.separator
					+ "projects";
			projectsDir = new File(projDir);
			if (!projectsDir.exists())
				projectsDir.mkdir();
		}

		if (projectsBakDir == null || !projectsBakDir.exists()) {
			projectsBakDir = new File(projectsDir.getParentFile(), "bak");
			if (!projectsBakDir.exists())
				projectsBakDir.mkdir();
		}
		JFileChooser fileChooser = new JFileChooser(projectsDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("open.project"));

		int ok = fileChooser.showDialog(this,
				LocalizedResources.applicationResources
						.getString("open.project"));
		if (ok == JFileChooser.APPROVE_OPTION) {
			java.io.File file = fileChooser.getSelectedFile();
			closeProject();
			openProject(file);
		}
	}

	private void showFindAndReplaceDialog() {
		if (document != null) {
			findAndReplacDialog.setFindString(((IDEditor) document.getEditor())
					.getSelectionText());
			findAndReplacDialog.setVisible(true);
		}
	}

	private JMenu buildFileMenu() {
		JMenu menu = new JMenu(
				LocalizedResources.applicationResources.getString("file"),
				false);
		controls.put("fileMenu", menu);
		menu.setMnemonic('f');

		JMenuItem mi = null;

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("new"),
				IconLoader.newIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				KeyEvent.CTRL_MASK));
		controls.put("newMenuItem", mi);
		mi.setMnemonic('n');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNew();
			}
		});
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("new.project"));
		controls.put("newProjectMenuItem", mi);
		mi.setMnemonic('p');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewProject();
			}
		});
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("new.document"));
		controls.put("newDocumentMenuItem", mi);
		mi.setMnemonic('m');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNewDocument();
			}
		});
		menu.add(mi);

		menu.add(new JSeparator());

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("open.project"),
				IconLoader.openProjectIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.ALT_MASK));
		controls.put("openProjectMenuItem", mi);
		mi.setMnemonic('o');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProject();
			}
		});
		menu.add(mi);

		menu.add(new JSeparator());
		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("save.document"),
				IconLoader.saveIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.CTRL_MASK));
		controls.put("saveDocumentMenuItem", mi);
		mi.setMnemonic('s');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).saveDocument();
			}
		});
		setFeature("saveDocumentMenuItem", false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("save.document.as"),
				IconLoader.saveAsIcon);
		controls.put("saveDocumentAsMenuItem", mi);
		mi.setMnemonic('a');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (document != null && document.getEditor() != null)
					((IDEditor) document.getEditor()).saveDocumentAs();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("save.documents"));
		controls.put("saveDocumentsMenuItem", mi);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDocuments();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("save.all"),
				IconLoader.saveAllIcon);
		controls.put("saveAllMenuItem", mi);
		mi.setMnemonic('l');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					project.saveAll();
					updateSaveStatus();
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					status.setText(LocalizedResources.applicationResources
							.getString("save.all.failed"));
				}
			}
		});
		mi.setEnabled(false);
		menu.add(mi);
		menu.add(new JSeparator());

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("save.project..."));
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.ALT_MASK));
		controls.put("saveProjectMenuItem", mi);
		mi.setMnemonic('v');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					project.saveProject();
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					status.setText(LocalizedResources.applicationResources
							.getString("save.project.failed"));
				}
			}
		});
		this.setFeature("saveProjectMenuItem", false);
		menu.add(mi);

		menu.add(new JSeparator());
		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("add.document..."),
				IconLoader.addIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				KeyEvent.CTRL_MASK));
		controls.put("addDocumentMenuItem", mi);
		mi.setMnemonic('a');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDocument();
			}
		});
		setFeature("addDocumentMenuItem", false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("open.document..."),
				IconLoader.openIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.CTRL_MASK));
		controls.put("openDocumentMenuItem", mi);
		mi.setMnemonic('o');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDocument();
			}
		});
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("remove.document..."),
				IconLoader.removeIcon);
		controls.put("removeDocumentMenuItem", mi);
		mi.setMnemonic('r');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removeDocument();
					if (project != null)
						project.saveProject();
					updateSaveStatus();
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					status.setText(LocalizedResources.applicationResources
							.getString("remove.document.failed"));
				}
			}
		});
		mi.setEnabled(false);

		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("close.document..."),
				IconLoader.closeIcon);
		controls.put("closeDocumentMenuItem", mi);
		mi.setMnemonic('c');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDocument();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("close.all.documents"));
		controls.put("closeAllDocumentsMenuItem", mi);
		mi.setMnemonic('u');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAllDocuments(project);
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		menu.add(new JSeparator());
		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("find/replace"),
				IconLoader.findIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				KeyEvent.CTRL_MASK));
		controls.put("findReplaceMenuItem", mi);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFindAndReplaceDialog();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		menu.add(new JSeparator());
		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("close.project"),
				IconLoader.closeProjectIcon);
		controls.put("closeProjectMenuItem", mi);
		mi.setMnemonic('t');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeProject();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		menu.add(new JSeparator());
		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("print..."),
				IconLoader.printIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				KeyEvent.CTRL_MASK));
		controls.put("printMenuItem", mi);
		mi.setMnemonic('p');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MichiganIDE.this.setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (document != null)
					((IDEditor) document.getEditor()).print();
				MichiganIDE.this.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		menu.add(new JSeparator());
		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("exit"));
		controls.put("exitMenuItem", mi);
		mi.setMnemonic('x');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		menu.add(mi);
		return menu;
	}

	private JMenu buildEditMenu() {
		JMenu menu = new JMenu(
				LocalizedResources.applicationResources.getString("edit"),
				false);
		controls.put("editMenu", menu);
		menu.setMnemonic('e');

		JMenuItem mi = null;

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("undo"),
				IconLoader.undoIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				KeyEvent.CTRL_MASK));
		controls.put("undoMenuItem", mi);
		mi.setMnemonic('u');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.undo();
				} catch (CannotUndoException ex) {
					updateUndoRedoStatus();
				}
			}
		});

		menu.add(mi);
		mi.setEnabled(false);

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("redo"),
				IconLoader.redoIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				KeyEvent.CTRL_MASK));
		controls.put("redoMenuItem", mi);
		mi.setMnemonic('r');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.redo();
				} catch (CannotRedoException ex) {
					updateUndoRedoStatus();
				}
			}
		});
		menu.add(mi);
		mi.setEnabled(false);

		menu.add(new JSeparator());

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("copy"),
				IconLoader.copyIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				KeyEvent.CTRL_MASK));
		controls.put("copyMenuItem", mi);
		mi.setMnemonic('c');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).copy();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("cut"),
				IconLoader.cutIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				KeyEvent.CTRL_MASK));
		controls.put("cutMenuItem", mi);
		mi.setMnemonic('u');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).cut();
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		mi = new JMenuItem(
				LocalizedResources.applicationResources.getString("paste"),
				IconLoader.pasteIcon);
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				KeyEvent.CTRL_MASK));
		controls.put("pasteMenuItem", mi);
		mi.setMnemonic('p');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).paste();
			}
		});
		mi.setEnabled(true);
		menu.add(mi);

		return menu;
	}

	private JMenu buildToolsMenu() {
		JMenu menu = new JMenu(
				LocalizedResources.applicationResources.getString("tools"),
				false);
		controls.put("toolsMenu", menu);

		menu.setMnemonic('t');

		JMenuItem mi = null;

		mi = new JMenuItem(
				LocalizedResources.applicationResources
						.getString("edit.project.properties..."),
				IconLoader.propertiesIcon);
		controls.put("projectPropertiesMenuItem", mi);
		mi.setMnemonic('p');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (project != null) {
					ProjectConfigurationWizard
							.showDialog(MichiganIDE.this,
									LocalizedResources.applicationResources
											.getString("project.properties"),
									getPopupLocation(), project);
				}
			}
		});
		mi.setEnabled(false);
		menu.add(mi);

		return menu;
	}

	private JMenuBar buildMenuBar() {
		JMenuBar mb = new JMenuBar();

		mb.add(buildFileMenu());
		mb.add(buildEditMenu());
		mb.add(buildToolsMenu());
		mb.add(buildHelpMenu());

		return mb;
	}

	private void exit() {
		if (closeProject() == JOptionPane.YES_OPTION)
			System.exit(0);
	}

	private void createNew() {
		String[] choices = {
				LocalizedResources.applicationResources.getString("document"),
				LocalizedResources.applicationResources.getString("project") };
		String choice = (String) JOptionPane
				.showInputDialog(this, LocalizedResources.applicationResources
						.getString("select.new.create"),
						LocalizedResources.applicationResources
								.getString("select.new"),
						JOptionPane.QUESTION_MESSAGE, IconLoader.newIcon,
						(Object[]) choices, choices[0]);
		if (choice == null)
			return;
		if (choice.equals(LocalizedResources.applicationResources
				.getString("project")))
			createNewProject();
		else if (choice.equals(LocalizedResources.applicationResources
				.getString("document")))
			createNewDocument();
	}

	private JToolBar buildToolbar() {
		toolbar = new JToolBar();
		ToolButton newButton = null;
		toolbar.add(newButton = new ToolButton(
				LocalizedResources.applicationResources.getString("new"),
				IconLoader.newIcon));
		controls.put("newButton", newButton);
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createNew();
			}
		});

		ToolButton openProjectButton = null;
		toolbar.add(openProjectButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("open.project"), IconLoader.openProjectIcon));
		controls.put("openProjectButton", openProjectButton);

		openProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProject();
			}
		});

		ToolButton closeProjectButton = null;
		toolbar.add(closeProjectButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("close.project"),
				IconLoader.closeProjectIcon));
		controls.put("closeProjectButton", closeProjectButton);
		closeProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeProject();
			}
		});
		closeProjectButton.setEnabled(false);

		ToolButton openButton = null;
		toolbar.add(openButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("open.document"), IconLoader.openIcon));
		controls.put("openButton", openButton);
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDocument();
			}
		});

		ToolButton closeButton = null;
		toolbar.add(closeButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("close.document"), IconLoader.closeIcon));
		controls.put("closeButton", closeButton);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDocument();
			}
		});
		closeButton.setEnabled(false);

		ToolButton addButton = null;
		toolbar.add(addButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("add.document"), IconLoader.addIcon));
		controls.put("addButton", addButton);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDocument();
			}
		});
		addButton.setEnabled(false);

		ToolButton removeButton = null;

		toolbar.add(removeButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("remove.document"), IconLoader.removeIcon));
		controls.put("removeButton", removeButton);
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removeDocument();
					try {
						if (project != null)
							project.saveProject();
						updateSaveStatus();
					} catch (java.io.IOException ex) {
					}
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					status.setText(LocalizedResources.applicationResources
							.getString("remove.document.failed"));
				}
			}
		});
		removeButton.setEnabled(false);

		ToolButton saveButton = null;
		toolbar.add(saveButton = new ToolButton(
				LocalizedResources.applicationResources.getString("save"),
				IconLoader.saveIcon));
		controls.put("saveButton", saveButton);
		saveButton.setEnabled(false);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (document != null && document.getEditor() != null)
					((IDEditor) document.getEditor()).saveDocument();
			}
		});

		ToolButton saveAllButton = null;
		toolbar.add(saveAllButton = new ToolButton(
				LocalizedResources.applicationResources.getString("save.all"),
				IconLoader.saveAllIcon));
		controls.put("saveAllButton", saveAllButton);
		saveAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Enumeration<DocumentDescriptor> enumeration = documents.elements();
					while (enumeration.hasMoreElements()) {
						DocumentDescriptor dd = enumeration
								.nextElement();
						if (dd.getEditor() != null && dd.isDirty())
							((IDEditor) document.getEditor()).saveDocument();
					}
					project.saveProject();
					updateSaveStatus();
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					status.setText(LocalizedResources.applicationResources
							.getString("save.all.failed"));
				}
			}
		});
		saveAllButton.setEnabled(false);

		ToolButton saveAsButton = null;
		toolbar.add(saveAsButton = new ToolButton(
				LocalizedResources.applicationResources.getString("save.as"),
				IconLoader.saveAsIcon));
		controls.put("saveAsButton", saveAsButton);
		saveAsButton.setEnabled(false);
		saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (document != null && document.getEditor() != null) {
					((IDEditor) document.getEditor()).saveDocumentAs();
				}
			}
		});

		ToolButton printButton = null;
		toolbar.add(printButton = new ToolButton(
				LocalizedResources.applicationResources.getString("print"),
				IconLoader.printIcon));
		controls.put("printButton", printButton);
		printButton.setEnabled(false);
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (document != null && document.getEditor() != null) {
					((IDEditor) document.getEditor()).print();
				}
			}
		});

		JSeparator vs = new JSeparator(JSeparator.VERTICAL);
		vs.setAlignmentX(0);
		vs.setMaximumSize(new Dimension(2, 50));
		toolbar.add(vs);

		ToolButton undoButton = null;
		undoButton = new ToolButton(
				LocalizedResources.applicationResources.getString("undo"),
				IconLoader.undoIcon);
		controls.put("undoButton", undoButton);
		toolbar.add(undoButton);
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.undo();
				} catch (CannotUndoException ex) {
					updateUndoRedoStatus();
				}
			}
		});
		undoButton.setEnabled(false);

		ToolButton redoButton = null;
		redoButton = new ToolButton(
				LocalizedResources.applicationResources.getString("redo"),
				IconLoader.redoIcon);
		controls.put("redoButton", redoButton);
		toolbar.add(redoButton);
		redoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.redo();
				} catch (CannotRedoException ex) {
					updateUndoRedoStatus();
				}
			}
		});
		redoButton.setEnabled(false);

		ToolButton copyButton = null;
		copyButton = new ToolButton(
				LocalizedResources.applicationResources.getString("copy"),
				IconLoader.copyIcon);
		controls.put("copyButton", copyButton);
		toolbar.add(copyButton);
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).copy();
			}
		});
		copyButton.setEnabled(false);

		ToolButton pasteButton = null;
		toolbar.add(pasteButton = new ToolButton(
				LocalizedResources.applicationResources.getString("paste"),
				IconLoader.pasteIcon));
		controls.put("pasteButton", pasteButton);
		pasteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).paste();
			}
		});
		pasteButton.setEnabled(true);

		ToolButton cutButton = null;
		toolbar.add(cutButton = new ToolButton(
				LocalizedResources.applicationResources.getString("cut"),
				IconLoader.cutIcon));
		controls.put("cutButton", cutButton);
		cutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((IDEditor) document.getEditor()).cut();
			}
		});
		cutButton.setEnabled(false);

		vs = new JSeparator(JSeparator.VERTICAL);
		vs.setAlignmentX(0);
		vs.setMaximumSize(new Dimension(2, 50));
		toolbar.add(vs);

		ToolButton findButton = null;
		toolbar.add(findButton = new ToolButton(
				LocalizedResources.applicationResources.getString("find"),
				IconLoader.findIcon));
		controls.put("findButton", findButton);
		findButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFindAndReplaceDialog();
			}
		});
		findButton.setEnabled(false);

		ToolButton findAgainButton = null;
		toolbar.add(findAgainButton = new ToolButton(
				LocalizedResources.applicationResources.getString("find.again"),
				IconLoader.findAgainIcon));
		controls.put("findAgainButton", findAgainButton);
		findAgainButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (findAndReplacDialog != null)
					findAndReplacDialog.find();
			}
		});
		findAgainButton.setEnabled(false);

		ToolButton replaceButton = null;
		toolbar.add(replaceButton = new ToolButton(
				LocalizedResources.applicationResources.getString("replace"),
				IconLoader.replaceIcon));
		controls.put("replaceButton", replaceButton);
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (findAndReplacDialog != null)
					findAndReplacDialog.replace();
			}
		});
		replaceButton.setEnabled(false);

		vs = new JSeparator(JSeparator.VERTICAL);
		vs.setAlignmentX(0);
		vs.setMaximumSize(new Dimension(2, 50));
		toolbar.add(vs);

		ToolButton propsButton = null;
		toolbar.add(propsButton = new ToolButton(
				LocalizedResources.applicationResources
						.getString("edit.project.properties"),
				IconLoader.propertiesIcon));
		controls.put("projectPropertiesButton", propsButton);
		propsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (project != null) {
					ProjectConfigurationWizard
							.showDialog(MichiganIDE.this,
									LocalizedResources.applicationResources
											.getString("project.properties"),
									getPopupLocation(), project);
				}
			}
		});
		propsButton.setEnabled(false);

		ToolButton helpButton = null;
		HelpSet hs = null;
		try {
			URL hsUrl = HelpSet.findHelpSet(null, "javahelp/XMLEditorHelp.hs");
			hs = new HelpSet(null, hsUrl);

			HelpBroker hb = hs.createHelpBroker();
			toolbar.add(helpButton = new ToolButton(
					LocalizedResources.applicationResources.getString("help"),
					IconLoader.helpIcon));
			controls.put("helpButton", helpButton);
			helpButton.addActionListener(new CSH.DisplayHelpFromSource(hb));
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return toolbar;
	}

	public Point getPopupLocation() {
		Rectangle bounds = getBounds();
		Point loc = new Point();
		loc.x = bounds.x + 200;
		loc.y = bounds.y + 200;
		return loc;
	}

	private void removeDocument() throws IOException {
		DocumentDescriptor info = null;
		if (project != null) {
			TreePath path = projectTree.getSelectionPath();
			if (path != null
					&& path.getLastPathComponent() instanceof DocumentDescriptor)
				info = (DocumentDescriptor) path.getLastPathComponent();
		}

		if (info == null)
			return;
		Object[] messageArguments = { info.getName() };
		formatter = new MessageFormat("");
		formatter.applyPattern(LocalizedResources.applicationResources
				.getString("remove.document.project"));
		int option = JOptionPane.showConfirmDialog(this, formatter
				.format(messageArguments),
				LocalizedResources.applicationResources
						.getString("remove.document"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				IconLoader.removeIcon);
		if (option != JOptionPane.YES_OPTION) {
			return;
		}
		IDEditor ie = (IDEditor) info.getEditor();
		if (ie != null) {
			ie.saveDocument();
			ie.closeDocument();
		}
		int pos = project.getIndex(info);
		project.removeDoc(info);

		// fire tree model event for project tree
		Object[] path = { project };
		int[] childIndices = { pos };
		Object[] children = { info };
		TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
				children);
		ProjectTreeModel ptm = (ProjectTreeModel) projectTree.getModel();
		ptm.fireTreeNodesRemoved(tme);

		Object[] message_Arguments = { info.getName() };
		formatter = new MessageFormat("");
		formatter.applyPattern(LocalizedResources.applicationResources
				.getString("removed.document"));

		option = JOptionPane.showConfirmDialog(
				this,
				formatter.format(message_Arguments)
						+ LocalizedResources.applicationResources
								.getString("document.delete"));
		if (option != JOptionPane.YES_OPTION) {
			return;
		}
		File file = getDocumentFile(info);
		file.delete();
		this.updateSaveStatus();
	}

	private boolean replaceDocument(DocumentDescriptor info) {

		if (info == null)
			return false;

		Collection<DocumentDescriptor> col = documents.values();
		Iterator<DocumentDescriptor> it = col.iterator();
		while (it.hasNext()) {
			DocumentDescriptor dd = it.next();
			if (dd.getName().equals(info.getName())
					&& dd.getPath().equals(info.getPath())) {

				IDEditor ie = (IDEditor) dd.getEditor();
				if (ie != null) {
					if (ie.getEditorView() != null) {
						Component comp = (Component) ie.getEditorView();
						int count = (fileTabs != null ? fileTabs.getTabCount()
								: 0);
						for (int i = 0; i < count; i++) {
							if (fileTabs.getComponentAt(i) == comp) {
								fileTabs.remove(i);
								treePane.setViewportView(outline);
								clearStatus();
								break;
							}
						}
						if (fileTabs.getTabCount() == 0) {
							setFeature("closeAllDocumentsMenuItem", false);
							setFeature("closeDocumentMenuItem", false);
							setFeature("closeButton", false);
							setFeature("printMenuItem", false);
							setFeature("printButton", false);
							setFeature("saveDocumentAsMenuItem", false);
							setFeature("saveAsButton", false);
						}
						documents.remove(ie.getEditorView());
					}

					if (ie.getMessagesView() != null) {
						Component comp = (Component) ie.getMessagesView();

						int count = (workTabs != null ? workTabs.getTabCount()
								: 0);
						for (int i = 0; i < count; i++) {
							if (workTabs.getComponentAt(i) == comp) {
								workTabs.remove(i);
								break;
							}
						}
					}

					if (ie.getOutlineView() == treePane.getViewport().getView())
						treePane.setViewportView(outline);
					dd.setEditor(null);
				}
				break;
			}
		}

		Object[] messageArguments = { info.getPath() + File.separator
				+ info.getName() };
		formatter = new MessageFormat("");
		formatter.applyPattern(LocalizedResources.applicationResources
				.getString("file.replace"));
		int option = JOptionPane.showConfirmDialog(this, formatter
				.format(messageArguments),
				LocalizedResources.applicationResources
						.getString("replace.document"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				IconLoader.removeIcon);
		if (option != JOptionPane.YES_OPTION) {
			return false;
		}
		int pos = project.getIndex(info);
		project.removeDoc(info);
		try {
			// fire tree model event for project tree
			Object[] path = { project };
			int[] childIndices = { pos };
			Object[] children = { info };
			TreeModelEvent tme = new TreeModelEvent(this, path, childIndices,
					children);
			ProjectTreeModel ptm = (ProjectTreeModel) projectTree.getModel();
			ptm.fireTreeNodesRemoved(tme);
		} catch (Exception e) {
		} finally {
			File file = getDocumentFile(info);
			file.delete();
		}
		return true;
	}

	private void addDocument() {
		if (project != null) {
			JFileChooser fileChooser = new JFileChooser(
					System.getProperty("user.home"));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle(LocalizedResources.applicationResources
					.getString("add.document"));

			if (lastFileOpened != null)
				fileChooser.setSelectedFile(lastFileOpened);

			int ok = fileChooser.showDialog(this,
					LocalizedResources.applicationResources
							.getString("add.document.project"));
			File docFile = null;
			if (ok == JFileChooser.APPROVE_OPTION) {
				lastFileOpened = docFile = fileChooser.getSelectedFile();
			} else
				return;

			String mimeType = CommonUtils.getMimeType(docFile);

			DocumentDescriptor info = DocumentDescriptorFactory
					.createNewDocumentDescriptor(mimeType.toString());

			try {
				info.setName(docFile.getName());
				info.setPath(docFile.getParent());

				project.addDoc(info);
				int pos = project.getIndex(info);

				// fire tree model event for project tree
				Object[] path = { project };
				int[] childIndices = { pos };
				Object[] children = { info };
				TreeModelEvent tme = new TreeModelEvent(this, path,
						childIndices, children);
				ProjectTreeModel ptm = (ProjectTreeModel) projectTree
						.getModel();
				ptm.fireTreeNodesInserted(tme);
				this.updateSaveStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void openDocumentFile(File docFile) {
		String mimeType = CommonUtils.getMimeType(docFile);

		DocumentDescriptor info = DocumentDescriptorFactory
				.createNewDocumentDescriptor(mimeType.toString());
		MichiganIDE.this.setCursor(Cursor
				.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			progress.setIndeterminate(true);
			info.setName(docFile.getName());
			info.setPath(docFile.getParent());

			info.setEditor(EditorFactory.createNewEditor(null, info, true));
			IDEditor ie = (IDEditor) info.getEditor();
			ie.addUndoableEditListener(undoHandler);
			ie.addPropertyChangeListener(propChangeHandler);
			ie.openDocument();

			document = info;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			progress.setIndeterminate(false);
		}
		MichiganIDE.this.setCursor(Cursor
				.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void openDocument() {

		JFileChooser fileChooser = new JFileChooser(
				System.getProperty("user.home"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle(LocalizedResources.applicationResources
				.getString("open.document"));

		if (lastFileOpened != null)
			fileChooser.setSelectedFile(lastFileOpened);

		int ok = fileChooser.showDialog(this,
				LocalizedResources.applicationResources
						.getString("open.document"));
		File docFile = null;
		if (ok == JFileChooser.APPROVE_OPTION) {
			lastFileOpened = docFile = fileChooser.getSelectedFile();
		} else
			return;
		if (docFile.length() > 0)
			openDocumentFile(docFile);
		else
			status.setText(LocalizedResources.applicationResources
					.getString("file.empty.error"));
	}

	private int saveDocuments() {
		int nfiles = project.getChildCount();
		Vector<DocumentDescriptor> list = new Vector<DocumentDescriptor>();
		for (int i = 0; i < nfiles; i++) {
			DocumentDescriptor docInfo = (DocumentDescriptor) project
					.getChildAt(i);
			if (docInfo.isDirty()) {
				list.add(docInfo);
				docInfo.setDoNotSave(true);
			}
		}

		if (list.size() > 0)
			list = ProjectSave.showDialog(this, "Save Documents",
					getPopupLocation(), list);
		if (list == null)
			return JOptionPane.CANCEL_OPTION;
		int count = (list != null ? list.size() : 0);
		for (int i = 0; i < count; i++) {
			IDEditor ie = ((IDEditor) list.elementAt(i)
					.getEditor());
			ie.saveDocument();
		}
		return JOptionPane.YES_OPTION;
	}

	public void updateUndoRedoStatus() {
		boolean canUndo = undoManager.canUndo();
		String undoName = undoManager.getUndoPresentationName();
		JMenuItem unmi = (JMenuItem) this.getProperty("undoMenuItem");
		unmi.setText(undoName);

		JButton undoButton = (JButton) this.getProperty("undoButton");
		undoButton.setToolTipText(undoName);
		undoButton.setEnabled(canUndo);
		unmi.setEnabled(canUndo);

		boolean canRedo = undoManager.canRedo();
		String redoName = undoManager.getRedoPresentationName();

		JMenuItem remi = (JMenuItem) this.getProperty("redoMenuItem");
		remi.setText(redoName);
		JButton redoButton = (JButton) this.getProperty("redoButton");
		redoButton.setToolTipText(redoName);
		redoButton.setEnabled(canRedo);
		remi.setEnabled(canRedo);
	}

	private class DocumentPopup extends JPopupMenu {
		private static final long serialVersionUID = 4663537120460008580L;
		public JMenuItem open, close, save;

		public DocumentPopup(String title) {
			super(title);
			init();
		}

		private void init() {
			JMenuItem mi = null;

			open = mi = new JMenuItem("Open", IconLoader.openIcon);
			mi.setMnemonic('o');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					MichiganIDE.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					IDEditor ie = (IDEditor) document.getEditor();
					ie.addUndoableEditListener(undoHandler);
					ie.addPropertyChangeListener(propChangeHandler);
					ie.openDocument();
					MichiganIDE.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			});
			add(mi);

			close = mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("close"),
					IconLoader.closeIcon);
			mi.setMnemonic('c');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					MichiganIDE.this.closeDocument();
				}
			});
			add(mi);

			this.addSeparator();

			save = mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("save"),
					IconLoader.saveIcon);
			mi.setMnemonic('s');
			mi.setEnabled(false);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					((IDEditor) document.getEditor()).saveDocument();
				}
			});
			add(mi);

			mi = new JMenuItem(
					LocalizedResources.applicationResources
							.getString("save.as..."),
					IconLoader.saveAsIcon);
			mi.setMnemonic('a');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					((IDEditor) document.getEditor()).saveDocumentAs();
				}
			});
			add(mi);

			this.addSeparator();
			mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("rename"));
			mi.setMnemonic('r');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					IDEditor ie = (IDEditor) document.getEditor();
					ie.renameDocument();
					Object view = ie.getEditorView();
					int count = (fileTabs != null ? fileTabs.getTabCount() : 0);
					for (int i = 0; i < count; i++) {
						if (fileTabs.getComponentAt(i).equals(view)) {
							fileTabs.setTitleAt(i, document.getName());
							break;
						}
					}
					projectTree.treeDidChange();
					MichiganIDE.this.project.setDirty(true);
					updateSaveStatus();
				}
			});
			add(mi);

			this.addSeparator();
			mi = new JMenuItem(
					LocalizedResources.applicationResources.getString("remove"),
					IconLoader.removeIcon);
			mi.setMnemonic('r');
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					try {
						removeDocument();
					} catch (IOException ex) {
					}
				}
			});
			add(mi);

		}
	}

	private class UndoableEditHandler implements UndoableEditListener {

		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.undoableEditHappened(e);
			updateUndoRedoStatus();
		}

	}

	private class PropertyChangeHandler implements PropertyChangeListener {

		private StringBuffer sb = new StringBuffer(16);

		public void propertyChange(PropertyChangeEvent e) {

			IDEditor editor = (IDEditor) e.getSource();

			String prop = e.getPropertyName();
			Object newValue = e.getNewValue();
			Object oldValue = e.getOldValue();

			if (IDEditor.PROP_LINE_COL.equals(prop)) {
				Point p = (Point) newValue;
				sb.setLength(0);
				sb.append(p.x).append(" : ").append(p.y);
				status3.setText(sb.toString());
				return;
			}

			if (IDEditor.PROP_STATUS.equals(prop)) {
				status.setText((String) newValue);
				return;
			}

			if (IDEditor.PROP_DIRTY.equals(prop)) {
				if (!getFeature("saveButton"))
					updateSaveStatus();
				return;
			}
			if (IDEditor.PROP_SAVE_STATUS.equals(prop)) {
				updateSaveStatus();
				return;
			}

			if (IDEditor.PROP_OUTLINE_VIEW.equals(prop)) {

				Component sel = fileTabs.getSelectedComponent();
				if (sel == null) {
					treePane.setViewportView(outline);
					return;
				}
				DocumentDescriptor dd = documents.get(sel);
				if (dd == null) {
					treePane.setViewportView(outline);
					return;
				}

				if (dd.getEditor() == editor) {
					if (oldValue == null && newValue != null) {
						treePane.setViewportView((Component) newValue);
						treePane.repaint();
					} else if (newValue == null) {
						treePane.setViewportView(outline);
					}
				}
				return;
			}

			if (IDEditor.PROP_EDITOR_VIEW.equals(prop)) {

				if (oldValue == null && newValue != null) {
					DocumentDescriptor nd = editor.getDocumentDescriptor();
					documents.put(newValue, nd);
					String docName = nd.getName();
					Icon icon = nd.getIcon();
					fileTabs.addTab(docName, icon, (Component) newValue);
					if (fileTabs.getTabCount() > 0) {
						fileTabs.setSelectedIndex(fileTabs.getTabCount() - 1);
						if (workTabs != null && workTabs.getTabCount() > 0) {
							workTabs.setSelectedIndex(workTabs.getTabCount() - 1);
						}
					}
					if (fileTabs.getTabCount() == 1) {
						setFeature("closeAllDocumentsMenuItem", true);
						setFeature("closeDocumentMenuItem", true);
						setFeature("closeButton", true);
						setFeature("printMenuItem", true);
						setFeature("printButton", true);
						setFeature("saveDocumentAsMenuItem", true);
						setFeature("saveAsButton", true);
						setFeature("findReplaceMenuItem", true);
						setFeature("findButton", true);
					}

				} else if (newValue == null && oldValue != null) {
					documents.remove(oldValue);

					int count = (fileTabs != null ? fileTabs.getTabCount() : 0);
					for (int i = 0; i < count; i++) {
						if (fileTabs.getComponentAt(i) == oldValue) {
							fileTabs.remove(i);

							if (count > 1) {
								fileTabs.setSelectedIndex(-1);
								fileTabs.setSelectedIndex(0);
							} else {
								treePane.setViewportView(outline);
								clearStatus();
							}
							break;
						}
					}
					if (fileTabs.getTabCount() == 0) {
						setFeature("closeAllDocumentsMenuItem", false);
						setFeature("closeDocumentMenuItem", false);
						setFeature("closeButton", false);
						setFeature("printMenuItem", false);
						setFeature("printButton", false);
						setFeature("saveDocumentAsMenuItem", false);
						setFeature("saveAsButton", false);
						setFeature("findReplaceMenuItem", false);
						setFeature("findButton", false);
						setFeature("findAgainButton", false);
						setFeature("replaceButton", false);
					}

				} else if (newValue == oldValue) {
					int count = (fileTabs != null ? fileTabs.getTabCount() : 0);
					for (int i = 0; i < count; i++) {
						if (fileTabs.getComponentAt(i) == oldValue) {
							fileTabs.setSelectedIndex(i);
							if (workTabs != null) {
								workTabs.setSelectedIndex(i);
							}
							break;
						}
					}
				}
				return;
			}

			if (IDEditor.PROP_MESSAGE_VIEW.equals(prop)) {

				if (oldValue == null && newValue != null) {
					workTabs.add(editor.getDocumentDescriptor().getName(),
							(Component) newValue);
					workTabs.setSelectedIndex(workTabs.getTabCount() - 1);
				} else if (newValue == null && oldValue != null) {
					int count = (workTabs != null ? workTabs.getTabCount() : 0);
					for (int i = 0; i < count; i++) {
						if (workTabs.getComponentAt(i) == oldValue) {
							workTabs.remove(i);
							break;
						}
					}
				}
				return;
			}

			if (IDEditor.PROP_REMOVE_MESSAGE_TITLE.equals(prop)) {

				int count = (workTabs != null ? workTabs.getTabCount() : 0);
				for (int i = 0; i < count; i++) {
					if (workTabs.getTitleAt(i).equals(oldValue)) {
						workTabs.remove(i);
						break;
					}
				}
				return;
			}

			if (IDEditor.PROP_TEXT_MODE.equals(prop)) {
				status2.setText(((Boolean) newValue).booleanValue() ? "Text Mode"
						: "XML Mode");
				if (((Boolean) oldValue).booleanValue() != ((Boolean) newValue)
						.booleanValue()) {
					undoManager.discardAllEdits();
					updateUndoRedoStatus();
				}
			}
			if (newValue instanceof Boolean) {

				if ("closeAllDocumentsMenuItem".equals(prop)) {
					if (fileTabs.getTabCount() == 0)
						setFeature(prop,
								((Boolean) e.getNewValue()).booleanValue());
					return;
				}
				setFeature(e.getPropertyName(),
						((Boolean) e.getNewValue()).booleanValue());
			} else {
				setProperty(e.getPropertyName(), newValue);
			}
		}

	}

	private class MichiganUndoManager extends UndoManager {
		private static final long serialVersionUID = 5716174486949058351L;

		public void undo() {
			super.undo();
			updateUndoRedoStatus();
		}

		public void redo() {
			super.redo();
			updateUndoRedoStatus();
		}

	}

}