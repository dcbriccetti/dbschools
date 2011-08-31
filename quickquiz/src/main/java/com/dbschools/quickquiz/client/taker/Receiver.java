package com.dbschools.quickquiz.client.taker;

import javax.swing.*;

import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.util.Util;
import com.dbschools.quickquiz.msg.*;
import com.dbschools.quickquiz.client.QuizState;

/**
 * JGroups receiver.
 * @author Dave Briccetti
*/
public final class Receiver extends ExtendedReceiverAdapter {
    private final TakerReceivedMessageProcessor rmp;

    public Receiver(TakerReceivedMessageProcessor rmp) {
        this.rmp = rmp;
    }

    @Override public void receive(final Message msg) {
        final Object msgObj = msg.getObject();
        if (msgObj instanceof AbstractBroadcastMsg) {
            rmp.processReceivedMsg(msg);
        }
    }

    @Override
    public void viewAccepted(View view) {
        rmp.processViewAccepted(view);
    }

    @Override
    public void setState(byte[] state) {
        try {
            final QuizState quizState = (QuizState) Util.objectFromByteBuffer(state);
            rmp.processNewQuizState(quizState);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
