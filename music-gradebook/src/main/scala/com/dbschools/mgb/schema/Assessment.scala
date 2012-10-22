package com.dbschools.mgb.schema

import java.sql.Timestamp

case class Assessment(
  assessment_id:    Int,
  assessment_time:  Timestamp,
  musician_id:      Int,
  user_id:          Int,
  pass:             Boolean
)
