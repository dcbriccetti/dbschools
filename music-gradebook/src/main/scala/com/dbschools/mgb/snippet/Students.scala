package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import schema._

class Students {

  def inGroups =
    "#studentRow"   #> GroupAssignments(None).map(row =>
      ".schYear  *" #> row.musicianGroup.school_year &
      ".stuName  *" #> row.musician.name &
      ".gradYear *" #> row.musician.graduation_year &
      ".id       *" #> row.musician.musician_id &
      ".stuId    *" #> row.musician.student_id &
      ".group    *" #> row.group.name &
      ".instr    *" #> row.instrument.name
    )

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull) select(m) on (m.musician_id === mg.map(_.musician_id)))

    "#studentRow"   #> musicians.map(m =>
      ".stuName  *" #> m.name &
      ".id       *" #> m.musician_id &
      ".stuId    *" #> m.student_id &
      ".gradYear *" #> m.graduation_year
    )
  }
}
