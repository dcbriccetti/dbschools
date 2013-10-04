package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class Tempo(
                            id:           Int,
                            tempo:        Int,
  @Column("piece_id")       pieceId:      Int,
  @Column("instrument_id")  instrumentId: Option[Int]
)
