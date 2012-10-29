package com.dbschools.mgb.schema

import org.squeryl.PrimitiveTypeMode._

case class GroupAssignment(musician: Musician, group: Group, musicianGroup: MusicianGroup,
  instrument: Instrument)

object GroupAssignments {
  def apply(id: Option[Int]) = {
    import AppSchema._
    val rows = from(musicians, groups, musicianGroups, instruments)((m, g, mg, i) =>
      where(conditions(id, m, mg, g, i))
      select(GroupAssignment(m, g, mg, i))
      orderBy(mg.school_year desc, m.last_name, m.first_name, g.name)
    )
    rows
  }

  private def conditions(opId: Option[Int], m: Musician, mg: MusicianGroup, g: Group, i: Instrument) = {
    val joinConditions = m.musician_id === mg.musician_id and mg.group_id === g.group_id and
      mg.instrument_id === i.instrument_id
    opId match {
      case None => joinConditions
      case Some(id) => joinConditions and m.musician_id === id
    }
  }
}
