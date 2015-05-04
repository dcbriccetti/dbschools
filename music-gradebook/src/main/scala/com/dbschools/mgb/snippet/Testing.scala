package com.dbschools.mgb
package snippet

import scala.xml.NodeSeq
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
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Reload, Noop, JsShowId, JsHideId}
import net.liftweb.http.js.JsCmd
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import AppSchema.users
import Selectors.Selection
import model._
import model.testingState._
import model.TestingManagerMessages._
import model.TestingManagerMessages.{Chat, DequeueMusicians, TestMusician}

class Testing extends SelectedMusician with Photos {
  private val log = Logger.getLogger(getClass)
  private val tm = Actors.testingManager

  def render = {
    var selectedScheduledIds = Set[Int]()

    def queueRow(sm: EnqueuedMusician, extraClass: Option[String], timeUntilCall: Option[Duration]): CssSel = {
      val m = sm.musician
      val opInstrumentName = Cache.instruments.find(_.id == sm.instrumentId).map(_.name.get)
      val formattedTime = ~timeUntilCall.map(t => Testing.formatter.print(t.toPeriod))

      "tr [id]"     #> Testing.queueRowId(m.id) &
      "tr [class+]" #> ~extraClass &
      "#qrsel *"    #> SHtml.ajaxCheckbox(false, (b) => {
        if (b)
          selectedScheduledIds += m.id
        else
          selectedScheduledIds -= m.id
        val del = selectedScheduledIds.nonEmpty
        Seq("toTop", "queueDelete", "queueDeleteInstrument").map(id => JsShowIdIf(id, del)).fold(Noop)(_ & _)
      }) &
      "#qrstu *"    #> Testing.studentNameLink(m, test = true) &
      "#qrphoto *"  #> img(m.permStudentId.get) &
      "#qrinst *"   #> ~opInstrumentName &
      "#qrpiece *"  #> sm.nextPieceName &
      "#qrtime *"   #> formattedTime
    }

    def testerSessions: List[CssSel] = {
      users.filter(_.enabled).toList.sortBy(_.last_name).map(user => {
        val ss = SessionStats(user.id)
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

    "#toTop" #> SHtml.ajaxButton("Top", () => {
      tm ! ToTop(selectedScheduledIds)
      Noop
    }) &
    "#queueDelete" #> SHtml.ajaxButton("Remove", () => {
      tm ! DequeueMusicians(selectedScheduledIds)
      Noop
    }, "title" -> "Remove all selected musicians") &
    "#queueDeleteInstrument" #> SHtml.ajaxButton("Remove Instruments", () => {
      tm ! DequeueInstrumentsOfMusicians(selectedScheduledIds)
      Noop
    }, "title" -> "Remove all students playing the instruments of the selected students") &
    ".queueRow"   #> enqueuedMusicians.items.map(musician => queueRow(musician, None, None)) &
    "#testerSessionsOuter" #> testerSessions &
    "#message"    #> SHtml.ajaxText("",
      _.trim match {
        case "" => // Ignore
        case msg =>
          tm ! Chat(ChatMessage(DateTime.now, Authenticator.opLoggedInUser.get, msg))
          JsJqVal("#message", "")
      }, "id" -> "message", "style" -> "width: 100%;", "placeholder" -> "Type message and press Enter"
    ) &
    ".messageRow" #> chatMessages.map(Testing.messageRow) &
    "#clearMessages" #> SHtml.ajaxButton("Clear", () => {
      tm ! ClearChat
      Noop
    }, displayNoneIf(chatMessages.isEmpty))
  }

  def changeTestingInstrument(sel: Selection): JsCmd = {
    Authenticator.opLoggedInUser.foreach(user => tm ! SetServicingQueue(user, sel))
    Reload
  }

  def queueInstrumentSelector = {
    import Selectors._
    val instruments = Cache.instruments.filterNot(_.name.get == "Unassigned").
      sortBy(_.sequence.get).map(i => i.id.toString -> i.name.get)
    selector("instrumentSelector", Seq(noneItem, allItem) ++ instruments,
      Authenticator.opLoggedInUser.flatMap(user => testingState.servicingQueueTesterIds.get(user.id)) | Left(false),
      changeTestingInstrument, None)
  }

  def queueService = {
    val opUser = Authenticator.opLoggedInUser // This appears on every page, even before login
    "#queue" #> (if (opUser.nonEmpty) PassThru else ClearNodes)
  }

  def desktopNotify = {
    val opUser = Authenticator.opLoggedInUser // This appears on every page, even before login

    "#notify"                 #> (if (opUser.nonEmpty) PassThru else ClearNodes) andThen
    "#desktopNotifyCheckbox"  #> SHtml.ajaxCheckbox(testingState.desktopNotify, notify => {
      opUser.foreach(user => testingState.desktopNotifyByTesterId += user.id -> notify)
      Noop
    })
  }

  def specialSchedule =
    "#specialSchedule"          #> (if (Authenticator.opLoggedInUser.nonEmpty) PassThru else ClearNodes) andThen
    "#specialScheduleCheckbox"  #> SHtml.ajaxCheckbox(testingState.specialSchedule, b => {
      tm ! SetSpecialSchedule(b)
      Noop
    })

  def period = model.Periods.periodWithin match {

    case period: Periods.Period if Authenticator.opLoggedInUser.nonEmpty =>
      def fh(h: Int) = (if (h > 12) h - 12 else h).toString
      def fm(m: Int) = f"$m%02d"
      val sh = fh(period.start.hour)
      val sm = fm(period.start.minute)
      val eh = fh(period.end.hour)
      val em = fm(period.end.minute)

      "#periodNumber" #> s"${period.num}, $sh:$sm–$eh:$em" &
      "progress"      #> <progress value={period.timePassedSecs.toString} max={period.totalSecs.toString}></progress>

    case _ =>
      "#period" #> NodeSeq.Empty
  }
}

