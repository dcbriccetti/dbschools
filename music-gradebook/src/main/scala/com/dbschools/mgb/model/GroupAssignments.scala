package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.Terms
import com.dbschools.mgb.schema._
import com.dbschools.mgb.schema.IdGenerator._
import com.dbschools.mgb.schema.Group
import com.dbschools.mgb.schema.Musician
import com.dbschools.mgb.schema.MusicianGroup

case class GroupAssignment(musician: Musician, group: Group, musicianGroup: MusicianGroup,
  instrument: Instrument)

object GroupAssignments extends Loggable {
  def apply(id: Option[Int], showPrevious: Boolean) = {
    import AppSchema._
    val rows = from(musicians, groups, musicianGroups, instruments)((m, g, mg, i) =>
      where(conditions(id, m, mg, g, i, showPrevious))
      select(GroupAssignment(m, g, mg, i))
      orderBy(mg.school_year desc, m.last_name, m.first_name, g.name)
    )
    rows
  }

  private def conditions(opId: Option[Int], m: Musician, mg: MusicianGroup, g: Group, i: Instrument,
      showPrevious: Boolean) = {
    val joinConditions = m.musician_id === mg.musician_id and mg.group_id === g.group_id and
      mg.instrument_id === i.instrument_id
    val currentYearCondition = mg.school_year === Terms.currentTerm
    val joinAndIdConditions = opId.map(id => joinConditions and m.musician_id === id) | joinConditions
    if (showPrevious) joinAndIdConditions else joinAndIdConditions and currentYearCondition
  }

  def create(musicianGroups: Iterable[Int], replaceExisting: Boolean, groupId: Int, instrumentId: Int): AnyVal = {
    val currentTerm = Terms.currentTerm
    if (!musicianGroups.isEmpty) {
      if (replaceExisting) {
        logger.info("Move %s to %d %d".format(musicianGroups, groupId, instrumentId))
        update(AppSchema.musicianGroups)(mg =>
          where(mg.id in musicianGroups)
          set(mg.group_id := groupId, mg.instrument_id := instrumentId))
      } else {
        AppSchema.musicianGroups.insert(
          from(AppSchema.musicianGroups)(mg =>
            where(mg.id in musicianGroups and not(mg.group_id === groupId and mg.school_year === currentTerm))
              select (MusicianGroup(genId(), mg.musician_id, groupId, instrumentId, currentTerm))
          ).toSeq
        )
      }
    }
  }
}
