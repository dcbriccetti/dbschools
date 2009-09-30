package com.dbschools.music.assess
import com.dbschools.music.orm._
import java.util.ArrayList 

object Pieces {
  def createPieces: java.util.List[Piece] = {
    val pieces = new ArrayList[Piece]
    val red1 = new Piece(0, "Red 1")
    red1.setId(1)
    val red2 = new Piece(0, "Red 2")
    red2.setId(2)
    pieces add red1
    pieces add red2
    pieces
  }
}