package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class PredefinedComment(
  @Column("comment_id")   id:           Int,
  @Column("comment_text") commentText:  String,
                          description:  String
)

