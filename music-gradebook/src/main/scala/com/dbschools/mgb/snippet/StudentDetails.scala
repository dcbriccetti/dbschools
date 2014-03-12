package com.dbschools.mgb
package snippet

import scala.xml.Text
import scalaz._
import Scalaz._
import net.liftweb._
import util._
import http._
import SHtml.{ajaxCheckbox, ajaxSelect, link}
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

    val user = Authenticator.opLoggedInUser.get
    var mins = testingState.callAfterMinsByTesterId(user.id)
    Actors.testingManager ! SetCallAfterMins(user, mins, false)

    def minutesSelector = {
      val sels = 10 to 2 by -1 map(n => n.toString -> s"after $n minutes")
      val allSels = sels ++ Seq("1" -> "after 1 minute", "0" -> "Immediately", "-1" -> "Never")
      val initialSel = mins.map(_.toString) | "-1"
      ajaxSelect(allSels, Full(initialSel), gid => {
        mins = gid.toInt match {
          case n if n >= 0 => Some(n)
          case _ => None
        }
        Actors.testingManager ! SetCallAfterMins(user, mins, false)
        Noop
      })
    }

    def callNowButton = ajaxCheckbox(false, b => {
      Actors.testingManager ! SetCallAfterMins(user, mins, b)
    })

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
      "#edit *"             #> link(ApplicationPaths.editStudent.href, () => {}, Text("Edit")) &
      ".grade"              #> Terms.graduationYearAsGrade(m.graduation_year.get) &
      ".stuId"              #> m.student_id.toString() &
      "#lastPiece *"        #> StudentDetails.lastPiece(lastPassFinder, m.id) &
      "#callNextAfter"      #> minutesSelector &
      "#callNow"            #> callNowButton &
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
