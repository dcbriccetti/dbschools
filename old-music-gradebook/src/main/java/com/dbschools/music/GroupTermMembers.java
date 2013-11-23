package com.dbschools.music;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A class for organizing what musicians are in what "GroupTerm"s (a group
 * during a time frame).
 * 
 * @author David C. Briccetti
 */
public class GroupTermMembers {
    private final Map<GroupTerm, Set<Integer>> groupTermMemberIdsMap;

    public GroupTermMembers(Map<GroupTerm, Set<Integer>> groupTermMemberIdsMap) {
        super();
        this.groupTermMemberIdsMap = groupTermMemberIdsMap;
    }

    public Set<Integer> get(GroupTerm key) {
        return groupTermMemberIdsMap.get(key);
    }

    public Set<Integer> put(GroupTerm key, Set<Integer> value) {
        return groupTermMemberIdsMap.put(key, value);
    }

    public Set<Integer> remove(GroupTerm key) {
        return groupTermMemberIdsMap.remove(key);
    }

    public void add(int groupId, int termId, int musicianId) {
        GroupTerm gt = new GroupTerm(groupId, termId);
        Set<Integer> musicianIds = groupTermMemberIdsMap.get(gt);
        if (musicianIds == null) {
            musicianIds = new HashSet<Integer>();
            groupTermMemberIdsMap.put(gt, musicianIds);
        }
        musicianIds.add(musicianId);
    }

    public void remove(int termId, int musicianId) {
        for (Entry<GroupTerm, Set<Integer>> entry : groupTermMemberIdsMap.entrySet()) {
            GroupTerm gt = entry.getKey();
            if (gt.getTermId() == termId) {
                entry.getValue().remove(musicianId);
            }
        }
    }
    
}