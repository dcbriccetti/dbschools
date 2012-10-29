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
import net.liftweb.http.js.JsCmds._
import schema.{Musician,GroupAssignment,GroupAssignments,Assessment,AppSchema}

class StudentDetails extends Loggable {
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
      "* *" #> (SHtml.ajaxCheckbox(false, checked => {Noop}) ++ Text(
        "%d: %s, %s".format(ga.musicianGroup.school_year, ga.group.name, ga.instrument.name)))

    def makeDetails(md: MusicianDetails) =
      ".heading *"      #> "%s, %d, %d, %d".format(md.musician.name, md.musician.student_id,
                           md.musician.musician_id, md.musician.graduation_year) &
      ".groups"         #> md.groups.map(makeGroups) &
      ".assessments *"  #> {
        val (pass, fail) = md.assessments.partition(_.pass)
        "Assessments: pass: %d, fail: %d".format(pass.size, fail.size)
      }

    "#student" #> musicianDetailsItems.map(makeDetails)
  }

  def moveControls = {
    def process(): JsCmd = {
      logger.info("Move form submitted")
      Noop
    }
    "#groups"       #> SHtml.ajaxSelect(AppSchema.groups.map(g => (g.group_id.toString, g.name)).toSeq, Empty, gid => {Noop}) &
    "#instruments"  #> (SHtml.ajaxSelect(AppSchema.instruments.map(i => (i.instrument_id.toString, i.name)).toSeq, Empty, iid => {Noop})
                         ++ SHtml.hidden(process))
  }
}