object Testing extends SelectedMusician with Photos {
  val log = Logger.getLogger(getClass)
  val SessionsToShowPerTester = 3
  private val tmf = DateTimeFormat.forStyle("-M")

  private val formatter = PeriodFormat.getDefault

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
    }, <span title={title}>{m.nameNickLast}</span>)
  }

  def sessionRow(show: Boolean)(tm: TestingMusician): CssSel = {
    val m = tm.musician
    "tr [id]"     #> Testing.sessionRowId(m.id) &
    "tr [style+]" #> (if (show) "" else "display: none;") &
    "#srphoto *"  #> img(m.permStudentId.get) &
    "#srstu *"    #> m.nameNickLast &
    "#srtester *" #> tm.tester.last_name &
    "#srtime *"   #> tmf.print(tm.startingTime) &
    ".srasmts *"  #> tm.numTests
  }

  def messageRow(chatMessage: ChatMessage) =
    "#mrtime *"   #> tmf.print(chatMessage.time) &
    "#mrtester *" #> chatMessage.user.last_name &
    "#mrmsg *"    #> chatMessage.msg

  def addMessage(chatMessage: ChatMessage) = JsJqPrepend("#messagesTable tbody",
    messageRow(chatMessage)(elemFromTemplate("testing", ".messageRow")).toString().encJs) &
    JsShowId("clearMessages")

  private var notifiedMusicianIds = Set[Int]()

  private case class IdAndTime(rowId: String, musician: Musician, opTimeMillis: Option[Long], formattedTime: String)

  def makeUpdateTimesUntilCallJs(testerDurations: Seq[TesterDuration]) = {
    val durs = testerDurations
    val goTest = "It’s time to test"
    val enqueuedMusicianIds = enqueuedMusicians.items.map(_.musician.id).toSet
    notifiedMusicianIds &= enqueuedMusicianIds // Remove anybody no longer in the queue

    val idAndTimes = enqueuedMusicians.items.zipWithIndex.map {
      case (enqueuedMusician, i) =>
        val formattedTime =
          if (i >= durs.size)
            ""
          else {
            if (durs(i).duration.getMillis > 0) {
              val p = durs(i).duration.toPeriod().withMillis(0)
              Testing.formatter.print(p) match {
                case "0 milliseconds" => goTest // todo properly suppress this
                case nz               => s"Calling in $nz"
              }
            } else goTest
          }
        val id = queueRowId(enqueuedMusician.musician.id)
        IdAndTime(id, enqueuedMusician.musician, if (i >= durs.size) None else Some(durs(i).duration.millis),
          formattedTime)
    }
    val callTimesJs = idAndTimes.map(it => JsJqHtml(s"#${it.rowId} .qrtime", it.formattedTime))
    (makeNotificationJs(idAndTimes) ++ callTimesJs).fold(Noop)(_ & _)
  }

  private def makeNotificationJs(idAndTimes: Iterable[IdAndTime]) = {
    val toNotify = if (testingState.desktopNotify)
      idAndTimes.filter(it =>
      (it.opTimeMillis.map(_ <= 0) | false) && ! notifiedMusicianIds.contains(it.musician.id))
    else Vector[IdAndTime]()
    notifiedMusicianIds ++= toNotify.map(_.musician.id)
    toNotify.map(it => JsRaw(s"""sendNotification("${it.musician.nameNickLast}, it’s time to test")""").cmd)
  }

  def clearMessages = JsJqRemove("#messagesTable tbody *") & JsHideId("clearMessages")

  def queueRowId(musicianId: Int) = "qr" + musicianId 
  
  def sessionRowId(musicianId: Int) = "sr" + musicianId
}
