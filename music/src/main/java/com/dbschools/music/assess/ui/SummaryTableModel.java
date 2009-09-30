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

import com.dbschools.music.ui.AbstractGroupAndInstrumentFilteredTableModel;
import com.dbschools.music.dao.RemoteDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dbschools.gui.barcell.BarCellValue;
import com.dbschools.music.TermUtils;
import com.dbschools.music.assess.SummaryRecord;
import com.dbschools.music.events.Event;
import com.dbschools.music.events.TypeCode;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.Piece;
import java.util.Collection;
import org.apache.log4j.Logger;

/**
 * A model for the summary table.
 */
public final class SummaryTableModel extends AbstractGroupAndInstrumentFilteredTableModel {

    private static final long serialVersionUID = -1086127737485824704L;
    private static Logger log = Logger.getLogger(SummaryTableModel.class);
    private final Integer schoolYear;
    private final List<SummaryRecord> summaryRecords = 
            Collections.synchronizedList(new ArrayList<SummaryRecord>());
    private final Map<Integer,Musician> musiciansMap = 
            new ConcurrentHashMap<Integer, Musician>();
    private final Map<Integer,Piece> piecesMap =
            new ConcurrentHashMap<Integer, Piece>();
    private static final String[] titles = {
        "Student", "Grd", "Instrument", "Next Piece",
        "Assessed", "Days", 
        "Visits", "Asmt", "Pass", "Rejs"
    };
    private static final Class<?>[] classes = {
        String.class, Integer.class, String.class, Piece.class, 
        Date.class, BarCellValue.class, 
        BarCellValue.class, BarCellValue.class, BarCellValue.class, Integer.class 
    };
    private int maxDays;
    private int maxPassed;
    private int maxAss;
    private int maxVisits;
    
    public SummaryTableModel(RemoteDao remoteDao, Integer schoolYear) {
        super(remoteDao);
        this.schoolYear = schoolYear;
    }

    @Override
    protected boolean causesRefresh(Event event) {
        return event.getTypeCode() == TypeCode.SAVE_MUSICAN_MUSIC_GROUP;
    }

    @Override
    protected void processNotRefreshCausingEvent(Event event) {
        if (event.getTypeCode() == TypeCode.UPDATE_OBJECT && 
                event.getDetails() instanceof SummaryRecord) {
            replaceRecord((SummaryRecord) event.getDetails());
        } else if (event.getTypeCode() == TypeCode.DELETE_OBJECT && 
                event.getDetails() instanceof Musician) {
            deleteMusician(((Musician) event.getDetails()));
        }
    }

    public void refresh() {
        loadData(true);
    }
    
    /**
     * Replaces a {@link SummaryRecord} in the model with the supplied SummaryRecord.
     * @param summaryRecord
     */
    public void replaceRecord(SummaryRecord summaryRecord) {
        if (summaryRecords != null) {
            final int i = summaryRecords.indexOf(summaryRecord);
            if (i != -1) {
                summaryRecords.set(i, summaryRecord);
                processModelDataUpdates();
            }
        }
    }

    /**
     * Replaces the stored data with the provided musician.
     * @param musician
     */
    public void replaceMusician(Musician musician) {
        musiciansMap.put(musician.getId(), musician);
    }

    private void deleteMusician(Musician musician) {
        musiciansMap.remove(musician.getId());
        for (SummaryRecord sr : summaryRecords) {
            if (sr.getMusicianId().equals(musician.getId())) {
                summaryRecords.remove(sr);
                break;
            }
        }
        fireChangeUnderSwing();
    }
    
    private void loadData(boolean refresh) {
        final RemoteDao rd = getRemoteDao();

        log.debug("Loading summary records");
        final Collection<SummaryRecord> newSummaryRecords = new ArrayList<SummaryRecord>(
                        rd.getSummaryRecords(refresh, getGroupIds(), getInstrumentIds()));
        log.debug("Loading musicians map");
        final Map<Integer, Musician> newMusiciansMap = rd.getMusiciansMap(refresh, true);
        log.debug("Loading pieces");
        final Map<Integer, Piece> newPiecesMap = rd.getPiecesMap();
        log.debug("Done loading data");
        
        summaryRecords.clear();
        summaryRecords.addAll(newSummaryRecords);
        musiciansMap.clear();
        musiciansMap.putAll(newMusiciansMap);
        piecesMap.clear();
        piecesMap.putAll(newPiecesMap);

        processModelDataUpdates();
    }

