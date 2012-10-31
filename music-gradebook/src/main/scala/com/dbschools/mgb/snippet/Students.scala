package com.dbschools.mgb
package snippet

import xml.Text
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import http.SHtml
import util._
import schema.{Musician, AppSchema}
import model.GroupAssignments

class Students {

  var showPrevious = false

  def showPreviousCb = SHtml.ajaxCheckbox(showPrevious, c => showPrevious = c)

  def inGroups =
    "#studentRow"   #> GroupAssignments(None, showPrevious).map(row =>
      ".schYear  *" #> row.musicianGroup.school_year &
      ".stuName  *" #> studentLink(row.musician) &
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
      ".stuName  *" #> studentLink(m) &
      ".id       *" #> m.musician_id &
      ".stuId    *" #> m.student_id &
      ".gradYear *" #> m.graduation_year
    )
  }

  private def studentLink(m: Musician) = SHtml.link("studentDetails?id=" + m.musician_id, () => {}, Text(m.name))
}
