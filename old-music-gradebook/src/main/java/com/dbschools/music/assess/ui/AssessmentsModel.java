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

package com.dbschools.music.assess.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.dbschools.gui.LongTextTableCellRenderer.LongString;
import com.dbschools.music.assess.KeywordCounts;
import com.dbschools.music.decortrs.AssessmentDecorator;
import com.dbschools.music.orm.Assessment;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.PredefinedComment;
import com.dbschools.music.orm.Subinstrument;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * TableModel for assessment history. 
 */
public final class AssessmentsModel extends AbstractTableModel {

    static Logger log = Logger.getLogger(AssessmentsModel.class);
    private static final long serialVersionUID = 1906830360256045038L;
    
    private final List<Assessment> assessments = 
            Collections.synchronizedList(new ArrayList<Assessment>());
    private final List<Assessment> allMusicAssessments = 
            Collections.synchronizedList(new ArrayList<Assessment>());
    private int numWeeksToShow;

    private static final String[] titles = {
           "Date", "Tester", "Piece", "Instrument", "Comments"
    };
    private final static Class<?>[] classes = {
            Date.class, String.class, PieceAndPass.class, String.class, LongString.class
    };

    /**
     * A composite of a piece and whether the musician passed it, for rendering
     * in a single table column.
     */
    public final static class PieceAndPass implements Comparable<PieceAndPass> {
        private final Piece piece;
        private final boolean passed;
        
        public PieceAndPass(Piece piece, boolean passed) {
            this.piece = piece;
            this.passed = passed;
        }
        
        public Piece getPiece() {
            return piece;
        }
        
        public boolean isPassed() {
            return passed;
        }

        public int compareTo(PieceAndPass o) {
            return piece.compareTo(o.piece);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((piece == null) ? 0 : piece.hashCode());
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
            final PieceAndPass other = (PieceAndPass) obj;
            if (piece == null) {
                if (other.piece != null)
                    return false;
            } else if (!piece.equals(other.piece))
                return false;
            return true;
        }
    }
    
    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return classes[columnIndex];
    }
    
    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        return titles[columnIndex];
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return titles.length;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return assessments == null ? 0 : assessments.size();
    }

    /**
     * Gets the Assessment at the specified row index. 
     * @param rowIndex row index
     * @return Assessment
     */
    public Assessment getMusicAssessment(final int rowIndex) {
        return assessments.get(rowIndex);
    }

    public int getAllAssessmentsCount() {
        return allMusicAssessments.size();
    }
    
    public void addOrUpdate(Assessment assessment) {
        boolean replaced = false;
        
        for (int i = 0; i < allMusicAssessments.size(); ++i) {
            Assessment ma = allMusicAssessments.get(i);
            if (assessment.getId().equals(ma.getId())) {
                allMusicAssessments.set(i, assessment);
                replaced = true;
                break;
            }
        }
        
        if (! replaced) {
            allMusicAssessments.add(assessment);
        }
        createFilteredData();
    }

    public void deleteAssessment(Assessment assessment) {
        allMusicAssessments.remove(assessment);
        createFilteredData();
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        final Assessment assess = getMusicAssessment(rowIndex);
        Object val = "";
        switch (columnIndex) {
        case 0:
            return assess.getAssessmentTime();
        case 1:
            return assess.getUser().getDisplayName();
        case 2:
            return new PieceAndPass(assess.getMusicPiece(), assess.isPass());
        case 3:
        {
            final Subinstrument subinstrument = assess.getMusicSubinstrument();
            return subinstrument == null ? assess.getMusicInstrument().getName() : subinstrument.getName();
        }
        case 4: 
            val = AssessmentDecorator.formatNotes(assess);
            break;
        }
        return val;
    }    

    public void setMusicAssessments(Collection<Assessment> assessments) {
        allMusicAssessments.clear();
        allMusicAssessments.addAll(assessments);
        createFilteredData();
    }

    private void createFilteredData() {
        if (allMusicAssessments == null) {
            return;
        }
        
        assessments.clear();
        final long msInAWeek = 1000L * 60 * 60 * 24 * 7;
        final long threshold = new Date().getTime() - msInAWeek * numWeeksToShow;
        
        for (Assessment a : allMusicAssessments) {
            if (a.getAssessmentTime().getTime() > threshold) {
                assessments.add(a);
            }
        }
        fireTableDataChanged();
    }

    public void setWeeksToShow(int value) {
        this.numWeeksToShow = value;
        createFilteredData();
    }

    /**
     * Returns the filtered-in assessments.
     * @return assessments
     */
    public Collection<Assessment> getSelectedAssessments() {
        return assessments;
    }
    
    /**
     * Gets a collection of each comment and the number of occurrences in
     * the currently-shown assessments, sorted by number of occurrences.
     * @return
     */
    public Collection<KeywordCounts> getKeywordCounts() {
        Collection<KeywordCounts> kcs = new TreeSet<KeywordCounts>();
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Assessment as : assessments) {
            for (PredefinedComment pc : as.getPredefinedComments()) {
                final String cmtTxt = pc.getText();
                Integer ct = map.get(cmtTxt);
                map.put(cmtTxt, ct == null ? 1 : ct + 1);
            }
        }
        for (Entry<String,Integer> ent : map.entrySet()) {
            kcs.add(new KeywordCounts(ent.getKey(), ent.getValue()));
        }
        return kcs;
    }
    
    /**
     * Returns the keyword counts as a string.
     * @see #getKeywordCounts()
     * @return
     */
    public String getKeywordCountsAsString() {
        String s = getKeywordCounts().toString();
        return s.substring(1, s.length() - 1); // Strip off []
    }
}
