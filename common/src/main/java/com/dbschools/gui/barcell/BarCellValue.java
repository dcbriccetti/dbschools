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

import java.io.Serializable;

/**
 * A value for a BarCellRenderer, consisting of "value" and "max," where value
 * is the current value and max is the highest possible value.
 */
public class BarCellValue implements Serializable, Comparable<BarCellValue> {

    private static final long serialVersionUID = -4247401575479885215L;

    private final Double value;

    private final Double max;

    private boolean nullIsMax;

    /**
     * Create a value using the supplied value and max.
     * 
     * @param value
     * @param max
     */
    public BarCellValue(Double value, Double max) {
        this.value = value;
        this.max = max;
    }

    public Double getMax() {
        return max;
    }

    public Double getValue() {
        return value;
    }

    public boolean isNullIsMax() {
        return nullIsMax;
    }

    public void setNullIsMax(boolean nullIsMax) {
        this.nullIsMax = nullIsMax;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(BarCellValue other) {
        if (value != null && other.value != null) {
            return value.compareTo(other.getValue());
        }

        if (value == null && other.value == null) {
            return 0;
        }

        int result = value == null ? -1 : 1;
        if (nullIsMax) {
            result = -result;
        }

        return result;
    }

}
