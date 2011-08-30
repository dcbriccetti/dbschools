package com.dbschools.music.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.dbschools.music.GroupTerm;
import com.dbschools.music.TermUtils;
import com.dbschools.music.assess.SummaryRecord;
import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.orm.Piece;
import java.util.HashMap;

/**
 * Various static helper methods.
 * @author David C. Briccetti
 */
public class Helper {

    static Collection<Integer> musiciansMatchingGroupAndInstrument(SessionFactory sessionFactory,
            Collection<Integer> groupIdList, Collection<Integer> instrumentIdList, 
            boolean currentTermOnly) {
        
        final Collection<Integer> results = new HashSet<Integer>();
        if ((groupIdList != null && groupIdList.isEmpty()) ||
                (instrumentIdList != null && instrumentIdList.isEmpty())) {
            // An empty criteria list means no matches
            return results;
        }
        
        final Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(MusicianGroup.class);
        if (groupIdList != null) {
            criteria.add(Restrictions.in("group.id", groupIdList));
        }
        if (instrumentIdList != null) {
            criteria.add(Restrictions.in("instrument.id", instrumentIdList));
        }
        if (currentTermOnly) {
            criteria.add(Restrictions.eq("schoolYear", TermUtils.getCurrentTerm()));
        }
        criteria.setFetchMode("musicianGroups", FetchMode.JOIN);
        List<MusicianGroup> list = criteria.list();
        for (MusicianGroup mg : list) {
            results.add(mg.getMusician().getId());
        }
        session.close();
        return new ArrayList<Integer>(results);
    }

    static Collection<Integer> extractMusicianIds(Collection<MusicianGroup> musicianGroups) {
        Collection<Integer> musicianIdList = new HashSet<Integer>(musicianGroups.size());
        for (MusicianGroup mg : musicianGroups) {
            final Integer id = mg.getMusician().getId();
            if (id != null) musicianIdList.add(id);
        }
        return musicianIdList;
    }

    public static Collection<Musician> getMusicians(boolean currentYearOnly,
            final Session session) {
        final Criteria criteria = session.createCriteria(Musician.class);
        criteria.setFetchMode("musicianGroups", FetchMode.JOIN);
        if (currentYearOnly) {
            criteria.createCriteria("musicianGroups").add(Restrictions.eq("schoolYear", 
                    TermUtils.getCurrentTerm())); 
        }
        List list = criteria.list();
        session.close();
        return list;
    }

    @SuppressWarnings("unchecked")
    static Collection<Piece> getMusicPieces(SessionFactory sessionFactory) {
        final Session session = sessionFactory.openSession();
        final SortedSet<Piece> pieces = new TreeSet<Piece>
                (session.createCriteria(Piece.class).list());
        session.close();
        return pieces;
    }

    @SuppressWarnings("unchecked")
    static Collection<SummaryRecord> getSummaryRecords(DatabaseInstance dbi, 
            Collection<Integer> musicianIds) {
        Collection<SummaryRecord> recs = new ArrayList<SummaryRecord>(musicianIds.size());
        if (musicianIds.isEmpty()) {
            return recs;
        }
        final Session session = dbi.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Musician.class);
        criteria.add(Restrictions.in("id", musicianIds));
        criteria.setFetchMode("assessments", FetchMode.JOIN);
        criteria.setFetchMode("rejections", FetchMode.JOIN);
        criteria.setFetchMode("musicianGroups", FetchMode.JOIN);
        final Collection<Musician> musicians = new TreeSet<Musician>(criteria.list());
    
        for (Musician musician : musicians) {
            final Piece nextPiece = dbi.getNextPieceFinder().nextPiece(
                    musician.getAssessments(), null, null);
            recs.add(new SummaryRecord(musician, nextPiece));
        }
        session.close();
        return recs;
    }

    static Set<Integer> getMusicianIdsSetForGroupTerm(
            Map<GroupTerm, Set<Integer>> groupTermMembersMap, GroupTerm term) {
        Set<Integer> set = groupTermMembersMap.get(term);
        if (set == null) {
            set = new HashSet<Integer>();
            groupTermMembersMap.put(term, set);
        }
        return set;
    }

}
