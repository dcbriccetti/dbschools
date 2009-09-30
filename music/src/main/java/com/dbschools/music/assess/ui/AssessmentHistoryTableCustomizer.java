package com.dbschools.music.assess.ui;

import com.dbschools.music.ui.DateCellRenderer;
import java.util.Date;

import javax.swing.JTable;

import com.dbschools.gui.LongTextTableCellRenderer;
import com.dbschools.gui.TableUtil;
import com.dbschools.gui.LongTextTableCellRenderer.LongString;
import com.dbschools.music.assess.ui.AssessmentsModel.PieceAndPass;
import com.dbschools.music.decortrs.PieceRenderer;

/**
 * Class with static methods to customize the assessment history table.
 * @author David C. Briccetti
 */
public class AssessmentHistoryTableCustomizer {
    public static void customize(JTable table) {
        table.setDefaultRenderer(LongString.class, new LongTextTableCellRenderer());
        table.setDefaultRenderer(Date.class, new DateCellRenderer());
        table.setDefaultRenderer(PieceAndPass.class, new PieceRenderer());
    }

    public static void setColumnWidths(JTable table) {
        final int preferredColWidths[] = { 75, 65, 60, 90, 250 };
        final int maxColWidths[] = { 75, 0, 200, 120, 0 };
        final int minColWidths[] = { 75, 60, 65, 0, 0 };

        TableUtil.setColumnWidths(preferredColWidths, maxColWidths,  
            minColWidths, table.getColumnModel(), null);
    }

}
