package com.dbschools.mgb.schema

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.{FunctionNode, EqualityExpression}
import org.squeryl.Schema
import net.liftweb.common.Loggable

object AppSchema extends Schema with Loggable{
  val musicians = table[Musician]("musician")
  val groups = table[Group]("music_group")
  val musicianGroups = table[MusicianGroup]("musician_group")
}

