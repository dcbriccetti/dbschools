package com.dbschools.gui;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Code to help with actions.
 * @author Dave Briccetti
 */
public class ActionUtil {

    /**
     * Attaches a delete action to a component's delete and backspace keys.
     * @param component
     * @param deleteAction
     */
    public static void attachDeleteAction(JComponent component, Action deleteAction) {
        final String deleteActionName = "delete";
        final KeyStroke deleteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        final KeyStroke backspaceKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);

        component.getActionMap().put(deleteActionName, deleteAction);
        final InputMap inputMap = component.getInputMap();
        inputMap.put(deleteKeyStroke, deleteActionName);
        inputMap.put(backspaceKeyStroke, deleteActionName);
    }

}
