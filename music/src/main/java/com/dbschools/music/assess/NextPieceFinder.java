package com.dbschools.music.assess;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dbschools.music.orm.Assessment;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.NoNextPieceIndicator;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.Subinstrument;

/**
 * NextPieceFinder finds the next piece on which a musician is to be tested.
 * 
 * @author David C. Briccetti
 */
public final class NextPieceFinder {

    static public class NoNextPieceException extends Exception {
        private static final long serialVersionUID = 7970649852149609743L;
    }
    
    private final SortedSet<Piece> pieces;
    
    /**
     * Creates a new NextPieceFinder.
     * @param pieces the pieces to consider
     */
    public NextPieceFinder(Collection<Piece> pieces) {
        this.pieces = new TreeSet<Piece>(pieces);
    }

    /**
     * Finds the next piece on which the musician must test, 
     * in the given collection of assessments,
     * on the specified instrument and subinstrument.
     * @param assessments a collection of assessments to search
     * @param instrument an instrument, or null if instrument is not to be considered
     * @param subinstrument a subinstrument, or null if subinstrument is not to be considered
     * @return the next piece
     */
    public Piece nextPiece(Collection<Assessment> assessments,
            Instrument instrument, Subinstrument subinstrument) {
        Piece highestPassedPiece = findHighestPass(assessments, instrument, subinstrument);
        if (highestPassedPiece == null) {
            return pieces.first(); 
        }
        
        for (Piece piece : pieces) {
            if (piece.compareTo(highestPassedPiece) > 0) {
                return piece;
            }
        }
        
        return new NoNextPieceIndicator();
    }

    /**
     * Finds the highest piece passed, in the given collection of assessments,
     * on the specified instrument and subinstrument.
     * @param assessments a collection of assessments to search
     * @param instrument an instrument, or null if instrument is not to be considered
     * @param subinstrument a subinstrument, or null if subinstrument is not to be considered
     * @return the highest piece passed
     */
    public Piece findHighestPass(Collection<Assessment> assessments,
            Instrument instrument, Subinstrument subinstrument) {
        Piece highestPass = null;
        for (Assessment assm : assessments) {
            final Piece piece = assm.getMusicPiece();
            if (! assm.isPass()) {
                continue;
            }
            if (instrument != null && ! instrument.equals(assm.getMusicInstrument())) {
                continue;
            }
            if (subinstrument != null && ! subinstrument.equals(assm.getMusicSubinstrument())) {
                continue;
            }
            
            if (highestPass == null || piece.compareTo(highestPass) > 0) {
                highestPass = piece;
            }
        }
        return highestPass;
    }

}
