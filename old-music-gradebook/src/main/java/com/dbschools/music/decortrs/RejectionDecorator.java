package com.dbschools.music.decortrs;

import com.dbschools.music.orm.Rejection;
import com.dbschools.music.orm.RejectionReason;
import com.dbschools.music.orm.User;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Wraps a rejection for display formatting.
 * @author Dave Briccetti
 */
public class RejectionDecorator {
    private final Rejection rejection;
    private static DateFormat dateFormatNormal = new SimpleDateFormat(
            Constants.DATE_TIME_FORMAT);

    public RejectionDecorator(Rejection rejection) {
        this.rejection = rejection;
    }

    public User getUser() {
        return rejection.getUser();
    }

    public String getRejectionTime() {
        return dateFormatNormal.format(rejection.getRejectionTime());
    }

    public RejectionReason getRejectionReason() {
        return rejection.getRejectionReason();
    }
    
}
