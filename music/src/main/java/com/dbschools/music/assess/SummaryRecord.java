/*
 * DBSchools
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

package com.dbschools.music.assess;

import com.dbschools.music.*;
import java.io.Serializable;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.dbschools.music.orm.Assessment;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.NoNextPieceIndicator;
import com.dbschools.music.orm.Piece;

/**
 * A summary of a musician's testing, including the number of passes and failures.
 * @author David C. Briccetti
 */
public final class SummaryRecord implements Serializable, Comparable<SummaryRecord> {
    private static final long serialVersionUID = -6412940360137446473L;
    private static final int ONE_HOUR = 1000 * 60 * 60;
    private final Integer musicianId;
    private final String instrument;
    private final Date lastAssessment;
    private final Integer daysSinceLastAssessment;
    private final Integer nextPieceId; // Null means no more pieces
    private final int numAssessments;
    private final int numVisits;
    private final int numPasses;
    private final int numFailures;
    private final int numRejections;
    
    /**
     * Creates a summary record for the specified musician.
     * 
     * @param musician the musician
     * @param nextPiece the next piece the musician is to be tested on
     */
    public SummaryRecord(Musician musician, Piece nextPiece) {
        musicianId = musician.getId();
        SortedSet<Assessment> assessments = 
                new TreeSet<Assessment>(musician.getAssessments());
        numAssessments = assessments.size();
        
        Date lastAssessmentTemp = null;
        int numAssessmentVisits = 0;
        int numPassesCounter = 0;
        int numFailuresCounter = 0;
        Long previousAssTime = null;
        
        for (Assessment ass : assessments) {
            if (ass.isPass()) {
                ++numPassesCounter;
            } else {
                ++numFailuresCounter;
            }
            
            final long assTime = ass.getAssessmentTime().getTime();
            if (lastAssessmentTemp == null || assTime > lastAssessmentTemp.getTime()) {
                lastAssessmentTemp = new Date(ass.getAssessmentTime().getTime());
            }
            
            // If an hour has passed since the last assessment, consider it a new "visit"
            if (previousAssTime == null) {
                ++numAssessmentVisits;
            } else {
                if (assTime > previousAssTime + ONE_HOUR) {
                    ++numAssessmentVisits;
                }
            }
            previousAssTime = assTime;
        }
        
        final Instrument primaryInstrumentForMusician = 
            Utils.primaryInstrumentForMusician(
                    musician.getMusicianGroups());
        instrument = primaryInstrumentForMusician == null ? null : 
            primaryInstrumentForMusician.getName();
        
        numPasses = numPassesCounter;
        numFailures = numFailuresCounter;
        
        if (lastAssessmentTemp != null) {
            daysSinceLastAssessment = (int) (((new Date().getTime()) - 
                    lastAssessmentTemp.getTime()) / (ONE_HOUR * 24));
        } else {
            daysSinceLastAssessment = null;
        }
        
        if (nextPiece != null && ! (nextPiece instanceof NoNextPieceIndicator)) {
            nextPieceId = nextPiece.getId();
        } else {
            nextPieceId = null;
        }
        
        numVisits = numAssessmentVisits + musician.getRejections().size();
        numRejections = musician.getRejections().size();
        lastAssessment = lastAssessmentTemp;
    }

    public Date getLastAssessment() {
        return lastAssessment;
    }
    
    public int getNumAssessments() {
        return numAssessments;
    }

    public int getNumVisits() {
        return numVisits;
    }

    public int getNumFailures() {
        return numFailures;
    }

    public int getNumPasses() {
        return numPasses;
    }

    public Integer getDaysSinceLastAssessment() {
        return daysSinceLastAssessment;
    }

    public Integer getNextPieceId() {
        return nextPieceId;
    }

    public int getNumRejections() {
        return numRejections;
    }

    public String getInstrument() {
        return instrument;
    }

    public Integer getMusicianId() {
        return musicianId;
    }

    public int compareTo(SummaryRecord o) {
        return musicianId.compareTo(o.musicianId);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((musicianId == null) ? 0 : musicianId.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SummaryRecord other = (SummaryRecord) obj;
        if (musicianId == null) {
            if (other.musicianId != null)
                return false;
        } else if (!musicianId.equals(other.musicianId))
            return false;
        return true;
    }

}
