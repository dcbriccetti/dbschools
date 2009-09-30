package com.dbschools.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class PopupListener extends MouseAdapter {
    private final JTable table;
    private final JPopupMenu popupMenu;
    
    public PopupListener(JTable table, JPopupMenu popup) {
        super();
        this.table = table;
        this.popupMenu = popup;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            ListSelectionModel selectionModel = table.getSelectionModel(); 
            int i = table.rowAtPoint(e.getPoint());
            if (! selectionModel.isSelectedIndex(i)) {
                selectionModel.setSelectionInterval(i, i);
            }
            popupMenu.show(e.getComponent(),
                       e.getX(), e.getY());
        }
    }
}