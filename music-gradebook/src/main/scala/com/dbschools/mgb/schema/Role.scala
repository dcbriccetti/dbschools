package com.dbschools.mgb
package schema

import org.squeryl.KeyedEntity

case class Role(
  id:   Int,
  name: String
) extends KeyedEntity[Int]
