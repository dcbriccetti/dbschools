package com.dbschools.music;

import com.dbschools.music.ui.AbstractGroupAndInstrumentFilteredTableModel;
import com.dbschools.music.dao.RemoteSaver;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;

/**
 * MusicianMover changes a musician's connections to groups and instruments.
 * 
 * @author David C. Briccetti
 */
public class MusicianMover {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger(MusicianMover.class);
    private final AbstractGroupAndInstrumentFilteredTableModel musicianTableModel;
    private final RemoteSaver remoteSaver;
    private final Instrument unassignedInstrument;
    
    public MusicianMover(AbstractGroupAndInstrumentFilteredTableModel musicianTableModel,
            RemoteSaver remoteSaver, Instrument unassignedInstrument) {
        super();
        this.musicianTableModel = musicianTableModel;
        this.remoteSaver = remoteSaver;
        this.unassignedInstrument = unassignedInstrument;
    }

    /**
     * Moves or adds the specified musicians to the specified group.
     * @param term the term defining the scope of the move
     * @param movingIntoMusicGroup the group the musicians are to me moved or
     * added to
     * @param musicians the musicians to be changed
     * @param copy whether the musicians are to be added (<code>true</code>)
     * or moved (<code>false</code>) to the specified group
     */
    public void moveSelectedToGroup(
            final Integer term,
            final Group movingIntoMusicGroup, 
            final Iterable<Musician> musicians, boolean copy) {
        final Collection<Integer> terms = termsColl(term);
        
        for (Musician musician : musicians) {
            final Collection<MusicianGroup> allGroupsForThisMusician = 
                    MusicianGroupUtil.getMusicianGroups(musician, terms);
            if (allGroupsForThisMusician.size() > 1) {
                continue;
                //TODO Don't even show this operation in menu if user is in > 1 group in the selected term
            }
            final MusicianGroup firstCurrentMusicianMusicGroup =
                allGroupsForThisMusician.isEmpty() ? null :
                allGroupsForThisMusician.iterator().next();
            
            if (! copy) {
                allGroupsForThisMusician.clear();
                musicianTableModel.removeMusicianFromGroups(
                        term, musician.getId());
            }
            musicianTableModel.addMusicianToGroup(movingIntoMusicGroup.getId(), 
                    term, musician.getId());
            
            final MusicianGroup newMusicianGroup = new MusicianGroup();
            newMusicianGroup.setMusician(musician);
            newMusicianGroup.setGroup(movingIntoMusicGroup);
            newMusicianGroup.setSchoolYear(term);
            setInstrument(firstCurrentMusicianMusicGroup, newMusicianGroup);
            allGroupsForThisMusician.add(newMusicianGroup);
            
            remoteSaver.saveMusicianMusicGroups(term, 
                    musician.getId(), allGroupsForThisMusician);
        }
        musicianTableModel.fireTableDataChanged();
    }

    private void setInstrument(final MusicianGroup firstCurrentMusicianMusicGroup, 
            final MusicianGroup musicianGroup) {
        if (firstCurrentMusicianMusicGroup != null) {
            musicianGroup.setInstrument(
                    firstCurrentMusicianMusicGroup.getInstrument());
            musicianGroup.setInstrumentRanking(
                    firstCurrentMusicianMusicGroup.getInstrumentRanking());
        } else {
            musicianGroup.setInstrument(unassignedInstrument);
            musicianGroup.setInstrumentRanking(0);
        }
    }

    /**
     * Changes the specified musicians to the specified instrument,
     * if the musician is only in one group.
     * @param term the term defining the scope of the move
     * @param movingIntoInstrument the instrument to which the musicians are
     * to be set
     * @param musicians the musicians to be changed
     */
    public void moveSelectedToInstrument(
            final Integer term,
            final Instrument movingIntoInstrument, 
            final Iterable<Musician> musicians) {
        
        final Collection<Integer> terms = termsColl(term);
        
        for (Musician musician : musicians) {
            final Collection<MusicianGroup> allGroupsForThisMusician =
                    MusicianGroupUtil.getMusicianGroups(musician, terms);
            if (allGroupsForThisMusician.size() == 1) {
                final MusicianGroup musicianGroup = 
                        allGroupsForThisMusician.iterator().next();
                musicianGroup.setInstrument(movingIntoInstrument);
                remoteSaver.saveMusicianMusicGroups(term, 
                        musician.getId(), allGroupsForThisMusician);
                musicianTableModel.setInstrument(term, musician.getId(), 
                        musicianGroup.getInstrument());
            }
        }
        musicianTableModel.fireTableDataChanged();
    }

    private Collection<Integer> termsColl(final Integer term) {
        Collection<Integer> terms = new ArrayList<Integer>();
        terms.add(term);
        return terms;
    }
}