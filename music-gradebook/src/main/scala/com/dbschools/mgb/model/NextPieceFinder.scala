package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema._
import com.dbschools.mgb.schema.Assessment

/**
 * NextPieceFinder finds the next piece on which a musician is to be tested.
 */
object NextPieceFinder extends Loggable {
  /**
   * Finds the next piece on which the musician must test,
   * in the given collection of assessments,
   * on the specified instrument and subinstrument.
   * @return the next piece
   */
  def nextPiece(musicianId: Int, opInstrumentId: Option[Int] = None,
      opSubinstrumentId: Option[Int] = None): Option[Piece] = {
    val assessments = AppSchema.assessments.where(_.musician_id === musicianId)
    val pieces = AppSchema.pieces.toList.sortBy(_.testOrder.get)
    val piecesMap = pieces.map(p => p.id -> p).toMap

    def allowAll(as: Assessment) = true
    def iFlt(i: Int)(as: Assessment) = as.instrument_id == i
    def siFlt(si: Int)(as: Assessment) = as.subinstrument_id == si
    def opIFilter = opInstrumentId.map(iFlt) | allowAll
    def opSubiFilter = opSubinstrumentId.map(siFlt) | allowAll

    val filteredAsmts = assessments.filter(as => opIFilter(as) && opSubiFilter(as))

    val hpp = filteredAsmts.foldLeft(None: Option[Piece]) { (opHighestPassedPiece, assmnt) =>
      val piece = piecesMap(assmnt.pieceId)
      val isPieceHigher = opHighestPassedPiece.map(_.testOrder.is.compareTo(piece.testOrder.is) > 0) | true

      if (assmnt.pass && isPieceHigher)
        Some(piece)
      else
        opHighestPassedPiece
    }
    logger.warn(hpp)
    hpp.map(highestPassedPiece =>
      pieces.find(_.testOrder.is.compareTo(highestPassedPiece.testOrder.is) > 0)
    ) | pieces.headOption
  }

  def lastPassed(musicianId: Option[Int]): Iterable[LastPass] = {
    val piecesByOrder = AppSchema.pieces.map(p => p.testOrder.is -> p.id).toMap
    from(AppSchema.assessments, AppSchema.pieces)((a, p) =>
      where(a.musician_id === musicianId.? and a.pieceId === p.id)
      groupBy(a.musician_id, a.instrument_id, a.subinstrument_id)
      compute(max(p.testOrder.is))
    ).map(row => LastPass(row.key._1, row.key._2, row.key._3, piecesByOrder(row.measures.get)))
  }
}

case class LastPass(musicianId: Int, instrumentId: Int, opSubinstrument: Option[Int], pieceId: Int) {
  val instruments = AppSchema.instruments.map(i => i.id -> i.name).toMap  // todo cache these
  val subinstruments = AppSchema.subinstruments.map(i => i.id -> i.name).toMap
  val pieces = AppSchema.pieces.map(p => p.id -> p.name).toMap

  override def toString = pieces(pieceId) + " on " + instruments(instrumentId) +
    ~opSubinstrument.map(subinstruments).map(n => " (%s)".format(n))
}
