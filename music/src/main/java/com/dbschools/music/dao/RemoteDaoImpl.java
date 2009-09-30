package com.dbschools.music.dao;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dbschools.DatabaseAccessException;
import com.dbschools.NoSuchUserPasswordException;
import com.dbschools.music.ClientSession;
import com.dbschools.music.GroupTerm;
import com.dbschools.music.assess.SummaryRecord;
import com.dbschools.music.events.Event;
import com.dbschools.music.events.EventObserver;
import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.PredefinedComment;
import com.dbschools.music.orm.RejectionReason;
import com.dbschools.music.orm.Tempo;
import com.dbschools.music.orm.User;
import com.dbschools.music.server.MusicServer;
import com.dbschools.music.server.MusicServerProxyFactory;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.*;

/**
 * A data access object for retrieving information from the database via RMI.
 * 
 * @author David C. Briccetti
 */
public final class RemoteDaoImpl implements RemoteDao {
    private static final Logger log = Logger.getLogger(RemoteDao.class);
    private final MusicServerProxyFactory musicServerProxyFactory;
    private MusicServer musicServer;
    private int sessionId;
    private User user;
    private Collection<Instrument> instruments;
    private Collection<Piece> pieces;
    private RemoteSaver remoteSaver;
    private Collection<PredefinedComment> predefinedComments;
    private Map<Integer, String> commentTextMap;
    private Map<Integer, Integer> commentCounts;
    private Collection<RejectionReason> rejectionReasons;
    private Collection<Group> groups;
    private Collection<Musician> musicians;
    private Map<GroupTerm, Set<Integer>> groupTermMemberIdsMap;
    private Map<Integer, Musician> musiciansMap;
    private Collection<MusicianGroup> musicianGroups;
    private Collection<Integer> terms;
    private Multimap<Integer, MusicianGroup> musicianMusicGroupsMap;
    private final Set<EventObserver> eventObservers = 
        Collections.synchronizedSet(new HashSet<EventObserver>());
    
    public RemoteDaoImpl(final MusicServerProxyFactory musicServerProxyfactory) {
        this.musicServerProxyFactory = musicServerProxyfactory;
    }

    public boolean addEventObserver(EventObserver eventObserver) {
        return eventObservers.add(eventObserver);
    }
    
    public boolean removeEventObserver(EventObserver eventObserver) {
        return eventObservers.remove(eventObserver);
    }
    
