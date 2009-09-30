package com.dbschools.music.orm;

public class NoNextPieceIndicator extends Piece {
    private static final long serialVersionUID = -1675723730481685516L;

    @Override
    public Integer getId() {
        return -1;
    }

    @Override
    public String toString() {
        return "No Next";
    }

}
