package com.dbschools.mgb
package model

import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Query
import scalaz._
import Scalaz._
import schema.{AppSchema, Instrument}
import schema.Group
import schema.Musician
import schema.MusicianGroup
import org.joda.time.DateTime
import snippet.{svSelectors, svSortingStudentsBy}
import Cache.lastAssTimeByMusician

case class GroupAssignment(musician: Musician, group: Group, musicianGroup: MusicianGroup, instrument: Instrument)

object GroupAssignments extends UserLoggable {
  private val log = Logger.getLogger(getClass)

  def apply(
    opMusicianId:           Option[Int],
    opSelectedTerm:         Option[Int] = None,
    opSelectedGroupId:      Option[Int] = None,
    opSelectedInstrumentId: Option[Int] = None,
    opTesting:              Option[Boolean] = None
  ): Query[GroupAssignment] = {
    import AppSchema._
    val rows = join(musicians, musicianGroups, groups, instruments)((m, mg, g, i) =>
      where(
        m.musician_id.get === opMusicianId.? and
        g.doesTesting     === opTesting.? and
        mg.group_id       === opSelectedGroupId.? and
        mg.instrument_id  === opSelectedInstrumentId.? and
        mg.school_year    === opSelectedTerm.?
      )
      select GroupAssignment(m, g, mg, i)
      orderBy(mg.school_year.desc, m.last_name.get, m.first_name.get, g.name)
      on(
        m.musician_id.get === mg.musician_id,
        mg.group_id       === g.id,
        mg.instrument_id  === i.idField.get
        )
    )
    rows
  }

  def assignments: Seq[GroupAssignment] = {
    val group = svSelectors.selectedGroupId
    val opTesting = if (group.isAll) Some(true) else None
    GroupAssignments(None, svSelectors.selectedTerm.rto, group.rto,
      svSelectors.selectedInstId.rto, opTesting).toSeq.sortBy(_.musicianGroup.school_year)
  }

  def sorted(lastPassesByMusician: Map[Int, Iterable[LastPass]]): Seq[GroupAssignment] = {
    val longAgo = new DateTime("1000-01-01").toDate
    val selected = assignments

    svSortingStudentsBy.is match {
      case SortStudentsBy.Name =>
        selected.sortBy(_.musician.nameLastFirstNick)
      case SortStudentsBy.LastAssessment =>
        selected.sortBy(ga => lastAssTimeByMusician.get(ga.musician.id).map(_.toDate) | longAgo)
      case SortStudentsBy.LastPassed =>
        def pos(id: Int) = ~(
          for {
            passes          <- lastPassesByMusician.get(id)
            sortedPasses    =  passes.toSeq.sortBy(-_.testOrder)
            highestLastPass <- sortedPasses.headOption
          } yield highestLastPass.testOrder)
        selected.sortBy(ga => -pos(ga.musician.id))
      case SortStudentsBy.NumPassed =>
        selected.sortBy(ga => -(~Cache.selectedTestingStatsByMusician(ga.musician.id).map(_.totalPassed)))
      case SortStudentsBy.PctPassed =>
        selected.sortBy(ga => -(~Cache.selectedTestingStatsByMusician(ga.musician.id).map(_.percentPassed)))
      case SortStudentsBy.Streak =>
        selected.sortBy(ga => -(~Cache.selectedTestingStatsByMusician(ga.musician.id).map(_.longestPassingStreakTimes.size)))
    }
  }
  
  def moveToGroup(musicianGroupId: Int, newId: Int, musicianName: String): Unit = {
    try {
      AppSchema.musicianGroups.update(mg =>
        where(mg.id === musicianGroupId)
          set (mg.group_id := newId)
      )
      val newG = ~Cache.groups.find(_.id == newId).map(_.name)
      info(s"Moved $musicianName to group $newG")
    } catch {
      case e: Exception => log.error(s"Move to group $musicianGroupId, $newId, $musicianName failed: " + e.getMessage)
    }
  }
}
