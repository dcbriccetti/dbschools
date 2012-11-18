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
import http.{S, Templates, SHtml}
import util._
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import model.{LastPassFinder, Terms, GroupAssignments}

class Students extends Loggable {

  private val lastPassFinder = new LastPassFinder()
  private var opSelectedTerm: Option[Int] = Some(Terms.currentTerm) // None means no specific term, therefore all
  private var opSelectedGroupId = none[Int]                         // None means no specific group, therefore all
  private var opSelectedInstId  = none[Int]                         // None means no specific instrument, therefore all

  def yearSelector = selector(Terms.allTermsFormatted, opSelectedTerm, opSelectedTerm = _)

  def groupSelector = selector(AppSchema.groups.toList.map(g => g.group_id.toString -> g.name),
    opSelectedGroupId, opSelectedGroupId = _)

  def instrumentSelector = selector(AppSchema.instruments.toList.map(i => i.id.toString -> i.name.is),
    opSelectedInstId, opSelectedInstId = _)

  private def selector(items: List[(String, String)], opId: Option[Int], fn: (Option[Int]) => Unit) = {
    val All = "All"
    SHtml.ajaxSelect((All, All) :: items, Full(opId.map(_.toString) | All), sel => {
      fn(if (sel == All) None else Some(sel.toInt))
      replaceContents
    })
  }

  private def replaceContents = {
    val template = "_inGroupsTable"
    Templates(List(template)).map(Replace("inGroups", _)) openOr {
      logger.error("Error loading template " + template)
      Noop
    }
  }

  def createNew = "#create [href]" #> ApplicationPaths.newStudent.href

    var newId = 0
    var grade = 6
    var name = ""
    var sex = "Male"
  def newStudent = {

    def saveStudent = {
      logger.warn("Creating student %d %d %s %s".format(newId, grade, name, sex))
      Noop
    }

    "#studentId" #> SHtml.text(if (newId == 0) "" else newId.toString,
                      id => Helpers.asInt(id).foreach(intId => newId = intId)) &
    "#grade"     #> SHtml.number(grade, grade = _, grade, 8) &
    "#name"      #> SHtml.text(name, name = _) &
    "#sex"       #> SHtml.select(List(("Male", "Male"), ("Female", "Female")), Full(sex), sex = _) &
    "#save"      #> SHtml.onSubmitUnit(() => saveStudent)
  }

  def inGroups = {
    val lastPasses = lastPassFinder.lastPassed().groupBy(_.musicianId)
    (if (opSelectedTerm   .isDefined) ".schYear" #> none[String] else PassThru) andThen (
    (if (opSelectedGroupId.isDefined) ".group"   #> none[String] else PassThru) andThen (
    (if (opSelectedInstId .isDefined) ".instr"   #> none[String] else PassThru) andThen (
    "#studentRow"   #> GroupAssignments(None, opSelectedTerm, opSelectedGroupId, opSelectedInstId).map(row =>
      ".schYear  *" #> Terms.formatted(row.musicianGroup.school_year) &
      ".stuName  *" #> studentLink(row.musician) &
      ".grade    *" #> Terms.graduationYearAsGrade(row.musician.graduation_year.is) &
      ".id       *" #> row.musician.musician_id.is &
      ".stuId    *" #> row.musician.student_id.is &
      ".group    *" #> row.group.name &
      ".instr    *" #> row.instrument.name.get &
      ".lastPass *" #> ~lastPasses.get(row.musician.musician_id.is).map(_.mkString(", "))
    ))))
  }

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull) select(m) on (m.musician_id.is === mg.map(_.musician_id)))

    "#studentRow"   #> musicians.map(m =>
      ".stuName  *" #> studentLink(m) &
      ".id       *" #> m.musician_id.is &
      ".stuId    *" #> m.student_id.is &
      ".grade    *" #> Terms.graduationYearAsGrade(m.graduation_year.is)
    )
  }

  private def studentLink(m: Musician) = SHtml.link("studentDetails?id=" + m.musician_id.is, () => {}, Text(m.name))
}
