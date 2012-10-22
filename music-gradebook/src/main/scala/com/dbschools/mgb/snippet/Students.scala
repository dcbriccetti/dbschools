package com.dbschools.mgb
package snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.Full
import util._
import Helpers._
import http._
import schema._
import schema.Group
import schema.Instrument
import scala.Some
import schema.Musician
import schema.MusicianGroup

class Students {

  private case class GroupAssignment(musician: Musician, group: Group, musicianGroup: MusicianGroup,
    instrument: Instrument)

  def inGroups =
    "#studentRow"   #> groupAssignments(None).map(row =>
      ".schYear  *" #> row.musicianGroup.school_year &
      ".stuName  *" #> row.musician.name &
      ".gradYear *" #> row.musician.graduation_year &
      ".id       *" #> row.musician.musician_id &
      ".stuId    *" #> row.musician.student_id &
      ".group    *" #> row.group.name &
      ".instr    *" #> row.instrument.name
    )

  def inNoGroups = {
    val musicians = join(AppSchema.musicians, AppSchema.musicianGroups.leftOuter)((m, mg) =>
      where(mg.map(_.id).isNull) select(m) on (m.musician_id === mg.map(_.musician_id)))

    "#studentRow"   #> musicians.map(m =>
      ".stuName  *" #> m.name &
      ".id       *" #> m.musician_id &
      ".stuId    *" #> m.student_id &
      ".gradYear *" #> m.graduation_year
    )
  }

  def details = {
    case class MusicianDetails(musician: Musician, groups: Iterable[GroupAssignment], assessments: Iterable[Assessment])

    def whereLike(search: String)(m: Musician)  = where(m.last_name.like("%" + search + "%"))
    def whereId(id: Int)(m: Musician)           = where(m.musician_id === id)

    val opWhere = S.param("name") match {
      case Full(search) => Some(whereLike(search) _)
      case _            => S.param("id").toOption.map(id => whereId(id.toInt) _)
    }

    val matchingMusicians = opWhere.map(whereFn => from(AppSchema.musicians)(musician =>
      whereFn(musician) select(musician)
      orderBy(musician.last_name, musician.first_name))).getOrElse(List[Musician]())

    val musicianDetailsItems = matchingMusicians.map(musician =>
      MusicianDetails(musician, groupAssignments(Some(musician.musician_id)),
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

  private def groupAssignments(id: Option[Int]) = {
    import AppSchema._
    val rows = from(musicians, groups, musicianGroups, instruments)((m, g, mg, i) =>
      where(conditions(id, m, mg, g, i))
      select(GroupAssignment(m, g, mg, i))
      orderBy(mg.school_year desc, m.last_name, m.first_name, g.name)
    )
    rows
  }

  private def conditions(opId: Option[Int], m: Musician, mg: MusicianGroup, g: Group, i: Instrument) = {
    val joinConditions = m.musician_id === mg.musician_id and mg.group_id === g.group_id and
      mg.instrument_id === i.instrument_id
    opId match {
      case None => joinConditions
      case Some(id) => joinConditions and m.musician_id === id
    }
  }
}
