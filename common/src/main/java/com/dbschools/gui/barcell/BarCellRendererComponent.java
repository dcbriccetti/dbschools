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

package com.dbschools.gui.barcell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.text.NumberFormatter;

/**
 * This Component draws a bar (as in a bar chart) with a numeric value.
 */
public class BarCellRendererComponent extends JComponent {
    private static final long serialVersionUID = 5361366898143591927L;

    private static NumberFormat numberFormat;

    private static NumberFormatter formatter;

    private static Color barColor;

    private BarCellValue barCellValue;

    /**
     * Creates a component to draw a bar with the specified value out of "max"
     * maximum value. If value = max / 2, then the bar will be drawn halfway
     * across.
     * 
     * @param barCellValue
     */
    public BarCellRendererComponent(BarCellValue barCellValue) {
        this.barCellValue = barCellValue;
        setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 4));
    }

    private NumberFormat getNumberFormat() {
        if (numberFormat == null) {
            numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
        }
        return numberFormat;
    }

    private NumberFormatter getNumberFormatter() {
        if (formatter == null) {
            formatter = new NumberFormatter(getNumberFormat());
        }
        return formatter;
    }

    private Color getBarColor() {
        if (barColor == null) {
            barColor = new Color(.8F, .8F, 1F);
        }
        return barColor;
    }

    /**
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Insets insets = getInsets();
        g.setColor(getBackground());
        g.fillRect(insets.left, insets.top, getWidth() - insets.left
                - insets.right, getHeight() - insets.top - insets.bottom);
        Double val = barCellValue.getValue();
        if (val == null && barCellValue.isNullIsMax()) {
            val = barCellValue.getMax();
        }

        if (val != null) {
            if (val.doubleValue() > 0D) {
                g.setColor(getBarColor());
                g.fill3DRect(insets.left, insets.top,
                        (int) ((getWidth() - insets.right)
                                / barCellValue.getMax().doubleValue() * val
                                .doubleValue()), getHeight() - insets.top
                                - insets.bottom, true);
            }

            g.setColor(getForeground());
            try {
                if (barCellValue.getValue() != null) {
                    g.drawString(getNumberFormatter().valueToString(
                            barCellValue.getValue()), insets.left * 2,
                            getHeight() - 3);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
