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
import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import com.dbschools.quickquiz.client.giver.QuizCreationOptions;

/**
 * Maintains all of the information about a quiz, including giver name, 
 * takers and notices.
 * 
 * @author David C. Briccetti
 */
public final class Quiz implements Serializable {
    private static final long serialVersionUID = 2332397917450744724L;

    /**
     * All of the player scores are to be reset
     */
    public static final int RESET_SCORES = 1;

    /**
     * All of the takers' last responses are to be reset
     */
    public static final int RESET_LAST_RESPONSES = 2;

    private final Integer id;
    private final String giverName;
    private final String quizName;
    private final String password;
    
    private Quiz(Integer id, String giverName, String quizName, String password) {
        this.id = id;
        this.giverName = giverName;
        this.quizName = quizName;
        this.password = password;
    }
    
    public Quiz(Integer id, QuizCreationOptions options) {
        this(id, options.getGiverName(), options.getName(), options.getPassword());
    }

    /**
     * Reset the last response and score of all takers in the quiz
     * 
     * @param takers
     *            A collection of takers
     * @param resetOptions
     */
    public static void resetTakers(final Collection<QuizTaker> takers,
            final int resetOptions) {
        for (QuizTaker taker : takers) {
            if ((resetOptions & RESET_LAST_RESPONSES) != 0) {
                taker.setLastResponse("");
            }

            if ((resetOptions & RESET_SCORES) != 0) {
                taker.setScore(0);
            }
        }
    }

    public String getName() {
        return quizName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getPassword() {
        return password;
    }

    public String getGiverName() {
        return this.giverName;
    }

    public Integer getId() {
        return id;
    }

}