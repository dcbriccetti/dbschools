package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.{Full, Loggable}
import util._
import http._
import js._
import js.JE.JsRaw
import js.JsCmds._
import Helpers.asInt
import js.JsCmds.Confirm
import model._
import schema.AppSchema
import schema.IdGenerator._
import model.GroupAssignment
import scala.Some
import schema.Musician
import schema.Assessment
import schema.MusicianGroup

class StudentDetails extends Loggable {
  private var selectedMusicianGroups = Map[Int, MusicianGroup]()
  private val reloadPage: JsCmd = JsRaw("location.reload()")
  private var opMusicianId = none[Int]
  private val groups = AppSchema.groups.toSeq.sortBy(_.name)
  private val instruments = AppSchema.instruments.toSeq.sortBy(_.sequence.is)

  def render = {

    case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])
    opMusicianId = S.param("id").flatMap(asInt).toOption
    val matchingMusicians = opMusicianId.map(id => from(AppSchema.musicians)(musician =>
      where(musician.musician_id === id)
      select(musician)
      orderBy(musician.last_name, musician.first_name)).toSeq) | Seq[Musician]()

    val musicianDetailsItems = matchingMusicians.map(musician =>
      MusicianDetails(musician, GroupAssignments(Some(musician.musician_id)),
      AppSchema.assessments.where(_.musician_id === musician.musician_id).toSeq))

    def makeDetails(md: MusicianDetails) =
      ".name *"         #> md.musician.name &
      ".grade *"        #> Terms.graduationYearAsGrade(md.musician.graduation_year) &
      ".stuId *"        #> md.musician.student_id &
      ".mgbId *"        #> md.musician.musician_id &
      ".lastPiece *"    #> new LastPassFinder().lastPassed(Some(md.musician.musician_id)).mkString(", ") &
      ".groups"         #>
        <table class="autoWidth noShade">
          <tr><th>Sel</th><th>Year</th><th>Group</th><th>Instrument</th></tr>
          {md.groups.map(ga => {
          <tr>
            <td>
              {assignmentCheckbox(ga)}
            </td>
            <td>
              {Terms.formatted(ga.musicianGroup.school_year)}
            </td>
            <td>
              {groupSelector(ga)}
            </td>
            <td>
              {instrumentSelector(ga)}
            </td>
          </tr>
        })}
        </table> &
      ".assessments *"  #> {
        val (pass, fail) = md.assessments.partition(_.pass)
        "Assessments: pass: %d, fail: %d".format(pass.size, fail.size)
      }

    "#student" #> musicianDetailsItems.map(makeDetails)
  }

  private def assignmentCheckbox(ga: GroupAssignment) =
    SHtml.ajaxCheckbox(false, checked => {
      if (checked) selectedMusicianGroups += ga.musicianGroup.id -> ga.musicianGroup
      else selectedMusicianGroups -= ga.musicianGroup.id
      if (selectedMusicianGroups.isEmpty) JsHideId("delete") else JsShowId("delete")
    })

  private def groupSelector(ga: GroupAssignment) =
    SHtml.ajaxSelect(groups.map(g => (g.group_id.toString, g.name)).toSeq,
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

  def groupAssignments =
    "#delete" #>
      SHtml.ajaxButton("Delete selected group assignments", () => {
        if (! selectedMusicianGroups.isEmpty) {
          Confirm("Are you sure you want to remove the %d selected group assignments?".format(selectedMusicianGroups.size),
            SHtml.ajaxInvoke(() => {
              AppSchema.musicianGroups.deleteWhere(_.id in selectedMusicianGroups.keys)
              selectedMusicianGroups = selectedMusicianGroups.empty
              reloadPage
            }))
        } else Noop
      }) &
    "#create" #> SHtml.ajaxButton("Create %s group assignment".format(Terms.formatted(Terms.currentTerm)), () => {
      for {
        group      <- groups.find(_.doesTesting)
        instrument <- instruments.find(_.name.is == "Unassigned")
        musicianId <- opMusicianId
      } {
        AppSchema.musicianGroups.insert(MusicianGroup(genId(), musicianId, group.group_id, instrument.id,
          Terms.currentTerm))
      }
      reloadPage
    })
}
