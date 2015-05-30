package com.dbschools.mgb.schema

import org.squeryl.KeyedEntity

case class User(
  id:         Int,
  login:      String,
  password:   String,
  first_name: String,
  last_name:  String,
  enabled:    Boolean,
  metronome:  Int
) extends KeyedEntity[Int] {
  def name = s"$first_name $last_name"
}
