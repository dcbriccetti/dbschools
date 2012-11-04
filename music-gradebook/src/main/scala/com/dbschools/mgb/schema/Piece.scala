package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

import net.liftweb.record.MetaRecord
import net.liftweb.record.Record
import net.liftweb.record.field.IntField
import net.liftweb.record.field.StringField
import net.liftweb.squerylrecord.KeyedRecord

case class Piece() extends Record[Piece] with KeyedRecord[Int] {
  override def meta = Piece
  
  @Column("piece_id")
  override val idField = new IntField(this)

  @Column("test_order")
  val testOrder = new IntField(this)

  val name = new StringField(this, "")
}

object Piece extends Piece with MetaRecord[Piece]
