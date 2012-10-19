package com.dbschools.mgb
package snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import Helpers._
import schema.{Musician, Group, MusicianGroup, Instrument, AppSchema}

class Students {

  def inGroups = {
    case class RowData(musician: Musician, group: Group, musicianGroup: MusicianGroup, instrument: Instrument)
    import AppSchema._
    val rows = from (musicians, groups, musicianGroups, instruments)((m, g, mg, i) =>
      where(m.musician_id === mg.musician_id and mg.group_id === g.group_id and
        mg.instrument_id === i.instrument_id)
      select(RowData(m, g, mg, i))
      orderBy(mg.school_year desc, m.last_name, m.first_name, g.name)
    )

    "#studentRow"   #> rows.map(row =>
      ".schYear  *" #> row.musicianGroup.school_year &
      ".stuName  *" #> row.musician.name &
      ".gradYear *" #> row.musician.graduation_year &
      ".id       *" #> row.musician.musician_id &
      ".stuId    *" #> row.musician.student_id &
      ".group    *" #> row.group.name &
      ".instr    *" #> row.instrument.name
    )
  }

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull)
      select(m)
      on(m.musician_id === mg.map(_.musician_id))
    )

    "#studentRow"   #> musicians.map(m =>
      ".stuName  *" #> m.name &
        ".id     *" #> m.musician_id &
      ".stuId    *" #> m.student_id &
      ".gradYear *" #> m.graduation_year
    )
  }
}
