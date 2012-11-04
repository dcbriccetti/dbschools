package com.dbschools.mgb
package snippet

import xml.Text
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.{Full, Loggable}
import http.js.JsCmds._
import http.js.JsCmds.Replace
import http.{Templates, SHtml}
import util._
import schema.{Musician, AppSchema}
import model.{Terms, GroupAssignments}

class Students extends Loggable {

  var selectedTerm: Option[Int] = Some(Terms.currentTerm) // None means no specific term, therefore all terms

  def showPreviousCb = {
    val All = "All"
    val allTerms = (All, All) :: Terms.allTermsFormatted
    SHtml.ajaxSelect(allTerms, Full(selectedTerm.map(_.toString) | All), term => {
      selectedTerm = if (term == All) None else Some(term.toInt)
      val template = "_inGroupsTable"
      Templates(List(template)).map(Replace("inGroups", _)) openOr {
        logger.error("Error loading template " + template)
        Noop
      }
    })
  }

  def inGroups =
    "#studentRow"   #> GroupAssignments(None, selectedTerm).map(row =>
      ".schYear  *" #> row.musicianGroup.school_year &
      ".stuName  *" #> studentLink(row.musician) &
      ".grade *"    #> Terms.graduationYearAsGrade(row.musician.graduation_year) &
      ".id       *" #> row.musician.musician_id &
      ".stuId    *" #> row.musician.student_id &
      ".group    *" #> row.group.name &
      ".instr    *" #> row.instrument.name.get
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
