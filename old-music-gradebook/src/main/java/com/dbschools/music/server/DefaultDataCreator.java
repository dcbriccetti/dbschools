package com.dbschools.music.server;

import java.io.IOException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.PredefinedComment;
import com.dbschools.music.orm.RejectionReason;
import com.dbschools.music.orm.Subinstrument;
import com.dbschools.music.orm.Tempo;
import com.dbschools.music.orm.User;

class DefaultDataCreator {
    private static final String DATA_PATH = "/data/";
    private static final String FIELD_DELIMITER = "\t";

    static void createIfEmpty(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        if (session.createCriteria(User.class).list().isEmpty()) {
            Transaction t = session.beginTransaction();
            try {
                createUser(session);
                createInstruments(session);
                createGroups(session);
                createPieces(session);
                createRejectionReasons(session);
                createPredefinedComments(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
            t.commit();
        }
        session.close();
        
    }

    private static void createPredefinedComments(Session session) throws IOException {
        for (String line : new FileLineProvider(DATA_PATH +
                "PredefinedComment.txt")) {
            session.save(new PredefinedComment(line));
        }
    }

    private static void createUser(Session session) throws HibernateException, IOException {
        for (String line : new FileLineProvider(DATA_PATH + "User.txt")) {
            String[] fields = line.split(FIELD_DELIMITER);
            if (fields.length == 4) {
                session.save(new User(fields[0], fields[1], fields[2], fields[3]));
            } else {
                throw new IllegalArgumentException(
                        "user line must contain four fields separated by spaces");
            }
        }
    }

    private static void createInstruments(Session session) throws HibernateException, IOException {
        int seq = 10;
        for (String line : new FileLineProvider(DATA_PATH + "Instrument.txt")) {
            final String[] fields = line.split(FIELD_DELIMITER);
            // instrument [subinstrument-1, ... subinstrument-n]
            Instrument inst = new Instrument(fields[0], seq);
            seq += 10;
            session.save(inst);
            if (fields.length > 1) {
                int subSeq = 10;
                for (int i = 1; i < fields.length; ++i) {
                    Subinstrument subinst = new Subinstrument(inst, fields[i], subSeq);
                    subSeq += 10;
                    session.save(subinst);
                }
            }
        }
    }

    private static void createGroups(Session session) throws HibernateException, IOException {
        for (String line : new FileLineProvider(DATA_PATH + "Group.txt")) {
            session.save(new Group(line, 0, true));
        }
    }

    private static void createPieces(Session session) throws HibernateException, IOException {
        int seq = 1;
        for (String line : new FileLineProvider(DATA_PATH + "Piece.txt")) {
            final String[] fields = line.split(FIELD_DELIMITER);
            // piece-name [tempo]
            Piece piece = new Piece(seq++, fields[0]);
            session.save(piece);
            if (fields.length == 2) {
                session.save(new Tempo(piece, Integer.parseInt(fields[1])));
            }
        }
    }
    
    private static void createRejectionReasons(Session session) throws HibernateException, IOException {
        for (String line : new FileLineProvider(DATA_PATH + "RejectionReason.txt")) {
            session.save(new RejectionReason(line));
        }
    }
}
