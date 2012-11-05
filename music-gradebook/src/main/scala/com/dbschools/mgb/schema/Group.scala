package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class Group(
  group_id:   Int,
  name:       String,
  @Column("does_testing")
  doesTesting: Boolean
)
