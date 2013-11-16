package com.dbschools.mgb.model

import scalaz._
import Scalaz._

class AssessmentState(lastPassFinder: LastPassFinder) extends SelectedMusician {

  val opNextPi = PieceAndInstrument.option(lastPassFinder)

  val commentTagSelections = scala.collection.mutable.Map(Cache.tags.map(_.id -> false): _*)
  var opSelInstId     = opNextPi.map(_.instId)
  var opSelSubinstId  = opNextPi.flatMap(_.opSubInstId)
  var opSelPieceId    = opNextPi.map(_.piece.id)
  var notes = ""

  def tempoFromPiece = opSelPieceId.flatMap(selPieceId =>
    // First look for a tempo for the specific instrument
    Cache.tempos.find(t => t.instrumentId == opSelInstId && t.pieceId == selPieceId) orElse
    Cache.tempos.find(_.pieceId == selPieceId)).map(_.tempo) | 0
}
