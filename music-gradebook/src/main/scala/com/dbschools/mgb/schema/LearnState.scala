package com.dbschools.mgb
package schema

import java.sql.Timestamp
import org.squeryl.KeyedEntity

case class LearnState(
  id:               Int,
  user_id:          Int,
  musician_id:      Int,
  due:              Timestamp
) extends KeyedEntity[Int]
