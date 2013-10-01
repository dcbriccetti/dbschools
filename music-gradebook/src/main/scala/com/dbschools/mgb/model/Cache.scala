package com.dbschools.mgb.model

import com.dbschools.mgb.schema.AppSchema
import org.squeryl.PrimitiveTypeMode._

object Cache {
  var groups = readGroups
  var instruments = readInstruments

  private def readGroups      = AppSchema.groups.toSeq.sortBy(_.name)
  private def readInstruments = AppSchema.instruments.toSeq.sortBy(_.sequence.is)

  def init(): Unit = {}

  def invalidateGroups(): Unit = {
    groups = readGroups
  }

  def invalidateInstruments(): Unit = {
    instruments = readInstruments
  }
}