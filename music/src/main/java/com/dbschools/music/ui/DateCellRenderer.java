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

package com.dbschools.music.ui;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Custom renderer for dates.
 * 
 * @author David C. Briccetti
 */
public final class DateCellRenderer extends DefaultTableCellRenderer {
    protected static final String DATE_FORMAT = "MMM dd, yy";

    private static final long serialVersionUID = 3864672500586390145L;
    private static DateFormat dateFormatLong = DateFormat.getDateTimeInstance(
        DateFormat.LONG, DateFormat.LONG);
    private DateFormat dateFormatNormal = new SimpleDateFormat(DATE_FORMAT);
    
    /**
     * Overrides the default format string.
     * 
     * @param formatString a format string in the format required by the 
     * {@link SimpleDateFormat} constructor
     */
    public void setFormatString(String formatString) {
        dateFormatNormal = new SimpleDateFormat(formatString);
    }
    
    /**
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        final Component tableCellRendererComponent = 
                super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
        final Calendar calAss = Calendar.getInstance();
        if (value != null) {
            calAss.setTime((Date) value);
            setValue(dateFormatNormal.format(value));
            setToolTipText(dateFormatLong.format(value));
        }
        return tableCellRendererComponent;
    }
}
