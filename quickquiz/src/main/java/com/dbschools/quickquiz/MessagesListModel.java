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

package com.dbschools.quickquiz;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * ListModel implementation for the messages JList. It provides the items 
 * in reverse order, so that the most recently added item appears at the top.
 * 
 * @author David C. Briccetti
 */
public final class MessagesListModel extends AbstractListModel {
    private static final long serialVersionUID = 4876545346450681980L;
    private final List<String> messages = new ArrayList<String>(100);

    /**
     * Add an element to the model
     * @param element The String element to add
     */
    public void add(final String element) {
        messages.add(element);
        fireContentsChanged(this, 0, messages.size());
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return messages.size();
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(final int index) {
        return messages.get(index);
    }
}
