package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb._
import common.{Full, Loggable}
import util._
import http._
import js._
import js.JE.JsRaw
import js.JsCmds._
import Helpers._
import js.JsCmds.Confirm
import model._
import schema.IdGenerator.genId
import model.GroupAssignment
import schema.{AppSchema, Musician, Assessment, MusicianGroup}

class StudentDetails extends Loggable {
  private var selectedMusicianGroups = Map[Int, MusicianGroup]()
  private val reloadPage: JsCmd = JsRaw("location.reload()")
  private var opMusicianId = none[Int]
  private val groups = AppSchema.groups.toSeq.sortBy(_.name)
  private val instruments = AppSchema.instruments.toSeq.sortBy(_.sequence.is)

  case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])

  def render = {

    opMusicianId = S.param("id").flatMap(asInt).toOption
    val matchingMusicians = opMusicianId.map(id => from(AppSchema.musicians)(musician =>
      where(musician.musician_id.is === id)
      select(musician)
      orderBy(musician.last_name.is, musician.first_name.is)).toSeq) | Seq[Musician]()

    val musicianDetailsItems = matchingMusicians.map(musician =>
      MusicianDetails(musician, GroupAssignments(Some(musician.musician_id.is)),
      AppSchema.assessments.where(_.musician_id === musician.musician_id.is).toSeq))

    def makeDetails(md: MusicianDetails) =
      ".name *"           #> md.musician.name &
      ".grade"            #> Terms.graduationYearAsGrade(md.musician.graduation_year.is) &
      ".stuId"            #> md.musician.student_id &
      ".mgbId"            #> md.musician.musician_id &
      ".lastPiece"        #> new LastPassFinder().lastPassed(Some(md.musician.musician_id.is)).mkString(", ") &
      ".assignmentRow *"  #> groupsTable(md.groups) &
      ".assessments"      #>  {
                                val (pass, fail) = md.assessments.partition(_.pass)
                                "Passes: %d, failures: %d".format(pass.size, fail.size)
                              }

    "#student" #> musicianDetailsItems.map(makeDetails)
  }

  private def groupsTable(groups: Iterable[GroupAssignment]) =
    groups.map(ga =>
      ".sel *"        #> assignmentCheckbox(ga) &
      ".year *"       #> Terms.formatted(ga.musicianGroup.school_year) &
      ".group *"      #> groupSelector(ga) &
      ".instrument *" #> instrumentSelector(ga)
    )

  private def assignmentCheckbox(ga: GroupAssignment) =
    SHtml.ajaxCheckbox(false, checked => {
      if (checked) selectedMusicianGroups += ga.musicianGroup.id -> ga.musicianGroup
      else selectedMusicianGroups -= ga.musicianGroup.id
      if (selectedMusicianGroups.isEmpty) JsHideId("delete") else JsShowId("delete")
    })

  private def groupSelector(ga: GroupAssignment) =
    SHtml.ajaxSelect(groups.map(g => (g.id.toString, g.name)).toSeq,
      Full(ga.musicianGroup.group_id.toString), gid => {
        AppSchema.musicianGroups.update(mg => where(mg.id === ga.musicianGroup.id)
          set (mg.group_id := gid.toInt))
        Noop
      })

  private def instrumentSelector(ga: GroupAssignment) =
    SHtml.ajaxSelect(instruments.map(i => (i.id.toString, i.name.is)).toSeq,
      Full(ga.musicianGroup.instrument_id.toString), iid => {
        AppSchema.musicianGroups.update(mg => where(mg.id === ga.musicianGroup.id)
          set (mg.instrument_id := iid.toInt))
        Noop
      })

  def groupAssignments = {
    def delete() = {
      if (! selectedMusicianGroups.isEmpty) {
        Confirm(
          "Are you sure you want to remove the %d selected group assignments?".format(selectedMusicianGroups.size),
          SHtml.ajaxInvoke(() => {
            AppSchema.musicianGroups.deleteWhere(_.id in selectedMusicianGroups.keys)
            selectedMusicianGroups = selectedMusicianGroups.empty
            reloadPage
          }))
      } else Noop
    }
    def create() = {
      for {
        group      <- groups.find(_.doesTesting)
        instrument <- instruments.find(_.name.is == "Unassigned")
        musicianId <- opMusicianId
      } {
        AppSchema.musicianGroups.insert(MusicianGroup(genId(), musicianId, group.id, instrument.id,
          Terms.currentTerm))
      }
      reloadPage
    }

    "#delete" #> SHtml.ajaxButton("Delete selected group assignments", () => delete) &
    "#create" #> SHtml.ajaxButton("Create %s group assignment".
                    format(Terms.formatted(Terms.currentTerm)), () => create)
  }

  def assessments = ".assessmentRow" #> {
    val rows = opMusicianId.map(AssessmentRows.forMusician) getOrElse Seq[AssessmentRow]()
    rows.map(fillAssRow)
  }

  private val dtf = DateTimeFormat.forStyle("SM")

  private def fillAssRow(ar: AssessmentRow) =
    ".date       *"   #> dtf.print(ar.date) &
    ".tester     *"   #> ar.tester &
    ".piece [class]"  #> (if (ar.pass) "pass" else "fail") &
    ".piece      *"   #> ar.piece &
    ".instrument *"   #> ar.instrument &
    ".comments   *"   #> ar.notes
}
