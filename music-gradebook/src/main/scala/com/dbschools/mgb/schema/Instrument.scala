package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

import net.liftweb.record.MetaRecord
import net.liftweb.record.Record
import net.liftweb.record.field.{IntField, StringField}
import net.liftweb.squerylrecord.KeyedRecord

/** Instrument (Violin, Cello, Trumpet, etc.) */
case class Instrument private() extends Record[Instrument] with KeyedRecord[Int]{
  override def meta = Instrument
  
  @Column("instrument_id")
  override val idField = new IntField(this)
  
  val name = new StringField(this, "") {
    override def validations = valMinLen(1, "Name is required") _ :: super.validations
  }

  val sequence = new IntField(this)
}

object Instrument extends Instrument with MetaRecord[Instrument]