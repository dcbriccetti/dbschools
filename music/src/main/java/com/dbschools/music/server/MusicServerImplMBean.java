package com.dbschools.music.server;

import java.util.Date;

public interface MusicServerImplMBean {
    int getNumLogins();
    int getNumGroupAssignments();
    int getNumAssessments();
    Date getLastLoginDate();
    Iterable<String> getLoggedInUserNames();
}
