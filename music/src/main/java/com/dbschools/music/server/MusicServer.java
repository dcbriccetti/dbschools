package com.dbschools.music.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Criterion;

import com.dbschools.DatabaseAccessException;
import com.dbschools.NoSuchUserPasswordException;
import com.dbschools.music.ClientSession;
import com.dbschools.music.GroupTerm;
import com.dbschools.music.assess.SummaryRecord;
import com.dbschools.music.events.Event;
import com.dbschools.music.orm.AbstractPersistentObject;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.PredefinedComment;
import com.dbschools.music.orm.RejectionReason;

/**
 * A server for the music application.
 * @author David C. Briccetti
 */
public interface MusicServer extends Remote {
    /**
     * Logs in to the server.
     * @param databaseName
     * @param userName
     * @param password
     * @return the ClientSession
     * @throws RemoteException
     * @throws DatabaseAccessException
     * @throws NoSuchUserPasswordException
     */
    ClientSession logIn(String databaseName, String userName, String password) 
            throws RemoteException, DatabaseAccessException, NoSuchUserPasswordException;

    /**
     * Logs out from the server.
     * @param sessionId
     * @throws RemoteException
     * @throws DatabaseAccessException
     * @throws NoSuchUserPasswordException
     */
    void logOut(int sessionId) throws RemoteException;

    /**
     * Retrieves the next event notification object, waiting if needed.
     * @param sessionId
     * @return
     * @throws RemoteException
     */
    Event getNextEvent(int sessionId) throws RemoteException;
    
    /**
     * Gets the musician with the specified ID.
     *
     * @param sessionId
     * @param musicianId
     * @return musician
     * @throws RemoteException
     */
    Musician getMusician(int sessionId, int musicianId) throws RemoteException;
    
    /**
     * Gets all musicians.
     * @param sessionId
     * @param currentYearOnly TODO
     * @return musicians
     * @throws RemoteException
     */
    Collection<Musician> getMusicians(int sessionId, boolean currentYearOnly) throws RemoteException;

    /**
     * Gets assessment information for the musicians with the IDs specified.
     * @param sessionId
     * @param musicianIds the IDs of the musicians
     * @return assessment information
     * @throws RemoteException
     */
    Collection<Musician> getAssessmentInfo(int sessionId, 
            Collection<Integer> musicianIds) throws RemoteException;
    
    /**
     * Gets a map, keyed on GroupTerm, of the IDs of the musicians
     * in the group.
     * @param sessionId
     * @return may of group members
     * @throws RemoteException
     */
    Map<GroupTerm, Set<Integer>> getGroupTermMemberIdsMap(int sessionId) throws RemoteException;

    /**
     * Returns entities of the specified class matching the specified
     * criteria. These entities can be classes such as {@link PredefinedComment}, 
     * {@link Instrument}, and {@link RejectionReason}s.
     * @param sessionId
     * @param entityClass the class of a database-mapped entity bean
     * @param criterionColl criteria for selecting entities
     * @return matching entities
     * @throws RemoteException
     */
    <T> Collection<T> getEntities(int sessionId, 
            Class<T> entityClass, Collection<Criterion> criterionColl) throws RemoteException;
    
    /**
     * Gets all music pieces.
     * @param sessionId
     * @return music pieces
     * @throws RemoteException
     */
    Collection<Piece> getMusicPieces(int sessionId) throws RemoteException;
    
    /**
     * Gets all summary records for the musicians specified.
     * @param sessionId
     * @param musicianIdList TODO
     * @return summary records
     * @throws RemoteException
     */
    public Collection<SummaryRecord> getSummaryRecords(int sessionId, Collection<Integer> musicianIdList) throws RemoteException;

    /**
     * Gets all summary records for musicians in the specified list of groups
     * on the specified list of instruments.
     * @param sessionId
     * @param groupIdList a list of group IDs, or null if not limiting on groups
     * @param instrumentIdList a list of instrument IDs, or null if not limiting on instruments
     * @param currentTermOnly TODO
     * @return summary records
     * @throws RemoteException
     */
    public Collection<SummaryRecord> getSummaryRecords(int sessionId, Collection<Integer> groupIdList,
            Collection<Integer> instrumentIdList, boolean currentTermOnly) throws RemoteException;

    /**
     * Gets a map of comment frequency counts, keyed on comment ID. For example, 
     * if comment ID 25 were used 1000 times, the map would contain a key/value pair
     * of (25, 1000).
     * @param sessionId
     * @return map of counts of comments
     * @throws RemoteException
     * @throws SQLException
     */
    Map<Integer, Integer> getCommentCounts(int sessionId) throws RemoteException, SQLException;

    /**
     * Saves a musician, and sets all the groups it belongs to, and with
     * which instrument.
     * @param sessionId
     * @param termId the term defining the scope of the operation
     * @param allGroupsForThisMusician
     * @throws RemoteException
     */
    void saveMusicianMusicGroups(int sessionId,
            int termId, Collection<MusicianGroup> allGroupsForThisMusician) 
            throws RemoteException;

    /**
     * Creates a new musician and sets his music groups.
     * @param sessionId
     * @param termId the term defining the scope of the operation
     * @param musician
     * @param allGroupsForThisMusician
     * @throws RemoteException 
     */
    void saveNewMusicianAndMusicGroups(int sessionId, int termId, Musician musician, 
            Collection<MusicianGroup> allGroupsForThisMusician) throws RemoteException;

    /**
     * Saves an object.
     * @param sessionId
     * @param object
     * @return the key of the saved object
     * @throws RemoteException
     */
    Serializable saveObject(int sessionId, Object object) throws RemoteException;
    
    /**
     * Updates an object.
     * @param sessionId
     * @param object
     * @throws RemoteException
     */
    void updateObject(int sessionId, Object object) throws RemoteException;
    
    /**
     * Deletes an object.
     * @param sessionId
     * @param object
     * @throws RemoteException
     */
    void deleteObject(int sessionId, Object object) throws RemoteException;
    
    /**
     * Gets a collection of terms for which musicians are assigned to groups.
     * @param sessionId
     * @return collection of terms
     */
    Collection<Integer> getSchoolYears(int sessionId) throws RemoteException;

}
