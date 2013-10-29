package com.dbschools.mgb.schema

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

case class Tempo(
                            id:           Int,
  @Column("piece_id")       pieceId:      Int,
  @Column("instrument_id")  instrumentId: Option[Int],
                            tempo:        Int
) extends KeyedEntity[Int]
