package com.dbschools.quickquiz.client.giver;

import javax.swing.*;

import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.util.Util;
import com.dbschools.quickquiz.msg.*;

/**
 * JGroups receiver.
 * @author Dave Briccetti
*/
class Receiver extends ExtendedReceiverAdapter {

    private final GiverReceivedMessageProcessor rmp;

    Receiver(GiverReceivedMessageProcessor rmp) {
        this.rmp = rmp;
    }

    @Override public void receive(final Message msg) {
        final Object msgObj = msg.getObject();
        if (msgObj instanceof AbstractBroadcastMsg) {
            rmp.processReceivedMsg(msg);
        }
    }

    @Override public void viewAccepted(View view) {
        rmp.processViewAccepted(view);
    }

    @Override public byte[] getState() {
        try {
            return Util.objectToByteBuffer(rmp.getQuizState());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
