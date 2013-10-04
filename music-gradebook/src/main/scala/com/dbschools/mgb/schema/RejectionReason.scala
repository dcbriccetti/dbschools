package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class RejectionReason(
                                    id:           Int,
  @Column("rejection_reason_text")  commentText:  String
)
