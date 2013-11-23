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

package com.dbschools.music.decortrs;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.dbschools.music.assess.ui.AssessmentsModel.PieceAndPass;
import com.dbschools.music.orm.Piece;

/**
 * A cell renderer for a piece, which shows the piece differently if the musician
 * failed the test.
 * 
 * @author David C. Briccetti
 */
public final class PieceRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -8579450633750526265L;

    /**
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected,
            hasFocus, row, column);
        if (value instanceof PieceAndPass) {
            PieceAndPass pieceAndPass = ((PieceAndPass) value);
            Piece piece = pieceAndPass.getPiece();
            setValue(piece.toString());
            tableCellRendererComponent.setForeground(
                    pieceAndPass.isPassed() ? Color.BLACK : Color.RED);
        }
        return tableCellRendererComponent;
    }
}
