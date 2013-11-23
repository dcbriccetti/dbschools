package com.dbschools.music;

import java.util.ArrayList;
import java.util.Collection;

import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;

public class MusicianGroupUtil {
    public static Collection<MusicianGroup> getMusicianGroups(Musician musician,
            Collection<Integer> terms) {
        Collection<MusicianGroup> groups = new ArrayList<MusicianGroup>();
        if (terms != null) {
            for (MusicianGroup musicianGroup : musician.getMusicianGroups()) {
                if (terms.contains(musicianGroup.getSchoolYear())) {
                    groups.add(musicianGroup);
                }
            }
        }
        return groups;
    }
}