package com.dbschools.music.server;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.dbschools.music.admin.ui.MusicianImportBatch;
import com.dbschools.music.TermUtils;
import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;

/**
 * Imports musicians.
 * @author David C. Briccetti
 */
class MusicianImporter {
    private final static Logger log = Logger.getLogger(MusicianImporter.class);
    
    public void importMusicians(MusicianImportBatch mib, final Session session) {
        String[] lines = mib.getImportText();
        for (String line : lines) {
            log.info(line);
            String[] fields = line.split("\t");
            if (fields.length == 5 || fields.length == 10) {
                processMusician(session, mib.getGroupId(), mib.getTermId(), fields, 0);
                if (fields.length == 10) {
                    processMusician(session, mib.getGroupId(), mib.getTermId(), fields, 5);
                }
            } else {
                log.warn("Was expecting 5 or 10 fields in line " + line);
            }
        }
    }

    private void processMusician(Session session, int groupId, int schoolYear, 
            String[] fields, int offset) {
        final String f = fields[offset + 0];
        if (StringUtils.isNumeric(f)) {
            final Musician musician;
            Instrument instrument = getUnassignedInstrument(session);
            Long studentId = Long.parseLong(f);
            Query query = session.createQuery("from Musician where studentId = :id");
            query.setLong("id", studentId);
            List<Musician> list = query.list();
            if (! list.isEmpty()) {
                musician = list.get(0);
                final Set<MusicianGroup> mgs = musician.getMusicianGroups();
                if (mgs != null && ! mgs.isEmpty()) {
                    instrument = mgs.iterator().next().getInstrument();
                }
            } else {
                musician = new Musician();
                musician.setStudentId(studentId);
                musician.setLastName(fields[offset + 1]);
                musician.setFirstName(fields[offset + 2]);
                musician.setGraduationYear(TermUtils.gradeAsGraduationYear(
                        Integer.parseInt(fields[offset + 3]), schoolYear));
                musician.setSex(fields[offset + 4]);
                session.save(musician);
            }
            Group group = (Group) session.load(Group.class, groupId);
            
            Query mgQuery = session.createQuery("from MusicianGroup where group = :group " +
                    "and musician = :musician " +
                    "and schoolYear = :schoolYear");
            mgQuery.setParameter("group", group);
            mgQuery.setParameter("musician", musician);
            mgQuery.setParameter("schoolYear", schoolYear);
            log.info("looking for " + mgQuery);
            List mgList = mgQuery.list();
            if (mgList.isEmpty()) {
                MusicianGroup mg = new MusicianGroup();
                mg.setGroup(group);
                mg.setInstrument(instrument);
                mg.setMusician(musician);
                log.info("termId: " + schoolYear);
                mg.setSchoolYear(schoolYear);
                log.info("saving " + mg);
                session.save(mg);
            }
        } else {
            log.warn(f + " is not a valid student ID");
        }
    }

    private Instrument getUnassignedInstrument(Session session) {
        return (Instrument) session.createQuery("from Instrument where name = 'Unassigned'").list().get(0);
    }
    
}