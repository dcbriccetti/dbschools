package com.dbschools.quickquiz.client;

import com.dbschools.quickquiz.msg.ChatMsg;
import com.dbschools.quickquiz.msg.PointsAwardedMsg;
import com.dbschools.quickquiz.QuizTaker;
import org.jgroups.View;
import org.jgroups.Address;

/**
 * Methods needed by our JGroups receiver.
 * @author Dave Briccetti
 */
public interface ReceivedMessageProcessor {
    QuizState getQuizState();

    void processViewAccepted(View view);
}
