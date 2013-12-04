package com.dbschools.mgb
package snippet

import java.text.NumberFormat
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.{Noop, JsShowId, JsHideId}
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import com.dbschools.mgb.schema.{Musician, AppSchema}
import AppSchema.{users}
import model.{SelectedMusician, EnqueuedMusician, TestingMusician, Cache, Actors, ChatMessage, Terms}
import model.testingState._
import model.TestingManagerMessages._

class Testing extends SelectedMusician {
  def render = {
    var selectedScheduledIds = Set[Int]()

    def queueRow(sm: EnqueuedMusician): CssSel = {
      val m = sm.musician
      val mgs = AppSchema.musicianGroups.where(mg => mg.musician_id === m.id and mg.school_year === Terms.currentTerm)
      val instrumentNames =
        for {
          instrumentId  <- mgs.map(_.instrument_id)
          instrument    <- Cache.instruments.find(_.id == instrumentId)
        } yield instrument.name.get

      "tr [id]"     #> Testing.queueRowId(m.id) &
      "#qrsel *"    #> SHtml.ajaxCheckbox(false, (b) => {
        if (b)
          selectedScheduledIds += sm.musician.id
        else
          selectedScheduledIds -= sm.musician.id
        JsShowIdIf("queueDelete", selectedScheduledIds.nonEmpty)
      }) &
      "#qrstu *"    #> Testing.studentNameLink(m, test = true) &
      "#qrinst *"   #> instrumentNames.toSet /* no dups */ .toSeq.sorted.mkString(", ") &
      "#qrpiece *"  #> sm.nextPieceName
    }

    def testerSessions: List[CssSel] = {
      users.filter(_.enabled).toList.sortBy(_.last_name).map(user => {
        val ss = Testing.SessionStats(user)
        val rowSels = ss.rows.map(Testing.sessionRow(show = true))

        ".testerName *"             #> user.last_name &
        ".numSessions *"            #> ss.num &
        ".avgMins *"                #> ss.avgMinsStr &
        "#testerSessions [id]"      #> s"user${user.id}" &
        "#testerSessions [style+]"  #> (if (ss.rows.isEmpty) "display: none" else "") &
        ".sessionRow"               #> rowSels
      })
    }

    "#queueDelete" #> SHtml.ajaxButton("Remove Selected", () => {
      Actors.testingManager ! DequeueMusicians(selectedScheduledIds)
      Noop
    }) &
    ".queueRow"   #> enqueuedMusicians.toSeq.sortBy(_.sortOrder).map(queueRow) &
    "#testerSessionsOuter" #> testerSessions &
    "#message"    #> SHtml.ajaxText("",
      _.trim match {
        case "" => // Ignore
        case msg =>
          Actors.testingManager ! Chat(ChatMessage(DateTime.now, Authenticator.opLoggedInUser.get, msg))
          JsJqVal("#message", "")
      }, "id" -> "message", "style" -> "width: 100%;", "placeholder" -> "Type message and press Enter"
    ) &
    ".messageRow" #> chatMessages.map(Testing.messageRow) &
    "#clearMessages" #> SHtml.ajaxButton("Clear", () => {
      Actors.testingManager ! ClearChat
      Noop
    }, displayNoneIf(chatMessages.isEmpty))
  }
}

object Testing extends SelectedMusician {

  val SessionsToShowPerTester = 3
  private val tmf = DateTimeFormat.forStyle("-M")

  case class SessionStats(rows: Seq[TestingMusician], num: Int, avgMins: Option[Double], avgMinsStr: String)

  object SessionStats {
    private val fnum = NumberFormat.getNumberInstance
    fnum.setMaximumFractionDigits(2)

    def apply(user: schema.User): SessionStats = {
      val rows = testingMusicians.filter(_.tester == user).toSeq.
        sortBy(-_.time.millis).take(Testing.SessionsToShowPerTester)
      val n = rows.size
      val avgMins = if (n < 2) None else
        Some((rows(0).time.getMillis - rows(n - 1).time.getMillis) / (n - 1) / 1000.0 / 60)
      SessionStats(rows, n, avgMins, ~avgMins.map(fnum.format))
    }
  }

  def studentNameLink(m: Musician, test: Boolean) = {
    val title = if (test)
      "Test this student and remove from the testing queue"
    else
      "See the details for this student (without affecting the testing queue)"

    SHtml.link(ApplicationPaths.studentDetails.href, () => {
      svSelectedMusician(Some(m))
      if (test)
        Authenticator.opLoggedInUser.foreach(user =>
          Actors.testingManager ! TestMusician(TestingMusician(m, user, DateTime.now)))
    }, <span title={title}>{m.first_name.get + " " + m.last_name}</span>)
  }

  def sessionRow(show: Boolean)(tm: TestingMusician): CssSel = {
    val m = tm.musician
    "tr [id]"     #> Testing.sessionRowId(m.id) &
    "tr [style+]" #> (if (show) "" else "display: none;") &
    "#srstu *"    #> (m.first_name.get + " " + m.last_name) &
    "#srtester *" #> tm.tester.last_name &
    "#srtime *"   #> tmf.print(tm.time) &
    ".srasmts *"  #> tm.numAsmts
  }

  def messageRow(chatMessage: ChatMessage) =
    "#mrtime *"   #> tmf.print(chatMessage.time) &
    "#mrtester *" #> chatMessage.user.last_name &
    "#mrmsg *"    #> chatMessage.msg

  def addMessage(chatMessage: ChatMessage) = JsJqPrepend("#messagesTable tbody",
    messageRow(chatMessage)(elemFromTemplate("testing", ".messageRow")).toString().encJs) &
    JsShowId("clearMessages")

  def clearMessages = JsJqRemove("#messagesTable tbody *") & JsHideId("clearMessages")

  def queueRowId(musicianId: Int) = "qr" + musicianId 
  
  def sessionRowId(musicianId: Int) = "sr" + musicianId
}
