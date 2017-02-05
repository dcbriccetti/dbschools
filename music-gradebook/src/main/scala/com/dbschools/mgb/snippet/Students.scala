package com.dbschools.mgb
package snippet

import java.io.File
import java.text.NumberFormat
import xml.{Elem, Node, NodeSeq, Text}
import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.{Full, Loggable}
import util._
import Helpers._
import http.{LiftRules, S, SHtml}
import http.provider.servlet.HTTPServletContext
import SHtml.{ElemAttr, ajaxButton, ajaxCheckbox, ajaxRadio, link, number, onSubmitUnit, text}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.http.js.JE.JsRaw
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import schema.{AppSchema, Musician}
import model._
import model.TestingManagerMessages._
import Cache.lastTestTimeByMusician

class Students extends SelectedMusician with Photos with ChartFeatures with LocationsGraph with Loggable {
  private val selectors = svSelectors.is

  private def replaceContents = {
    val elemId = "dynamicSection"
    Replace(elemId, elemFromTemplate("students", s"#$elemId")) &
    JsRaw("activateTips();")
  }

  selectors.opCallback = Some(() => replaceContents)
  def yearSelector:       Elem = selectors.yearSelector
  def groupSelector:      Elem = selectors.groupSelector
  def instrumentSelector: Elem = selectors.instrumentSelector

  private val lastPassesByMusician = new LastPassFinder().lastPassed().groupBy(_.musicianId)

