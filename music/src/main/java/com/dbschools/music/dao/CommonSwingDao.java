package com.dbschools.music.dao;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.server.MusicServer;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Data access object for saving data asynchronously, and having a callback
 * on the Swing thread when complete.
 * 
 * @author David C. Briccetti
 */
public final class CommonSwingDao implements RemoteSaver {
    private static final Logger log = Logger.getLogger(CommonSwingDao.class);
    private final MusicServer musicServer;
    private final int sessionId;
    private final ExecutorService exec = Executors.newFixedThreadPool(5);
    private final Component msgParent;

    public CommonSwingDao(MusicServer musicServer, int sessionId, Component msgParent) {
        this.msgParent = msgParent;
        if (musicServer == null) {
            throw new IllegalArgumentException("musicServer is null");
        }
        this.musicServer = musicServer;
        if (sessionId == 0) {
            throw new IllegalArgumentException("sessionId is 0");
        }
        this.sessionId = sessionId;
    }

    private void assignId(final Integer id, Object object) 
            throws IllegalAccessException, InvocationTargetException, 
            NoSuchMethodException {
        PropertyUtils.setProperty(object, "id", id);
    }
    
    @Override
    public void save(final Object object) {
        exec.execute(new Runnable(){
            @Override public void run() {
                try {
                    Integer id = (Integer) musicServer.saveObject(sessionId, object);
                    if (id != null) {
                        assignId(id, object);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e);
                    JOptionPane.showMessageDialog(msgParent, e.getMessage(), "Error Saving",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    @Override
    public void update(final Object object) {
        exec.execute(new Runnable(){
            @Override public void run() {
                try {
                    musicServer.updateObject(sessionId, object);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    log.error(e);
                    JOptionPane.showMessageDialog(msgParent, e.getMessage(), "Error Saving",
                            JOptionPane.ERROR_MESSAGE);
                }
            }});
    }

    @Override
    public void delete(final Object object) {
        exec.execute(new Runnable() {
            @Override public void run() {
                try {
                    musicServer.deleteObject(sessionId, object);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    log.error(e);
                    JOptionPane.showMessageDialog(msgParent, e.getMessage(), "Error Saving",
                            JOptionPane.ERROR_MESSAGE);
                }
            }});
    }

    @Override
    public void saveMusicianMusicGroups(final int termId,
            final int musicianId, final Collection<MusicianGroup> allGroupsForThisMusician) {
        exec.execute(new Runnable() {
            @Override public void run() {
                try {
                    musicServer.saveMusicianMusicGroups(
                            sessionId, termId, allGroupsForThisMusician);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e);
                    JOptionPane.showMessageDialog(msgParent, e.getMessage(), "Error Saving",
                            JOptionPane.ERROR_MESSAGE);
                }
            }});
    }

    @Override
    public void saveNewMusicianAndMusicGroups(final int termId,
            final Musician musician, final Set<MusicianGroup> allGroupsForThisMusician) {
        exec.execute(new Runnable() {
            @Override public void run() {
                try {
                    musicServer.saveNewMusicianAndMusicGroups(sessionId, termId, musician, 
                            allGroupsForThisMusician);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e);
                    JOptionPane.showMessageDialog(msgParent, e.getMessage(), "Error Saving",
                            JOptionPane.ERROR_MESSAGE);
                }
            }});
    }
}