    /**
     * Clears the cache of all previously-retrieved data.
     */
    public void clearCache() {
        instruments = null;
        pieces = null;
        predefinedComments = null;
        commentCounts = null;
        musicians = null;
        rejectionReasons = null;
        groups = null;
        musicians = null;
        groupTermMemberIdsMap = null;
        musiciansMap = null;
        musicianGroups = null;
        musicianMusicGroupsMap = null;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<Instrument> getInstruments() {
        if (instruments == null) {
            try {
                instruments = new TreeSet<Instrument>(
                        musicServer.getEntities(
                                sessionId, Instrument.class, null));
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return instruments;
    }
    
    public Collection<Piece> getMusicPieces() {
        if (pieces == null) {
            try {
                pieces = musicServer.getMusicPieces(sessionId);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return pieces;
    }

    @SuppressWarnings("unchecked")
    public Collection<Integer> getSchoolYears() {
        if (terms == null) {
            try {
                terms = musicServer.getSchoolYears(sessionId);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return terms;
    }
    
    public Collection<SummaryRecord> getSummaryRecords(Collection<Integer> ids) {
        try {
            return musicServer.getSummaryRecords(sessionId, ids);
        } catch (RemoteException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public Collection<SummaryRecord> getSummaryRecords(boolean refresh, 
            Collection<Integer> groupIdList, Collection<Integer> instrumentIdList) {
        try {
            return musicServer.getSummaryRecords(sessionId, groupIdList, instrumentIdList, true);
        } catch (RemoteException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
    

    @SuppressWarnings("unchecked")
    public Collection<PredefinedComment> getComments() {
        if (predefinedComments == null) {
            try {
                predefinedComments = musicServer.getEntities(sessionId, PredefinedComment.class, null);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return predefinedComments;
    }

    /**
     * Return a map of predefined comment IDs to their text values.
     * @return map of IDs to text values
     */
    public Map<Integer, String> getCommentTextMap() {
        if (commentTextMap == null) {
            commentTextMap = new HashMap<Integer, String>();
            for (PredefinedComment pc : getComments()) {
                commentTextMap.put(pc.getId(), pc.getText());
            }
        }
        return commentTextMap;
    }
    
    /**
     * @return a map of comment id, frequency pairs
     */
    public Map<Integer, Integer> getCommentsCounts() {
        if (commentCounts == null) {
            try {
                commentCounts = musicServer.getCommentCounts(sessionId);
            } catch (Exception e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return commentCounts;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<RejectionReason> getRejectionReasons() { 
        if (rejectionReasons == null) {
            try {
                rejectionReasons = musicServer.getEntities(sessionId, RejectionReason.class, null);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return rejectionReasons;
    }

    /**
     * @param musicianId ID of the musician
     * @return the musician object, with all assessment info connected to it
     */
    public Musician getMusicianAndAssessmentInfo(int musicianId) {
        try {
            return musicServer.getAssessmentInfo(sessionId, 
                    Arrays.asList(musicianId)).iterator().next();
        } catch (RemoteException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Collection<MusicianGroup> getMusicianGroups() {
        if (musicianGroups == null) {
            try {
                musicianGroups = musicServer.getEntities(sessionId, MusicianGroup.class, null);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return musicianGroups;
    }

    /**
     * @return a map, keyed by musician ID, of {@link MusicianGroup} objects
     * for the musician
     */
    public Multimap<Integer, MusicianGroup> getMusicianGroupsMap() {
        if (musicianMusicGroupsMap == null) {
            final TreeMultimap<Integer, MusicianGroup> newMap = Multimaps.newTreeMultimap();

            for (MusicianGroup mmg : getMusicianGroups()) {
                newMap.put(mmg.getMusician().getId(), mmg);
            }
            musicianMusicGroupsMap = Multimaps.unmodifiableMultimap(newMap);
        }
        return musicianMusicGroupsMap;
    }
    
    public Collection<Musician> getMusicians(boolean refresh, boolean currentTermOnly) {
        if (refresh || musicians == null) {
            try {
                musicians = musicServer.getMusicians(sessionId, currentTermOnly);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return musicians;
    }

    public Collection<Group> getGroups() {
        if (groups == null) {
            try {
                groups = musicServer.getEntities(sessionId, Group.class, null);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return groups;
    }

    public Map<GroupTerm, Set<Integer>> getGroupTermMemberIdsMap(boolean refresh) {
        if (refresh || groupTermMemberIdsMap == null) {
            try {
                groupTermMemberIdsMap = musicServer.getGroupTermMemberIdsMap(sessionId);
            } catch (RemoteException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        return groupTermMemberIdsMap;
    }

    /**
     * @param refresh 
     * @param currentTermOnly include only musicians in groups this term
     * @return a map of {@link Musician} objects, keyed by musician ID
     */
    public Map<Integer,Musician> getMusiciansMap(boolean refresh, boolean currentTermOnly) {
        if (refresh || musiciansMap == null) {
            musiciansMap = new HashMap<Integer, Musician>();
            for (Musician m : getMusicians(refresh, currentTermOnly)) {
                musiciansMap.put(m.getId(), m);
            }
        }
        return musiciansMap;
    }

    /**
     * @return a map of {@link Piece} objects, keyed by piece ID
     */
    public Map<Integer,Piece> getPiecesMap() {
        Map<Integer,Piece> piecesMap = new HashMap<Integer,Piece>(); 
        for (Piece mp : getMusicPieces()) {
            piecesMap.put(mp.getId(), mp);
        }
        return piecesMap;
    }

    public void logIn(String databaseName, String login, String password) 
            throws RemoteException, NotBoundException, 
            DatabaseAccessException, NoSuchUserPasswordException {
        
        if (StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(password)) {
            musicServer = musicServerProxyFactory.getInstance();
            ClientSession clientSession = musicServer.logIn(databaseName, login, password);
            sessionId = clientSession.getSessionId();
            user = clientSession.getUser();
            remoteSaver = new CommonSwingDao(musicServer, sessionId);
            createEventNotifier();
        }
    }

    private void createEventNotifier() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) { // TODO add a flag so we can stop
                                    // normally
                        final Event event = musicServer.getNextEvent(sessionId);
                        log.debug(event);
                        
                        if (event.getDetails() instanceof Musician || event.getDetails() instanceof MusicianGroup
                                || event.getDetails() instanceof Group) {
                            log.info("Clearing musician and group caches");
                            groups = null;
                            musicians = null;
                            musicianGroups = null;
                            musicianMusicGroupsMap = null;
                            groupTermMemberIdsMap = null;
                        } else if (event.getDetails() instanceof Instrument) {
                            instruments = null;
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                synchronized(eventObservers) {
                                    log.info("Notifying " + eventObservers.size() + " observers");
                                    for (EventObserver eo : eventObservers) {
                                        eo.notify(event);
                                    }
                                }
                            }});
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, "Event Notifier").start();
    }
    
    public void logOut() throws RemoteException {
        musicServer.logOut(sessionId);
    }

    public void delete(Object object) {
        clearCache();
        remoteSaver.delete(object);
    }

    public void save(Object object) {
        clearCachesIfNeeded(object);
        remoteSaver.save(object);
    }

    public void update(Object object) {
        clearCachesIfNeeded(object);
        remoteSaver.update(object);
    }

    private void clearCachesIfNeeded(Object object) {
        if (object instanceof MusicianGroup) {
            musicianGroups = null;
        } else if (object instanceof PredefinedComment) {
            predefinedComments = null;
        } else if (object instanceof Tempo || object instanceof Piece) {
            pieces = null;
        }
    }

    public void saveMusicianMusicGroups(int termId,
            int musicianId, Collection<MusicianGroup> allGroupsForThisMusician) {
        remoteSaver.saveMusicianMusicGroups(termId, 
                musicianId, allGroupsForThisMusician);
    }

    public void saveNewMusicianAndMusicGroups(final int termId, Musician musician, 
            Set<MusicianGroup> allGroupsForThisMusician) {
        remoteSaver.saveNewMusicianAndMusicGroups(termId,
                musician, allGroupsForThisMusician);
    }

    public User getUser() {
        return user;
    }


}