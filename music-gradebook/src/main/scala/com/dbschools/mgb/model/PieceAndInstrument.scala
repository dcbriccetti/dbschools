package com.dbschools.mgb
package model

import schema.Piece
import snippet.svSelectors

case class PieceAndInstrument(piece: Piece, instId: Int, opSubInstId: Option[Int] = None)

object PieceAndInstrument extends SelectedMusician {

  def option(lastPassFinder: LastPassFinder): Option[PieceAndInstrument] = {
    val currentGroups = GroupAssignments(opMusician.map(_.id), opSelectedTerm = Some(Terms.currentTerm)).toVector
    val currentInstIds = currentGroups.map(_.instrument.id)
    val opSelGroupInstId = svSelectors.is.selectedGroupId.rto.flatMap(groupId =>
      currentGroups.find(_.group.id == groupId)).map(_.instrument.id)

    val pi = for {
      musician              <- opMusician
      allLastPassed         =  lastPassFinder.lastPassed(Some(musician.id))
      lastPassesOnACurInst  =  allLastPassed.filter(currentInstIds contains _.instrumentId)
      oneLastPass           <- opSelGroupInstId.flatMap(iId => lastPassesOnACurInst.find(_.instrumentId == iId)) orElse
                                lastPassesOnACurInst.headOption
      nextPiece             <- Cache.nextPiece(oneLastPass.piece)
    } yield PieceAndInstrument(nextPiece, oneLastPass.instrumentId, oneLastPass.opSubinstrumentId)

    pi orElse (opSelGroupInstId orElse currentInstIds.headOption).map(iid =>
      PieceAndInstrument(Cache.pieces.head, iid))
  }
}
