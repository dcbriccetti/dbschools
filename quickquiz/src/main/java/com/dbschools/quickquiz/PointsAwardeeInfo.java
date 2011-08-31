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
import java.util.Date;

/**
 * Information about the awarding of points to one Taker
 * 
 * @author David C. Briccetti
 */
public final class PointsAwardeeInfo implements Serializable {
    private static final long serialVersionUID = 70043865791210632L;
    private final QuizTaker taker;
    private final int points;
    private final Date time = new Date();

    public PointsAwardeeInfo(QuizTaker taker, int points) {
        this.taker = taker;
        this.points = points;
    }

    private Date getTime() {
        return time;
    }

    public QuizTaker getTaker() {
        return taker;
    }

    public int getPoints() {
        return points;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PointsAwardeeInfo { taker: " + getTaker() + ", points: " + getPoints() + ", time: " + getTime() + " }";
    }

}
