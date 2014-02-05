package com.dbschools.mgb
package snippet

import scala.xml.{NodeSeq, Text}
import scalaz._
import Scalaz._
import net.liftweb._
import util._
import http._
import Helpers._
import bootstrap.liftweb.ApplicationPaths
import net.liftweb.http.js.JsCmds._
import net.liftweb.common.Full
import com.dbschools.mgb.model._
import model.TestingManagerMessages.SetCallAfterMins

class StudentDetails extends Collapsible with SelectedMusician with Photos {
  private object svCollapsibleShowing extends SessionVar[Array[Boolean]](Array(false, false, false))
  private val collapsibleShowing = svCollapsibleShowing.is

  def render = {

    def minutesSelector = {
      Authenticator.opLoggedInUser.map(user => { 
        val sels = 10 to 2 by -1 map(n => n.toString -> s"after $n minutes")
        val allSels = sels ++ Seq("1" -> "after 1 minute", "0" -> "Immediately", "-1" -> "Never")
        val initialSel = testingState.callAfterMinsByTesterId(user.id).map(_.toString) | "-1"
        SHtml.ajaxSelect(allSels, Full(initialSel), gid => {
          val mins = gid.toInt match {
            case n if n >= 0 => Some(n)
            case _ => None
          }
          Actors.testingManager ! SetCallAfterMins(user, mins)
          Noop
        })
      }) getOrElse Text("")
    }

    val lastPassFinder = new LastPassFinder
    opMusician.map(m => {
      val collapseSels = (0 to 2).map(n => s"#collapse$n [class+]" #> (if (collapsibleShowing(n)) "in" else ""))
      val qEmpty = testingState.enqueuedMusicians.isEmpty

      collapseSels.reduce(_ & _) &
      "#nextStu1 [class+]"  #> (if (qEmpty) "hide" else "show") &
      "#nextStu2 [class+]"  #> (if (qEmpty) "show" else "hide") &
      "#callNext [class+]"  #> (if (qEmpty) "hide" else "show") &
      "#photo"              #> img(m.permStudentId.get) &
      "#name *"             #> m.nameFirstLast &
      "#edit *"             #> SHtml.link(ApplicationPaths.editStudent.href, () => {}, Text("Edit")) &
      ".grade"              #> Terms.graduationYearAsGrade(m.graduation_year.get) &
      ".stuId"              #> m.student_id.toString() &
      "#lastPiece *"        #> StudentDetails.lastPiece(lastPassFinder, m.id) &
      "#callNextAfter"      #> minutesSelector &
      "#inQueue"            #> (if (testingState.enqueuedMusicians.exists(m.id)) PassThru else ClearNodes)
    }) getOrElse PassThru
  }

  def js = collapseMonitorJs(collapsibleShowing)
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
