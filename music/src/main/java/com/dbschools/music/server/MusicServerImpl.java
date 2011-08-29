package com.dbschools.music.server;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.dbschools.DatabaseAccessException;
import com.dbschools.NoSuchUserPasswordException;
import com.dbschools.music.ClientSession;
import com.dbschools.music.Constants;
import com.dbschools.music.GroupTerm;
import com.dbschools.music.admin.ui.MusicianImportBatch;
import com.dbschools.music.assess.SummaryRecord;
import com.dbschools.music.assess.SummaryRecordDependency;
import com.dbschools.music.decortrs.YearDecorator;
import com.dbschools.music.events.Event;
import com.dbschools.music.events.TypeCode;
import com.dbschools.music.orm.Log;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.User;
import java.math.BigInteger;
import java.util.HashSet;

/**
 * A server implementation for the music application.
 * @author David C. Briccetti
 */
public class MusicServerImpl extends UnicastRemoteObject
        implements MusicServer, MusicServerImplMBean {
    private static final long serialVersionUID = -3517652103010873651L;
    static Logger logger = Logger.getLogger(MusicServerImpl.class);

    private final Map<String,DatabaseInstance> databaseInstances = 
            new ConcurrentHashMap<String, DatabaseInstance>();
    private final MusicianImporter musicianImporter = new MusicianImporter();
    private final Map<Integer,ClientSession> clientSessions = 
            new ConcurrentHashMap<Integer,ClientSession>();

    private int numLogins;
    private int numGroupAssignments;
    private int numAssessments;
    private Date lastLoginDate;

    /**
     * Creates a music server using the RMI Registry on the default port.
     * @param databases a map of databases
     * @throws RemoteException
     */
    public MusicServerImpl(final Map<String,SessionFactory> databases) throws RemoteException {
        this(databases, Registry.REGISTRY_PORT);
    }

    /**
     * Creates a music server using the RMI Registry on the specified port.
     * @param databases a map of databases
     * @param rmiRegistryPort the port to use
     * @throws RemoteException
     */
    public MusicServerImpl(final Map<String,SessionFactory> databases,
            Integer rmiRegistryPort) throws RemoteException {
        for (Entry<String,SessionFactory> e : databases.entrySet()) {
            final String databaseName = e.getKey();
            final SessionFactory sessionFactory = e.getValue();
            DefaultDataCreator.createIfEmpty(sessionFactory);
            databaseInstances.put(databaseName,
                    new DatabaseInstance(sessionFactory, Helper.getMusicPieces(sessionFactory)));
            logger.debug("DatabaseInstance created for " + databaseName);
        }
        final Registry rmiRegistry = LocateRegistry.getRegistry(rmiRegistryPort);
        logger.debug(rmiRegistry + "contains " + Arrays.asList(rmiRegistry.list()));
        rmiRegistry.rebind(Constants.RMI_MUSIC_BIND_NAME, this);
        logger.info("MusicServer created and bound to registry");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName("com.dbschools:type=MusicServer");
            mbs.registerMBean(this, objectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Musician getMusician(int sessionId, Integer musicianId) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final Session session = getHibernateSession(clientSession);
        Musician musician = (Musician) session.load(Musician.class, musicianId);
        session.close();
        return musician;
    }

    @Override
    public Event getNextEvent(int sessionId) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        try {
            logger.debug("Fetching event from " + clientSession);
            return clientSession.dequeueEvent();
        } catch (InterruptedException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Musician> getMusicians(int sessionId, boolean currentYearOnly)
    throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final Session session = getHibernateSession(clientSession);
        return Helper.getMusicians(currentYearOnly, session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Musician> getAssessmentInfo(int sessionId, 
            Collection<Integer> musicianIds) throws RemoteException {
        final Collection<Musician> musicians = new TreeSet<Musician>();
        if (musicianIds != null && musicianIds.size() > 0) {
            final ClientSession clientSession = clientSessions.get(sessionId);
            final Session session = getHibernateSession(clientSession);
            Criteria criteria = session.createCriteria(Musician.class);
            criteria.setFetchMode("assessments", FetchMode.JOIN);
            criteria.setFetchMode("rejections", FetchMode.JOIN);
            criteria.setFetchMode("musicianGroups", FetchMode.JOIN);
            criteria.add(Restrictions.in("id", musicianIds));
            musicians.addAll(criteria.list());
            session.close();
        }
        return musicians;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<GroupTerm,Set<Integer>> getGroupTermMemberIdsMap(int sessionId) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final Session session = getHibernateSession(clientSession);
        final Criteria criteria = session.createCriteria(MusicianGroup.class);
        Collection<MusicianGroup> musicianGroups = criteria.list();
        Map<GroupTerm, Set<Integer>> groupTermMembersMap = 
                new HashMap<GroupTerm, Set<Integer>>();
        for (MusicianGroup mg : musicianGroups) {
            Set<Integer> musicianIdsForGroupTerm = Helper.getMusicianIdsSetForGroupTerm(groupTermMembersMap, 
                    new GroupTerm(mg.getGroup().getId(), mg.getSchoolYear()));
            musicianIdsForGroupTerm.add(mg.getMusician().getId());
        
        }
        session.close();
        return groupTermMembersMap;
    }

    @Override
    public <T> Collection<T> getEntities(
            int sessionId, Class<T> entityClass, 
            Collection<Criterion> criterionColl) 
            throws RemoteException {
        
        final ClientSession clientSession = clientSessions.get(sessionId);
        org.hibernate.classic.Session session = getHibernateSession(clientSession);
        final Criteria criteria = session.createCriteria(entityClass);
        if (criterionColl != null) {
            for (Criterion criterion : criterionColl) {
                criteria.add(criterion);
            }
        }
        final List<T> entities = criteria.list();
        session.close();
        return entities;
    }

    @Override
    public Collection<Piece> getMusicPieces(int sessionId)
            throws RemoteException {
        final DatabaseInstance dbi = databaseInstances.get(clientSessions.get(sessionId).getDatabaseName());
        return Helper.getMusicPieces(dbi.getSessionFactory());
    }

    @Override
    public Collection<SummaryRecord> getSummaryRecords(int sessionId, Collection<Integer> musicianIdList) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final DatabaseInstance dbi = databaseInstances.get(clientSession.getDatabaseName());
        return Helper.getSummaryRecords(dbi, musicianIdList);
    }

    @Override
    public Collection<SummaryRecord> getSummaryRecords(int sessionId, Collection<Integer> groupIdList,
            Collection<Integer> instrumentIdList, boolean currentTermOnly) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final DatabaseInstance dbi = databaseInstances.get(clientSession.getDatabaseName());
        return Helper.getSummaryRecords(dbi, Helper.musiciansMatchingGroupAndInstrument(
                dbi.getSessionFactory(), groupIdList, instrumentIdList, currentTermOnly));
    }
    
    @Override
    public Map<Integer, Integer> getCommentCounts(int sessionId) throws RemoteException, SQLException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final Session session = getHibernateSession(clientSession);
        Transaction transaction = session.beginTransaction();
        final List<Object[]> list = session.createSQLQuery(
                "select predefinedcomments_comment_id, count(predefinedcomments_comment_id) " +
                "from assessment_tag " +
                "group by predefinedcomments_comment_id").list();
        Map<Integer, Integer> commentCounts = new HashMap<Integer,Integer>();
        for (Object[] obj : list) {
            commentCounts.put((Integer) obj[0], ((BigInteger) obj[1]).intValue());
        }
        transaction.commit();
        session.close();
        return commentCounts;
    }

    private void notifyChangedSummaryRecord(final DatabaseInstance dbi,
            final Integer musicianId) {
        final Collection<SummaryRecord> summaryRecords = 
                Helper.getSummaryRecords(dbi, Arrays.asList(musicianId));
        final SummaryRecord summaryRecord = summaryRecords.iterator().next(); // There is only one
        enqueueForAllClients(new Event(TypeCode.UPDATE_OBJECT, summaryRecord));
    }
    
    private void enqueueForAllClients(Event event) {
        synchronized(clientSessions) {
            for (ClientSession cs : clientSessions.values()) {
                cs.enqueueEvent(event);
            }
        }
    }
    
    private void log(ClientSession clientSession, Session session, TypeCode typeCode, String details) {
        session.save(new Log(typeCode, clientSession.getUser().getLogin(), details));
    }

    @Override
    public void saveMusicianMusicGroups(int sessionId, int schoolYear,
            Collection<MusicianGroup> musicianGroups)
            throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final DatabaseInstance dbi = databaseInstances.get(clientSession.getDatabaseName());
        if (musicianGroups.isEmpty()) {
            return;
        }
        
        final Session session = getHibernateSession(clientSession);
        Transaction transaction = session.beginTransaction();
        session.createQuery("delete MusicianGroup where musician_id in (:musicianIdList) and schoolYear = :schoolYear")
                .setParameterList("musicianIdList", Helper.extractMusicianIds(musicianGroups))
                .setParameter("schoolYear", schoolYear)
                .executeUpdate();

        final TypeCode typeCode = TypeCode.SAVE_MUSICAN_MUSIC_GROUP;

        for (MusicianGroup mmg : musicianGroups) {
            final String msg = mmg.getMusician().getId() + " "
                    + mmg.getMusician().getStudentId() + " "
                    + mmg.getMusician().getName() + " put in "
                    + new YearDecorator(mmg.getSchoolYear()) + " "
                    + mmg.getGroup().getName() + " on "
                    + mmg.getInstrument().getName();
            logger.info(msg);
            session.save(mmg);
            log(clientSession, session, typeCode, msg);
            ++numGroupAssignments;
            enqueueForAllClients(new Event(typeCode, mmg));
        }
        
        transaction.commit();
        session.close();
    }

    @Override
    public void saveNewMusicianAndMusicGroups(int sessionId, int termId, Musician musician,
            Collection<MusicianGroup> allGroupsForThisMusician) throws RemoteException {
       saveObject(sessionId, musician);
       saveMusicianMusicGroups(sessionId, termId, 
               allGroupsForThisMusician);
    }

    @Override
    public Serializable saveObject(int sessionId, Object object) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final DatabaseInstance dbi = databaseInstances.get(clientSession.getDatabaseName());
        final Session session = getHibernateSession(clientSession);
        Transaction transaction = session.beginTransaction();
        Serializable key = null;
        
        if (object instanceof MusicianImportBatch) {
            musicianImporter.importMusicians((MusicianImportBatch) object, session);
        } else {
            key = session.save(object);
        }
        final TypeCode typeCode = TypeCode.SAVE_OBJECT;
        log(clientSession, session, typeCode, object.toString());
        transaction.commit();
        session.close();
        enqueueForAllClients(new Event(typeCode, object));
        processSummaryRecordDependency(object, dbi);
        return key;
    }

    private void processSummaryRecordDependency(Object object,
            final DatabaseInstance dbi) {
        if (object instanceof SummaryRecordDependency) {
            notifyChangedSummaryRecord(dbi, ((SummaryRecordDependency) object).getMusician().getId());
        }
    }

    @Override
    public void updateObject(int sessionId, Object object) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final DatabaseInstance dbi = databaseInstances.get(clientSession.getDatabaseName());
        final Session session = getHibernateSession(clientSession);
        Transaction transaction = session.beginTransaction();
        session.update(object);
        final TypeCode typeCode = TypeCode.UPDATE_OBJECT;
        log(clientSession, session, typeCode, object.toString());
        transaction.commit();
        session.close();
        enqueueForAllClients(new Event(typeCode, object));
        processSummaryRecordDependency(object, dbi);
    }

    @Override
    public void deleteObject(int sessionId, Object object)
            throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final DatabaseInstance dbi = databaseInstances.get(clientSession.getDatabaseName());
        final Session session = getHibernateSession(clientSession);
        Transaction transaction = session.beginTransaction();
        final TypeCode typeCode = TypeCode.DELETE_OBJECT;
        final Set<Integer> invalidSRMusicians = new HashSet<Integer>();
        if (object instanceof Iterable) {
            for (Object elem: (Iterable) object) {
                if (elem instanceof SummaryRecordDependency) {
                    invalidSRMusicians.add(((SummaryRecordDependency) elem).getMusician().getId());
                }
                deleteElement(session, elem, clientSession, typeCode, dbi);
            }
        } else {
            deleteElement(session, object, clientSession, typeCode, dbi);
            processSummaryRecordDependency(object, dbi);
        }
        transaction.commit();
        for (Integer id : invalidSRMusicians) {
            notifyChangedSummaryRecord(dbi, id);
        }
        session.close();
    }

    private void deleteElement(final Session session, Object elem, final ClientSession clientSession, final TypeCode typeCode, final DatabaseInstance dbi) throws HibernateException {
        session.delete(elem);
        log(clientSession, session, typeCode, elem.toString());
        enqueueForAllClients(new Event(typeCode, elem));
    }

    private org.hibernate.classic.Session getHibernateSession(
            final ClientSession clientSession) {
        return databaseInstances.get(clientSession.getDatabaseName()).getSessionFactory().openSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ClientSession logIn(String databaseName, String userName, String password) 
            throws RemoteException, DatabaseAccessException, NoSuchUserPasswordException {
             
        logger.info("User " + userName + " logging in to " + databaseName); 

        final DatabaseInstance dbi = databaseInstances.get(databaseName);
        final SessionFactory sessionFactory = dbi.getSessionFactory();
        if (sessionFactory == null) {
            throw new DatabaseAccessException("Database " + databaseName + " not found");
        }
        final Session session = sessionFactory.openSession();
        List<User> userList = session.createCriteria(User.class).
                add(Restrictions.eq("login", userName)).list();
        try {
            if (userList.isEmpty() || ! StringUtils.equals(userList.get(0).getPassword(), password)) {
                throw new NoSuchUserPasswordException();
            }
            final ClientSession clientSession = new ClientSession(userList.get(0), databaseName);
            clientSessions.put(clientSession.getSessionId(), clientSession);
            ++numLogins;
            lastLoginDate = new Date();
            Transaction transaction = session.beginTransaction();
            log(clientSession, session, TypeCode.LOGIN, null);
            transaction.commit();
            return clientSession;
        } finally {
            session.close();
        }
    }
    
    @Override
    public void logOut(int sessionId) throws RemoteException {
        ClientSession clientSession = clientSessions.get(sessionId);
        final Session session = getHibernateSession(clientSession);
        Transaction transaction = session.beginTransaction();
        log(clientSession, session, TypeCode.LOGOUT, null);
        transaction.commit();
        session.close();
        clientSessions.remove(clientSession.getSessionId());
        logger.info("User " +  clientSession.getUser().getLogin() + " logging out"); 
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Integer> getSchoolYears(int sessionId) throws RemoteException {
        final ClientSession clientSession = clientSessions.get(sessionId);
        final Session session = getHibernateSession(clientSession);
        final List<Integer> terms = session.createQuery("select distinct schoolYear from MusicianGroup").list();
        session.close();
        return terms;
    }

    @Override
    public int getNumLogins() {
        return numLogins;
    }

    @Override
    public Iterable<String> getLoggedInUserNames() {
        Collection<String> names = new ArrayList<String>();
        for (ClientSession cs : clientSessions.values()) {
            final User user = cs.getUser();
            names.add(user.getFirstName() + " " + user.getLastName());
        }
        return names;
    }

    @Override
    public int getNumAssessments() {
        return numAssessments;
    }

    @Override
    public int getNumGroupAssignments() {
        return numGroupAssignments;
    }

    @Override
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

}
