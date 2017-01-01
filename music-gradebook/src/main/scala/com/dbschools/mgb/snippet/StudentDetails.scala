package com.dbschools.mgb
package snippet

import java.text.NumberFormat
import scala.xml.{Node, NodeSeq, Text}
import org.scala_tools.time.Imports._
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
import model.Cache.{containingTerm, currentMester}

class StudentDetails extends Collapsible with SelectedMusician with Photos {
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
      ".grade"              #> Terms.graduationYearAsGrade(m.graduation_year.get) &
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

  def summary: (NodeSeq) => NodeSeq =
    (for {
      musician <- opMusician
      stats <- Cache.selectedTestingStatsByMusician(musician.id)
    } yield {
      "#passes"             #> stats.totalPassed &
      "#failures"           #> stats.totalFailed &
      "#passPercent"        #> stats.percentPassed &
      "#xPasses"            #> stats.outsideClassPassed &
      "#xFailures"          #> stats.outsideClassFailed &
      "#totalDaysTested"    #> stats.totalDaysTested &
      "#inClassDaysTested"  #> stats.inClassDaysTested &
      "#pd"                 #> (if (stats.inClassDaysTested == 0) "" else nfmt.format(stats.totalPassed.toFloat / stats.inClassDaysTested)) &
      "#streak"             #> stats.longestPassingStreakTimes.size
    }) getOrElse PassThru

  def summaryTitle = Text((if (svStatsDisplay.is == StatsDisplay.Term) "Term" else "School Year") + " Summary")

  def chartData: Node = {
    val allYear = svStatsDisplay.is == StatsDisplay.Year
    val filteredRows = AssessmentRows(Some(opMusician.map(_.id).get), limit = Int.MaxValue).filter { ar =>
      allYear || containingTerm(ar.date) == currentMester }
    val df = DateTimeFormat.forPattern("MM/dd")
    val groupedAndSortedRows = filteredRows.groupBy(ar => df.print(ar.date.withDayOfWeek(1))).toSeq.sortBy(_._1)
    val dataValues = groupedAndSortedRows.map { case (date, groupOfTests) => s"""
    {
        "label" : "$date" ,
        "value" : ${groupOfTests.seq.count(_.pass)}
    }"""}.mkString(",")

    Script(JsRaw(s"""
chartData = [
    {
        key: "Passes Per Week",
        values: [
            $dataValues
        ]
    }
];"""))
  }
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
