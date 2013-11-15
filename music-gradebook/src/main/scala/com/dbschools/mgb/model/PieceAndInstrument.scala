package com.dbschools.mgb.model

import com.dbschools.mgb.schema.Piece

case class PieceAndInstrument(piece: Piece, instId: Int, opSubInstId: Option[Int] = None)

object PieceAndInstrument extends SelectedMusician {

  def option(lastPassFinder: LastPassFinder): Option[PieceAndInstrument] = {
    val currentGroups = GroupAssignments(opMusician.map(_.id), opSelectedTerm = Some(Terms.currentTerm)).toSeq
    val currentInstIds = currentGroups.map(_.instrument.id)

    val pi = for {
      musician      <- opMusician
      allLastPassed =  lastPassFinder.lastPassed(Some(musician.id))
      lastPass      <- allLastPassed.find(lp => currentInstIds contains lp.instrumentId)
      piece         <- Cache.pieces.find(_.id == lastPass.pieceId)
      nextPiece     =  Cache.nextPiece(piece)
    } yield PieceAndInstrument(nextPiece, lastPass.instrumentId, lastPass.opSubinstrumentId)

    pi orElse currentGroups.headOption.map(ga => PieceAndInstrument(Cache.pieces.head, ga.instrument.id))
  }
}