    @Override
    public String getColumnName(int col) {
        return titles[col];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return classes[columnIndex];
    }
    
    public int getColumnCount() {
        return titles.length;
    }

    public int getRowCount() {
        return summaryRecords.size();
    }

    /**
     * Gets the record at the specified row index.
     * 
     * @param rowIndex the index of the requested record
     * @return the record at the specified row index
     */
    private SummaryRecord getRecordAt(int rowIndex) {
        return summaryRecords.get(rowIndex);
    }
    
    /**
     * Gets the musician at the specified row index.
     * 
     * @param rowIndex the index of the requested record
     * @return the musician at the specified row index
     */
    public Musician getMusicianAt(int rowIndex) {
        return musiciansMap.get(summaryRecords.get(rowIndex).getMusicianId());
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        final SummaryRecord sum = getRecordAt(rowIndex);
        if (sum == null) {
            // Some sort of logic error?
            return "";
        }
        final Object val;
        switch (columnIndex) {
        case 0:
        {
            final Musician musician = musiciansMap.get(sum.getMusicianId());
            val = musician == null ? "?" : musician.getName();
        }
            break;
        case 1:
        {
            final Musician musician = musiciansMap.get(sum.getMusicianId());
            val = musician == null ? "?" : TermUtils.graduationYearAsGrade(musician.getGraduationYear(), schoolYear);
        }
            break;
        case 2:
            val = sum.getInstrument();
            break;
        case 3:
            final Integer nextPieceId = sum.getNextPieceId();
            val = nextPieceId == null ? "?" : piecesMap.get(nextPieceId);
            break;
        case 4:
            val = sum.getLastAssessment();
            break;
        case 5:
            Double dval = sum.getDaysSinceLastAssessment() == null ?
                null : (double) sum.getDaysSinceLastAssessment().intValue();
            final BarCellValue barCellValue = new BarCellValue(dval, (double) maxDays);
            barCellValue.setNullIsMax(true);
            val = barCellValue;
            break;
        case 6:
            dval = sum.getNumVisits() > 0 ?
                    (double) sum.getNumVisits() : null;
            val = new BarCellValue(dval, (double) maxVisits);
            break;
        case 7:
            dval = sum.getNumAssessments() > 0 ?
                    (double) sum.getNumAssessments() : null;
            val = new BarCellValue(dval, (double) maxAss);
            break;
        case 8:
            dval = sum.getNumAssessments() > 0 ?
                    (double) sum.getNumPasses() : null;
            val = new BarCellValue(dval, (double) maxPassed);
            break;
        case 9:
            val = sum.getNumRejections() > 0 ? sum.getNumRejections() : null;
            break;
        default:
            throw new IllegalArgumentException("Illegal columnIndex: " +  //$NON-NLS-1$
                    columnIndex);
        }
        return val;
    }    

    private void processModelDataUpdates() {
        calculateStatistics();
        rebuildFilteredList();
    }

    private void calculateStatistics() {
        int aMaxDays = 0;
        int aMaxPassed = 0;
        int aMaxAss = 0;
        int aMaxVisits = 0;

        for (SummaryRecord rec : summaryRecords) {
            aMaxDays = max(aMaxDays, rec.getDaysSinceLastAssessment());
            aMaxPassed = max(aMaxPassed, rec.getNumPasses());
            aMaxAss = max(aMaxAss, rec.getNumAssessments());
            aMaxVisits = max(aMaxVisits, rec.getNumVisits());
        }
        
        maxDays = aMaxDays;
        maxPassed = aMaxPassed;
        maxAss = aMaxAss;
        maxVisits = aMaxVisits;
    }
 
    private static int max(int max, Integer val) {
        if (val != null) {
            final int d = val;
            if (d > max) {
                max = d;
            }
            
        }
        return max;
    }
     
    @Override
    protected void rebuildFilteredList() {
        fireChangeUnderSwing();
    }

}
