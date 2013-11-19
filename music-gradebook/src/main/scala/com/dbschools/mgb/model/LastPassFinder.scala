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
    from(AppSchema.assessments, AppSchema.pieces)((a, p) =>
      where(a.pass === true and a.musician_id === musicianId.? and
        a.pieceId === p.id and a.assessment_time < upTo.map(toTs).?)
      groupBy(a.musician_id, a.instrument_id, a.subinstrument_id)
      compute max(p.testOrder.get)
      orderBy max(p.testOrder.get).desc
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
  override def toString = {
    val opSi = opSubinstrumentId.flatMap(subinstruments)
    piece.name.get + " on " + ~instrumentName(instrumentId) + ~opSi.map(Subinstrument.suffix)
  }
}

