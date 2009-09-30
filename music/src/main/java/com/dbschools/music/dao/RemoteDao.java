package com.dbschools.music.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.dbschools.music.orm.*;
import com.dbschools.music.assess.SummaryRecord;
import com.dbschools.music.GroupTerm;
import com.dbschools.music.events.EventObserver;
import com.google.common.collect.Multimap;

/**
 * @author Dave Briccetti
 */
public interface RemoteDao extends RemoteSaver {
    @SuppressWarnings("unchecked")
    Collection<Instrument> getInstruments();

    Collection<Piece> getMusicPieces();

    @SuppressWarnings("unchecked")
    Collection<Integer> getSchoolYears();

    Collection<SummaryRecord> getSummaryRecords(Collection<Integer> ids);

    Collection<SummaryRecord> getSummaryRecords(boolean refresh, 
            Collection<Integer> groupIdList, Collection<Integer> instrumentIdList);

    @SuppressWarnings("unchecked")
    Collection<PredefinedComment> getComments();

    Map<Integer, String> getCommentTextMap();

    Map<Integer, Integer> getCommentsCounts();

    @SuppressWarnings("unchecked")
    Collection<RejectionReason> getRejectionReasons();

    Musician getMusicianAndAssessmentInfo(int musicianId);

    @SuppressWarnings("unchecked")
    Collection<MusicianGroup> getMusicianGroups();

    Multimap<Integer,MusicianGroup> getMusicianGroupsMap();

    Collection<Musician> getMusicians(boolean refresh, boolean currentTermOnly);

    Collection<Group> getGroups();

    Map<GroupTerm, Set<Integer>> getGroupTermMemberIdsMap(boolean refresh);

    Map<Integer,Musician> getMusiciansMap(boolean refresh, boolean currentTermOnly);

    Map<Integer,Piece> getPiecesMap();

    boolean addEventObserver(EventObserver eventObserver);

    User getUser();
}
