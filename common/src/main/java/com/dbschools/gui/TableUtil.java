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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableUtil {

    public static void setColumnWidths(int[] preferredColWidths, int[] maxColWidths, 
        int[] minColWidths, TableColumnModel columnModel, boolean[] columnsShowing) {
        
        List<Integer> visibleColumns = visibleColumnsMap(columnsShowing,
                preferredColWidths.length);
        assert visibleColumns.size() == columnModel.getColumnCount();
        
        for (int visColIdx = 0; visColIdx < columnModel.getColumnCount(); ++visColIdx) {
            int allColsIdx = visibleColumns.get(visColIdx);
            TableColumn column = columnModel.getColumn(visColIdx);
            column.setPreferredWidth(preferredColWidths[allColsIdx]);
            if (maxColWidths[allColsIdx] > 0) {
                column.setMaxWidth(maxColWidths[allColsIdx]);
            }
            
            if (minColWidths[allColsIdx] > 0) {
                column.setMinWidth(minColWidths[allColsIdx]);
            }
        }
    }

    public static List<Integer> visibleColumnsMap(
            boolean[] columnsShowing, int len) {
        List<Integer> visibleColumns = new ArrayList<Integer>();
        for (int i = 0; i < len; ++i) {
            if (columnsShowing == null || columnsShowing[i]) {
                visibleColumns.add(i);
            }
        }
        return visibleColumns;
    }

    /**
     * Returns a sorted array of model row indexes for a table, supporting
     * TableSorter.
     * @param table the JTable
     * @return row indexes
     */
    public static int[] selectedSortedModelRows(JTable table) {
        final int[] selectedRows = table.getSelectedRows();
        final TableModel model = table.getModel();
        final int[] selectedModelRows = new int[selectedRows.length];
        
        for (int i = 0; i < selectedRows.length; ++i) {
            final int index;
            if (model instanceof TableSorter) {
                index = ((TableSorter) model).modelIndex(selectedRows[i]);
            } else { 
                index = selectedRows[i];
            }
            selectedModelRows[i] = index;
        }
        Arrays.sort(selectedModelRows);
        return selectedModelRows;
    }
    
    public static void deleteRows(final int[] sortedRows, 
            final List<?> list, final AbstractTableModel model) {
        for (int i = sortedRows.length - 1; i >= 0; --i) {
            list.remove(sortedRows[i]);
        }
        model.fireTableDataChanged();
    }

    /**
     * Sets up a sorting table, based on Sun's TableSorter.
     * @param table
     * @param tableModel
     * @return TableSorter
     */
    public static TableSorter setUpSortingTable(JTable table, TableModel tableModel) {
        final TableSorter sorter = new TableSorter(tableModel);
        final JTableHeader tableHeader = table.getTableHeader();
        sorter.setTableHeader(tableHeader); 
    
        //Set up tool tips for column headers.
        tableHeader.setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");
    
        table.setModel(sorter);
        
        return sorter;
    }

    public static boolean isOkToDelete(Component parent, final int num) {
        final StringBuffer buf = new StringBuffer("Are you sure you want to delete the ");
        PromptUtils.addMessageToPrompt(buf, num, "selected item");
        buf.append('?');
        final int choice = JOptionPane.showConfirmDialog(parent, buf.toString());
        final boolean okToDelete = choice == JOptionPane.YES_OPTION;
        return okToDelete;
    }

}
