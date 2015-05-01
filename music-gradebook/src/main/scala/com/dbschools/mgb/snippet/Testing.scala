package com.dbschools.mgb
package snippet

import java.text.NumberFormat
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
import net.liftweb.http.{SessionVar, SHtml}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Reload, Noop, JsShowId, JsHideId}
import net.liftweb.http.js.JsCmd
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import AppSchema.users
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
        Seq("toTop", "queueDelete", "queueDeleteInstrument").map(id => JsShowIdIf(id, del)).fold(Noop)(_ & _)
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

    "#lastTestOrder" #> SHtml.ajaxCheckbox(testingState.enqueuedMusicians.lastTestOrder, (checked) => {
      tm ! SetLastTestOrder(checked)
      Noop
    }) &
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
    ".queueRow"   #> {
      val durs = Testing.sortedDurs(testingState.timesUntilCall)
      val items = enqueuedMusicians.items
      items.zipWithIndex.map {
        case (s, i) =>
          queueRow(s,
            if (i < testingState.timesUntilCall.count(_.millis < 0)) Some("selected") else None,
            if (i < durs.size) Some(durs(i)) else None)
      }
    } &
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

  private val selectors = svTestingSelectors.get
  selectors.opCallback = Some(() => changeTestingInstrument())

  def changeTestingInstrument(): JsCmd = {
    Authenticator.opLoggedInUser.foreach(user => tm ! SetServicingQueue(user, selectors.selectedInstId))
    Reload
  }
  def queueInstrumentSelector = selectors.instrumentSelector(includeNone = true, exclude = Seq("Unassigned"))

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

  private var notifiedMusicianIds = Set[Int]()

  def updateTimesUntilCall(timesUntilCall: Iterable[Duration]) = {
    val durs = Testing.sortedDurs(timesUntilCall)
    val goTest = "It’s time to test"
    val enqueuedMusicianIds = enqueuedMusicians.items.map(_.musician.id).toSet
    notifiedMusicianIds &= enqueuedMusicianIds // Remove anybody no longer in the queue

    case class IdAndTime(rowId: String, musician: Musician, opTimeMillis: Option[Long], formattedTime: String)

    val idAndTimes = enqueuedMusicians.items.zipWithIndex.map {
      case (enqueuedMusician, i) =>
        val formattedTime =
          if (i >= durs.size)
            ""
          else {
            if (durs(i).getMillis > 0) {
              val p = durs(i).toPeriod().withMillis(0)
              Testing.formatter.print(p) match {
                case "0 milliseconds" => goTest // todo properly suppress this
                case nz               => s"Calling in $nz"
              }
            } else goTest
          }
        val id = queueRowId(enqueuedMusician.musician.id)
        IdAndTime(id, enqueuedMusician.musician, if (i >= durs.size) None else Some(durs(i).millis), formattedTime)
    }

    val toNotify = if (testingState.desktopNotify)
      idAndTimes.filter(it =>
      (it.opTimeMillis.map(_ <= 0) | false) && ! notifiedMusicianIds.contains(it.musician.id))
    else Vector[IdAndTime]()
    notifiedMusicianIds ++= toNotify.map(_.musician.id)
    val notifications = toNotify.map(it => JsRaw(s"""sendNotification("${it.musician.nameNickLast}, it’s time to test")""").cmd)
    val callTimes = idAndTimes.map(it => JsJqHtml(s"#${it.rowId} .qrtime", it.formattedTime))

    (notifications ++ callTimes).fold(Noop)(_ & _)
  }

  def clearMessages = JsJqRemove("#messagesTable tbody *") & JsHideId("clearMessages")

  def queueRowId(musicianId: Int) = "qr" + musicianId 
  
  def sessionRowId(musicianId: Int) = "sr" + musicianId
}

object svTestingSelectors extends SessionVar[Selectors]({
  val s = new Selectors()
  s.selectedInstId = Left(false)
  s
})
