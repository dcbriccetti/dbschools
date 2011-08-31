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

package com.dbschools.quickquiz.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.dbschools.quickquiz.QuizTaker;

/**
 * Provides the table model for the quiz takers.
 * 
 * @author David C. Briccetti
 */
public final class TakerTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 3345484257791104131L;

    private List<QuizTaker> takers = new ArrayList<QuizTaker>();

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 5;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return takers.size();
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0: return String.class;
        case 1: return Integer.class;
        case 2: return String.class;
        case 3: return Date.class;
        case 4: return String.class;
        default:
            assert false : columnIndex;
            return Object.class;
        }
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(final int row, final int column) {
        final QuizTaker taker = takers.get(row);
        final Object cellVal;
        switch (column) {
        case 0:
            cellVal = taker.getTakerName();
            break;
        case 1:
            cellVal = taker.getScore();
            break;
        case 2:
            cellVal = taker.getLastResponse();
            break;
        case 3:
            cellVal = taker.getLastResponseReceivedAt();
            break;
        case 4:
            String addr = taker.getAddress().toString();
            final int pos = addr.indexOf(':');
            if (pos > 0) {
                addr = addr.substring(0, pos); 
            }
            cellVal = addr;
            break;
        default:
            assert false : column;
            cellVal = "Error";
            break;
        }
        return cellVal;
    }

    /**
     * Sets the roster property (quickquiz.Roster) value.
     * @param takers
     */
    void setTakers(final List<QuizTaker> takers) {
        this.takers = takers;
        fireTableDataChanged();
    }
}