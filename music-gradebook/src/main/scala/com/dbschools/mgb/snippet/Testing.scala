package com.dbschools.mgb
package snippet

import java.text.NumberFormat
import scalaz._
import Scalaz._
import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import org.joda.time.format.PeriodFormat
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.{Noop, JsShowId, JsHideId}
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import AppSchema.users
import model._
import model.testingState._
import model.TestingManagerMessages._
import model.TestingManagerMessages.{Chat, DequeueMusicians, TestMusician}

class Testing extends SelectedMusician with Photos {
  def render = {
    var selectedScheduledIds = Set[Int]()

    def queueRow(sm: EnqueuedMusician, extraClass: Option[String], timeUntilCall: Option[Duration]): CssSel = {
      val m = sm.musician
      val mgs = AppSchema.musicianGroups.where(mg => mg.musician_id === m.id and mg.school_year === Terms.currentTerm)
      val instrumentNames =
        for {
          instrumentId  <- mgs.map(_.instrument_id)
          instrument    <- Cache.instruments.find(_.id == instrumentId)
        } yield instrument.name.get

      val formattedTime = ~timeUntilCall.map(t => Testing.formatter.print(t.toPeriod))

      "tr [id]"     #> Testing.queueRowId(m.id) &
      "tr [class+]" #> ~extraClass &
      "#qrsel *"    #> SHtml.ajaxCheckbox(false, (b) => {
        if (b)
          selectedScheduledIds += m.id
        else
          selectedScheduledIds -= m.id
        val del = selectedScheduledIds.nonEmpty
        JsShowIdIf("queueDelete", del) & JsShowIdIf("queueDeleteInstrument", del)
      }) &
      "#qrstu *"    #> Testing.studentNameLink(m, test = true) &
      "#qrphoto *"  #> img(m.permStudentId.get) &
      "#qrinst *"   #> instrumentNames.toSet /* no dups */ .toSeq.sorted.mkString(", ") &
      "#qrpiece *"  #> sm.nextPieceName &
      "#qrtime *"   #> formattedTime
    }

    def testerSessions: List[CssSel] = {
      users.filter(_.enabled).toList.sortBy(_.last_name).map(user => {
        val ss = Testing.SessionStats(user.id)
        val rowSels = ss.rows.take(Testing.SessionsToShowPerTester).map(Testing.sessionRow(show = true))

        ".testerName *"             #> user.last_name &
        ".numSessions *"            #> ss.num &
        ".avgMins *"                #> ss.avgMinsStr &
        ".stdev *"                  #> ss.σStr &
        "#testerSessions [id]"      #> s"user${user.id}" &
        "#testerSessions [style+]"  #> (if (ss.rows.isEmpty) "display: none" else "") &
        ".sessionRow"               #> rowSels
      })
    }

    "#queueDelete" #> SHtml.ajaxButton("Remove", () => {
      Actors.testingManager ! DequeueMusicians(selectedScheduledIds)
      Noop
    }, "title" -> "Remove all selected musicians") &
    "#queueDeleteInstrument" #> SHtml.ajaxButton("Remove Instruments", () => {
      Actors.testingManager ! DequeueInstrumentsOfMusicians(selectedScheduledIds)
      Noop
    }, "title" -> "Remove all students playing the instruments of the selected students") &
    ".queueRow"   #> {
      val durs = Testing.sortedDurs(testingState.timesUntilCall)
      enqueuedMusicians.sorted.zipWithIndex.map {
        case (s, i) =>
          queueRow(s,
            if (i < testingState.timesUntilCall.count(_.millis < 0)) Some("success") else None,
            if (i < durs.size) Some(durs(i)) else None)
      }
    } &
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

object Testing extends SelectedMusician with Photos {
  val log = Logger.getLogger(getClass)
  val SessionsToShowPerTester = 3
  private val tmf = DateTimeFormat.forStyle("-M")

  private val formatter = PeriodFormat.getDefault

  case class SessionStats(rows: Seq[TestingMusician], num: Int, avgMins: Option[Double], avgMinsStr: String,
    σ: Option[Double], σStr: String)

  object SessionStats {
    private val fnum = NumberFormat.getNumberInstance
    fnum.setMaximumFractionDigits(2)

    def apply(userId: Int): SessionStats = {
      val rows = testingMusicians.filter(_.tester.id == userId).toSeq.sortBy(-_.startingTime.millis)
      val n = rows.size
      val lengths = if (n < 2) List[Double]() else
        for {
          i <- 1 until n
        } yield (rows(i - 1).startingTime.getMillis - rows(i).startingTime.getMillis) / 1000.0 / 60
      val avgMins = if (n < 2) None else Some(lengths.sum / (n - 1))
      val opσ = avgMins.map(am => Stats.stdev(lengths, am))
      SessionStats(rows, n, avgMins, ~avgMins.map(fnum.format), opσ, ~opσ.map(fnum.format))
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
          Actors.testingManager ! TestMusician(TestingMusician(m, user, DateTime.now,
            Some(testingState.enqueuedMusicians))))
    }, <span title={title}>{m.nameFirstLast}</span>)
  }

  def sessionRow(show: Boolean)(tm: TestingMusician): CssSel = {
    val m = tm.musician
    "tr [id]"     #> Testing.sessionRowId(m.id) &
    "tr [style+]" #> (if (show) "" else "display: none;") &
    "#srphoto *"  #> img(m.permStudentId.get) &
    "#srstu *"    #> m.nameFirstLast &
    "#srtester *" #> tm.tester.last_name &
    "#srtime *"   #> tmf.print(tm.startingTime) &
    ".srasmts *"  #> tm.numAsmts
  }

  def messageRow(chatMessage: ChatMessage) =
    "#mrtime *"   #> tmf.print(chatMessage.time) &
    "#mrtester *" #> chatMessage.user.last_name &
    "#mrmsg *"    #> chatMessage.msg

  def addMessage(chatMessage: ChatMessage) = JsJqPrepend("#messagesTable tbody",
    messageRow(chatMessage)(elemFromTemplate("testing", ".messageRow")).toString().encJs) &
    JsShowId("clearMessages")

  private def sortedDurs(timesUntilCall: Iterable[Duration]) = timesUntilCall.toSeq.sortBy(_.millis)

  def updateTimesUntilCall(timesUntilCall: Iterable[Duration]) = {
    val durs = Testing.sortedDurs(timesUntilCall)
    enqueuedMusicians.sorted.zipWithIndex.map {
      case (s, i) =>
        val formattedTime =
          if (i < durs.size) {
            val p = durs(i).toPeriod().withMillis(0)
            if (durs(i).getMillis > 0) s"Calling in ${Testing.formatter.print(p)}" else ""
          } else ""
        val id = queueRowId(s.musician.id)
        JsJqHtml(s"#$id .qrtime", formattedTime match { case "0 milliseconds" => "" case f => f })
    }.fold(Noop)(_ & _)
  }

  def clearMessages = JsJqRemove("#messagesTable tbody *") & JsHideId("clearMessages")

  def queueRowId(musicianId: Int) = "qr" + musicianId 
  
  def sessionRowId(musicianId: Int) = "sr" + musicianId
}
