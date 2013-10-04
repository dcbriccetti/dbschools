package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class PredefinedComment(
                          id:           Int,
  @Column("comment_text") commentText:  String,
                          description:  String
)

