package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.schema._

class LastPassFinder {
  val instruments     = AppSchema.instruments   .map(i => i.id -> i.name.is).toMap
  val subinstruments  = AppSchema.subinstruments.map(i => i.id -> i.name.is).toMap
  val pieces = AppSchema.pieces.toSeq
  val pieceNames = pieces.map(p => p.id -> p.name.is).toMap
  val piecesByOrder = pieces.map(p => p.testOrder.is -> p.id).toMap

  def lastPassed(musicianId: Option[Int] = None): Iterable[LastPass] = {
    from(AppSchema.assessments, AppSchema.pieces)((a, p) =>
      where(a.musician_id === musicianId.? and a.pieceId === p.id)
      groupBy(a.musician_id, a.instrument_id, a.subinstrument_id)
      compute(max(p.testOrder.is))
      orderBy(max(p.testOrder.is) desc)
    ).map(group => {
      val testOrder = group.measures.get
      LastPass(group.key._1, group.key._2, group.key._3, piecesByOrder(testOrder), testOrder)
    })
  }

  case class LastPass(musicianId: Int, instrumentId: Int, opSubinstrumentId: Option[Int],
      pieceId: Int, testOrder: Int) {
    override def toString = pieceNames(pieceId) + " on " + instruments(instrumentId) +
      ~opSubinstrumentId.map(subinstruments).map(n => " (%s)".format(n))
  }
}
