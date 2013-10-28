package com.dbschools.mgb
package snippet

import xml.{NodeSeq, Text}
import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.{Full, Loggable}
import util._
import Helpers._
import net.liftweb.http.{Templates, SHtml}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Replace
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import model.{LastPassFinder, Terms, GroupAssignments}

class Students extends Loggable {

  private val selectors = new Selectors(() => replaceContents)

  private def replaceContents = {
    val template = "_inGroupsTable"
    Templates(List(template)).map(Replace("inGroups", _)) openOr {
      logger.error("Error loading template " + template)
      Noop
    }
  }

  def yearSelector = selectors.yearSelector
  def groupSelector = selectors.groupSelector
  def instrumentSelector = selectors.instrumentSelector

  private val lastPassFinder = new LastPassFinder()

  def createNew = "#create [href]" #> ApplicationPaths.newStudent.href

  var newId = 0
  var grade = 6
  var name = ""
  var sex = "Male"

  def newStudent = {

    def saveStudent = {
      logger.warn(s"Creating student $newId $grade $name $sex")
      Noop
    }

    "#studentId" #> SHtml.text(if (newId == 0) "" else newId.toString,
                      id => Helpers.asInt(id).foreach(intId => newId = intId)) &
    "#grade"     #> SHtml.number(grade, grade = _, grade, 8) &
    "#name"      #> SHtml.text(name, name = _) &
    "#sex"       #> SHtml.select(List(("Male", "Male"), ("Female", "Female")), Full(sex), sex = _) &
    "#save"      #> SHtml.onSubmitUnit(() => saveStudent)
  }

  private val lastAssTimeByMusician = (for {
    gm <- from(AppSchema.assessments)(a => groupBy(a.musician_id) compute max(a.assessment_time))
    m <- gm.measures
  } yield gm.key -> new DateTime(m.getTime)).toMap

  def inGroups = {
    val fmt = DateTimeFormat.forStyle("S-")
    val lastPassesByMusician = lastPassFinder.lastPassed().groupBy(_.musicianId)
    (if (selectors.opSelectedTerm   .isDefined) ".schYear" #> none[String] else PassThru) andThen (
    (if (selectors.opSelectedGroupId.isDefined) ".group"   #> none[String] else PassThru) andThen (
    (if (selectors.opSelectedInstId .isDefined) ".instr"   #> none[String] else PassThru) andThen (
    ".studentRow"   #> GroupAssignments(None, selectors.opSelectedTerm, selectors.opSelectedGroupId,
                          selectors.opSelectedInstId).map(row =>
      ".schYear  *" #> Terms.formatted(row.musicianGroup.school_year) &
      ".stuName  *" #> studentLink(row.musician) &
      ".grade    *" #> Terms.graduationYearAsGrade(row.musician.graduation_year.is) &
      ".group    *" #> row.group.name &
      ".instr    *" #> row.instrument.name.get &
      ".lastAss  *" #> lastAssTimeByMusician.get(row.musician.musician_id.is).map(fmt.print).getOrElse("") &
      ".lastPass *" #> formatLastPasses(lastPassesByMusician.get(row.musician.musician_id.is))
    ))))
  }

  private def formatLastPasses(opLastPasses: Option[Iterable[LastPassFinder#LastPass]]): NodeSeq = {
    val lastPasses = opLastPasses.getOrElse(Seq[LastPassFinder#LastPass]()).map(lp => Text(lp.toString))
    lastPasses.fold(NodeSeq.Empty)(_ ++ <br/> ++ _).drop(1)
  }

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull) select m on (m.musician_id.is === mg.map(_.musician_id)))

    ".studentRow"   #> musicians.map(m =>
      ".stuName  *" #> studentLink(m) &
      ".id       *" #> m.musician_id.is &
      ".stuId    *" #> m.student_id.is &
      ".grade    *" #> Terms.graduationYearAsGrade(m.graduation_year.is)
    )
  }

  private def studentLink(m: Musician) = SHtml.link(Students.urlToDetails(m), () => {}, Text(m.name))
}

object Students {
  def urlToDetails(m: Musician) = "studentDetails?id=" + m.musician_id.is
}