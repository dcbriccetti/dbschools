package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

import net.liftweb.record.MetaRecord
import net.liftweb.record.Record
import net.liftweb.record.field.IntField
import net.liftweb.record.field.StringField
import net.liftweb.squerylrecord.KeyedRecord

case class Subinstrument private() extends Record[Subinstrument] with KeyedRecord[Int] {
  override def meta = Subinstrument
  
  @Column("id")
  override val idField = new IntField(this)

  @Column("instrument_id")
  val instrumentId = new IntField(this)

  val sequence = new IntField(this)

  val name = new StringField(this, "")
}

object Subinstrument extends Subinstrument with MetaRecord[Subinstrument] {
  def suffix(si: String): String = s" ($si)"
  def suffix(si: Subinstrument): String = suffix(si.name.get)
}
