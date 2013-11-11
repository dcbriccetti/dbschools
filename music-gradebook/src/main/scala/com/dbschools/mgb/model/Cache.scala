package com.dbschools.mgb.model

import com.dbschools.mgb.schema.{Piece, AppSchema}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode.{inTransaction => inT}
import org.joda.time.DateTime
import scalaz._
import Scalaz._

object Cache {
  var groups = readGroups
  var groupTerms = readGroupTerms
  var instruments = readInstruments
  var subinstruments = readSubinstruments
  var tags = readTags
  var pieces = readPieces
  var tempos = readTempos

  private var _lastAssTimeByMusician = inT(for {
    gm <- from(AppSchema.assessments)(a => groupBy(a.musician_id) compute max(a.assessment_time))
    m <- gm.measures
  } yield gm.key -> new DateTime(m.getTime)).toMap
  def lastAssTimeByMusician = _lastAssTimeByMusician
  def updateLastAssTime(musicianId: Int, time: DateTime): Unit = _lastAssTimeByMusician += musicianId -> time

  private def readGroups      = inT {AppSchema.groups.toSeq.sortBy(_.name)}
  private def readGroupTerms  = inT {AppSchema.groupTerms.toList}
  private def readInstruments = inT {AppSchema.instruments.toSeq.sortBy(_.sequence.get)}
  private def readSubinstruments
                              = inT {AppSchema.subinstruments.groupBy(_.instrumentId.get)}
  private def readTags        = inT {AppSchema.predefinedComments.toSeq.sortBy(_.commentText)}
  private def readPieces      = inT {AppSchema.pieces.toSeq.sortBy(_.testOrder.get)}
  private def readTempos      = inT {AppSchema.tempos.toSeq.sortBy(_.instrumentId)}

  def init(): Unit = {}

  def invalidateGroups(): Unit = { groups = readGroups }

  def invalidateGroupTerms(): Unit = { groupTerms = readGroupTerms }

  def invalidateInstruments(): Unit = { instruments = readInstruments }

  def invalidateSubinstruments(): Unit = { subinstruments = readSubinstruments }

  def invalidateTags(): Unit = { tags = readTags }

  def invalidatePieces(): Unit = { pieces = readPieces }

  def invalidateTempos(): Unit = { tempos = readTempos }

  def nextPiece(piece: Piece) = pieces.find(_.testOrder.get.compareTo(piece.testOrder.get) > 0) | pieces.head

  def filteredGroups(opSelectedTerm: Option[Int]) = {
    val ids = for {
      gt      <- Cache.groupTerms
      selTerm <- opSelectedTerm
      if gt.term == selTerm
    } yield gt.groupId

    Cache.groups.filter(g => ids.isEmpty || (ids contains g.id))
  }

}