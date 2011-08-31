package com.dbschools.quickquiz.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;

import org.jgroups.Address;
import com.dbschools.quickquiz.QuizTaker;

/**
 * The current state of a quiz.
 * @author Dave Briccetti
 */
public class QuizState implements Serializable {
    private final Map<Address, QuizTaker> takers = new ConcurrentHashMap<Address, QuizTaker>();
    private boolean chatEnabled = true;
    private String question;

    public Map<Address, QuizTaker> getTakers() {
        return takers;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
