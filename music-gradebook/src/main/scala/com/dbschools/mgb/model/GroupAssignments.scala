package com.dbschools.mgb.model

import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema._
import com.dbschools.mgb.schema.IdGenerator._
import com.dbschools.mgb.schema.Group
import com.dbschools.mgb.schema.Musician
import com.dbschools.mgb.schema.MusicianGroup

case class GroupAssignment(musician: Musician, group: Group, musicianGroup: MusicianGroup,
  instrument: Instrument)

object GroupAssignments extends Loggable {
  def apply(opId: Option[Int], opSelectedTerm: Option[Int] = None, opSelectedGroupId: Option[Int] = None,
      opSelectedInstrumentId: Option[Int] = None) = {
    import AppSchema._
    val rows = from(musicians, groups, musicianGroups, instruments)((m, g, mg, i) =>
      where(
        m.musician_id.is  === opId.? and
        m.musician_id.is  === mg.musician_id and
        mg.group_id       === opSelectedGroupId.? and
        mg.group_id       === g.group_id and
        mg.instrument_id  === opSelectedInstrumentId.? and
        mg.instrument_id  === i.idField.is and
        mg.school_year    === opSelectedTerm.?
      )
      select(GroupAssignment(m, g, mg, i))
      orderBy(mg.school_year desc, m.last_name.is, m.first_name.is, g.name)
    )
    rows
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
