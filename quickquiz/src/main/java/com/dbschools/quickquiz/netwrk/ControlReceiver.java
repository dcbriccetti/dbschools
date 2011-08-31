package com.dbschools.quickquiz.netwrk;

import java.util.Map;

import org.jgroups.Address;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.util.Util;

import com.dbschools.quickquiz.Quiz;
import com.dbschools.quickquiz.msg.QuizReadyMsg;

final class ControlReceiver extends ExtendedReceiverAdapter {
    private final Map<Address, Quiz> quizzes;
    
    public ControlReceiver(Map<Address, Quiz> quizzes) {
        super();
        this.quizzes = quizzes;
    }

    @Override
    public byte[] getState() {
        try {
            return Util.objectToByteBuffer(quizzes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setState(byte[] state) {
        quizzes.clear();
        try {
            quizzes.putAll((Map<Address, Quiz>) Util.objectFromByteBuffer(state));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receive(Message msg) {
        final Object obj = msg.getObject();
        if (obj instanceof QuizReadyMsg) {
            quizzes.put(msg.getSrc(), ((QuizReadyMsg) obj).getQuiz());
        }
    }

    @Override
    public void viewAccepted(View view) {
        com.dbschools.quickquiz.netwrk.Util.removeMapEntriesForLeftSources(view, quizzes);
    }
}