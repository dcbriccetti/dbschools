package com.dbschools.mgb
package snippet

import java.io.File
import xml.{NodeSeq, Text}
import scala.collection.immutable.TreeSet
import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import org.joda.time.{Seconds, Days}
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.{Full, Loggable}
import util._
import Helpers._
import http.{LiftRules, S, SessionVar, SHtml}
import http.provider.servlet.HTTPServletContext
import SHtml.{ElemAttr, text, number, onSubmitUnit, ajaxRadio, ajaxButton, ajaxCheckbox, link}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.http.js.JE.JsRaw
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import model._
import model.TestingManagerMessages._
import Cache.lastAssTimeByMusician

class Students extends SelectedMusician with Photos with Loggable {
  private val selectors = svSelectors.is

  private def replaceContents = {
    val elemId = "dynamicSection"
    Replace(elemId, elemFromTemplate("students", s"#$elemId")) &
    JsRaw("activateTips();")
  }

  selectors.opCallback = Some(() => replaceContents)
  def yearSelector = selectors.yearSelector
  def groupSelector = selectors.groupSelector
  def instrumentSelector = selectors.instrumentSelector

  private val lastPassFinder = new LastPassFinder()
  private val lastPassesByMusician = lastPassFinder.lastPassed().groupBy(_.musicianId)

  def createNew = "#create" #> SHtml.link(ApplicationPaths.editStudent.href,
    () => svSelectedMusician(None), Text("New Student"))

  def sortBy = {
    val orders = Seq(SortStudentsBy.Name, SortStudentsBy.LastAssessment, SortStudentsBy.LastPiece)
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

  var newId = 0
  var grade = 6
  var name = ""
  var selectedMusicians = Vector[Musician]()

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

  def render = {
    val fmt = DateTimeFormat.forStyle("S-")

    val groupAssignments = GroupAssignments.sorted(lastPassesByMusician)
    svGroupAssignments(groupAssignments)

    def cbId(musicianId: Int) = "mcb" + musicianId

    def enableButtons =
      JsEnableIf("#schedule", selectedMusicians.nonEmpty) & Students.adjustButtons

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
        groupAssignments.map(row => {
          JsCheckIf("#" + cbId(row.musician.id), false)
        }).fold(Noop)(_ & _) & enableButtons
      }, flattrs(disableIf(selectedMusicians.isEmpty)): _*)

    def clearScheduleButton =
      ajaxButton("Clear", () => {
        Actors.testingManager ! ClearQueue
        Noop
      }, flattrs(disableIf(model.testingState.enqueuedMusicians.isEmpty &&
        model.testingState.testingMusicians.isEmpty)): _*)

    (if (selectors.opSelectedTerm   .isDefined) ".schYear" #> none[String] else PassThru) andThen (
    (if (selectors.opSelectedGroupId.isDefined) ".group"   #> none[String] else PassThru) andThen (
    (if (selectors.opSelectedInstId .isDefined) ".instr"   #> none[String] else PassThru) andThen (
    (if (svPicturesDisplay.is == PicturesDisplay.Large) "#studentsTable"     #> NodeSeq.Empty else PassThru) andThen (
    (if (svPicturesDisplay.is != PicturesDisplay.Large) "#studentsContainer" #> NodeSeq.Empty else PassThru) andThen (

    "#testAll"                #> testAllButton &
    "#autoSelect"             #> autoSelectButton &
    "#schedule"               #> scheduleButton &
    "#clearSchedule"          #> clearScheduleButton &
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

      val now = DateTime.now
      groupAssignments.map(row => {
        val lastAsmtTime = lastAssTimeByMusician.get(row.musician.id)
        ".sel      *" #> selectionCheckbox(row.musician) &
        ".schYear  *" #> Terms.formatted(row.musicianGroup.school_year) &
        ".stuName  *" #> studentLink(row.musician) &
        ".photo    *" #> (if (svPicturesDisplay.is != PicturesDisplay.None) img(row.musician.permStudentId.get) else NodeSeq.Empty) &
        ".grade    *" #> Terms.graduationYearAsGrade(row.musician.graduation_year.get) &
        ".group    *" #> row.group.name &
        ".instr    *" #> row.instrument.name.get &
        ".lastAss  *" #> ~lastAsmtTime.map(fmt.print) &
        ".daysSince *" #> ~lastAsmtTime.map(la => Days.daysBetween(la, now).getDays.toString) &
        ".lastPass *" #> formatLastPasses(lastPassesByMusician.get(row.musician.id))
      })
    })))))
  }

  private def formatLastPasses(opLastPasses: Option[Iterable[LastPass]]): NodeSeq = {
    val lastPasses = opLastPasses.getOrElse(Seq[LastPass]()).map(lp => Text(lp.toString))
    lastPasses.fold(NodeSeq.Empty)(_ ++ <br/> ++ _).drop(1)
  }

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull) select m on (m.musician_id.get === mg.map(_.musician_id)))

    ".studentRow"   #> musicians.map(m =>
      ".stuName  *" #> studentLink(m) &
      ".id       *" #> m.id &
      ".stuId    *" #> m.student_id.get &
      ".grade    *" #> Terms.graduationYearAsGrade(m.graduation_year.get)
    )
  }

  private def scheduleSelectedMusicians() {
    val now = DateTime.now
    val musiciansToEnqueue = selectedMusicians.map(musician => {
      val lastAsmtTime = lastAssTimeByMusician.get(musician.id)
      val opNextPieceName = for {
        lastPasses  <- lastPassesByMusician.get(musician.id)
        lastPass    <- lastPasses.headOption
        nextPiece   <- Cache.nextPiece(lastPass.piece)
      } yield nextPiece.name.get
      val longAgo = 60L * 60 * 24 * 365 * 100
      val secondsSince = lastAsmtTime.map(la => Seconds.secondsBetween(la, now).getSeconds.toLong) | longAgo
      val sortOrder = -secondsSince
      EnqueuedMusician(musician, sortOrder, opNextPieceName | Cache.pieces.head.name.get)
    })
    Actors.testingManager ! EnqueueMusicians(musiciansToEnqueue)
  }

  private def studentLink(m: Musician) = {
    link(ApplicationPaths.studentDetails.href, () => {
      svSelectedMusician(Some(m))
    }, Text(m.name))
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

  def img(permId: Long) = {
    def fn(dir: String) = s"$dir/$permId.jpg"

    (for {
      p <- paths(permId)
      fnr = fn(p.rel)
      f = new File(fn(p.abs)) if f.exists
    } yield <img src={fnr} title={s"<img style='width: 172px;' src='$fnr'/>"}/>) getOrElse NodeSeq.Empty
  }
}

object svSortingStudentsBy extends SessionVar[SortStudentsBy.Value](SortStudentsBy.Name)

object svPicturesDisplay extends SessionVar[PicturesDisplay.Value](PicturesDisplay.Small)

object svSelectors extends SessionVar[Selectors](new Selectors())

object svGroupAssignments extends SessionVar[Seq[GroupAssignment]](Nil)
