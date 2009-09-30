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

import javax.swing.JCheckBox;

/**
 * A specialized JCheckBox that associates an ID with the checkbox. 
 */
final class CommentCheckBox extends JCheckBox {

    private static final long serialVersionUID = 5312318165759877548L;
    private int id;

    /**
     * Creates an object, given a string and an ID.
     * 
     * @param string the string for the checkbox
     * @param id the id of the checkbox
     * @param selected whether the checkbox is selected
     */
    public CommentCheckBox(String string, int id, boolean selected) {
        super(string);
        this.id = id;
        setSelected(selected);
    }
    
    /**
     * Gets the ID associated with this checkbox.
     * @return ID
     */
    public int getId() {
        return id;
    }

}
