package com.dbschools.mgb.schema

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

case class PredefinedComment(
                          id:           Int,
  @Column("comment_text") commentText:  String,
                          description:  String
)  extends KeyedEntity[Int]
