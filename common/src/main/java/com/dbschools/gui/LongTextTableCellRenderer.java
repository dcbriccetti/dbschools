/*
 * QuickQuiz
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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang.StringUtils;

/**
 * Sets tooltip text for the cell being rendered.
 * 
 * @author David C. Briccetti
 */
public final class LongTextTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -1376092781703644904L;

    /**
     * Used to set renderers by class.
     */
    public static class LongString {
        private LongString() {
            // Prevent instantiation
        }
    }

    /**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
    public Component getTableCellRendererComponent(
		final JTable table,
		final Object value,
		final boolean isSelected,
		final boolean hasFocus,
		final int row,
		final int column) {

        if (value instanceof String) {
            final String strVal = (String) value;
            setToolTipText(StringUtils.isBlank(strVal) ? null : "<html>" + strVal + "</html>");
        }

		return super.getTableCellRendererComponent(table, value, isSelected, 
                hasFocus, row, column);
	}

}
