package com.dbschools.music.assess.ui;

import com.dbschools.music.dao.RemoteDao;
import java.awt.Dimension;

import javax.swing.BorderFactory;

import com.dbschools.gui.WindowLauncher;
import com.dbschools.music.NonPersistentPreferences;
import com.dbschools.music.orm.Musician;

/**
 * Launches a student details window for the specified musician.
 * 
 * @author David C. Briccetti
 */
public final class StudentDetailsLauncher extends WindowLauncher {
    private final RemoteDao remoteDao;
    private final NonPersistentPreferences nonPersistentPreferences = 
            new NonPersistentPreferences(); 
    
    public StudentDetailsLauncher(final RemoteDao remoteDao) {
        this.remoteDao = remoteDao;
    }

    public void launch(Musician musician) {
        final StudentDetailsPanel shp = new StudentDetailsPanel(remoteDao,
                musician, nonPersistentPreferences);
        shp.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        shp.setPreferredSize(new Dimension(600,500));
        launch(shp, musician.getName() + " - DBSchools Music Gradebook: Student Details");
    }

}