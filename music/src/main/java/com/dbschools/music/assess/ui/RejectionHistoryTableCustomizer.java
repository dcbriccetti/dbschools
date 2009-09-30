package com.dbschools.music.assess.ui;

import com.dbschools.music.ui.DateCellRenderer;
import com.dbschools.music.*;
import java.util.Date;

import javax.swing.JTable;

import com.dbschools.gui.TableUtil;

/**
 * Class with a static method to customize the rejection history table.
 * @author David C. Briccetti
 */
public class RejectionHistoryTableCustomizer {
    public static void customize(JTable rejectionsTable) {
        rejectionsTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        final int preferredColWidths[] = { 75, 50, 250 };
        final int maxColWidths[] = { 75, 0, 0 };
        final int minColWidths[] = { 0, 0, 0 };
        TableUtil.setColumnWidths(preferredColWidths, maxColWidths, 
            minColWidths, rejectionsTable.getColumnModel(), null);
    }
}
