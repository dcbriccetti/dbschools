package com.dbschools.music.dao;

import java.util.Collection;
import java.util.Set;

import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;

/**
 * The RemoteSaver interface contains methods for saving data to a 
 * remote database.
 * 
 * @author David C. Briccetti
 */
public interface RemoteSaver {
    void save(Object object);
    void update(Object object);
    void delete(Object object);
    void saveMusicianMusicGroups(
            int termId, int musicianId, Collection<MusicianGroup> allGroupsForThisMusician);
    void saveNewMusicianAndMusicGroups(final int termId,
            final Musician musician, final Set<MusicianGroup> allGroupsForThisMusician);
}
