package com.dbschools.mgb
package model

import org.squeryl.PrimitiveTypeMode._
import scalaz._
import Scalaz._
import net.liftweb.common.Loggable
import schema.{AppSchema, Instrument}
import schema.Group
import schema.Musician
import schema.MusicianGroup
import org.joda.time.DateTime
import snippet.{svSortingStudentsBy, svSelectors}
import Cache.lastAssTimeByMusician

case class GroupAssignment(musician: Musician, group: Group, musicianGroup: MusicianGroup, instrument: Instrument)

object GroupAssignments extends Loggable {
  def apply(
    opMusicianId:           Option[Int],
    opSelectedTerm:         Option[Int] = None,
    opSelectedGroupId:      Option[Int] = None,
    opSelectedInstrumentId: Option[Int] = None
  ) = {
    import AppSchema._
    val rows = from(musicians, groups, musicianGroups, instruments)((m, g, mg, i) =>
      where(
        m.musician_id.get === opMusicianId.? and
        m.musician_id.get === mg.musician_id and
        mg.group_id       === opSelectedGroupId.? and
        mg.group_id       === g.id and
        mg.instrument_id  === opSelectedInstrumentId.? and
        mg.instrument_id  === i.idField.get and
        mg.school_year    === opSelectedTerm.?
      )
      select GroupAssignment(m, g, mg, i)
      orderBy(mg.school_year.desc, m.last_name.get, m.first_name.get, g.name)
    )
    rows
  }

  def sorted(lastPassesByMusician: Map[Int, Iterable[LastPass]]) = {
    val longAgo = new DateTime("1000-01-01").toDate

    val byYear = GroupAssignments(None, svSelectors.opSelectedTerm, svSelectors.opSelectedGroupId,
      svSelectors.opSelectedInstId).toSeq.sortBy(_.musicianGroup.school_year)

    svSortingStudentsBy.is match {
      case SortStudentsBy.Name =>
        byYear.sortBy(_.musician.nameLastFirstNick)
      case SortStudentsBy.LastAssessment =>
        byYear.sortBy(ga => lastAssTimeByMusician.get(ga.musician.id).map(_.toDate) | longAgo)
      case SortStudentsBy.LastPiece =>
        def pos(id: Int) =
          lastPassesByMusician.get(id).toList.flatten.sortBy(-_.testOrder).lastOption.map(_.testOrder) | 0
        byYear.sortBy(ga => -pos(ga.musician.id))
    }
  }
}
