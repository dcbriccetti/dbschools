package com.dbschools.music;

import com.dbschools.music.admin.ui.MusicianTableModel;
import java.util.Iterator;

import com.dbschools.music.orm.Musician;

/**
 * An iterator for the musicians currently selected in the musician table.
 * 
 * @author David C. Briccetti
 */
public class SelectedMusicianIterator implements Iterator<Musician>, Iterable<Musician> {
    private final int[] selectedRows;
    private final MusicianTableModel musicianTableModel;
    private int nextIndex = 0;
    
    public SelectedMusicianIterator(final int[] selectedRows,
            final MusicianTableModel musicianTableModel) {
        super();
        this.selectedRows = selectedRows;
        this.musicianTableModel = musicianTableModel;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return nextIndex < selectedRows.length;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Musician next() {
        return musicianTableModel.getRowAt(selectedRows[nextIndex++]);
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Musician> iterator() {
        return this;
    }

}