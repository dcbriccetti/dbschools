package com.dbschools.music.server;

import com.dbschools.music.assess.NextPieceFinder;
import java.util.Collection;
import java.util.TreeSet;

import org.hibernate.SessionFactory;

import com.dbschools.music.orm.Piece;

/**
 * State data for each database being used.
 * @author David C. Briccetti
 */
final class DatabaseInstance {
    private final SessionFactory sessionFactory;
    private final NextPieceFinder nextPieceFinder;

    public DatabaseInstance(SessionFactory sessionFactory, Collection<Piece> musicPieces) {
        this.sessionFactory = sessionFactory;
        nextPieceFinder = new NextPieceFinder(new TreeSet<Piece>(musicPieces));
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public NextPieceFinder getNextPieceFinder() {
        return nextPieceFinder;
    }
}