package com.dbschools.mgb
package model

import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import schema.{AppSchema, Instrument}
import schema.Group
import schema.Musician
import schema.MusicianGroup

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

  def create(musicianGroups: Iterable[Int], replaceExisting: Boolean, groupId: Int, instrumentId: Int): AnyVal = {
    val currentTerm = Terms.currentTerm
    if (musicianGroups.nonEmpty) {
      if (replaceExisting) {
        logger.info(s"Move $musicianGroups to $groupId $instrumentId")
        update(AppSchema.musicianGroups)(mg =>
          where(mg.id in musicianGroups)
          set(mg.group_id := groupId, mg.instrument_id := instrumentId))
      } else {
        AppSchema.musicianGroups.insert(
          from(AppSchema.musicianGroups)(mg =>
            where(mg.id in musicianGroups and not(mg.group_id === groupId and mg.school_year === currentTerm))
            select MusicianGroup(0, mg.musician_id, groupId, instrumentId, currentTerm)
          ).toSeq
        )
      }
    }
  }
}
