package com.dbschools.quickquiz.client.taker;

import com.dbschools.quickquiz.client.ReceivedMessageProcessor;
import com.dbschools.quickquiz.client.QuizState;
import org.jgroups.Message;

/**
 * Methods needed by the taker JGroups receiver.
 * @author Dave Briccetti
 */
public interface TakerReceivedMessageProcessor extends ReceivedMessageProcessor {
    void processNewQuizState(QuizState quizState);
    void processReceivedMsg(Message msg);
}
