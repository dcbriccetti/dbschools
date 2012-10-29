package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import http._
import schema._
import scala.Some
import schema.Musician

class StudentDetails {
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
      "* *" #> "%d: %s, %s".format(ga.musicianGroup.school_year, ga.group.name, ga.instrument.name)

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
}
