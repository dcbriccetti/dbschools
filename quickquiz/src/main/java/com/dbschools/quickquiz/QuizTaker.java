/*
 * QuickQuiz
 * Copyright (C) 2005 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.dbschools.quickquiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jgroups.Address;

import com.dbschools.quickquiz.msg.AnswerMsg;

/**
 * Information about a quiz taker, including the name, host address, and last
 * response.
 * 
 * @author David C. Briccetti
 */
public final class QuizTaker 
        implements Serializable, Comparable<QuizTaker>, Cloneable {

    private static final long serialVersionUID = 6029627918958056346L;

    private final String takerName;
    private final Address address;

    private int numQuestionsSent;
    private int score;
    private String lastResponse = "";

    private final List<AnswerMsg> answerHistory = new ArrayList<AnswerMsg>();

    private java.util.Date lastResponseReceivedAt = new java.util.Date();

    /**
     * Creates an instance of the class.
     * @param takerName the name of the person taking the quiz
     * @param address 
     */
    public QuizTaker(String takerName, Address address) {
        this.takerName = takerName;
        this.address = address;
    }

    public int compareTo(QuizTaker o) {
        return takerName.compareTo(o.getTakerName());
    }

    public Address getAddress() {
        return address;
    }

    public String getLastResponse() {
        return lastResponse;
    }

    public Date getLastResponseReceivedAt() {
        return lastResponseReceivedAt;
    }

    public String getTakerName() {
        return takerName;
    }

    public int getScore() {
        return score;
    }

    public void setLastResponse(final String lastResponse) {
        this.lastResponse = lastResponse;
    }

    public void setLastResponseReceivedAt(
            final java.util.Date lastResponseReceivedAt) {
        this.lastResponseReceivedAt = lastResponseReceivedAt;
    }

    public void setScore(final int score) {
        this.score = score;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "QuizTaker { name: " + takerName + ", score: " + score
                + ", hostAddress: " + address.toString() + " }";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((takerName == null) ? 0 : takerName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final QuizTaker other = (QuizTaker) obj;
        if (takerName == null) {
            if (other.takerName != null)
                return false;
        } else if (!takerName.equals(other.takerName))
            return false;
        return true;
    }

}