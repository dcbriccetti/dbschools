package com.dbschools.music;

import com.dbschools.gui.WindowLauncher;
import com.dbschools.music.assess.ui.AssessmentSummariesPanel;
import com.dbschools.music.dao.RemoteDao;

public final class TestMusiciansTester {
    public static void main(String[] args) {
        final RemoteDao remoteDao = (RemoteDao) SpringAccess.getContext()
                        .getBean("remoteDao");
        new WindowLauncher().launch(
                new AssessmentSummariesPanel(remoteDao, 2006), "DBSchools Musician Testing");
    }
}
