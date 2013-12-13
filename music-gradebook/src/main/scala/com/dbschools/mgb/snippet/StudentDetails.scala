package com.dbschools.mgb
package snippet

import scala.xml.Text
import net.liftweb._
import util._
import http._
import Helpers._
import bootstrap.liftweb.ApplicationPaths
import model.{testingState, LastPassFinder, SelectedMusician, Terms}

class StudentDetails extends Collapsible with SelectedMusician with Photos {
  private object svCollapsibleShowing extends SessionVar[Array[Boolean]](Array(false, false, false))
  private val collapsibleShowing = svCollapsibleShowing.is

  def render = {

    val lastPassFinder = new LastPassFinder
    opMusician.map(m => {
      val collapseSels = (0 to 2).map(n => s"#collapse$n [class+]" #> (if (collapsibleShowing(n)) "in" else ""))
      val qEmpty = testingState.enqueuedMusicians.isEmpty

      collapseSels.reduce(_ & _) &
      "#nextStu1 [class+]"  #> (if (qEmpty) "hide" else "show") &
      "#nextStu2 [class+]"  #> (if (qEmpty) "show" else "hide") &
      "#photo"              #> img(m.permStudentId.get) &
      "#name *"             #> m.nameFirstLast &
      "#edit *"             #> SHtml.link(ApplicationPaths.editStudent.href, () => {}, Text("Edit")) &
      ".grade"              #> Terms.graduationYearAsGrade(m.graduation_year.get) &
      ".stuId"              #> m.student_id.toString() &
      "#lastPiece *"        #> StudentDetails.lastPiece(lastPassFinder, m.id) &
      "#inQueue"            #> (if (testingState.enqueuedMusicians.exists(_.musician.id == m.id)) PassThru else ClearNodes)
    }) getOrElse PassThru
  }

  def js = collapseMonitorJs(collapsibleShowing)
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
