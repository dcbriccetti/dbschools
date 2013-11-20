package com.dbschools.mgb
package snippet

import scala.xml.Text
import net.liftweb._
import util._
import http._
import Helpers._
import bootstrap.liftweb.ApplicationPaths
import model.{LastPassFinder, SelectedMusician, Terms}

class StudentDetails extends Collapsible with SelectedMusician {
  private object svCollapsibleShowing extends SessionVar[Array[Boolean]](Array(false, false, false))
  private val collapsibleShowing = svCollapsibleShowing.is

  def render = {

    val lastPassFinder = new LastPassFinder
    opMusician.map(m => {
      val collapseSels = (0 to 2).map(n => s"#collapse$n [class+]" #> (if (collapsibleShowing(n)) "in" else ""))

      collapseSels.reduce(_ & _) &
      "#name *"           #> m.nameFirstLast &
      "#edit *"           #> SHtml.link(ApplicationPaths.editStudent.href, () => {}, Text("Edit")) &
      ".grade"            #> Terms.graduationYearAsGrade(m.graduation_year.get) &
      ".stuId"            #> m.student_id.toString() &
      "#lastPiece *"      #> StudentDetails.lastPiece(lastPassFinder, m.id)
    }) getOrElse PassThru
  }

  def js = collapseMonitorJs(collapsibleShowing)
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
