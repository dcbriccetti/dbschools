package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import com.dbschools.mgb.schema._
import Terms.toTs

class LastPassFinder {
  val pieces = Cache.pieces
  val piecesById = pieces.map(p => p.id -> p).toMap
  val pieceOrderToId = pieces.map(p => p.testOrder.get -> p.id).toMap
  val numPieces = pieces.size
  lazy val pieceIdToPosition = pieces.sortBy(_.testOrder.get).map(_.id).zipWithIndex.toMap

  def lastPassed(
      musicianId: Option[Int]       = None,
      upTo:       Option[DateTime]  = None
  ): Iterable[LastPass] =
  {
    join(AppSchema.assessments, AppSchema.pieces, AppSchema.musicianGroups)((a, p, mg) =>
      where(
        a.pass === true and
        a.assessment_time < upTo.map(toTs).? and
        a.musician_id === musicianId.? and
        mg.school_year === Terms.currentTerm)
      groupBy(a.musician_id, a.instrument_id, a.subinstrument_id)
      compute max(p.testOrder.get)
      orderBy max(p.testOrder.get).desc
      on(a.pieceId === p.id, a.musician_id === mg.musician_id and a.instrument_id === mg.instrument_id)
    ).map(group => {
      val testOrder = group.measures.get
      val pieceId = pieceOrderToId(testOrder)
      LastPass(group.key._1, group.key._2, group.key._3, piecesById(pieceId),
        testOrder, pieceIdToPosition(pieceId))
    })
  }
}

case class LastPass(musicianId: Int, instrumentId: Int, opSubinstrumentId: Option[Int],
    piece: Piece, testOrder: Int, position: Int) {
  def subinstruments(id: Int) = Cache.subinstruments.find(_.id == id)
  def instrumentName(id: Int) = Cache.instruments.find(_.id == id).map(_.name.get)
  override def toString = formatted(withInstrument = true)
  def formatted(withInstrument: Boolean = false) = {
    val opSi = opSubinstrumentId.flatMap(subinstruments)
    piece.name.get + (if (withInstrument) " on " + ~instrumentName(instrumentId) + ~opSi.map(Subinstrument.suffix) else "")
  }
}
