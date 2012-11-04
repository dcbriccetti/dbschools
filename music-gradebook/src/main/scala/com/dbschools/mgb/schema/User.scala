package com.dbschools.mgb.schema

import org.squeryl.KeyedEntity

case class User(
  id:         Int,
  login:      String,
  password:   String,
  epassword:  String,
  first_name: String,
  last_name:  String,
  enabled:    Boolean
) extends KeyedEntity[Int]
