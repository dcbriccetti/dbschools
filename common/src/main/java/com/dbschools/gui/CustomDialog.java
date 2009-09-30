/*
 * DBSchools
 * Copyright (C) 2005 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.dbschools.gui;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public abstract class CustomDialog extends JDialog {

    /**
     * @param owner
     * @param modal
     * @throws java.awt.HeadlessException
     */
    public CustomDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
        setLocationRelativeTo(owner);
        
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exitDialog(false);
        }};

        final JRootPane pane = getRootPane();
        pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
                "escape");
        pane.getActionMap().put("escape", cancelAction);
    }

    protected void setDefaultButton(final JButton button) {
        getRootPane().setDefaultButton(button);
    }
    
    protected boolean okPushed;

    protected void exitDialog(boolean okPushed) {
        this.okPushed = okPushed;
        hide();
    }

    /**
     * @return whether the dialog was dismissed with the OK button
     */
    public boolean isOkPushed() {
        return okPushed;
    }

}
