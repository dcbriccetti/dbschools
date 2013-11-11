package com.dbschools.mgb
package snippet

import scala.xml.Text
import org.squeryl.PrimitiveTypeMode._
import org.apache.log4j.Logger
import net.liftweb._
import net.liftweb.common.{Empty, Full}
import util._
import http._
import js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import Helpers._
import net.liftweb.http.js.JsCmds.{Confirm, SetHtml}
import model.{Cache, GroupAssignment, GroupAssignments, LastPassFinder, TagCounts, Terms}
import schema.{Assessment, AppSchema, Musician, MusicianGroup}

class StudentDetails extends TagCounts with Collapsible with MusicianFromReq {
  private val log = Logger.getLogger(getClass)
  private object svExpanded extends SessionVar[Array[Boolean]](Array(false, false, false))
  private val expanded = svExpanded.is
  private var selectedMusicianGroups = Set[Int]()
  private val groupSelectorValues = Cache.groups.map(g => (g.id.toString, g.name)).toSeq
  private var newAssignmentGroupId = groupSelectorValues(0)._1.toInt
  private val opMusicianDetails = opMusician.map(musician =>
    MusicianDetails(musician, GroupAssignments(Some(musician.id)),
    AppSchema.assessments.where(_.musician_id === musician.id).toSeq))

  case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])

  def render = {

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

    def groupsTable(groups: Iterable[GroupAssignment]) =
      groups.map(ga =>
        ".sel *"        #> assignmentCheckbox(ga) &
        ".year *"       #> Terms.formatted(ga.musicianGroup.school_year) &
        ".group *"      #> groupSelector(ga) &
        ".instrument *" #> instrumentSelector(ga)
      )

    def assessmentsSummary(md: MusicianDetails) = {
      val (pass, fail) = md.assessments.partition(_.pass)
      val tagCountsStr = tagCounts(md.musician.id) match {
        case Nil => ""
        case n => n.map(tc => s"${tc.tag}: ${tc.count}").mkString(", comments: ", ", ", "")
      }
      s"Passes: ${pass.size}, failures: ${fail.size}$tagCountsStr"
    }

    def makeDetails(lastPassFinder: LastPassFinder)(md: MusicianDetails) = {
      val collapseSels = (0 to 2).map(n => s"#collapse$n [class+]" #> (if (expanded(n)) "in" else ""))

      collapseSels.reduce(_ & _) &
      ".lastName *"       #> SHtml.swappable(<span id="lastName">{md.musician.last_name.get}</span>,
                                SHtml.ajaxText(md.musician.last_name.get,
                                changeName(md.musician, md.musician.last_name, "lastName"))) &
      ".firstName *"      #> SHtml.swappable(<span id="firstName">{md.musician.first_name.get}</span>,
                                SHtml.ajaxText(md.musician.first_name.get,
                                changeName(md.musician, md.musician.first_name, "firstName"))) &
      ".grade"            #> Terms.graduationYearAsGrade(md.musician.graduation_year.get) &
      ".stuId"            #> SHtml.swappable(<span id="stuId">{md.musician.student_id.toString}</span>,
                                SHtml.ajaxText(md.musician.student_id.toString, changeStuId(md.musician))) &
      "#lastPiece *"      #> StudentDetails.lastPiece(lastPassFinder, md.musician.id) &
      ".assignmentRow *"  #> groupsTable(md.groups) &
      ".assessmentsSummary *" #> assessmentsSummary(md)
    }

    "#student"  #> opMusicianDetails.map(makeDetails(new LastPassFinder()))
  }

  def js = collapseMonitorJs(expanded)

  private def assignmentCheckbox(ga: GroupAssignment) =
    SHtml.ajaxCheckbox(false, checked => {
      if (checked) selectedMusicianGroups += ga.musicianGroup.id
      else selectedMusicianGroups -= ga.musicianGroup.id
      if (selectedMusicianGroups.isEmpty) JsHideId("delete") else JsShowId("delete")
    })

  private def groupSelector(ga: GroupAssignment) =
    SHtml.ajaxSelect(groupSelectorValues, Full(ga.musicianGroup.group_id.toString), gid => {
      AppSchema.musicianGroups.update(mg => where(mg.id === ga.musicianGroup.id)
        set (mg.group_id := gid.toInt))
      Noop
    })

  private def instrumentSelector(ga: GroupAssignment) =
    SHtml.ajaxSelect(Cache.instruments.map(i => (i.id.toString, i.name.get)).toSeq,
      Full(ga.musicianGroup.instrument_id.toString), iid => {
        AppSchema.musicianGroups.update(mg => where(mg.id === ga.musicianGroup.id)
          set (mg.instrument_id := iid.toInt))
        Noop
      })

  def groupAssignments = {
    def delete() = {
      if (selectedMusicianGroups.nonEmpty) {
        Confirm(
          s"Are you sure you want to remove the ${selectedMusicianGroups.size} selected group assignments?",
          SHtml.ajaxInvoke(() => {
            val ids = selectedMusicianGroups
            AppSchema.musicianGroups.deleteWhere(_.id in ids)
            log.info("Deleted group assignment(s): " + ids)
            selectedMusicianGroups = selectedMusicianGroups.empty
            Reload
          }))
      } else Noop
    }

    def create() = {
      def opSoleAssignedInstrumentId: Option[Int] = {
        for {
          md <- opMusicianDetails
          gps = md.groups.map(_.instrument.id).toSet.toSeq
          if gps.size == 1
        } yield gps(0)
      }

      for {
        instrumentId <- opSoleAssignedInstrumentId orElse Cache.instruments.find(_.name.get == "Unassigned").map(_.id)
        musician     <- opMusician
      } {
        val musicianGroup = MusicianGroup(0, musician.id, newAssignmentGroupId, instrumentId,
          Terms.currentTerm)
        AppSchema.musicianGroups.insert(musicianGroup)
        log.info("Made musician assignment: " + musicianGroup)
      }
      Reload
    }

    def nextSel = {
      SHtml.ajaxSelect(groupSelectorValues, Empty, gid => {
        newAssignmentGroupId = gid.toInt
        Noop
      })
    }

    "#delete"   #> SHtml.ajaxButton("Remove from selected groups", () => delete()) &
    "#create"   #> SHtml.ajaxButton(s"Add to group", () => create()) &
    "#nextSel"  #> nextSel
  }
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
