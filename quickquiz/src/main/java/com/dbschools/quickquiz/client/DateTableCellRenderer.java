package com.dbschools.quickquiz.client;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A JTable cell render for dates.
 * @author David C. Briccetti
 */
final class DateTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -3632908205508095084L;

    private final SimpleDateFormat fmtTimePart1 = new SimpleDateFormat("hh:mm:");
    private final SimpleDateFormat fmtTimePart2 = new SimpleDateFormat("ss.S");
    private final DecimalFormat fmtSeconds = new DecimalFormat("##.#");

    @Override
    public Component getTableCellRendererComponent(JTable table, 
            Object value, boolean isSelected, boolean hasFocus, 
            int row, int column) {
        if (value != null) {
            final StringBuffer buf = new StringBuffer(fmtTimePart1
                    .format(value));
            final String secsStr = fmtTimePart2.format(value);
            final float f = Float.parseFloat(secsStr);
            buf.append(fmtSeconds.format(f));
            value = buf.toString();
        } else {
            value = "";
        }
        return super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
    }
}