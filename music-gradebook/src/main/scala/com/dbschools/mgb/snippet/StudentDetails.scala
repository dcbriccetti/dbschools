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
import schema.{Assessment, Musician, AppSchema}
import model.{GroupAssignments, GroupAssignment}

class StudentDetails extends Loggable {
  private var selectedMusicians = Set[Int]()
  private var selectedMusicianGroups = Set[Int]()
  private val reloadPage = JsRaw("location.reload()")

  def render = {
    case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])

    def whereLike(search: String)(m: Musician)  = where(m.last_name.like("%" + search + "%"))
    def whereId(id: Int)(m: Musician)           = where(m.musician_id === id)

    val opWhere =
      S.param("name").map(like => whereLike(like) _).orElse(
      S.param("id")  .map(id   => whereId(id.toInt) _))

    val matchingMusicians = opWhere.map(whereFn => from(AppSchema.musicians)(musician =>
      whereFn(musician) select(musician)
      orderBy(musician.last_name, musician.first_name)).toSeq) | Seq[Musician]()

    val musicianDetailsItems = matchingMusicians.map(musician =>
      MusicianDetails(musician, GroupAssignments(Some(musician.musician_id)),
      AppSchema.assessments.where(_.musician_id === musician.musician_id).toSeq))

    def makeGroups(ga: GroupAssignment) =
      "* *" #>  (SHtml.ajaxCheckbox(false, checked => {
                  if (checked) selectedMusicianGroups += ga.musicianGroup.id
                  else selectedMusicianGroups -= ga.musicianGroup.id
                  Noop
                }) ++ Text("%d: %s, %s".format(ga.musicianGroup.school_year, ga.group.name, ga.instrument.name)))

    def makeDetails(md: MusicianDetails) =
      ".heading *"      #> (SHtml.ajaxCheckbox(false, checked => {
                          if (checked) selectedMusicians += md.musician.musician_id
                          else selectedMusicians -= md.musician.musician_id
                          Noop
                        }) ++ Text("%s, %d, %d, %d".format(md.musician.name, md.musician.student_id,
                           md.musician.musician_id, md.musician.graduation_year))) &
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
    var selectedInstrumentId = instruments.headOption.map(_.instrument_id)
    var replaceExistingAssignment = true

    def process(): JsCmd = {
      selectedGroupId <|*|> selectedInstrumentId map {
        case (g, i) => GroupAssignments.create(selectedMusicianGroups, replaceExistingAssignment, g, i)
      }
      reloadPage
    }

    "#replace"     #> (SHtml.ajaxCheckbox(replaceExistingAssignment, chk => {replaceExistingAssignment = chk; Noop}) ++
                      Text("Replace the existing group assignment, if one exists, otherwise create an additional one")) &
    "#groups"      #> SHtml.ajaxSelect(groups.map(g => (g.group_id.toString, g.name)).toSeq, Empty, gid => {
                       selectedGroupId = Some(gid.toInt) }) &
    "#instruments" #> (SHtml.ajaxSelect(AppSchema.instruments.map(i => (i.instrument_id.toString, i.name)).toSeq,
                       Empty, iid => {
                       selectedInstrumentId = Some(iid.toInt)}) ++ SHtml.hidden(process))
  }


  def deleteGroupAssignment = SHtml.ajaxButton("Delete", () => {
    if (! selectedMusicianGroups.isEmpty) {
      Confirm("Are you sure you want to remove the %d selected group assignments?".format(selectedMusicianGroups.size),
        SHtml.ajaxInvoke(() => {
          AppSchema.musicianGroups.deleteWhere(_.id in selectedMusicianGroups)
          reloadPage
        }))
    } else Noop
  })
}
