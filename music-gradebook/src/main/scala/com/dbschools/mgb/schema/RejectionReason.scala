package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class RejectionReason(
  @Column("rejection_reason_id")   id:           Int,
  @Column("rejection_reason_text") commentText:  String
)
