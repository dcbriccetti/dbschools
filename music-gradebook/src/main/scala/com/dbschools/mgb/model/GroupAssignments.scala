package com.dbschools.mgb
package model

import org.apache.log4j.Logger
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
  private val log = Logger.getLogger(getClass)

  def apply(
    opMusicianId:           Option[Int],
    opSelectedTerm:         Option[Int] = None,
    opSelectedGroupId:      Option[Int] = None,
    opSelectedInstrumentId: Option[Int] = None,
    opTesting:              Option[Boolean] = None
  ) = {
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

  def sorted(lastPassesByMusician: Map[Int, Iterable[LastPass]]) = {
    val longAgo = new DateTime("1000-01-01").toDate

    val group = svSelectors.selectedGroupId
    val opTesting = if (group.isAll) Some(true) else None
    val byYear = GroupAssignments(None, svSelectors.selectedTerm.rto, group.rto,
      svSelectors.selectedInstId.rto, opTesting).toSeq.sortBy(_.musicianGroup.school_year)

    svSortingStudentsBy.is match {
      case SortStudentsBy.Name =>
        byYear.sortBy(_.musician.nameLastFirstNick)
      case SortStudentsBy.LastAssessment =>
        byYear.sortBy(ga => lastAssTimeByMusician.get(ga.musician.id).map(_.toDate) | longAgo)
      case SortStudentsBy.LastPassed =>
        def pos(id: Int) = ~(
          for {
            passes          <- lastPassesByMusician.get(id)
            sortedPasses    =  passes.toSeq.sortBy(-_.testOrder)
            highestLastPass <- sortedPasses.headOption
          } yield highestLastPass.testOrder)
        byYear.sortBy(ga => -pos(ga.musician.id))
      case SortStudentsBy.NumPassed =>
        val np = Cache.numPassesThisTermByMusician
        def pos(id: Int) = ~np.get(id)
        byYear.sortBy(ga => -pos(ga.musician.id))
      case SortStudentsBy.PctPassed =>
        def pos(id: Int) = ~Cache.testingStatsByMusician.get(id).map(_.percentPassed)
        byYear.sortBy(ga => -pos(ga.musician.id))
      case SortStudentsBy.Streak =>
        def pos(id: Int) = ~Cache.testingStatsByMusician.get(id).map(_.longestPassingStreakTimes.size)
        byYear.sortBy(ga => -pos(ga.musician.id))
    }
  }
  
  def moveToGroup(musicianGroupId: Int, newId: Int, musicianName: String): Unit = {
    try {
      AppSchema.musicianGroups.update(mg =>
        where(mg.id === musicianGroupId)
          set (mg.group_id := newId)
      )
      val newG = ~Cache.groups.find(_.id == newId).map(_.name)
      log.info(s"Moved $musicianName to group $newG")
    } catch {
      case e: Exception => log.error(s"Move to group $musicianGroupId, $newId, $musicianName failed: " + e.getMessage)
    }
  }
}
