package com.dbschools.quickquiz.client.giver;

import com.dbschools.quickquiz.client.ReceivedMessageProcessor;
import org.jgroups.Message;

/**
 * Methods needed by the giver JGroups receiver.
 * @author Dave Briccetti
 */
public interface GiverReceivedMessageProcessor extends ReceivedMessageProcessor {
    void processReceivedMsg(Message msg);
}
