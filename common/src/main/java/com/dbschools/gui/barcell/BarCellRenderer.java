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

package com.dbschools.gui.barcell;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Uses a BarCellRendererComponent to draw a bar in a JTable cell.
 */
public class BarCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 4088282783877442285L;

    /**
     * Sets a BarCellRenderer in place for the specified table.
     * @param table
     */
    public static void setForTable(JTable table) {
        table.setDefaultRenderer(BarCellValue.class, new BarCellRenderer());
    }

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable table, 
            Object value,
            boolean isSelected, 
            @SuppressWarnings("unused") boolean hasFocus, 
            @SuppressWarnings("unused") int row, 
            @SuppressWarnings("unused") int column) {
        
        final BarCellValue bcVal = (BarCellValue) value;
        
        final BarCellRendererComponent component = new 
                BarCellRendererComponent(bcVal);
        
        if (isSelected) {
            component.setForeground(table.getSelectionForeground());
            component.setBackground(table.getSelectionBackground());
        } else {
            component.setForeground(table.getForeground());
            component.setBackground(table.getBackground());
        }

        return component; 
    }
}
