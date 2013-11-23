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

package com.dbschools.music.assess.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.dbschools.music.orm.Rejection;

/**
 * Provides a TableModel for rejection history. 
 */
public final class RejectionsModel extends AbstractTableModel {

    private static final long serialVersionUID = 9032108151941970799L;

    private List<Rejection> rejections;
    
    /** Column titles */
    public static final String[] TITLES = { "Date", "Tester", "Reason" };
    private static final Class<?>[] classes = { Date.class, String.class, String.class };
    
    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(final int col) {
        return TITLES[col];
    }
    
    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return classes[columnIndex];
    }
    
    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return TITLES.length;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return rejections == null ? 0 : rejections.size();
    }
    
    /**
     * Gets the record at the specified row index.
     * 
     * @param rowIndex the index of the row being requested
     * @return the record at the specified row index
     */
    public Rejection getRecord(final int rowIndex) {
        return rejections.get(rowIndex);
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        final Rejection rej = getRecord(rowIndex);
        switch (columnIndex) {
        case 0: return rej.getRejectionTime();
        case 1: return rej.getUser().getDisplayName();
        case 2: return rej.getRejectionReason().getText();
        default:
            throw new IllegalArgumentException("Illegal columnIndex: " +  //$NON-NLS-1$
                    columnIndex);
        }
    }    

    public void setMusicRejections(Collection<Rejection> rejections) {
        this.rejections = new ArrayList<Rejection>(rejections);
        Collections.sort(this.rejections);
        fireTableDataChanged();
    }

    public void addRejection(Rejection rejection) {
        rejections.add(rejection);
        fireTableDataChanged();
    }

    public void deleteRejection(Rejection rejection) {
        rejections.remove(rejection);
        fireTableDataChanged();
    }
}
