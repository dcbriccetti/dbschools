package com.dbschools.mgb.model

import com.dbschools.mgb.schema.AppSchema
import org.squeryl.PrimitiveTypeMode._

object Cache {
  var groups = readGroups
  var instruments = readInstruments
  var subinstruments = readSubinstruments
  var tags = readTags
  var pieces = readPieces
  var tempos = readTempos

  private def readGroups      = inTransaction {AppSchema.groups.toSeq.sortBy(_.name)}
  private def readInstruments = inTransaction {AppSchema.instruments.toSeq.sortBy(_.sequence.is)}
  private def readSubinstruments = inTransaction {AppSchema.subinstruments.groupBy(_.instrumentId.is)}
  private def readTags        = inTransaction {AppSchema.predefinedComments.toSeq.sortBy(_.commentText)}
  private def readPieces      = inTransaction {AppSchema.pieces.toSeq.sortBy(_.testOrder.is)}
  private def readTempos      = inTransaction {AppSchema.tempos.toSeq.sortBy(_.instrumentId)}

  def init(): Unit = {}

  def invalidateGroups(): Unit = { groups = readGroups }

  def invalidateInstruments(): Unit = { instruments = readInstruments }

  def invalidateSubinstruments(): Unit = { subinstruments = readSubinstruments }

  def invalidateTags(): Unit = { tags = readTags }

  def invalidatePieces(): Unit = { pieces = readPieces }

  def invalidateTempos(): Unit = { tempos = readTempos }
}