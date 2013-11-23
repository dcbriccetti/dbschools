package com.dbschools.music.assess

import java.util.ArrayList
import junit.framework.TestCase
import junit.framework.Assert.assertEquals
import com.dbschools.music.orm.{Assessment,Instrument}

class TestNextPieceFinder extends TestCase {
  def testNextPiece() {
    val pieces = Pieces.createPieces
    val npf = new NextPieceFinder(pieces)
    val assmts = new ArrayList[Assessment]
    val inst = new Instrument("Trumpet", 10)
    val nextPiece = npf.nextPiece(assmts, inst, null)
    assertEquals(1, nextPiece.getId)
  }
}
