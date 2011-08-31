package com.dbschools.music.admin.ui;

import com.dbschools.music.ui.AbstractGroupAndInstrumentFilteredTableModel;
import com.dbschools.music.*;
import com.dbschools.music.dao.RemoteDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.dbschools.music.events.Event;
import com.dbschools.music.events.TypeCode;
import com.dbschools.gui.JwsClipboardUtil;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;

/**
 * Musician table model.
 * 
 * @author David C. Briccetti
 */
public class MusicianTableModel extends AbstractGroupAndInstrumentFilteredTableModel {
    private static final long serialVersionUID = 5002242080715656046L;
    private final static Logger log = Logger
            .getLogger(MusicianTableModel.class);

    private final String[] columnNames = {"Name", "ID", "Grd", "Sx", "Group", "Instrument"};
    
    private List<Musician> musicians;
    private final List<Musician> filteredMusicians = 
            Collections.synchronizedList(new ArrayList<Musician>());
    private Integer schoolYear;

    public MusicianTableModel(RemoteDao remoteDao) {
        super(remoteDao);
        load(false);
    }

    protected boolean causesRefresh(Event event) {
        final TypeCode t = event.getTypeCode();
        return t  == TypeCode.REMOVE_FROM_ALL_GROUPS || t == TypeCode.SAVE_MUSICAN_MUSIC_GROUP ||
                (t == TypeCode.SAVE_OBJECT && event.getDetails() instanceof MusicianImportBatch);
    }

    @Override
    protected void processNotRefreshCausingEvent(Event event) {
        TypeCode code = event.getTypeCode();
        if (code == TypeCode.DELETE_OBJECT && event.getDetails() instanceof Musician) {
            filteredMusicians.remove(event.getDetails());
            musicians.remove(event.getDetails());
            fireChangeUnderSwing();
        }
    }
    
    public void setSchoolYear(Integer schoolYear) {
        this.schoolYear = schoolYear;
    }

    private void load(boolean refresh) {
        // Sort, and delete duplicates
        musicians = new ArrayList<Musician>(new 
                TreeSet<Musician>(getRemoteDao().getMusicians(refresh, false)));
        rebuildFilteredList();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return filteredMusicians.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Musician musician = filteredMusicians.get(rowIndex);
        switch (columnIndex) {
        case 0: return musician.getName();
        case 1: return musician.getStudentId();
        case 2: return TermUtils.graduationYearAsGrade(
                musician.getGraduationYear(), schoolYear);
        case 3: return musician.getSex();
        case 4: 
        {
            StringBuilder sb = new StringBuilder();
            for (MusicianGroup musicianGroup : getMusicianGroupsInTerms(musician)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(musicianGroup.getGroup().getName());
            }
            return displayValue(sb);
        }
        case 5: 
        {
            StringBuilder sb = new StringBuilder();
            for (MusicianGroup musicianGroup : getMusicianGroupsInTerms(musician)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                final Instrument instrument = musicianGroup.getInstrument();
                sb.append(instrument == null ? Constants.UNASSIGNED : instrument.getName());
            }
            return displayValue(sb);
        }
        }
        return "foo";
    }

    private String displayValue(StringBuilder sb) {
        return sb.length() == 0 ? "None" : sb.toString();
    }

    @Override
    protected void rebuildFilteredList() {
        filteredMusicians.clear();
        final Collection<Integer> matchingMusicianIds = getMatchingMusicianIds();
        if (musicians != null) {
            for (Musician m : musicians) {
                if (matchingMusicianIds.contains(m.getId())) {
                    filteredMusicians.add(m);
                }
            }
        }
        
        fireChangeUnderSwing();
    }

    @Override
    public Class<?> getColumnClass(
            @SuppressWarnings("unused") int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Musician getRowAt(int rowIndex) {
        return filteredMusicians.get(rowIndex);
    }

    @Override
    public void refresh() {
        reloadServerData(true);
        load(true);
    }

    public void copyToClipboard() {
        final Iterable<MusicianGroup> musicianGroups = getSortedMusicianGroupsForSchoolYear();
        String lastGroup = "";
        String lastInst = "";

        final char newline = '\n';
        final StringBuilder sb = new StringBuilder(newline);
        
        for (MusicianGroup mg : musicianGroups) {
            final String groupName = mg.getGroup().getName();
            if (! groupName.equals(lastGroup)) {
                sb.append(groupName).append(newline);
                lastGroup = groupName;
                lastInst = "";
            }
            
            final String instName = mg.getInstrument().getName();
            if (! instName.equals(lastInst)) {
                sb.append('\t').append(instName).append(newline);
                lastInst = instName;
            }
            sb.append("\t\t").append(mg.getMusician().getFirstName())
                    .append(' ').append(mg.getMusician().getLastName())
                    .append(newline);
        }
        
        log.debug(sb.toString());
        
        JwsClipboardUtil.setStringToJwsOrSystemClipboard(sb.toString());
    }

    private Collection<MusicianGroup> getSortedMusicianGroupsForSchoolYear() {
        List<MusicianGroup> musicianGroups = new ArrayList<MusicianGroup>();
        for (Musician musician : filteredMusicians) {
            for (MusicianGroup musicianGroup : musician.getMusicianGroups()) {
                if (musicianGroup.getSchoolYear().equals(schoolYear)) {
                    musicianGroups.add(musicianGroup);
                }
            }
        }
        Collections.sort(musicianGroups, 
                MusicianGroup.getGroupInstrumentMusicianComparator());
        return musicianGroups;
    }

}