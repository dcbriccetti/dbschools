package com.dbschools.mgb
package snippet

import xml.Text
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.{Loggable, Empty}
import util._
import http._
import js._
import js.JE.JsRaw
import js.JsCmds._
import Helpers.asInt
import schema.{Assessment, Musician, MusicianGroup, AppSchema}
import model.{Terms, GroupAssignments, GroupAssignment}

class StudentDetails extends Loggable {
  private var selectedMusicianGroups = Map[Int, MusicianGroup]()
  private val reloadPage = JsRaw("location.reload()")

  def render = {
    case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])
    val opId = S.param("id").flatMap(asInt).toOption
    val matchingMusicians = opId.map(id => from(AppSchema.musicians)(musician =>
      where(musician.musician_id === id)
      select(musician)
      orderBy(musician.last_name, musician.first_name)).toSeq) | Seq[Musician]()

    val musicianDetailsItems = matchingMusicians.map(musician =>
      MusicianDetails(musician, GroupAssignments(Some(musician.musician_id), None),
      AppSchema.assessments.where(_.musician_id === musician.musician_id).toSeq))

    def makeGroups(ga: GroupAssignment) =
      "* *" #>  (SHtml.ajaxCheckbox(false, checked => {
                  if (checked) selectedMusicianGroups += ga.musicianGroup.id -> ga.musicianGroup
                  else selectedMusicianGroups -= ga.musicianGroup.id
                  Noop
                }) ++ Text("%d: %s, %s".format(ga.musicianGroup.school_year, ga.group.name, ga.instrument.name.get)))

    def makeDetails(md: MusicianDetails) =
      ".heading *"      #> "%s, %d, %d, %d".format(md.musician.name, md.musician.student_id,
                           md.musician.musician_id, Terms.graduationYearAsGrade(md.musician.graduation_year)) &
      ".groups"         #> md.groups.map(makeGroups) &
      ".assessments *"  #> {
        val (pass, fail) = md.assessments.partition(_.pass)
        "Assessments: pass: %d, fail: %d".format(pass.size, fail.size)
      }

    "#student" #> musicianDetailsItems.map(makeDetails)
  }

  def moveControls = {
    val groups = AppSchema.groups.toSeq
    var selectedGroupId = groups.headOption.map(_.group_id)
    val instruments = AppSchema.instruments.toSeq
    var selectedInstrumentId = instruments.headOption.map(_.idField.get)
    var replaceExistingAssignment = true

    def process(): JsCmd = {
      selectedGroupId <|*|> selectedInstrumentId map {
        case (g, i) => GroupAssignments.create(selectedMusicianGroups.keys, replaceExistingAssignment, g, i)
      }
      reloadPage
    }

    "#replace"     #> (SHtml.ajaxCheckbox(replaceExistingAssignment, chk => {replaceExistingAssignment = chk; Noop}) ++
                      Text("Replace the existing group assignment, if one exists, otherwise create an additional one")) &
    "#groups"      #> SHtml.ajaxSelect(groups.map(g => (g.group_id.toString, g.name)).toSeq, Empty, gid => {
                       selectedGroupId = Some(gid.toInt) }) &
    "#instruments" #> (SHtml.ajaxSelect(AppSchema.instruments.map(i => (i.idField.get.toString, i.name.get)).toSeq,
                       Empty, iid => {
                       selectedInstrumentId = Some(iid.toInt)}) ++ SHtml.hidden(process))
  }

  def deleteGroupAssignment = SHtml.ajaxButton("Delete", () => {
    if (! selectedMusicianGroups.isEmpty) {
      Confirm("Are you sure you want to remove the %d selected group assignments?".format(selectedMusicianGroups.size),
        SHtml.ajaxInvoke(() => {
          AppSchema.musicianGroups.deleteWhere(_.id in selectedMusicianGroups.keys)
          selectedMusicianGroups = selectedMusicianGroups.empty
          reloadPage
        }))
    } else Noop
  })
}
