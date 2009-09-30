package com.dbschools.music.ui;

import com.dbschools.music.*;
import com.dbschools.music.dao.RemoteDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.dbschools.music.events.Event;
import com.dbschools.music.events.EventObserver;
import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import javax.swing.SwingUtilities;

/**
 * A table model for tables whose contents may be filtered on 
 * {@link Group} and {@link Instrument}.
 * @author David C. Briccetti
 */
public abstract class AbstractGroupAndInstrumentFilteredTableModel extends AbstractTableModel {
    private final Logger log = Logger.getLogger(getClass());
    private Collection<Group> groupFilter = Collections.emptyList();
    private Collection<Instrument> instrumentFilter = Collections.emptyList();
    private Collection<Integer> termFilter = Collections.emptyList();
    private GroupTermMembers groupTermMembers;
    private Collection<MusicianGroup> musicianGroups = Collections.emptyList();
    private RemoteDao remoteDao;

    public AbstractGroupAndInstrumentFilteredTableModel(RemoteDao remoteDao) {
        this.remoteDao = remoteDao;
        createNotificationHandler();
        reloadServerData(false);
    }

    protected RemoteDao getRemoteDao() {
        return remoteDao;
    }

    protected void reloadServerData(boolean refresh) {
        musicianGroups = remoteDao.getMusicianGroups();
        groupTermMembers = new GroupTermMembers(remoteDao.getGroupTermMemberIdsMap(refresh));
    }
    
    protected abstract boolean causesRefresh(Event event);
    protected abstract void refresh();
    protected void processNotRefreshCausingEvent(Event event) {
        // Do nothing. Override if needed.
    }
    
    public void setInstrument(int musicianId, int term, Instrument instrument) {
        for (MusicianGroup mg : musicianGroups) {
            if (mg.getMusician().getId().equals(musicianId) 
                    && mg.getSchoolYear() == term) {
                mg.setInstrument(instrument);
            }
        }
    }

    public void removeMusicianFromGroups(int term, int musicianId) {
        groupTermMembers.remove(term, musicianId);
    }
    
    public void addMusicianToGroup(int groupId, int term, int musicianId) {
        groupTermMembers.add(groupId, term, musicianId);
    }

    protected abstract void rebuildFilteredList();

    public void setGroupFilter(Collection<Group> groups) {
        this.groupFilter = groups;
        rebuildFilteredList();
    }

    public void setTermFilter(Collection<Integer> termFilter) {
        this.termFilter = termFilter;
        rebuildFilteredList();
    }

    public void setInstrumentFilter(Collection<Instrument> instruments) {
        this.instrumentFilter = instruments;
        rebuildFilteredList();
    }

    protected Collection<MusicianGroup> getMusicianGroupsInTerms(Musician musician) {
        return MusicianGroupUtil.getMusicianGroups(musician, termFilter);
    }
    
    /**
     * Gets a collection of musician IDs that match the filters in effect, and
     * the GroupTerms to which a musician belongs, and on what instruments.
     * @param musicianId
     * @return whether the musician is included
     */
    protected Collection<Integer> getMatchingMusicianIds() {
        final HashSet<Integer> matchingMusicianIds = new HashSet<Integer>();
        
        for (MusicianGroup mg : musicianGroups) {
            if (groupFilter.contains(mg.getGroup()) 
                    && termFilter.contains(mg.getSchoolYear()) 
                    && instrumentFilter.contains(mg.getInstrument())) {
                matchingMusicianIds.add(mg.getMusician().getId());
            }
        }

        return matchingMusicianIds;
    }

    protected Collection<Integer> getGroupIds() {
        if (groupFilter == null) {
            return null;
        }
        Collection<Integer> groupIds = new ArrayList<Integer>();
        for (Group mg : groupFilter) {
            groupIds.add(mg.getId());
        }
        return groupIds;
    }
    
    protected Collection<Integer> getInstrumentIds() {
        if (instrumentFilter == null) {
            return null;
        }
        Collection<Integer> instrumentIds = new ArrayList<Integer>();
        for (Instrument mg : instrumentFilter) {
            instrumentIds.add(mg.getId());
        }
        return instrumentIds;
    }

    private void createNotificationHandler() {
        final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
        new Thread(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        Event event = eventQueue.take();
                        eventQueue.clear();
                        log.debug("Refreshing model, because of event " + event);
                        refresh();
                    } catch (InterruptedException ex) {
                        log.error(ex);
                    }
                }
            }
        }, "Queue reader").start();
        remoteDao.addEventObserver(new EventObserver() {

            public void notify(Event event) {
                if (causesRefresh(event)) {
                    log.debug("Got event " + event);
                    eventQueue.add(event);
                } else {
                    processNotRefreshCausingEvent(event);
                }
            }
        });
    }

    protected void fireChangeUnderSwing() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableDataChanged();
            }
        });
    }
}
