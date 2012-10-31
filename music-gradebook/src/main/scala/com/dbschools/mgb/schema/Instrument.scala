package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

import net.liftweb.record.MetaRecord
import net.liftweb.record.Record
import net.liftweb.record.field.IntField
import net.liftweb.record.field.StringField
import net.liftweb.squerylrecord.KeyedRecord

/** Instruments (Violin, Cello, Trumpet, etc.)
 * 
 * @since 1.0.0
 */
case class Instrument private() extends Record[Instrument] with KeyedRecord[Int]{
  override def meta = Instrument
  
  /** Instrument's identifier */
  @Column("instrument_id")
  override val idField = new IntField(this)
  
  /** Instrument's name (i.e.: Viola, Trombone, etc.) */
  val name = new StringField(this, "")
}

object Instrument extends Instrument with MetaRecord[Instrument]