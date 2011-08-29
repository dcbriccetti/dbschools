package com.dbschools.music.server;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNumeric;

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
        for (String line : mib.getImportText()) {
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

    private void processMusician(Session session, int groupId, int schoolYear, String[] fields, int offset) {
        final String studentIdStr = fields[offset + 0];
        if (isNumeric(studentIdStr)) {
            final Musician musician;
            Instrument instrument = getUnassignedInstrument(session);
            final Long studentId = Long.parseLong(studentIdStr);
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
                musician = createMusician(session, schoolYear, studentId, fields, offset);
            }
            Group group = (Group) session.load(Group.class, groupId);

            if (! hasAssignment(session, schoolYear, musician, group)) {
                makeAssignment(session, schoolYear, musician, group, instrument);
            }
        } else {
            log.warn(studentIdStr + " is not a valid student ID");
        }
    }

    private Musician createMusician(Session session, int schoolYear, Long studentId, String[] fields, int offset) {
        Musician musician = new Musician();
        musician.setStudentId(studentId);
        musician.setLastName(fields[offset + 1]);
        musician.setFirstName(fields[offset + 2]);
        musician.setGraduationYear(TermUtils.gradeAsGraduationYear(
                Integer.parseInt(fields[offset + 3]), schoolYear));
        musician.setSex(fields[offset + 4]);
        session.save(musician);
        return musician;
    }

    private boolean hasAssignment(Session session, int schoolYear, Musician musician, Group group) {
        Query mgQuery = session.createQuery("from MusicianGroup where group = :group " +
                "and musician = :musician and schoolYear = :schoolYear");
        mgQuery.setParameter("group", group);
        mgQuery.setParameter("musician", musician);
        mgQuery.setParameter("schoolYear", schoolYear);
        log.info("looking for " + mgQuery);
        return ! mgQuery.list().isEmpty();
    }

    private void makeAssignment(Session session, int schoolYear, Musician musician, Group group,
            Instrument instrument) {
        MusicianGroup mg = new MusicianGroup();
        mg.setGroup(group);
        mg.setInstrument(instrument);
        mg.setMusician(musician);
        log.info("termId: " + schoolYear);
        mg.setSchoolYear(schoolYear);
        log.info("saving " + mg);
        session.save(mg);
    }

    private Instrument getUnassignedInstrument(Session session) {
        return (Instrument) session.createQuery("from Instrument where name = 'Unassigned'").list().get(0);
    }
    
}