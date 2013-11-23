package com.dbschools.music.assess.ui;

import com.dbschools.music.ui.DateCellRenderer;
import java.util.Date;

import javax.swing.JTable;

import com.dbschools.gui.TableUtil;
import com.dbschools.gui.barcell.BarCellRenderer;
import com.dbschools.music.decortrs.PieceRenderer;
import com.dbschools.music.orm.Piece;

/**
 * Class with a static method to customize the summary table.
 * @author David C. Briccetti
 */
public class SummaryTableCustomizer {
    public static void customize(JTable summariesTable) {

        summariesTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        summariesTable.setDefaultRenderer(Piece.class, new PieceRenderer());
        BarCellRenderer.setForTable(summariesTable);
        
        final int preferredColWidths[] = { 140, 35, 140, 100, 75, 50, 50, 50, 50, 40 };
        final int maxColWidths[] = { 0, 35, 150, 0, 75, 50, 50, 50, 50, 40 };
        final int minColWidths[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        TableUtil.setColumnWidths(preferredColWidths, maxColWidths, 
            minColWidths, summariesTable.getColumnModel(), null);
    }
}
