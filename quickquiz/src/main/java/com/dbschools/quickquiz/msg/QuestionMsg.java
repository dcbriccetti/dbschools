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

package com.dbschools.quickquiz.msg;

/**
 * Information about a question asked by the giver.
 * 
 * @author David C. Briccetti
 */
public final class QuestionMsg extends AbstractBroadcastMsg {
    private static final long serialVersionUID = -4770004031948885723L;

    private final String question;
    private final Integer timeLimitSeconds;

    public QuestionMsg(final String question, final Integer timeLimitSeconds) {
        this.question = question;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public String getQuestion() {
        return question;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "QuestionMsg { question: " + question + " }";
    }

}