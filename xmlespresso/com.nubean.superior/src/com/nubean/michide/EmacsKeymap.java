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

import com.nubean.michbase.editor.IDEditor;
import com.nubean.michutil.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.event.*;
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

public class EmacsKeymap {

	private static final JTextComponent.KeyBinding[] emacsBaseBindings = {
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F,
					InputEvent.CTRL_MASK), DefaultEditorKit.forwardAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_A,
					InputEvent.CTRL_MASK), DefaultEditorKit.beginLineAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_E,
					InputEvent.CTRL_MASK), DefaultEditorKit.endLineAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_B,
					InputEvent.CTRL_MASK), DefaultEditorKit.backwardAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_N,
					InputEvent.CTRL_MASK), DefaultEditorKit.downAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_P,
					InputEvent.CTRL_MASK), DefaultEditorKit.upAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V,
					InputEvent.CTRL_MASK), DefaultEditorKit.pageDownAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					InputEvent.CTRL_MASK), DefaultEditorKit.cutAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
					InputEvent.CTRL_MASK), DefaultEditorKit.pasteAction) };

	private static final JTextComponent.KeyBinding[] emacsMarkBindings = {
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F,
					InputEvent.CTRL_MASK),
					DefaultEditorKit.selectionForwardAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_A,
					InputEvent.CTRL_MASK),
					DefaultEditorKit.selectionBeginLineAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_E,
					InputEvent.CTRL_MASK),
					DefaultEditorKit.selectionEndLineAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_B,
					InputEvent.CTRL_MASK),
					DefaultEditorKit.selectionBackwardAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_N,
					InputEvent.CTRL_MASK), DefaultEditorKit.selectionDownAction),
			new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_P,
					InputEvent.CTRL_MASK), DefaultEditorKit.selectionUpAction) };

	public static Keymap createKeymap() {
		JTextPane tc = new JTextPane();
		Keymap parentKeymap = tc.getKeymap();
		Keymap emacsKeymap = JTextComponent.addKeymap("emacs", parentKeymap);

		JTextComponent.loadKeymap(emacsKeymap, emacsBaseBindings, tc
				.getActions());

		emacsKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.CTRL_MASK), new EmacsCtrlXAction(emacsKeymap));

		emacsKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK), new EmacsFindAction(emacsKeymap));

		emacsKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(
				KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), new EmacsMarkAction(
				emacsKeymap));

		emacsKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0), new EmacsEscapeAction(emacsKeymap));

		return emacsKeymap;

	}

	private static class EmacsCtrlXAction extends AbstractAction {
		private static final long serialVersionUID = 9206759183006993092L;

		private Keymap keymap;

		private boolean ignore;

		private int modifiers;

		public EmacsCtrlXAction(Keymap k) {
			super();
			keymap = k;
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent tc = (JTextComponent) e.getSource();
			Keymap emacsCtrlXKeymap = JTextComponent.addKeymap("emacsCtrlX",
					null);

			ignore = true;
			tc.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
					JTextComponent tc = (JTextComponent) e.getSource();
					switch (e.getKeyChar()) {
					case 's':
						ideAction(tc, "saveDocumentsMenuItem");
						break;
					case 'g':
						gotoAction(tc);
						break;
					}
					if (!ignore) {
						tc.removeKeyListener(this);
						tc.setKeymap(keymap);
					} else
						ignore = false;
					e.consume();
				}

				public void keyPressed(KeyEvent e) {
					JTextComponent tc = (JTextComponent) e.getSource();
					modifiers |= e.getModifiers();
					switch (e.getKeyCode()) {
					case KeyEvent.VK_S:
						if (modifiers == KeyEvent.CTRL_MASK) {
							IDEditor editor = getEditor(tc);
							if (editor != null)
								editor.saveDocument();
						}
						break;
					case KeyEvent.VK_W:
						if (modifiers == KeyEvent.CTRL_MASK) {
							ideAction(tc, "saveDocumentAsButton");
						}
						break;
					case KeyEvent.VK_G:
						if (modifiers == KeyEvent.CTRL_MASK) {
							tc.removeKeyListener(this);
							tc.setKeymap(keymap);
						}
						break;
					}
					e.consume();
				}

				public void keyReleased(KeyEvent e) {
					e.consume();
				}

			});

			tc.setKeymap(emacsCtrlXKeymap);
		}
	}

	private static class EmacsFindAction extends AbstractAction {
		private static final long serialVersionUID = -8971730280033691614L;

		private Keymap keymap;

		private int modifiers;

		public EmacsFindAction(Keymap k) {
			super();
			keymap = k;
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent tc = (JTextComponent) e.getSource();
			ideAction(tc, "findButton");
			Keymap emacsFindKeymap = JTextComponent
					.addKeymap("emacsFind", null);

			tc.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
					e.consume();
				}

				public void keyPressed(KeyEvent e) {
					JTextComponent tc = (JTextComponent) e.getSource();
					modifiers |= e.getModifiers();
					switch (e.getKeyCode()) {
					case KeyEvent.VK_S:
						if (modifiers == KeyEvent.CTRL_MASK) {
							ideAction(tc, "findAgainButton");
						}
						break;
					case KeyEvent.VK_ENTER:
						tc.removeKeyListener(this);
						tc.setKeymap(keymap);
						try {
							tc.setCaretPosition(tc.getSelectionStart());
						} catch (Exception ex) {
						}
						IDEditor editor = getEditor(tc);
						if (editor != null)
							editor.clear();
						break;
					case KeyEvent.VK_G:
						if (modifiers == KeyEvent.CTRL_MASK) {
							tc.removeKeyListener(this);
							tc.setKeymap(keymap);
							editor = getEditor(tc);
							if (editor != null)
								editor.clear();
						}
						break;
					default:
						if (e.getModifiers() == 0)
							java.awt.Toolkit.getDefaultToolkit().beep();
						break;
					}
					e.consume();
				}

				public void keyReleased(KeyEvent e) {
					e.consume();
				}

			});

			tc.setKeymap(emacsFindKeymap);
		}
	}

	private static class EmacsMarkAction extends AbstractAction {
		private static final long serialVersionUID = 6090371372328915082L;

		private Keymap keymap;

		public EmacsMarkAction(Keymap k) {
			super();
			keymap = k;
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent tc = (JTextComponent) e.getSource();
			Keymap emacsMarkKeymap = JTextComponent.addKeymap("emacsMark",
					keymap);

			JTextComponent.loadKeymap(emacsMarkKeymap, emacsMarkBindings, tc
					.getActions());

			emacsMarkKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(
					KeyEvent.VK_G, InputEvent.CTRL_MASK), new EmacsResetAction(
					keymap));

			tc.setKeymap(emacsMarkKeymap);
		}
	}

	private static class EmacsResetAction extends AbstractAction {
		private static final long serialVersionUID = -7456902708064419556L;
		private Keymap keymap;

		public EmacsResetAction(Keymap keymap) {
			super();
			this.keymap = keymap;
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent tc = (JTextComponent) e.getSource();
			IDEditor editor = getEditor(tc);
			if (editor != null)
				editor.clear();
			tc.setKeymap(keymap);
		}

	}

	private static class EmacsEscapeAction extends AbstractAction {
		private static final long serialVersionUID = -4212564640143564391L;

		private Keymap keymap;

		private boolean ignore;

		public EmacsEscapeAction(Keymap k) {
			super();
			keymap = k;
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent tc = (JTextComponent) e.getSource();
			Keymap emacsEscapeKeymap = JTextComponent.addKeymap("emacsEscape",
					null);
			ignore = true;
			tc.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
					JTextComponent tc = (JTextComponent) e.getSource();
					switch (e.getKeyChar()) {
					case '<':
						action(tc, DefaultEditorKit.beginAction);
						break;
					case '>':
						action(tc, DefaultEditorKit.endAction);
						break;
					case 'v':
						action(tc, DefaultEditorKit.pageDownAction);
						break;
					case 'w':
						action(tc, DefaultEditorKit.copyAction);
						break;
					case '%':
						ideAction(tc, "replaceButton");
						break;
					}
					if (!ignore) {
						tc.removeKeyListener(this);
						tc.setKeymap(keymap);
					} else
						ignore = false;
					e.consume();
				}

				public void keyPressed(KeyEvent e) {
					e.consume();
				}

				public void keyReleased(KeyEvent e) {
					e.consume();
				}

			});

			tc.setKeymap(emacsEscapeKeymap);
		}
	}

	private static IDEditor getEditor(JTextComponent c) {
		IDEditor editor = null;
		if (c != null) {
			try {
				AbstractDocument doc = (AbstractDocument) c.getDocument();
				Class klass = doc.getClass();

				Method m = klass.getMethod("getEditor", new Class[]{});
				editor = (IDEditor) m.invoke(doc, new Object[]{});
			} catch (Exception e) {
			}
		}
		return editor;
	}

	private static AbstractIDE getIDE(java.awt.Component c) {
		AbstractIDE ide = null;
		if (c != null) {
			try {
				while ((c = c.getParent()) != null) {
					if (c instanceof AbstractIDE) {
						ide = (AbstractIDE) c;
						break;
					}
				}
			} catch (Exception e) {
			}
		}
		return ide;
	}

	private static Action getAction(JTextComponent tc, String name) {
		Action[] actions = tc.getActions();
		for (int i = 0; i < actions.length; i++) {
			if (actions[i].getValue(Action.NAME).equals(name))
				return actions[i];
		}
		return null;
	}

	private static void action(JTextComponent tc, String name) {
		Action action = getAction(tc, name);
		if (action != null) {
			ActionEvent ae = new ActionEvent(tc, ActionEvent.ACTION_PERFORMED,
					name);
			action.actionPerformed(ae);
		}
	}

	private static void ideAction(JTextComponent tc, String name) {
		AbstractIDE ide = getIDE(tc);
		if (ide != null) {
			AbstractButton button = (AbstractButton) ide.getProperty(name);
			if (button != null && button.isEnabled())
				button.doClick();
		}
	}

	private static void gotoAction(JTextComponent tc) {
		ActionEvent ae = new ActionEvent(tc, ActionEvent.ACTION_PERFORMED,
				"goto");
		GotoAction action = new GotoAction(tc);
		action.actionPerformed(ae);
	}
}
