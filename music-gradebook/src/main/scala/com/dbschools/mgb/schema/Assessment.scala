package com.dbschools.mgb.schema

import java.sql.Timestamp
import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

case class Assessment(
  assessment_id:    Int,
  assessment_time:  Timestamp,
  musician_id:      Int,
  instrument_id:    Int,
  subinstrument_id: Option[Int],
  user_id:          Int,
  @Column("piece_id")
  pieceId:          Int,
  pass:             Boolean,
  notes:            String
) extends KeyedEntity[Int] {
  def id = assessment_id
}
