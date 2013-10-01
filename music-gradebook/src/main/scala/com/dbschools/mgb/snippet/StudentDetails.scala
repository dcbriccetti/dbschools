package com.dbschools.mgb
package snippet

import xml.Text
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb._
import common.{Full, Loggable}
import util._
import http._
import js.JsCmds._
import Helpers._
import js.JsCmds.Confirm
import net.liftweb.http.js.JsCmds.SetHtml
import model._
import model.{GroupAssignment, TagCounts}
import schema.{AppSchema, Musician, Assessment, MusicianGroup}

class StudentDetails extends TagCounts with Loggable {
  private var selectedMusicianGroups = Map[Int, MusicianGroup]()
  private var opMusician = none[Musician]
  private val groups = AppSchema.groups.toSeq.sortBy(_.name)
  private val instruments = AppSchema.instruments.toSeq.sortBy(_.sequence.is)

  case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])

  def render = {

    opMusician = for {
      musicianId  <- S.param("id").flatMap(asInt).toOption
      musician    <- AppSchema.musicians.lookup(musicianId)
    } yield musician

    val opMusicianDetails = opMusician.map(musician =>
      MusicianDetails(musician, GroupAssignments(Some(musician.musician_id.is)),
      AppSchema.assessments.where(_.musician_id === musician.musician_id.is).toSeq))

    def changeName(musician: Musician, field: record.field.StringField[Musician], id: String)(t: String) = {
      field(t)
      if (musician.validate.isEmpty) {
        AppSchema.musicians.update(musician)
        SetHtml(id, Text(t))
      } else Noop
    }

    def changeStuId(musician: Musician)(t: String) =
      Helpers.asInt(t).toOption.map(newId => {
        musician.student_id(newId)
        AppSchema.musicians.update(musician)
        SetHtml("stuId", Text(newId.toString))
      }) getOrElse Noop

    def makeDetails(md: MusicianDetails) =
      ".lastName *"       #> SHtml.swappable(<span id="lastName">{md.musician.last_name.is}</span>,
                                SHtml.ajaxText(md.musician.last_name.is,
                                changeName(md.musician, md.musician.last_name, "lastName"))) &
      ".firstName *"      #> SHtml.swappable(<span id="firstName">{md.musician.first_name.is}</span>,
                                SHtml.ajaxText(md.musician.first_name.is,
                                changeName(md.musician, md.musician.first_name, "firstName"))) &
      ".grade"            #> Terms.graduationYearAsGrade(md.musician.graduation_year.is) &
      ".stuId"            #> SHtml.swappable(<span id="stuId">{md.musician.student_id.toString}</span>,
                                SHtml.ajaxText(md.musician.student_id.toString, changeStuId(md.musician))) &
      ".mgbId"            #> md.musician.musician_id &
      ".lastPiece"        #> new LastPassFinder().lastPassed(Some(md.musician.musician_id.is)).mkString(", ") &
      ".assignmentRow *"  #> groupsTable(md.groups) &
      ".assessments"      #>  {
                                val (pass, fail) = md.assessments.partition(_.pass)
                                val tagCountsStr = tagCounts(md.musician.id) match {
                                  case Nil => ""
                                  case n => n.map(tc => s"${tc.tag}: ${tc.count}").mkString(", comments: ", ", ", "")
                                }
                                s"Passes: ${pass.size}, failures: ${fail.size}$tagCountsStr"
                              }

    "#student" #> opMusicianDetails.map(makeDetails)
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
          s"Are you sure you want to remove the ${selectedMusicianGroups.size} selected group assignments?",
          SHtml.ajaxInvoke(() => {
            AppSchema.musicianGroups.deleteWhere(_.id in selectedMusicianGroups.keys)
            selectedMusicianGroups = selectedMusicianGroups.empty
            Reload
          }))
      } else Noop
    }
    def create() = {
      for {
        group      <- groups.find(_.doesTesting)
        instrument <- instruments.find(_.name.is == "Unassigned")
        musician   <- opMusician
      } {
        AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id, group.id, instrument.id,
          Terms.currentTerm))
      }
      Reload
    }

    "#delete" #> SHtml.ajaxButton("Delete selected group assignments", () => delete) &
    "#create" #> SHtml.ajaxButton(s"Create ${Terms.formatted(Terms.currentTerm)} group assignment", () => create())
  }

  def assessments = ".assessmentRow" #> {
    val rows = opMusician.map(_.id).map(AssessmentRows.forMusician) getOrElse Seq[AssessmentRow]()
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
