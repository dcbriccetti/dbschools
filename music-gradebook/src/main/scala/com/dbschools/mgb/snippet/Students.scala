package com.dbschools.mgb
package snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import Helpers._
import schema.{Musician, Group, MusicianGroup, AppSchema}

class Students {

  def render = {
    case class RowData(musician: Musician, group: Group, musicianGroup: MusicianGroup)
    import AppSchema._
    val rows = from (musicians, groups, musicianGroups)((m, g, mg) =>
      where(m.musician_id === mg.musician_id and mg.group_id === g.group_id)
      select(RowData(m, g, mg))
      orderBy(mg.school_year, m.last_name, m.first_name, g.name)
    )

    "#studentRow" #> rows.map(row =>
      ".gradYear *" #> row.musician.graduation_year &
      ".stuName  *" #> (row.musician.last_name + ", " + row.musician.first_name) &
      ".schYear  *" #> row.musicianGroup.school_year &
      ".group    *" #> row.group.name
    )
  }
}
