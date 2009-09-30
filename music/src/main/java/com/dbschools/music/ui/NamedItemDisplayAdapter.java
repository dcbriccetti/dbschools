/**
 * 
 */
package com.dbschools.music.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dbschools.music.orm.NamedItem;

/**
 * An adapter for displaying the name of a {@link NamedItem} using a 
 * {@link #toString()} method. NamedItemDisplayAdapters can be added to 
 * UI controls, and the name of the contained NamedItem will be displayed.
 * 
 * @author Dave Briccetti
 */
public class NamedItemDisplayAdapter {

    private final NamedItem namedItem;
    
    public NamedItemDisplayAdapter(NamedItem namedItem) {
        this.namedItem = namedItem;
    }
    
    public NamedItem getNamedItem() {
        return namedItem;
    }
    
    @Override
    public String toString() {
        return namedItem.getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((namedItem == null) ? 0 : namedItem.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NamedItemDisplayAdapter other = (NamedItemDisplayAdapter) obj;
        if (namedItem == null) {
            if (other.namedItem != null)
                return false;
        } else if (!namedItem.equals(other.namedItem))
            return false;
        return true;
    }

    public static Collection<NamedItemDisplayAdapter> getItemList(
            Collection<? extends NamedItem> namedItems) {
        List<NamedItemDisplayAdapter> items = new 
                ArrayList<NamedItemDisplayAdapter>(namedItems.size());
        for (NamedItem item : namedItems) {
            items.add(new NamedItemDisplayAdapter(item));
        }
        return items;
    }
    
}