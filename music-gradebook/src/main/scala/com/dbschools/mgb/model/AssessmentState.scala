package com.dbschools.mgb.model

import scalaz._
import Scalaz._

class AssessmentState(lastPassFinder: LastPassFinder) extends SelectedMusician {

  val pieces = Cache.pieces
  val piecesById = pieces.map(p => p.id -> p).toMap

  val commentTagSelections = scala.collection.mutable.Map(Cache.tags.map(_.id -> false): _*)

  private val opNextPi = PieceAndInstrument.option(lastPassFinder)
  var opSelInstId     = opNextPi.map(_.instId)
  var opSelSubinstId  = opNextPi.flatMap(_.opSubInstId)
  var opSelPieceId    = opNextPi.map(_.piece.id)

  var notes = ""

  def tempoFromPiece = opSelPieceId.flatMap(selPieceId =>
    // First look for a tempo for the specific instrument
    Cache.tempos.find(t => t.instrumentId == opSelInstId && t.pieceId == selPieceId) orElse
    Cache.tempos.find(_.pieceId == selPieceId)).map(_.tempo) | 0

  def next(pass: Boolean): Unit = {
    commentTagSelections.keys.map(k => commentTagSelections(k) = false)
    notes = ""
    if (pass)
      opSelPieceId = for {
        curPieceId <- opSelPieceId
        piece      <- piecesById.get(curPieceId)
        nextPiece  <- Cache.nextPiece(piece)
      } yield nextPiece.id
  }
}
