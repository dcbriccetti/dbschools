package com.dbschools.music;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.dbschools.DatabaseAccessException;
import com.dbschools.NoSuchUserPasswordException;
import com.dbschools.music.orm.*;
import com.dbschools.music.server.MusicServer;
import com.dbschools.music.server.MusicServerProxyFactory;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import org.junit.Before;

public class TestMusicServer {
    private final MusicServerProxyFactory musicServerProxyFactory = new MusicServerProxyFactory("localhost");
    private MusicServer ms;
    private ClientSession clientSession;
    private int sessionId;
    
    @Before public void setUp() throws RemoteException, NotBoundException, DatabaseAccessException, NoSuchUserPasswordException {
        ms = musicServerProxyFactory.getInstance();
        clientSession = ms.logIn("dbsmusic-demo", "jdoe", "password");
        sessionId = clientSession.getSessionId();
    }

    @Test public void saveAssessment() throws RemoteException, NotBoundException, DatabaseAccessException, NoSuchUserPasswordException {
        Assessment a = new Assessment();
        a.setAssessmentTime(new Date());
        a.setUser(clientSession.getUser());
        a.setMusicInstrument(ms.getEntities(sessionId, Instrument.class, null).iterator().next());
        a.setMusician(ms.getEntities(sessionId, Musician.class, null).iterator().next());
        a.setMusicPiece(ms.getEntities(sessionId, Piece.class, null).iterator().next());

        Set<PredefinedComment> cmts = new HashSet<PredefinedComment>();
        cmts.add(ms.getEntities(sessionId, PredefinedComment.class, null).iterator().next());
        a.setPredefinedComments(cmts);
              
        ms.saveObject(sessionId, a);
    }
    
    @Test public void testGetMusician() throws RemoteException, NotBoundException, DatabaseAccessException, NoSuchUserPasswordException {
        Musician musician = ms.getMusician(sessionId, 100514);
        assertNotNull(musician);
        Set<Assessment> assessments = musician.getAssessments();
        assertNotNull(assessments);
    }
    
    @Test public void testMoveMusician() throws RemoteException, NotBoundException, DatabaseAccessException, NoSuchUserPasswordException {
        List<Group> musicGroups = new ArrayList<Group>(ms.getEntities(sessionId, Group.class, null));
        List<Instrument> instruments = new ArrayList<Instrument>(
                ms.getEntities(sessionId, Instrument.class, null));
        final Integer schoolYear = TermUtils.getCurrentTerm();
        Musician musician = new Musician(1L, "Sue", "Jones", schoolYear, "F");
        MusicianGroup mg = new MusicianGroup(musician, schoolYear, musicGroups.get(0), instruments.get(0));
        ArrayList<MusicianGroup> mgs = new ArrayList<MusicianGroup>();
        mgs.add(mg);
        ms.saveNewMusicianAndMusicGroups(sessionId, schoolYear, musician, mgs);
        
        List<Musician> musicians = new ArrayList<Musician>(ms.getMusicians(sessionId, true));
        mgs.clear();
        mgs.add(new MusicianGroup(musicians.get(0), schoolYear, musicGroups.get(1), instruments.get(1)));
        ms.saveMusicianMusicGroups(sessionId, schoolYear, mgs);
        ms.logOut(sessionId);
    }
    
    @Test(expected=NoSuchUserPasswordException.class) 
    public void testBadPasswordIsRejected() throws RemoteException, NotBoundException, DatabaseAccessException, NoSuchUserPasswordException {
        MusicServer ms = musicServerProxyFactory.getInstance();
        ms.logIn("dbsmusic-demo", "admin", "wrong");
    }
    
}
