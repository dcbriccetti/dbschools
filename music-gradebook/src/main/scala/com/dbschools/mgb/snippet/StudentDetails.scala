package com.dbschools.mgb
package snippet

import java.text.NumberFormat
import scala.xml.{Node, NodeSeq, Text}

import org.apache.log4j.Logger
import scalaz._
import Scalaz._
import net.liftweb._
import net.liftweb.http.js.JE.JsRaw
import util._
import http._
import SHtml.{ajaxCheckbox, ajaxSelect, link}
import Helpers._
import bootstrap.liftweb.ApplicationPaths
import net.liftweb.http.js.JsCmds._
import net.liftweb.common.Full
import model._
import model.TestingManagerMessages.SetCallAfterMins

class StudentDetails extends Collapsible with SelectedMusician with Photos {
  private val log = Logger.getLogger(getClass)
  private object svCollapsibleShowing extends SessionVar[Array[Boolean]](Array(false, false, false, false))
  private val collapsibleShowing = svCollapsibleShowing.is

  def render: (NodeSeq) => NodeSeq = {

    val user = Authenticator.opLoggedInUser.get
    var mins = testingState.callAfterMinsByTesterId(user.id)
    Actors.testingManager ! SetCallAfterMins(user, mins, callNow = false)

    def minutesSelector = {
      val sels = 10 to 2 by -1 map(n => n.toString -> s"after $n minutes")
      val allSels = sels ++ Seq("1" -> "after 1 minute", "-1" -> "Never")
      val initialSel = mins.map(_.toString) | "-1"
      ajaxSelect(allSels, Full(initialSel), gid => {
        mins = gid.toInt match {
          case n if n >= 0 => Some(n)
          case _ => None
        }
        Actors.testingManager ! SetCallAfterMins(user, mins, callNow = false)
        Noop
      })
    }

    def callNowButton = ajaxCheckbox(false, b => {
      Actors.testingManager ! SetCallAfterMins(user, mins, b)
    })

    val lastPassFinder = new LastPassFinder
    opMusician.map(m => {
      val qEmpty = testingState.enqueuedMusicians.isEmpty
      val testerServicingQueue = testingState.servicingQueueTesterIds contains Authenticator.opLoggedInUser.get.id
      val showQueueControls = ! qEmpty && testerServicingQueue
      val showIfShow = if (showQueueControls) "show" else "hide"
      val hideIfShow = if (showQueueControls) "hide" else "show"
      val allowIfCanWrite = if (Authenticator.canWrite) PassThru else ClearNodes

      "#groupsPanel"        #> allowIfCanWrite &
      "#assessmentPanel"    #> allowIfCanWrite &
      "#nextStu1 [class+]"  #> showIfShow &
      "#nextStu2 [class+]"  #> hideIfShow &
      "#callNext [class+]"  #> showIfShow &
      "#photo"              #> img(m.permStudentId.get) &
      "#name *"             #> m.nameFirstNickLast &
      "#edit *"             #> (if (Authenticator.canWrite) link(ApplicationPaths.editStudent.href, () => {}, Text("Edit")) else NodeSeq.Empty) &
      ".grade"              #> SchoolYears.graduationYearAsGrade(m.graduation_year.get) &
      ".stuId"              #> m.permStudentId.toString() &
      "#lastPiece *"        #> StudentDetails.lastPiece(lastPassFinder, m.id) &
      "#numberPassed *"     #> ~Cache.selectedTestingStatsByMusician(m.id).map(_.totalPassed) &
      "#callNextAfter"      #> minutesSelector &
      "#callNow"            #> callNowButton &
      "#inQueue"            #> (if (testingState.enqueuedMusicians.exists(m.id)) PassThru else ClearNodes)
    }) getOrElse PassThru
  }

  def js: Node = collapseMonitorJs(collapsibleShowing)

  def expand: (NodeSeq) => NodeSeq =
    (for {
      indexStr <- S.attr("index")
      index    <- Helpers.asInt(indexStr)
      if collapsibleShowing(index)
    } yield s"#collapse$index [class+]" #> "in") getOrElse PassThru

  private val nfmt = NumberFormat.getInstance
  nfmt.setMaximumFractionDigits(2)
  nfmt.setMinimumFractionDigits(2)
  private val nfmt0 = NumberFormat.getInstance
  nfmt0.setMaximumFractionDigits(0)

  def summary: (NodeSeq) => NodeSeq =
    (for {
      musician <- opMusician
      stats <- Cache.selectedTestingStatsByMusician(musician.id)
    } yield {
      "#pd"                 #> (if (stats.inClassDaysTested == 0) "" else
        s"${stats.totalPassed} ÷ ${stats.inClassDaysTested} = " + nfmt.format(stats.passesPerInClassTestDays)) &
      "#failures"           #> stats.totalFailed &
      "#passPercent"        #> stats.percentPassed &
      "#xPasses"            #> stats.outsideClassPassed &
      "#xFailures"          #> stats.outsideClassFailed &
      "#totalDaysTested"    #> stats.totalDaysTested &
      "#streak"             #> stats.longestPassingStreakTimes.size &
      "#testScorePct"       #> (nfmt0.format(stats.testScorePercent) + "%") &
      "#passesNeeded"       #> stats.passesNeeded &
      "#moreDetailsPanel [style]" #> ("display: " + (if (svShowMoreStudentDetails.is) "block" else "none")) &
      "#moreDetails"        #> ajaxCheckbox(svShowMoreStudentDetails.is, (show) => {
                                  svShowMoreStudentDetails(show)
                                  JsRaw("""$("#moreDetailsPanel").""" +
                                    (if (show) "show()" else "hide()")) }, "id" -> "moreDetails")
    }) getOrElse PassThru

  def summaryTitle = Text((if (svStatsDisplay.is == StatsDisplay.Term) "Term" else "School Year") + " Summary")
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