  def createNew = "#create" #> SHtml.link(ApplicationPaths.editStudent.href,
    () => svSelectedMusician(None), Text("New Student"))

  def sortBy = {
    val orders = Seq(SortStudentsBy.Name, SortStudentsBy.LastAssessment,
      SortStudentsBy.LastPassed, SortStudentsBy.NumPassed, SortStudentsBy.PctPassed, SortStudentsBy.Streak)
    ajaxRadio[SortStudentsBy.Value](orders, Full(svSortingStudentsBy.is), (s) => {
      svSortingStudentsBy(s)
      replaceContents
    }).flatMap(item => <label style="margin-right: .5em;">{item.xhtml} {item.key.toString} </label>)
  }

  def picturesDisplay = {
    val choices = Seq(PicturesDisplay.None, PicturesDisplay.Small, PicturesDisplay.Large)
    ajaxRadio[PicturesDisplay.Value](choices, Full(svPicturesDisplay.is), (s) => {
      svPicturesDisplay(s)
      replaceContents
    }).flatMap(item => <label style="margin-right: .5em;">{item.xhtml} {item.key.toString} </label>)
  }

  def statsDisplay: Seq[Node] = {
    ajaxCheckbox(svShowStatsOnStudentsPage.is, (show) => {
      svShowStatsOnStudentsPage(show)
      replaceContents
    }, "id" -> "statsDisplay", "title" -> "Show stats for the selected time period on this page")
  }

  def statsRange: Seq[Node] = {
    val choices = Seq(StatsDisplay.Term, StatsDisplay.Year)
    ajaxRadio[StatsDisplay.Value](choices, Full(svStatsDisplay.is), (s) => {
      svStatsDisplay(s)
      replaceContents
    }).flatMap(item => <label style="margin-right: .5em;">{item.xhtml} {item.key.toString} </label>)
  }

  var newId = 0
  var grade = 6
  var name = ""
  var selectedMusicians = Vector[Musician]()
  var moveToGroup = Selection.NoItems

  def newStudent = {

    def saveStudent = {
      logger.warn(s"Creating student $newId $grade $name")
      Noop
    }

    "#studentId" #> text(if (newId == 0) "" else newId.toString,
                      id => Helpers.asInt(id).foreach(intId => newId = intId)) &
    "#grade"     #> number(grade, grade = _, grade, 8) &
    "#name"      #> text(name, name = _) &
    "#save"      #> onSubmitUnit(() => saveStudent)
  }

  def render: (NodeSeq) => NodeSeq = {
    val fmt = DateTimeFormat.forStyle("S-")

    val groupAssignments = GroupAssignments.sorted(lastPassesByMusician)
    svGroupAssignments(groupAssignments)

    def cbId(musicianId: Int) = "mcb" + musicianId

    def enableButtons =
      JsEnableIf("#schedule", selectedMusicians.nonEmpty) & JsEnableIf("#moveToGroup", selectedMusicians.nonEmpty) &
        JsEnableIf("#moveToGroupSelector", selectedMusicians.nonEmpty) & Students.adjustButtons

    def uncheckAll = JsCheckIf(".sel input", false)

    def testAllButton = ajaxButton("Test All", () => {
      groupAssignments.foreach(selectedMusicians :+= _.musician)
      scheduleSelectedMusicians()
      RedirectTo(ApplicationPaths.testing.href)
    })

    def autoSelectButton = ajaxButton("Check 5", () => {
      val indexOfLastChecked = LastCheckedIndex.find(
        groupAssignments.map(_.musician), selectedMusicians)
      groupAssignments.drop(indexOfLastChecked + 1).take(5).map(row => {
        selectedMusicians :+= row.musician
        JsCheckIf("#" + cbId(row.musician.id), true)
      }).fold(Noop)(_ & _) & enableButtons
    })

    def flattrs(attrs: Option[TheStrBindParam]*): Seq[ElemAttr] = attrs.flatten

    def scheduleButton =
      ajaxButton("Add Checked", () => {
        scheduleSelectedMusicians()
        selectedMusicians = Vector()
        uncheckAll & enableButtons
      }, flattrs(disableIf(selectedMusicians.isEmpty)): _*)

    def clearScheduleButton =
      ajaxButton("Clear", () => {
        Actors.testingManager ! ClearQueue
        Noop
      }, flattrs(disableIf(model.testingState.enqueuedMusicians.isEmpty &&
        model.testingState.testingMusicians.isEmpty)): _*)

    def moveToGroupButton =
      ajaxButton("Move to", () => {
        moveToGroup.rto.map(groupId => {
          val selIds = selectedMusicians.map(_.musician_id.get).toSet
          groupAssignments.withFilter(ga => selIds contains ga.musician.id).foreach(ga => {
            model.GroupAssignments.moveToGroup(ga.musicianGroup.id, groupId, ga.musician.nameFirstNickLast)
          })
          selectedMusicians = Vector()
          replaceContents
        }) getOrElse Noop
      }, flattrs(disableIf(selectedMusicians.isEmpty)): _*)

    def moveToGroupSelector = {
      import Selectors._
      val disables = Seq(disableIf(selectedMusicians.isEmpty)).flatten
      val groupsWithoutAll = Selectors.groupsWithoutAll(selectors.selectedSchoolYear)
      moveToGroup = groupsWithoutAll match {
        case first :: rest  => Selection(first._1.toInt)
        case _              => Selection.NoItems
      }
      selector("moveToGroupSelector", groupsWithoutAll, moveToGroup, s => moveToGroup = s, None, disables: _*)
    }

    def exportButton = ajaxButton("Export", () => { S.redirectTo("export/students.xlsx") })

    (if (selectors.selectedSchoolYear.value.isRight) ".schYear" #> none[String] else PassThru) andThen (
    (if (selectors.selectedGroupId   .value.isRight) ".group"   #> none[String] else PassThru) andThen (
    (if (selectors.selectedInstId    .value.isRight) ".instr"   #> none[String] else PassThru) andThen (
    (if (svPicturesDisplay.is == PicturesDisplay.Large) "#studentsTable"     #> ClearNodes else PassThru) andThen (
    (if (svPicturesDisplay.is != PicturesDisplay.Large) "#studentsContainer" #> ClearNodes else PassThru) andThen (
    (if (!svShowStatsOnStudentsPage.is) ".statsCol" #> ClearNodes else PassThru) andThen (
    (if (!Authenticator.canWrite) "#moveToControls" #> ClearNodes else PassThru) andThen (
      // todo don’t show passfails for past years, or show them correctly

    "#testAll"                #> testAllButton &
    "#autoSelect"             #> autoSelectButton &
    "#schedule"               #> scheduleButton &
    "#clearSchedule"          #> clearScheduleButton &
    "#moveToGroup"            #> moveToGroupButton &
    "#moveToGroupSelector"    #> moveToGroupSelector &
    "#export"                 #> exportButton &
    ".photoContainer"         #> {
      var lastId = -1
      val uniqMs = groupAssignments.map(_.musician).filter(m =>
        if (m.id == lastId) false else {
          lastId = m.id
          true
        }
      )
      uniqMs.map(m =>
        ".stuName  *" #> studentLink(m) &
        ".photo"      #> img(m.permStudentId.get)
      )} &
    ".studentRow"             #> {
      def selectionCheckbox(musician: Musician) =
        ajaxCheckbox(false, checked => {
          if (checked) selectedMusicians :+= musician
          else selectedMusicians = selectedMusicians.filterNot(_ == musician)
          enableButtons
        }, "id" -> cbId(musician.id))

      val nfmt = NumberFormat.getInstance
      nfmt.setMaximumFractionDigits(2)
      nfmt.setMinimumFractionDigits(2)
      val nfmt0 = NumberFormat.getInstance
      nfmt0.setMaximumFractionDigits(0)

      val now = DateTime.now
      groupAssignments.map(row => {
        val lastAsmtTime = lastTestTimeByMusician.get(row.musician.id)
        val opStats = Cache.selectedTestingStatsByMusician(row.musician.id)
        def stat(fn: TestingStats => Int) = ~opStats.map(fn)
        def statd(fn: TestingStats => Double) = ~opStats.map(fn)
        val passed  = stat(_.totalPassed)
        val inClassDaysTested = stat(_.inClassDaysTested)
        def bz /* blank if zero */[A](value: A) =
          if (value == 0) "" else value.toString
        val passingImprovement =
          for {
            stats <- opStats
            ti    <- stats.opTestingImprovement
            title = "Recent passes per day: " + ti.recentDailyPassCounts.mkString(", ")
          } yield <span title={title}>{nfmt.format(ti.slope)}</span>
        ".sel      *" #> selectionCheckbox(row.musician) &
        ".schYear  *" #> SchoolYears.formatted(row.musicianGroup.school_year) &
        ".stuName  *" #> studentLink(row.musician) &
        ".photo    *" #> (if (svPicturesDisplay.is != PicturesDisplay.None) img(row.musician.permStudentId.get) else NodeSeq.Empty) &
        ".grade    *" #> SchoolYears.graduationYearAsGrade(row.musician.graduation_year.get) &
        ".group    *" #> row.group.name &
        ".instr    *" #> row.instrument.name.get &
        ".passed *"           #> bz(passed) &
        ".failed *"           #> bz(stat(_.totalFailed)) &
        ".passedPct *"        #> s"${stat(_.percentPassed)}%" &
        ".passedX *"          #> bz(stat(_.outsideClassPassed)) &
        ".failedX *"          #> bz(stat(_.outsideClassFailed)) &
        ".inClassDaysTested *" #> bz(inClassDaysTested) &
        ".avgPassedPerDay *"  #> (if (inClassDaysTested == 0) "" else nfmt.format(passed.toFloat / inClassDaysTested)) &
        ".passesPerWeek [id]" #> s"passesPerWeek${row.musician.id}" &
        ".lastAss  *"   #> ~lastAsmtTime.map(fmt.print) &
        ".testScorePct *"     #> (nfmt0.format(statd(_.testScorePercent)) + "%") &
        ".passesNeeded *"     #> bz(stat(_.passesNeeded)) &
        ".passStreak *"       #> stat(_.longestPassingStreakTimes.size) &
        ".lastPass *"         #> formatLastPasses(row)
      })
    } &
    "#locationsGraph [width]"  #> LocationsGraphWidth &
    "#locationsGraph [height]" #> LocationsGraphHeight &
    "#drawLocationsChart" #> makeLocationsChart("#locationsGraph", groupAssignments, lastPassesByMusician)
    )))))))
  }

  private def formatLastPasses(row: GroupAssignment): NodeSeq = {
    val passes = lastPassesByMusician.getOrElse(row.musician.id, Seq[LastPass]())
    val lastPasses = passes.map(lp => Text(lp.formatted(passes.size > 1 || lp.instrumentId != row.instrument.id)))
    lastPasses.fold(NodeSeq.Empty)(_ ++ <br/> ++ _).drop(1)
  }

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull) select m on (m.musician_id.get === mg.map(_.musician_id)))

    ".studentRow"   #> musicians.map(m =>
      ".stuName  *" #> studentLink(m) &
      ".id       *" #> m.id &
      ".stuId    *" #> m.permStudentId.get &
      ".grade    *" #> SchoolYears.graduationYearAsGrade(m.graduation_year.get)
    )
  }

  private def scheduleSelectedMusicians() {
    val mgmsByMusician = selectors.musicianGroups.toVector.groupBy(_.m.id)
    val musiciansToEnqueue = for {
      musician  <- selectedMusicians
      mgms      <- mgmsByMusician.get(musician.id)
      mgm       <- mgms.headOption
      instrumentId = mgm.mg.instrument_id
      opNextPieceName = for {
        lastPasses  <- lastPassesByMusician.get(musician.id)
        lastPass    <- lastPasses.headOption
        nextPiece   <- Cache.nextPiece(lastPass.piece)
      } yield nextPiece.name.get
    } yield EnqueuedMusician(musician, instrumentId, opNextPieceName | Cache.pieces.head.name.get)
    Actors.testingManager ! EnqueueMusicians(musiciansToEnqueue)
  }

  private def studentLink(m: Musician) = {
    link(ApplicationPaths.studentDetails.href, () => {
      svSelectedMusician(Some(m))
    }, Text(m.nameNickLast), "target" -> "student")
  }

}

object Students {
  def adjustButtons = {
    val ne = model.testingState.enqueuedMusicians.nonEmpty
    val testSel = "#test"
    JsEnableIf("#clearSchedule", ne) & JsEnableIf(testSel, ne) & JsClassIf(testSel, "btn-primary", ne)
  }
}

trait Photos {
  val opPdir = Props.get("photosDir").toOption

  case class Paths(rel: String, abs: String)

  def paths(permId: Long) =
    for {
      relPath <- opPdir
      ctx      = LiftRules.context.asInstanceOf[HTTPServletContext].ctx
      absPath  = ctx.getRealPath("/" + relPath)
    } yield Paths(relPath, absPath)

  def pictureFilename(permId: Long) = {
    def fn(dir: String) = s"$dir/$permId.jpg"

    for {
      p <- paths(permId)
      fnr = fn(p.rel)
      f = new File(fn(p.abs)) if f.exists
    } yield fnr
  }

  def img(permId: Long) = pictureFilename(permId).map(fnr =>
    <img src={fnr} title={s"<img style='width: 172px;' src='$fnr'/>"}/>) getOrElse NodeSeq.Empty
}
